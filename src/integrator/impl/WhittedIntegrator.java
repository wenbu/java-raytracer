package integrator.impl;

import camera.Camera;
import core.RayDifferential;
import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Normal3;
import integrator.SamplerIntegrator;
import sampler.Sampler;
import scene.Scene;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;
import scene.lights.Light.VisibilityTester;

public class WhittedIntegrator extends SamplerIntegrator
{
    private final int maxDepth;
    
    public WhittedIntegrator(Sampler sampler, Camera camera, int maxDepth)
    {
        super(sampler, camera);
        this.maxDepth = maxDepth;
    }
    
    @Override
    protected RGBSpectrum getRadiance(RayDifferential ray, Scene scene, Sampler sampler, int depth)
    {
        RGBSpectrum radiance = new RGBSpectrum(0);
        
        SurfaceInteraction surfaceInteraction = scene.intersect(ray);
        if (surfaceInteraction == null)
        {
            for (Light light : scene.getLights())
            {
                radiance.plusEquals(light.emittedRadiance(ray));
            }
            return radiance;
        }
        
        // compute emitted and reflected light at ray intersection point
        Normal3 n = surfaceInteraction.getShadingGeometry().getN();
        Direction3 wo = surfaceInteraction.getWo();
        surfaceInteraction.computeScatteringFunctions(ray);

        radiance.plusEquals(surfaceInteraction.getEmittedRadiance(wo));

        for (Light light : scene.getLights())
        {
            var sample = light.sampleRadiance(surfaceInteraction, sampler.get2D());
            RGBSpectrum incidentRadiance = sample.getFirst();
            Direction3 wi = sample.getSecond();
            double pdf = sample.getThird();
            VisibilityTester visibility = sample.getFourth();
            
            if (incidentRadiance.isBlack() || pdf == 0)
            {
                continue;
            }
            
            RGBSpectrum f = surfaceInteraction.getBsdf().f(wo, wi);
            if (!f.isBlack() && visibility.unoccluded(scene))
            {
                radiance.plusEquals(f.times(incidentRadiance).times(wi.absDot(n) / pdf));
            }
        }
        
        if (depth + 1 < maxDepth)
        {
            radiance.plusEquals(specularReflect(ray, surfaceInteraction, scene, sampler, depth));
            radiance.plusEquals(specularTransmit(ray, surfaceInteraction, scene, sampler, depth));
        }
        
        return radiance;
        
    }

}
