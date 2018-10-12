package sampler.impl;

import static utilities.SamplingUtilities.latinHypercube;
import static utilities.SamplingUtilities.stratifiedSample1D;
import static utilities.SamplingUtilities.stratifiedSample2D;

import java.util.Collections;
import java.util.List;

import core.math.Point2;
import sampler.PixelSampler;
import sampler.Sampler;

public class StratifiedSampler extends PixelSampler
{
    private final int xPixelSamples;
    private final int yPixelSamples;
    private final boolean jitterSamples;

    public StratifiedSampler(int xPixelSamples, int yPixelSamples, boolean jitterSamples,
            int nSampledDimensions)
    {
        super(xPixelSamples * yPixelSamples, nSampledDimensions);
        this.xPixelSamples = xPixelSamples;
        this.yPixelSamples = yPixelSamples;
        this.jitterSamples = jitterSamples;
    }

    @Override
    public void startPixel(Point2 p)
    {
        // generate single stratified samples for pixel
        for (int i = 0; i < samples1D.size(); i++)
        {
            stratifiedSample1D(samples1D.get(i), random, jitterSamples);
            Collections.shuffle(samples1D.get(i), random);
        }
        for (int i = 0; i < samples2D.size(); i++)
        {
            stratifiedSample2D(samples2D.get(i),
                               xPixelSamples,
                               yPixelSamples,
                               random,
                               jitterSamples);
            Collections.shuffle(samples2D.get(i), random);
        }
        // generate arrays of stratified samples for pixel
        for (int i = 0; i < sampleArrays1D.size(); i++)
        {
            List<List<Double>> currentDimension = sampleArrays1D.get(i);
            for (long j = 0; j < currentDimension.size(); j++)
            {
                List<Double> currentSample = currentDimension.get((int) j);
                stratifiedSample1D(currentSample, random, jitterSamples);
                Collections.shuffle(currentSample, random);
            }
        }
        for (int i = 0; i < sampleArrays2D.size(); i++)
        {
            List<List<Point2>> currentDimension = sampleArrays2D.get(i);
            for (long j = 0; j < currentDimension.size(); j++)
            {
                List<Point2> currentSample = currentDimension.get((int) j);
                latinHypercube(currentSample, random);
            }
        }
        super.startPixel(p);
    }

    @Override
    public Sampler getCopy(int seed)
    {
        StratifiedSampler copy = new StratifiedSampler(xPixelSamples,
                                                       yPixelSamples,
                                                       jitterSamples,
                                                       samples1D.size());
        copy.random.setSeed(seed);
        return copy;
    }
}
