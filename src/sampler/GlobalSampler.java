package sampler;

import java.util.List;

import core.math.Point2;

public abstract class GlobalSampler extends Sampler
{
    // The next dimension that will be sampled
    private int dimension;
    
    // index of current sample in current pixel
    private long intervalSampleIndex;
    
    // leave 5 for camera samples
    private final int arrayStartDim = 5;
    private int arrayEndDim;
    
    public GlobalSampler(long samplesPerPixel)
    {
        super(samplesPerPixel);
    }
    
    public abstract long getIndexForSample(long sampleNum);
    
    public abstract double sampleDimension(long index, int dimension);
    
    @Override
    public void startPixel(Point2 p)
    {
        super.startPixel(p);
        dimension = 0;
        intervalSampleIndex = getIndexForSample(0);
        
        // compute arrayEndDim for dimensions used for array samples
        arrayEndDim = arrayStartDim + sampleArrays1D.size() + 2 * sampleArrays2D.size();
        
        // compute 1d array samples
        for (int i = 0; i < sampleArrays1D.size(); i++)
        {
            // all arrays for a given sample
            List<List<Double>> sampleArrays = sampleArrays1D.get(i);
            int numArrays = sampleArrays.size();
            int numSamples = numArrays * (int) samplesPerPixel;
            for (int j = 0; j < numSamples; j++)
            {
                long index = getIndexForSample(j);
                // array selector
                long sampleIndex = index / numArrays;
                // array index
                long arrayIndex = index % numArrays;
                sampleArrays.get((int) sampleIndex)
                            .set((int) arrayIndex, sampleDimension(index, arrayStartDim + i));
            }
        }
        // compute 2d array samples
        int dim = arrayStartDim + sampleArrays1D.size();
        for (int i = 0; i < sampleArrays2D.size(); i++)
        {
            List<List<Point2>> sampleArrays = sampleArrays2D.get(i);
            int numArrays = sampleArrays.size();
            int numSamples = numArrays * (int) samplesPerPixel;
            for (int j = 0; j < numSamples; j++)
            {
                long index = getIndexForSample(j);
                long sampleIndex = index / numArrays;
                long arrayIndex = index % numArrays;
                sampleArrays.get((int) sampleIndex)
                            .set((int) arrayIndex,
                                 new Point2(sampleDimension(index, dim),
                                            sampleDimension(index, dim + 1)));
            }
        }
    }
    
    @Override
    public boolean startNextSample()
    {
        dimension = 0;
        intervalSampleIndex = getIndexForSample(currentPixelSampleIndex + 1);
        return super.startNextSample();
    }
    
    @Override
    public boolean setSampleNumber(long sampleNum)
    {
        dimension = 0;
        intervalSampleIndex = getIndexForSample(sampleNum);
        return super.setSampleNumber(sampleNum);
    }
    
    @Override
    public double get1D()
    {
        if (dimension >= arrayStartDim && dimension < arrayEndDim)
        {
            dimension = arrayEndDim;
        }
        return sampleDimension(intervalSampleIndex, dimension++);
    }

    @Override
    public Point2 get2D()
    {
        if (dimension + 1 >= arrayStartDim && dimension < arrayEndDim)
        {
            dimension = arrayEndDim;
        }
        Point2 sample = new Point2(sampleDimension(intervalSampleIndex, dimension),
                                   sampleDimension(intervalSampleIndex, dimension + 1));
        dimension += 2;
        return sample;
    }

    @Override
    public abstract Sampler getCopy(int seed);
}
