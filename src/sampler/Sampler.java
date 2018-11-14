package sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import core.math.Point2;
import camera.CameraSample;

public abstract class Sampler
{
    protected static final Logger logger = Logger.getLogger(Sampler.class.getName());
    
    // XXX Java's lists and arrays are indexed by ints. This might become a problem with very large sample counts.
    protected final long samplesPerPixel;
    
    protected Point2 currentPixel;
    protected long currentPixelSampleIndex;
    
    /*
     *  All 1D sample arrays for the current pixel.
     *  
     *  e.g. if arrays S, T, and U are requested, this means that
     *  each pixel requires a set of sample arrays <S, T, U>. Let
     *  x be the number of samples per pixel. Then the structure of
     *  sampleArrays1D is: 
     *  
     *  [ [S1, S2, S3, ... Sx],
     *    [T1, T2, T3, ... Tx],
     *    [U1, U2, U3, ... Ux] ]
     *    
     *  where each Sx, Tx, Ux is a List with the size requested in
     *  request1DArray.
     *  
     *  To use pbrt terminology, the first index represents the dimension, and
     *  the second represents the sample number.
     *  
     *  Note that in the cpp implementation, [S1...Sx] is a single linearized
     *  vector of floats.
     */
    protected List<List<List<Double>>> sampleArrays1D = new ArrayList<>();
    protected List<List<List<Point2>>> sampleArrays2D = new ArrayList<>();
    protected int sampleArray1DIndex = 0;
    protected int sampleArray2DIndex = 0;
    
    public Sampler(long samplesPerPixel)
    {
        this.samplesPerPixel = samplesPerPixel;
        if (samplesPerPixel > Integer.MAX_VALUE)
        {
            logger.severe("SamplesPerPixel (" + samplesPerPixel + ") > Integer.MAX_VALUE.");
        }
    }
    
    public void startPixel(Point2 p)
    {
        currentPixel = p;
        currentPixelSampleIndex = 0;
        sampleArray1DIndex = 0;
        sampleArray2DIndex = 0;
    }
    
    public abstract double get1D();
    public abstract Point2 get2D();
    
    /**
     * Allocate n 1D samples. Should be called before rendering begins.
     * @param n number of 1D samples
     */
    public void request1DArray(int n)
    {
        List<List<Double>> newSampleArray = new ArrayList<>((int) samplesPerPixel);
        for (long i = 0; i < samplesPerPixel; i++)
        {
            List<Double> sampleArray = new ArrayList<>(Collections.nCopies(n, 0.0));
            newSampleArray.add(sampleArray);
        }
        sampleArrays1D.add(newSampleArray);
    }
    
    /**
     * Allocate n 2D samples. Should be called before rendering begins.
     * @param n number of 2D samples.
     */
    public void request2DArray(int n)
    {
        List<List<Point2>> newSampleArray = new ArrayList<>((int) samplesPerPixel);
        for (long i = 0; i < samplesPerPixel; i++)
        {
            List<Point2> sampleArray = new ArrayList<>(Collections.nCopies(n, null));
            newSampleArray.add(sampleArray);
        }
        sampleArrays2D.add(newSampleArray);
    }
    
    /**
     * Get an array of 1D samples of size n.
     * @param n number of samples in the array
     * @return an array of n 1D samples
     */
    public List<Double> get1DArray(int n)
    {
        if (sampleArray1DIndex == sampleArrays1D.size())
        {
            return null;
        }
        return sampleArrays1D.get(sampleArray1DIndex++).get((int) currentPixelSampleIndex);
    }
    
    /**
     * Get an array of 2D samples of size n.
     * @param n number of samples in the array
     * @return an array of n 2D samples
     */
    public List<Point2> get2DArray(int n)
    {
        if (sampleArray2DIndex == sampleArrays2D.size())
        {
            return null;
        }
        return sampleArrays2D.get(sampleArray2DIndex++).get((int) currentPixelSampleIndex);
    }
    
    /**
     * Should always be called before getting a sample.
     * 
     * @return true if the caller may proceed with getting samples for the current
     *         pixel. If false, the caller should start a new pixel.
     */
    public boolean startNextSample()
    {
        sampleArray1DIndex = 0;
        sampleArray2DIndex = 0;
        return ++currentPixelSampleIndex < samplesPerPixel;
    }
    
    /**
     * Set the sample index of the current pixel. Useful for algorithms that don't use 
     * all the samples in a pixel before moving on to another.
     * @param sampleNum
     * @return true if sampleNum is less than the number of samples per pixel.
     */
    public boolean setSampleNumber(long sampleNum)
    {
        sampleArray1DIndex = 0;
        sampleArray2DIndex = 0;
        currentPixelSampleIndex = sampleNum;
        return currentPixelSampleIndex < samplesPerPixel;
    }
    
    public long getCurrentSampleNumber()
    {
        return currentPixelSampleIndex;
    }
    
    public abstract Sampler getCopy(int seed);
    
    public CameraSample getCameraSample(Point2 pRaster)
    {
        Point2 pFilm = pRaster.plus(get2D());
        double time = get1D();
        Point2 pLens = get2D();
        return new CameraSample(pFilm, pLens, time);
    }
    
    /**
     * Given a desired sample count, get a rounded up number of samples more suitable
     * to the sampler implementation, if applicable. The returned value should be used
     * when calling the {@link #request1DArray(int)} or {@link #request2DArray(int)}
     * methods.
     * @param n number of desired samples
     * @return a rounded number of samples (always at least the input value) that
     * should be used when requesting samples.
     */
    public int roundSampleCount(int n)
    {
        return n;
    }
    
    public long getSamplesPerPixel()
    {
        return samplesPerPixel;
    }
}
