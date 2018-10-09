package scene.lights.impl;

import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.space.BoundingSphere;
import core.tuple.Quadruple;
import scene.Scene;
import scene.interactions.Interaction;
import scene.lights.Light;
import scene.medium.Medium.MediumInterface;

public class DirectionalLight extends Light
{
    private final RGBSpectrum luminance;
    private final Direction3 direction;
    
    private Point3 worldCenter;
    private double worldRadius;
    
    public DirectionalLight(Transformation lightToWorld, RGBSpectrum luminance, Direction3 direction)
    {
        super(EnumSet.of(LightType.DELTA_DIRECTION), lightToWorld, new MediumInterface());
        this.luminance = luminance;
        this.direction = lightToWorld.transform(Direction3.getNormalizedDirection(direction));
    }

    @Override
    public void preprocess(Scene scene)
    {
        BoundingSphere boundingSphere = scene.worldBound().boundingSphere();
        worldCenter = boundingSphere.getCenter();
        worldRadius = boundingSphere.getRadius();
    }
    
    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, VisibilityTester> sampleRadiance(
            Interaction ref, Point2 u)
    {
        Direction3 wi = direction;
        double pdf = 1;
        Point3 pOutside = ref.getP().plus(direction.times(2 * worldRadius));
        VisibilityTester vis = new VisibilityTester(ref,
                                                    new Interaction(pOutside,
                                                                    ref.getT(),
                                                                    mediumInterface));
        return new Quadruple<>(luminance, wi, pdf, vis);
    }

    @Override
    public RGBSpectrum power()
    {
        return luminance.times(Math.PI * worldRadius * worldRadius);
    }
}
