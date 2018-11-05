package scene.lights.impl;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point2;
import core.math.Transformation;
import core.tuple.Quadruple;
import scene.geometry.Shape;
import scene.interactions.Interaction;
import scene.lights.AreaLight;
import scene.medium.Medium.MediumInterface;

public class DiffuseAreaLight extends AreaLight
{
    private final RGBSpectrum emittedRadiance;
    private Shape shape;
    private final double area;
    
    public DiffuseAreaLight(Transformation lightToWorld, MediumInterface mediumInterface,
            RGBSpectrum emittedRadiance, int numSamples, Shape shape)
    {
        super(lightToWorld, numSamples, mediumInterface);
        this.emittedRadiance = emittedRadiance;
        this.shape = shape;
        this.area = shape.surfaceArea();
    }

    @Override
    public RGBSpectrum radiance(Interaction intr, Direction3 w)
    {
        return intr.getN().dot(w) > 0 ? emittedRadiance : new RGBSpectrum(0);
    }

    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, VisibilityTester> sampleRadiance(
            Interaction ref, Point2 u)
    {
        Interaction pShape = shape.sample(ref, u);
        pShape.setMediumInterface(mediumInterface);
        Direction3 wi = pShape.getP().minus(ref.getP()).normalize();
        double pdf = shape.pdf(ref, wi);
        VisibilityTester vis = new VisibilityTester(ref, pShape);
        return new Quadruple<>(radiance(pShape, wi.times(-1)), wi, pdf, vis);
    }

    @Override
    public double pdfRadiance(Interaction ref, Direction3 wi)
    {
        return shape.pdf(ref, wi);
    }

    @Override
    public RGBSpectrum power()
    {
        return emittedRadiance.times(area * Math.PI);
    }

}
