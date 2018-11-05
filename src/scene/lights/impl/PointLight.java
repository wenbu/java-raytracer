package scene.lights.impl;

import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.tuple.Quadruple;
import scene.interactions.Interaction;
import scene.lights.Light;
import scene.medium.Medium.MediumInterface;

public class PointLight extends Light
{
    private final Point3 position;
    private final RGBSpectrum intensity;
    
    public PointLight(Transformation lightToWorld, MediumInterface mediumInterface, RGBSpectrum intensity)
    {
        super(EnumSet.of(LightType.DELTA_POSITION), lightToWorld, mediumInterface);
        position = lightToWorld.transform(new Point3());
        this.intensity = intensity;
    }
    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, VisibilityTester> sampleRadiance(Interaction ref, Point2 u)
    {
        Direction3 wi = position.minus(ref.getP()).normalize();
        double pdf = 1;
        VisibilityTester vis = new VisibilityTester(ref, new Interaction(position, ref.getT(), mediumInterface));
        RGBSpectrum radiance = intensity.divideBy(position.minus(ref.getP()).lengthSquared());
        return new Quadruple<>(radiance, wi, pdf, vis);
    }

    @Override
    public RGBSpectrum power()
    {
        return intensity.times(4 * Math.PI);
    }
    
    @Override
    public double pdfRadiance(Interaction ref, Direction3 wi)
    {
        return 0;
    }
}
