package scene.lights;

import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Transformation;
import scene.interactions.Interaction;
import scene.medium.Medium.MediumInterface;

public abstract class AreaLight extends Light
{
    protected AreaLight(Transformation lightToWorld, int numSamples, MediumInterface mediumInterface)
    {
        super(EnumSet.of(LightType.AREA), lightToWorld, numSamples, mediumInterface);
    }
    
    /**
     * Given a point <code>intr</code> on the surface of the light, return the emitted radiance
     * in the outgoing direction <code>w</code>.
     */
    public abstract RGBSpectrum radiance(Interaction intr, Direction3 w);
}
