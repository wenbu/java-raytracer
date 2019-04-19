package integrator.impl;

import static utilities.IntegratorUtilities.uniformSampleOneLight;

import camera.Camera;
import core.RayDifferential;
import core.colors.RGBSpectrum;
import core.math.Direction3;
import integrator.SamplerIntegrator;
import sampler.Sampler;
import scene.Scene;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;
import scene.materials.functions.AbstractBidirectionalDistributionFunction.BxDFType;

import java.util.EnumSet;

public class PathIntegrator extends SamplerIntegrator
{
    private final int maxDepth;

    public PathIntegrator(int maxDepth, Camera camera, Sampler sampler)
    {
        super(sampler, camera);
        this.maxDepth = maxDepth;
    }

    @Override
    protected RGBSpectrum getRadiance(RayDifferential r, Scene scene, Sampler sampler, int depth)
    {
        RGBSpectrum radiance = new RGBSpectrum(0);
        RGBSpectrum beta = new RGBSpectrum(1); // path throughput weight
        RayDifferential ray = new RayDifferential(r);
        boolean specularBounce = false;

        for (int bounces = 0; ; bounces++)
        {
            // find next path vertex and accumulate contribution
            // intersect ray with scene
            SurfaceInteraction surfaceInteraction = scene.intersect(ray);

            // possibly add emitted light at intersection
            if (bounces == 0 || specularBounce)
            {
                // add emitted light at path vertex or from environment
                if (surfaceInteraction != null)
                {
                    RGBSpectrum emittedRadianceAtVertex = surfaceInteraction.getEmittedRadiance(ray.getDirection().times(-1));
                    radiance.plusEquals(emittedRadianceAtVertex.times(beta));
                }
                else
                {
                    for (Light light : scene.getLights())
                    {
                        radiance.plusEquals(light.emittedRadiance(ray).times(beta));
                    }
                }
            }

            // terminate path if ray escaped or maxDepth reached
            if (surfaceInteraction == null || bounces >= maxDepth)
            {
                break;
            }

            // compute scattering functions and skip medium boundaries
            surfaceInteraction.computeScatteringFunctions(ray);
            if (surfaceInteraction.getBsdf() == null)
            {
                ray = new RayDifferential(surfaceInteraction.spawnRay(ray.getDirection()));
                bounces--;
                continue;
            }

            // sample illumination from lights to find path contribution
            RGBSpectrum lightSample = uniformSampleOneLight(surfaceInteraction, scene, sampler);
            radiance.plusEquals(lightSample.times(beta));

            // sample BSDF to get new path direction
            Direction3 wo = ray.getDirection().times(-1);
            var sample = surfaceInteraction.getBsdf().sampleF(wo, sampler.get2D(), EnumSet.allOf(BxDFType.class));
            RGBSpectrum f = sample.getFirst();
            Direction3 wi = sample.getSecond();
            double pdf = sample.getThird();
            EnumSet<BxDFType> sampledTypes = sample.getFourth();

            if (f.isBlack() || pdf == 0)
            {
                break;
            }

            beta.timesEquals(f.times(wi.absDot(surfaceInteraction.getShadingGeometry().getN())).divideBy(pdf));
            specularBounce = sampledTypes.contains(BxDFType.SPECULAR);

            ray = new RayDifferential(surfaceInteraction.spawnRay(wi));

            // TODO account for subsurface sampling

            // possibly terminate path with русская рулетка
            if (bounces > 3)
            {
                double q = Math.max(0.05, 1 - beta.g());
                if (sampler.get1D() < q)
                {
                    break;
                }
                beta.divideEquals(1 - q);
            }
        }
        return radiance;
    }
}
