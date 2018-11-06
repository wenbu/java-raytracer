package texture.impl;

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
        // TODO image caching
        T[] img;
        Point2 resolution;
        if (clazz == Double.class)
        {
            var tex = ImageUtilities.getGrayscaleImageArray(fileName, gamma);
            img = (T[]) tex.getFirst();
            resolution = tex.getSecond();
        }
        else if (clazz == RGBSpectrum.class)
        {
            var tex = ImageUtilities.getImageArray(fileName, gamma);
            img = (T[]) tex.getFirst();
            resolution = tex.getSecond();
        }
        else
        {
            throw new RuntimeException("Unsupported class " + clazz.getName());
        }
        return new MipMap<>(clazz, resolution, img, doTrilinear, maxAnisotropy, wrapMode);
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

}
