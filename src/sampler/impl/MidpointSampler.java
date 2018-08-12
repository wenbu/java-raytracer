package sampler.impl;

import java.util.HashSet;
import java.util.Set;

import sampler.Sampler;
import core.Pixel;
import core.Sample;
import core.math.Point3;

public class MidpointSampler implements Sampler
{
    @Override
    public Set<Sample> getSamples(Pixel pixel)
    {
        Set<Sample> samples = new HashSet<>();
        samples.add(new Sample(getMidpoint(pixel)));
        return samples;
    }

    private Point3 getMidpoint(Pixel pixel)
    {
        Point3 pixelUL = pixel.getPixelUL();
        Point3 pixelLL = pixel.getPixelLL();
        Point3 pixelLR = pixel.getPixelLR();

        return pixelLL.plus(pixelLR.minus(pixelLL).times(0.5))
                      .plus(pixelUL.minus(pixelLL).times(0.5));
    }
}
