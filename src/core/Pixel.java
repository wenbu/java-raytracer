package core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import core.colors.Color;
import core.math.Point;

/**
 * A Pixel represents a pixel in the image. It is defined by four corner points
 * and stores a collection of color samples.
 */
public class Pixel
{
    private final Point pixelUL;
    private final Point pixelUR;
    private final Point pixelLL;
    private final Point pixelLR;

    private final Set<Color> samples;

    public Pixel(Point pixelUL,
            Point pixelUR,
            Point pixelLL,
            Point pixelLR)
    {
        this.pixelUL = pixelUL;
        this.pixelUR = pixelUR;
        this.pixelLL = pixelLL;
        this.pixelLR = pixelLR;

        samples = new HashSet<>();
    }

    public void addSample(Color sample)
    {
        samples.add(sample);
    }

    public void addSamples(Collection<Color> samples)
    {
        this.samples.addAll(samples);
    }

    public Point getPixelUL()
    {
        return pixelUL;
    }

    public Point getPixelUR()
    {
        return pixelUR;
    }

    public Point getPixelLL()
    {
        return pixelLL;
    }

    public Point getPixelLR()
    {
        return pixelLR;
    }

}
