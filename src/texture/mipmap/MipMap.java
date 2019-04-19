package texture.mipmap;

import static utilities.MathUtilities.*;

import java.lang.reflect.Array;
import java.util.logging.Logger;

import core.BlockedArray;
import core.colors.RGBSpectrum;
import core.math.Direction2;
import core.math.Point2;
import core.tuple.Pair;
import metrics.MetricsLogger;

public class MipMap<T>
{
    private static final Logger logger = Logger.getLogger(MipMap.class.getName());
    private static final MetricsLogger metricsLogger = MetricsLogger.getInstance();

    private final Class<T> clazz;
    private final boolean doTrilinear;
    private final double maxAnisotropy;
    private final ImageWrap wrapMode;
    private final Point2 resolution;
    private final BlockedArray<T>[] pyramid;
    private final T black;
    
    private static final int WEIGHT_LUT_SIZE = 128;
    private static final double[] WEIGHT_LUT = new double[WEIGHT_LUT_SIZE];
    static
    {
        double alpha = 2;
        for (int i = 0; i < WEIGHT_LUT_SIZE; i++)
        {
            double r2 = i / (WEIGHT_LUT_SIZE - 1);
            WEIGHT_LUT[i] = Math.exp(-alpha * r2) - Math.exp(-alpha);
        }
    }

    public MipMap(Class<T> clazz, Point2 resolution, T[] img, boolean doTrilinear, double maxAnisotropy,
            ImageWrap wrapMode)
    {
        this.clazz = clazz;
        this.doTrilinear = doTrilinear;
        this.maxAnisotropy = maxAnisotropy;
        this.wrapMode = wrapMode;

        if (clazz == Double.class)
        {
            black = (T) Double.valueOf(0);
        }
        else if (clazz == RGBSpectrum.class)
        {
            black = (T) new RGBSpectrum(0, 0, 0);
        }
        else
        {
            throw new RuntimeException("Unsupported type " + clazz.getName());
        }
        
        // XXX TODO Resampling isn't working. Need to investigate.
        T[] resampledImage = null;
        if (!isPowerOf2((int) resolution.get(0)) || !isPowerOf2((int) resolution.get(1)))
        {
            Pair<Point2, T[]> resampled = MipMapResampler.resample(clazz, resolution, img, wrapMode);
            this.resolution = resampled.getFirst();
            resampledImage = resampled.getSecond();
        }
        else
        {
            this.resolution = resolution;
        }
        
        // initialize mipmap levels
        long mipmapProcessStart = System.currentTimeMillis();
        int nLevels = 1 + log2Int((int) Math.max(this.resolution.get(0), this.resolution.get(1)));
        pyramid = (BlockedArray<T>[]) Array.newInstance(BlockedArray.class, nLevels);
        // initialize most detailed level
        pyramid[0] = new BlockedArray<>((int) this.resolution.get(0),
                                        (int) this.resolution.get(1),
                                        resampledImage == null ? img : resampledImage,
                                        clazz);
        for (int i = 1; i < nLevels; i++)
        {
            // initialize ith level
            int sRes = Math.max(1, pyramid[i - 1].uSize() / 2);
            int tRes = Math.max(1, pyramid[i - 1].vSize() / 2);
            pyramid[i] = new BlockedArray<>(sRes, tRes, clazz);
            // filter four texels from finer level
            // TODO parallelize?
            for (int t = 0; t < tRes; t++)
            {
                for (int s = 0; s < sRes; s++)
                {
                    T filteredValue;
                    T t0 = getTexel(i - 1, 2 * s, 2 * t);
                    T t1 = getTexel(i - 1, 2 * s + 1, 2 * t);
                    T t2 = getTexel(i - 1, 2 * s, 2 * t + 1);
                    T t3 = getTexel(i - 1, 2 * s + 1, 2 * t + 1);
                    
                    if (clazz == Double.class)
                    {
                        double f = 0.25 * ((Double) t0 + (Double) t1 + (Double) t2 + (Double) t3);
                        filteredValue = (T) Double.valueOf(f);
                    }
                    else if (clazz == RGBSpectrum.class)
                    {
                        RGBSpectrum f = ((RGBSpectrum) t0).plus((RGBSpectrum) t1)
                                                          .plus((RGBSpectrum) t2)
                                                          .plus((RGBSpectrum) t3)
                                                          .times(0.25);
                        filteredValue = (T) f;
                    }
                    else
                    {
                        throw new RuntimeException("Unsupported class " + clazz.getName());
                    }
                    pyramid[i].set(s, t, filteredValue);
                }
            }
        }
        long mipmapProcessEnd = System.currentTimeMillis();
        metricsLogger.onMipmapProcessed(mipmapProcessEnd - mipmapProcessStart);
    }

