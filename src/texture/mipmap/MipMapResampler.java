package texture.mipmap;

import core.colors.RGBSpectrum;
import core.math.Point2;
import core.tuple.Pair;
import metrics.MetricsLogger;

import java.lang.reflect.Array;
import java.util.logging.Logger;

import static texture.mipmap.MipMap.resampleWeights;
import static utilities.MathUtilities.clamp;
import static utilities.MathUtilities.nextPower2;

public class MipMapResampler
{
    private static final Logger logger = Logger.getLogger(MipMapResampler.class.getName());
    private static final MetricsLogger metricsLogger = MetricsLogger.getInstance();

    public static <T> Pair<Point2, T[]> resample(Class<T> clazz, Point2 resolution, T[] img, MipMap.ImageWrap wrapMode)
    {
        long textureResampleStart = System.currentTimeMillis();

        // resample image to power of 2 resolution
        int resizeX = nextPower2((int) resolution.get(0));
        int resizeY = nextPower2((int) resolution.get(1));
        Point2 resampledPow2 = new Point2(resizeX, resizeY);

        // resample image in s
        MipMap.ResampleWeight[] sWeights = resampleWeights((int) resolution.get(0), resizeX);
        T[] resampledImage = (T[]) Array.newInstance(clazz, resizeX * resizeY);
        // apply sWeights to zoom in s direction
        // TODO: parallelize in t?
        for (int t = 0; t < resolution.get(1); t++)
        {
            for (int s = 0; s < resizeX; s++)
            {
                try
                {
                    // compute texel(s, t) in s-zoomed image
                    int zoomedIndex = t * resizeX + s;
                    resampledImage[zoomedIndex] = clazz.newInstance();
                    for (int j = 0; j < 4; j++)
                    {
                        int origS = sWeights[s].firstTexel + j;
                        switch(wrapMode)
                        {
                            case REPEAT:
                                origS = origS % (int) resolution.get(0);
                                break;
                            case CLAMP:
                                origS = (int) clamp(origS, 0, resolution.get(0));
                                break;
                            default:
                                break;
                        }
                        if (origS >= 0 && origS < (int) resolution.get(0))
                        {
                            // lol
                            double weight = sWeights[s].weight[j];
                            if (clazz == Double.class)
                            {
                                double toAdd = weight * ((Double) img[t * (int) resolution.get(0) + origS]);
                                double existingValue = (Double) resampledImage[zoomedIndex];
                                resampledImage[zoomedIndex] = (T) Double.valueOf(existingValue + toAdd);
                            }
                            else if (clazz == RGBSpectrum.class)
                            {
                                RGBSpectrum toAdd = ((RGBSpectrum) img[t *
                                        (int) resolution.get(0) +
                                        origS]).times(weight);
                                RGBSpectrum existingValue = (RGBSpectrum) resampledImage[zoomedIndex];
                                resampledImage[zoomedIndex] = (T) existingValue.plus(toAdd);
                            }
                            else
                            {
                                throw new RuntimeException("Unsupported class " + clazz.getName());
                            }
                        }
                    }
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                    logger.severe("Failed to initialize mipmap.");
                    throw new RuntimeException(e);
                }
            }
        }
        // resample image in t
        MipMap.ResampleWeight[] tWeights = resampleWeights((int) resolution.get(1), resizeY);
        // TODO parallelize?
        for (int s = 0; s < resizeX; s++)
        {
            T[] workData = (T[]) Array.newInstance(clazz, resizeY);
            for (int t = 0; t < resizeY; t++)
            {
                try
                {
                    workData[t] = clazz.newInstance();
                    for (int j = 0; j < 4; j++)
                    {
                        int offset = tWeights[t].firstTexel + j;
                        switch(wrapMode)
                        {
                            case REPEAT:
                                offset = offset % (int) resolution.get(1);
                                break;
                            case CLAMP:
                                offset = (int) clamp(offset, 0, (int) resolution.get(1) - 1);
                                break;
                            default:
                                break;
                        }
                        if (offset >= 0 && offset < (int) resolution.get(1))
                        {
                            // lol x2
                            double weight = tWeights[t].weight[j];
                            if (clazz == Double.class)
                            {
                                double toAdd = weight * ((Double) resampledImage[offset * resizeX + s]);
                                double existingValue = (Double) workData[t];
                                workData[t] = (T) Double.valueOf(existingValue + toAdd);
                            }
                            else if (clazz == RGBSpectrum.class)
                            {
                                RGBSpectrum toAdd = ((RGBSpectrum) resampledImage[offset * resizeX + s]).times(weight);
                                RGBSpectrum existingValue = (RGBSpectrum) workData[t];
                                workData[t] = (T) existingValue.plus(toAdd);
                            }
                            else
                            {
                                throw new RuntimeException("Unsupported class " + clazz.getName());
                            }
                        }
                    }
                }
                catch (InstantiationException | IllegalAccessException e)
                {
                    throw new RuntimeException("Failed to initialize mipmap", e);
                }
            }
            for (int t = 0; t < resizeY; t++)
            {
                T clampedValue;
                if (clazz == Double.class)
                {
                    clampedValue = (T) Double.valueOf(clamp((Double) workData[t], 0, Double.POSITIVE_INFINITY));
                }
                else if (clazz == RGBSpectrum.class)
                {
                    clampedValue = (T) ((RGBSpectrum) workData[t]).clamp(0, Double.POSITIVE_INFINITY);
                }
                else
                {
                    throw new RuntimeException("Unsupported class " + clazz.getName());
                }
                resampledImage[t * resizeX + s] = clampedValue;
            }
        }

        long textureResampleEnd = System.currentTimeMillis();
        metricsLogger.onTextureResampled(textureResampleEnd - textureResampleStart);
        return new Pair<>(resampledPow2, resampledImage);
    }
}
