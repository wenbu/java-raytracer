package texture.cache;

import core.colors.RGBSpectrum;
import texture.impl.MipMap;
import utilities.ImageUtilities;

import java.util.HashMap;
import java.util.Map;

public class TextureCache
{
    private final Map<Class<?>, SpecificTextureCache<?>> caches = new HashMap<>();

    private static TextureCache instance = null;

    public <T> MipMap<T> getTexture(String fileName, boolean doTrilinear, double maxAnisotropy,
                                    MipMap.ImageWrap wrapMode, double scale, boolean gamma, Class<T> clazz)
    {
        if (!caches.containsKey(clazz))
        {
            throw new RuntimeException("No cache registered for type " + clazz);
        }

        SpecificTextureCache<T> cache = (SpecificTextureCache<T>) caches.get(clazz);
        return cache.getTexture(fileName, doTrilinear, maxAnisotropy, wrapMode, scale, gamma);
    }

    public static TextureCache getInstance()
    {
        if (instance == null)
        {
            instance = new TextureCache();
            SpecificTextureCache<Double> grayscaleTextureCache = new SpecificTextureCache<>(Double.class, ImageUtilities::getGrayscaleImageArray);
            SpecificTextureCache<RGBSpectrum> colorTextureCache = new SpecificTextureCache<>(RGBSpectrum.class, ImageUtilities::getImageArray);
            instance.registerCache(Double.class, grayscaleTextureCache);
            instance.registerCache(RGBSpectrum.class, colorTextureCache);
        }

        return instance;
    }

    private <T> void registerCache(Class<T> clazz, SpecificTextureCache<T> cache)
    {
        caches.put(clazz, cache);
    }
}
