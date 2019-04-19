package texture.cache;

import core.math.Point2;
import core.tuple.Pair;
import metrics.MetricsLogger;
import texture.mipmap.MipMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

// TODO this name sucks
public class SpecificTextureCache<T>
{
    private final Class<T> clazz;
    private final BiFunction<String, Boolean, Pair<T[], Point2>> imageLoaderFunction;
    private final Map<TextureCacheKey, MipMap<T>> cache;
    private final MetricsLogger metricsLogger = MetricsLogger.getInstance();

    public SpecificTextureCache(Class<T> clazz, BiFunction<String, Boolean, Pair<T[], Point2>> imageLoaderFunction)
    {
        this.clazz = clazz;
        this.cache = new HashMap<>();
        this.imageLoaderFunction = imageLoaderFunction;
    }

    public MipMap<T> getTexture(String fileName, boolean doTrilinear, double maxAnisotropy,
                                MipMap.ImageWrap wrapMode, double scale, boolean gamma)
    {
        TextureCacheKey key = new TextureCacheKey(fileName, doTrilinear, maxAnisotropy, wrapMode, scale, gamma);

        if (cache.containsKey(key))
        {
            metricsLogger.onTextureCacheHit();
            return cache.get(key);
        }
        else
        {
            metricsLogger.onTextureCacheMiss();
            var tex = imageLoaderFunction.apply(fileName, gamma);
            T[] img = tex.getFirst();
            Point2 resolution = tex.getSecond();

            MipMap<T> mipMap = new MipMap<>(clazz, resolution, img, doTrilinear, maxAnisotropy, wrapMode);
            cache.put(key, mipMap);
            return mipMap;
        }
    }
}
