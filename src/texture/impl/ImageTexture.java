package texture.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import core.colors.RGBSpectrum;
import core.math.Direction2;
import core.math.Point2;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;
import texture.impl.MipMap.ImageWrap;
import texture.mapping.TextureMapping2D;
import utilities.ImageUtilities;

public class ImageTexture<T> implements Texture<T>
{
    private static final Logger logger = Logger.getLogger(ImageTexture.class.getName());
    
    private final TextureMapping2D mapping;
    private MipMap<T> mipMap;

    // concurrent?
    private static final Map<TextureCacheKey, MipMap<RGBSpectrum>> colorTextureCache = new HashMap<>();
    private static final Map<TextureCacheKey, MipMap<Double>> grayscaleTextureCache = new HashMap<>();

    public ImageTexture(TextureMapping2D mapping, String fileName, boolean doTrilinear,
            double maxAnisotropy, ImageWrap wrapMode, double scale, boolean gamma, Class<T> clazz)
    {
        this.mapping = mapping;
        
        long start = System.currentTimeMillis();
        mipMap = getTexture(fileName, doTrilinear, maxAnisotropy, wrapMode, scale, gamma, clazz);
        long end = System.currentTimeMillis();
        
        logger.info("Spent " + (end - start) + "ms initializing " + fileName + ".");
    }

    private MipMap<T> getTexture(String fileName, boolean doTrilinear, double maxAnisotropy,
            ImageWrap wrapMode, double scale, boolean gamma, Class<T> clazz)
    {
        TextureCacheKey key = new TextureCacheKey(fileName, doTrilinear, maxAnisotropy, wrapMode, scale, gamma);

        T[] img;
        Point2 resolution;
        MipMap<T> mipMap;
        if (clazz == Double.class)
        {
            if (grayscaleTextureCache.containsKey(key))
            {
                return (MipMap<T>) grayscaleTextureCache.get(key);
            }
            else
            {
                var tex = ImageUtilities.getGrayscaleImageArray(fileName, gamma);
                img = (T[]) tex.getFirst();
                resolution = tex.getSecond();
                mipMap = new MipMap<>(clazz, resolution, img, doTrilinear, maxAnisotropy, wrapMode);
                grayscaleTextureCache.put(key, (MipMap<Double>) mipMap);
            }
        }
        else if (clazz == RGBSpectrum.class)
        {
            if (colorTextureCache.containsKey(key))
            {
                return (MipMap<T>) colorTextureCache.get(key);
            }
            else
            {
                var tex = ImageUtilities.getImageArray(fileName, gamma);
                img = (T[]) tex.getFirst();
                resolution = tex.getSecond();
                mipMap = new MipMap<>(clazz, resolution, img, doTrilinear, maxAnisotropy, wrapMode);
                colorTextureCache.put(key, (MipMap<RGBSpectrum>) mipMap);
            }
        }
        else
        {
            throw new RuntimeException("Unsupported class " + clazz.getName());
        }
        return mipMap;
    }

    @Override
    public T evaluate(SurfaceInteraction surfaceInteraction)
    {
        var p = mapping.map(surfaceInteraction);
        Point2 st = p.getFirst();
        Direction2 dstdx = p.getSecond();
        Direction2 dstdy = p.getThird();
        return mipMap.lookup(st, dstdx, dstdy);
    }

    private static class TextureCacheKey
    {
        private final String    fileName;
        private final boolean   doTrilinear;
        private final double    maxAnisotropy;
        private final ImageWrap wrapMode;
        private final double    scale;
        private final boolean   gamma;

        public TextureCacheKey(String fileName, boolean doTrilinear, double maxAnisotropy, ImageWrap wrapMode, double scale, boolean gamma)
        {
            this.fileName = fileName;
            this.doTrilinear = doTrilinear;
            this.maxAnisotropy = maxAnisotropy;
            this.wrapMode = wrapMode;
            this.scale = scale;
            this.gamma = gamma;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TextureCacheKey that = (TextureCacheKey) o;
            return doTrilinear == that.doTrilinear &&
                   Double.compare(that.maxAnisotropy, maxAnisotropy) == 0 &&
                   Double.compare(that.scale, scale) == 0 &&
                   gamma == that.gamma &&
                   Objects.equals(fileName, that.fileName) &&
                   wrapMode == that.wrapMode;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(fileName, doTrilinear, maxAnisotropy, wrapMode, scale, gamma);
        }
    }
}
