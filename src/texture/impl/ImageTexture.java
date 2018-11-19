package texture.impl;

import java.util.logging.Logger;

import core.math.Direction2;
import core.math.Point2;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;
import texture.cache.TextureCache;
import texture.impl.MipMap.ImageWrap;
import texture.mapping.TextureMapping2D;

public class ImageTexture<T> implements Texture<T>
{
    private static final Logger logger = Logger.getLogger(ImageTexture.class.getName());
    
    private final TextureMapping2D mapping;
    private MipMap<T> mipMap;

    public ImageTexture(TextureMapping2D mapping, String fileName, boolean doTrilinear,
            double maxAnisotropy, ImageWrap wrapMode, double scale, boolean gamma, Class<T> clazz)
    {
        this.mapping = mapping;
        mipMap = TextureCache.getInstance().getTexture(fileName, doTrilinear, maxAnisotropy, wrapMode, scale, gamma, clazz);
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
