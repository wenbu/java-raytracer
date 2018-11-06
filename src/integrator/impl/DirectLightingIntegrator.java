package integrator.impl;

import static utilities.IntegratorUtilities.uniformSampleAllLights;
import static utilities.IntegratorUtilities.uniformSampleOneLight;

import java.util.LinkedList;
import java.util.List;

import camera.Camera;
import core.RayDifferential;
import core.colors.RGBSpectrum;
import core.math.Direction3;
import integrator.SamplerIntegrator;
import sampler.Sampler;
import scene.Scene;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;

/**
 * A Monte Carlo integrator that does direct lighting only.
 */
public class DirectLightingIntegrator extends SamplerIntegrator
{
    public enum LightStrategy
    {
        /**
         * Take Light.numSamples samples from each light.
         */
        UNIFORM_SAMPLE_ALL,
        
        /**
         * Take one sample from one light, chosen at random.
         */
        UNIFORM_SAMPLE_ONE
    }
    
    private final LightStrategy strategy;
    private final int maxDepth;
    private List<Integer> numLightSamples = new LinkedList<>();
    
    public DirectLightingIntegrator(Sampler sampler, Camera camera, LightStrategy lightStrategy, int maxDepth)
    {
        super(sampler, camera);
        this.strategy = lightStrategy;
        this.maxDepth = maxDepth;
    }
    
    @Override
    public void preprocess(Scene scene, Sampler sampler)
    {
        if (strategy == LightStrategy.UNIFORM_SAMPLE_ALL)
        {
            // compute number of samples for each light
            for (Light light : scene.getLights())
            {
                numLightSamples.add(sampler.roundSampleCount(light.getNumSamples()));
            }
            // request samples
            for (int i = 0; i < maxDepth; i++)
            {
                for (int j = 0; j < scene.getLights().size(); j++)
                {
                    sampler.request2DArray(numLightSamples.get(j));
                    sampler.request2DArray(numLightSamples.get(j));
                }
            }
        }
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
                radiance = radiance.plus(light.emittedRadiance(ray));
            }
            return radiance;
        }
        
        // compute scattering functions
        surfaceInteraction.computeScatteringFunctions(ray);
        if (surfaceInteraction.getBsdf() == null)
        {
            return getRadiance(new RayDifferential(surfaceInteraction.spawnRay(ray.getDirection())),
                               scene,
                               sampler,
                               depth);
        }
        Direction3 wo = surfaceInteraction.getWo();
        
        radiance = radiance.plus(surfaceInteraction.getEmittedRadiance(wo));
        
        if (scene.getLights().size() > 0)
        {
            switch (strategy)
            {
            case UNIFORM_SAMPLE_ALL:
                radiance.plusEquals(uniformSampleAllLights(surfaceInteraction,
                                                           scene,
                                                           sampler,
                                                           numLightSamples));
                break;
            case UNIFORM_SAMPLE_ONE:
                radiance.plusEquals(uniformSampleOneLight(surfaceInteraction, scene, sampler));
                break;
            }
        }
        
        if (depth + 1 < maxDepth)
        {
            radiance = radiance.plus(specularReflect(ray, surfaceInteraction, scene, sampler, depth));
            radiance = radiance.plus(specularTransmit(ray, surfaceInteraction, scene, sampler, depth));
        }
        
        return radiance;
    }

}