    BlockedArray<T>[] getPyramid()
    {
        return pyramid;
    }

    /**
     * Lookup with no filter. 
     */
    public T lookup(Point2 st)
    {
        return lookup(st, 0);
    }
    
    /**
     * Lookup with isotropic triangle filter (trilinear)
     */
    public T lookup(Point2 st, double width)
    {
        // compute mipmap level
        // pick the level such that the filter covers four texels
        double level = numLevels() - 1 + log2(Math.max(width, 1e-8));

        // perform trilinear interpolation
        if (level < 0)
        {
            return triangle(0, st);
        }
        else if (level >= numLevels() - 1)
        {
            return getTexel(numLevels() - 1, 0, 0);
        }
        else
        {
            int iLevel = (int) Math.floor(level);
            double delta = level - iLevel;
            if (clazz == Double.class)
            {
                return (T) Double.valueOf(lerp(delta, (Double) triangle(iLevel, st), (Double) triangle(iLevel + 1, st)));
            }
            else if (clazz == RGBSpectrum.class)
            {
                return (T) lerp(delta, (RGBSpectrum) triangle(iLevel, st), (RGBSpectrum) triangle(iLevel + 1, st));
            }
            else
            {
                throw new RuntimeException("Unsupported class " + clazz.getName());
            }
        }
    }
    
    private T triangle(int level, Point2 st)
    {
        level = clamp(level, 0, numLevels() - 1);
        double s = st.get(0) * pyramid[level].uSize() - 0.5;
        double t = st.get(1) * pyramid[level].vSize() - 0.5;
        int s0 = (int) Math.floor(s);
        int t0 = (int) Math.floor(t);
        double ds = s - s0;
        double dt = t - t0;
        
        double m0 = (1 - ds) * (1 - dt);
        double m1 = (1 - ds) * dt;
        double m2 = ds * (1 - dt);
        double m3 = ds * dt;
        if (clazz == Double.class)
        {
            return (T) Double.valueOf(m0 * (Double) getTexel(level, s0, t0) +
                                      m1 * (Double) getTexel(level, s0, t0 + 1) +
                                      m2 * (Double) getTexel(level, s0 + 1, t0) +
                                      m3 * (Double) getTexel(level, s0 + 1, t0 + 1));
        }
        else if (clazz == RGBSpectrum.class)
        {
            RGBSpectrum tex0 = (RGBSpectrum) getTexel(level, s0, t0);
            RGBSpectrum tex1 = (RGBSpectrum) getTexel(level, s0, t0+1);
            RGBSpectrum tex2 = (RGBSpectrum) getTexel(level, s0+1, t0);
            RGBSpectrum tex3 = (RGBSpectrum) getTexel(level, s0+1, t0+1);
            return (T) tex0.times(m0)
                           .plus(tex1.times(m1))
                           .plus(tex2.times(m2))
                           .plus(tex3.times(m3));
        }
        else
        {
            throw new RuntimeException("Unsupported class " + clazz.getName());
        }
    }
    
