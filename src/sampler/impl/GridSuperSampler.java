package sampler.impl;

import java.util.HashSet;
import java.util.Set;

import core.Pixel;
import core.Sample;
import core.math.Direction;
import core.math.Point;
import sampler.Sampler;

public class GridSuperSampler implements Sampler
{
    private final int numSamplesX;
    private final int numSamplesY;

    public GridSuperSampler(int numSamplesX, int numSamplesY)
    {
        this.numSamplesX = numSamplesX;
        this.numSamplesY = numSamplesY;
    }

    @Override
    public Set<Sample> getSamples(Pixel pixel)
    {
        Set<Sample> samples = new HashSet<>();

        Point pixelLL = pixel.getPixelLL();
        Point pixelUL = pixel.getPixelUL();
        Point pixelLR = pixel.getPixelLR();

        Direction dx = pixelLR.minus(pixelLL);
        Direction dy = pixelUL.minus(pixelLL);

        double distanceFromEdgeX = 1.0 / ( 2.0 * numSamplesX );
        double distanceFromEdgeY = 1.0 / ( 2.0 * numSamplesY );
        double distanceBetweenSamplesX = 1.0 / numSamplesX;
        double distanceBetweenSamplesY = 1.0 / numSamplesY;

        for (int x = 0; x < numSamplesX; x++)
        {
            double distanceX = distanceFromEdgeX +
                               ( x * distanceBetweenSamplesX );
            for (int y = 0; y < numSamplesY; y++)
            {
                double distanceY = distanceFromEdgeY +
                                   ( y * distanceBetweenSamplesY );
                samples.add(new Sample(pixelLL.plus(dx.times(distanceX))
                                              .plus(dy.times(distanceY))));
            }
        }
        
        return samples;
    }

}
