package core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import core.colors.RGBSpectrum;
import core.math.Point3;

/**
 * A Pixel represents a pixel in the image. It is defined by four corner points
 * and stores a collection of color samples.
 */
public class Pixel
{
    private final Point3 pixelUL;
    private final Point3 pixelUR;
    private final Point3 pixelLL;
    private final Point3 pixelLR;

    private final Set<RGBSpectrum> samples;

    public Pixel(Point3 pixelUL,
            Point3 pixelUR,
            Point3 pixelLL,
            Point3 pixelLR)
    {
        this.pixelUL = pixelUL;
        this.pixelUR = pixelUR;
        this.pixelLL = pixelLL;
        this.pixelLR = pixelLR;

        samples = new HashSet<>();
    }

    public void addSample(RGBSpectrum sample)
    {
        samples.add(sample);
    }

    public void addSamples(Collection<RGBSpectrum> samples)
    {
        this.samples.addAll(samples);
    }

    public Point3 getPixelUL()
    {
        return pixelUL;
    }

    public Point3 getPixelUR()
    {
        return pixelUR;
    }

    public Point3 getPixelLL()
    {
        return pixelLL;
    }

    public Point3 getPixelLR()
    {
        return pixelLR;
    }

}