    /**
     * Anisotropic elliptically weighted average lookup
     */
    public T lookup(Point2 st, Direction2 dst0, Direction2 dst1)
    {
        if (doTrilinear)
        {
            double width = Math.max(Math.max(Math.abs(dst0.get(0)), Math.abs(dst0.get(1))),
                                    Math.max(Math.abs(dst1.get(0)), Math.abs(dst1.get(1))));
            return lookup(st, 2 * width);
        }
        // compute ellipse minor and major axes
        if (dst0.lengthSquared() < dst1.lengthSquared())
        {
            var temp = dst0;
            dst0 = dst1;
            dst1 = temp;
        }
        double majorLength = dst0.length();
        double minorLength = dst1.length();
        // clamp ellipse eccentricity
        if (minorLength * maxAnisotropy < majorLength && minorLength > 0)
        {
            double scale = majorLength / ( minorLength * maxAnisotropy);
            dst1.timesEquals(scale);
            minorLength *= scale;
        }
        if (minorLength == 0)
        {
            return triangle(0, st);
        }
        // choose level of detail for lookup and perform filtering
        double lod = Math.max(0,  numLevels() - 1 + log2(minorLength));
        int ilod = (int) Math.floor(lod);
        T ewa0 = ellipticallyWeightedAverage(ilod, st, dst0, dst1);
        T ewa1 = ellipticallyWeightedAverage(ilod + 1, st, dst0, dst1);
        if (clazz == Double.class)
        {
            return (T) Double.valueOf(lerp(lod - ilod, (Double) ewa0, (Double) ewa1));
        }
        else if (clazz == RGBSpectrum.class)
        {
            return (T) lerp(lod - ilod, (RGBSpectrum) ewa0, (RGBSpectrum) ewa1);
        }
        else
        {
            throw new RuntimeException("Unsupported class " + clazz.getName());
        }
    }
    
    private T ellipticallyWeightedAverage(int level, Point2 st, Direction2 dst0, Direction2 dst1)
    {
        if (level >= numLevels())
        {
            return getTexel(numLevels() - 1, 0, 0);
        }
        // convert EWA coordinates to appropriate scale for level
        st = new Point2(st.get(0) * pyramid[level].uSize() - 0.5,
                        st.get(1) * pyramid[level].vSize() - 0.5);
        dst0 = new Direction2(dst0.get(0) * pyramid[level].uSize(),
                              dst0.get(1) * pyramid[level].vSize());
        dst1 = new Direction2(dst1.get(0) * pyramid[level].uSize(),
                              dst1.get(1) * pyramid[level].vSize());
        // compute ellipse coefficients to bound filter region
        // e(s, t) = A*s^2 + B*s*t + C*t^2 < F
        double A = dst0.get(1) * dst0.get(1) + dst1.get(1) * dst1.get(1) + 1;
        double B = -2 * (dst0.get(0) * dst0.get(1) + dst1.get(0) * dst1.get(1));
        double C = dst0.get(0) * dst0.get(0) + dst1.get(0) * dst1.get(0) + 1;
        double invF = 1 / (A * C - B * B * 0.25);
        A *= invF;
        B *= invF;
        C *= invF;
        // compute (s, t) bounding box of ellipse
        double det = -B * B + 4 * A * C;
        double invDet = 1 / det;
        double uSqrt = Math.sqrt(det * C);
        double vSqrt = Math.sqrt(A * det);
        int s0 = (int) Math.ceil(st.get(0) - 2 * invDet * uSqrt);
        int s1 = (int) Math.floor(st.get(0) + 2 * invDet * uSqrt);
        int t0 = (int) Math.ceil(st.get(1) - 2 * invDet * vSqrt);
        int t1 = (int) Math.floor(st.get(1) + 2 * invDet * vSqrt);
        // scan over ellipse bound and compute quadratic equation
        T sum;
        if (clazz == Double.class)
        {
            sum = (T) Double.valueOf(0.0);
        }
        else if (clazz == RGBSpectrum.class)
        {
            sum = (T) new RGBSpectrum(0, 0, 0);
        }
        else
        {
            throw new RuntimeException("Unsupported class " + clazz.getName());
        }
        double sumWeights = 0;
        for (int it = t0; it <= t1; it++)
        {
            double tt = it - st.get(1);
            for (int is = s0; is <= s1; is++)
            {
                double ss = is - st.get(0);
                // compute squared radius and filter texel if inside ellipse
                double r2 = A * ss * ss + B * ss * tt + C * tt * tt;
                if (r2 < 1)
                {
                    int index = Math.min((int) (r2 * WEIGHT_LUT_SIZE), WEIGHT_LUT_SIZE - 1);
                    double weight = WEIGHT_LUT[index];
                    T texel = getTexel(level, is, it);
                    if (clazz == Double.class)
                    {
                        sum = (T) Double.valueOf(((Double) sum) + (Double) texel * weight);
                    }
                    else if (clazz == RGBSpectrum.class)
                    {
                        ((RGBSpectrum) sum).plusEquals(((RGBSpectrum) texel).times(weight));
                    }
                    sumWeights += weight;
                }
            }
        }
        if (clazz == Double.class)
        {
            return (T) (Double.valueOf(((Double) sum) / sumWeights));
        }
        else if (clazz == RGBSpectrum.class)
        {
            return (T) ((RGBSpectrum) sum).divideBy(sumWeights);
        }
        else
        {
            throw new RuntimeException("Unsupported type " + clazz.getName());
        }
    }
    
