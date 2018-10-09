package sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import core.math.Point2;

public abstract class PixelSampler extends Sampler
{
    protected final List<List<Double>> samples1D;
    protected final List<List<Point2>> samples2D;
    protected int current1DDimension = 0;
    protected int current2DDimension = 0;
    protected final Random random;
    
    public PixelSampler(long samplesPerPixel, int nSampledDimensions)
    {
        super(samplesPerPixel);
        
        random = new Random();
        samples1D = new ArrayList<>(nSampledDimensions);
        samples2D = new ArrayList<>(nSampledDimensions);
        for (int i = 0; i < nSampledDimensions; i++)
        {
            List<Double> pixelSamples1D = new ArrayList<>((int) samplesPerPixel);
            List<Point2> pixelSamples2D = new ArrayList<>((int) samplesPerPixel);
            for (int j = 0; j < samplesPerPixel; j++)
            {
                pixelSamples1D.add(0.0);
                pixelSamples2D.add(null);
            }
            
            samples1D.add(pixelSamples1D);
            samples2D.add(pixelSamples2D);
        }
    }
    
    @Override
    public boolean startNextSample()
    {
        current1DDimension = 0;
        current2DDimension = 0;
        return super.startNextSample();
    }
    
    @Override
    public boolean setSampleNumber(long sampleNum)
    {
        current1DDimension = 0;
        current2DDimension = 0;
        return super.setSampleNumber(sampleNum);
    }

    @Override
    public double get1D()
    {
        if (current1DDimension < samples1D.size())
        {
            return samples1D.get(current1DDimension++).get((int) currentPixelSampleIndex);
        }
        else
        {
            return random.nextDouble();
        }
    }

    @Override
    public Point2 get2D()
    {
        if (current2DDimension < samples2D.size())
        {
            try
            {
            List<Point2> samples = samples2D.get(current2DDimension++);
            Point2 sample = samples.get((int) currentPixelSampleIndex);
            return sample;
            }
            catch (IndexOutOfBoundsException e)
            {
                throw e;
            }
        }
        else
        {
            return new Point2(random.nextDouble(), random.nextDouble());
        }
    }

    @Override
    public abstract Sampler getCopy(int seed);
}
