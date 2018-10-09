package scene.lights;

import java.util.EnumSet;
import java.util.Set;

import core.Ray;
import core.RayDifferential;
import core.colors.RGBSpectrum;
import core.colors.Colors;
import core.math.Direction3;
import core.math.Point2;
import core.math.Transformation;
import core.tuple.Quadruple;
import sampler.Sampler;
import scene.Scene;
import scene.interactions.Interaction;
import scene.interactions.impl.SurfaceInteraction;
import scene.medium.Medium.MediumInterface;

public abstract class Light
{
    public enum LightType
    {
        DELTA_POSITION, DELTA_DIRECTION, AREA, INFINITE
    }
    
    private final EnumSet<LightType> lightType;
    private final Transformation lightToWorld;
    private final Transformation worldToLight;
    private final int numSamples;
    protected final MediumInterface mediumInterface;
    
    public Light(EnumSet<LightType> lightType, Transformation lightToWorld, MediumInterface mediumInterface)
    {
        this(lightType, lightToWorld, 1, mediumInterface);
    }
    
    public Light(EnumSet<LightType> lightType, Transformation lightToWorld, int numSamples, MediumInterface mediumInterface)
    {
        this.lightType = lightType;
        this.lightToWorld = lightToWorld;
        this.worldToLight = lightToWorld.inverse();
        this.numSamples = Math.max(1, numSamples);
        this.mediumInterface = mediumInterface;
    }
    
    public boolean isDeltaLight()
    {
        return lightType.contains(LightType.DELTA_POSITION) || lightType.contains(LightType.DELTA_DIRECTION);
    }
    
    public abstract Quadruple<RGBSpectrum, Direction3, Double, VisibilityTester> sampleRadiance(Interaction ref, Point2 u);
    public abstract RGBSpectrum power();
    public void preprocess(Scene scene)
    {
        
    }
    
    /**
     * Return radiance due to rays that don't hit any geometry. (i.e. that escape the scene)
     * Default implementation returns no radiance.
     */
    public RGBSpectrum emittedRadiance(RayDifferential ray)
    {
        return new RGBSpectrum(0);
    }
    
    public static class VisibilityTester
    {
        private final Interaction p0;
        private final Interaction p1;
        
        public VisibilityTester(Interaction p0, Interaction p1)
        {
            this.p0 = p0;
            this.p1 = p1;
        }
        
        public Interaction p0()
        {
            return p0;
        }
        
        public Interaction p1()
        {
            return p1;
        }
        
        public boolean unoccluded(Scene scene)
        {
            return !scene.intersectP(p0.spawnRayTo(p1));
        }
        
        public RGBSpectrum transmittance(Scene scene, Sampler sampler)
        {
            Ray ray = p0.spawnRayTo(p1);
            RGBSpectrum transmittance = new RGBSpectrum(1, 1, 1);
            while(true)
            {
                SurfaceInteraction isect = scene.intersect(ray);
                // handle opaque surface
                if (isect != null && isect.getPrimitive().getMaterial() != null)
                {
                    return new RGBSpectrum(0, 0, 0);
                }
                /* TODO
                 * if (ray.getMedium() != null)
                 * {
                 *     transmittance = transmittance.times(ray.getMedium().transmittance(ray, sampler));
                 * }
                 */
                if (isect == null)
                {
                    break;
                }
                ray = isect.spawnRayTo(p1);
            }
            return transmittance;
        }
    }
}
