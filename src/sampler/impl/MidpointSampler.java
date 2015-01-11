package sampler.impl;

import java.util.HashSet;
import java.util.Set;

import sampler.Sampler;
import core.Pixel;
import core.Sample;
import core.math.Point;

public class MidpointSampler implements Sampler
{
    @Override
    public Set<Sample> getSamples(Pixel pixel)
    {
        Set<Sample> samples = new HashSet<>();
        samples.add(new Sample(getMidpoint(pixel)));
        return samples;
    }

    private Point getMidpoint(Pixel pixel)
    {
        Point pixelUL = pixel.getPixelUL();
        Point pixelLL = pixel.getPixelLL();
        Point pixelLR = pixel.getPixelLR();

        return pixelLL.plus(pixelLR.minus(pixelLL).times(0.5))
                      .plus(pixelUL.minus(pixelLL).times(0.5));
    }
}