    private T getTexel(int level, int s, int t)
    {
        BlockedArray<T> l = pyramid[level];
        // compute texel (s, t) accounting for boundary conditions
        switch(wrapMode)
        {
            case REPEAT:
                s = Math.floorMod(s, l.uSize());
                t = Math.floorMod(t, l.vSize());
                break;
            case CLAMP:
                s = clamp(s, 0, l.uSize() - 1);
                t = clamp(t, 0, l.vSize() - 1);
                break;
            case BLACK:
                if (s < 0 || s >= l.uSize() || t < 0 || t >= l.vSize())
                {
                    return black;
                }
                break;
        }
        return l.get(s, t);
    }
    
    public int width()
    {
        return (int) resolution.get(0);
    }
    
    public int height()
    {
        return (int) resolution.get(1);
    }
    
    public int numLevels()
    {
        return pyramid.length;
    }
    
    static ResampleWeight[] resampleWeights(int oldRes, int newRes)
    {
        if (newRes < oldRes)
        {
            throw new IllegalArgumentException("Programming error. Cannot resample to lower resolution. oldRes: " +
                                               oldRes + "; newRes: " + newRes);
        }
        ResampleWeight[] wt = new ResampleWeight[newRes];
        double filterWidth = 2.0;
        for (int i = 0; i < newRes; i++)
        {
            // compute resampling weights for ith texel
            double center = (i + 0.5) * oldRes / newRes;
            wt[i] = new ResampleWeight();
            wt[i].firstTexel = (int) Math.floor((center - filterWidth) + 0.5);
            for (int j = 0; j < 4; j++)
            {
                double pos = wt[i].firstTexel + j + 0.5;
                wt[i].weight[j] = lanczos((pos - center) / filterWidth);
            }
            // normalize filter weights
            double inverseSumWeights = 1 / (wt[i].weight[0] + wt[i].weight[1] + wt[i].weight[2] +
                                            wt[i].weight[3]);
            for (int j = 0; j < 4; j++)
            {
                wt[i].weight[j] *= inverseSumWeights;
            }
        }
        return wt;
    }

    static class ResampleWeight
    {
        int firstTexel;
        double[] weight = new double[4];
    }
    
    public enum ImageWrap
    {
        REPEAT, BLACK, CLAMP;
    }
}
