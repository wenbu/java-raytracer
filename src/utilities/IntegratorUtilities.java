package utilities;

import static utilities.SamplingUtilities.powerHeuristic;

import java.util.EnumSet;
import java.util.List;

import core.Ray;
import core.RayDifferential;
import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point2;
import sampler.Sampler;
import scene.Scene;
import scene.interactions.Interaction;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;
import scene.lights.Light.VisibilityTester;
import scene.materials.functions.AbstractBidirectionalDistributionFunction.BxDFType;

public class IntegratorUtilities
{
    public static RGBSpectrum uniformSampleAllLights(Interaction it, Scene scene, Sampler sampler,
            List<Integer> numLightSamples)
    {
        return uniformSampleAllLights(it, scene, sampler, numLightSamples, false);
    }
    
    public static RGBSpectrum uniformSampleAllLights(Interaction it, Scene scene, Sampler sampler,
            List<Integer> numLightSamples, boolean handleMedia)
    {
        RGBSpectrum radiance = new RGBSpectrum(0);
        for (int j = 0; j < scene.getLights().size(); j++)
        {
            // accumulate contribution of jth light
            Light light = scene.getLights().get(j);
            int numSamples = numLightSamples.get(j);
            List<Point2> uLightArray = sampler.get2DArray(numSamples);
            List<Point2> uScatteringArray = sampler.get2DArray(numSamples);
            
            if (uLightArray == null || uScatteringArray == null)
            {
                // Sampler arrays exhausted; use a single sample for illumination
                Point2 uLight = sampler.get2D();
                Point2 uScattering = sampler.get2D();
                radiance.plusEquals(estimateDirect(it,
                                                   uScattering,
                                                   light,
                                                   uLight,
                                                   scene,
                                                   sampler,
                                                   handleMedia));
            }
            else
            {
                // estimate direct lighting using sample arrays
                RGBSpectrum directRadiance = new RGBSpectrum(0);
                for (int k = 0; j < numSamples; k++)
                {
                    directRadiance.plusEquals(estimateDirect(it,
                                                             uScatteringArray.get(k),
                                                             light,
                                                             uLightArray.get(k),
                                                             scene, sampler, handleMedia));
                }
                radiance.plusEquals(directRadiance.divideBy(numSamples));
            }
        }
        return radiance;
    }
    
    public static RGBSpectrum uniformSampleOneLight(Interaction it, Scene scene, Sampler sampler)
    {
        return uniformSampleOneLight(it, scene, sampler, false);
    }
    
    public static RGBSpectrum uniformSampleOneLight(Interaction it, Scene scene, Sampler sampler,
            boolean handleMedia)
    {
        // randomly choose a single light to sample
        int numLights = scene.getLights().size();
        if (numLights == 0)
        {
            return new RGBSpectrum(0);
        }
        int lightNum = Math.min((int) (sampler.get1D() * numLights), numLights - 1);
        Light light = scene.getLights().get(lightNum);

        Point2 uLight = sampler.get2D();
        Point2 uScattering = sampler.get2D();
        return estimateDirect(it,
                              uScattering,
                              light,
                              uLight,
                              scene,
                              sampler,
                              handleMedia).times(numLights);
    }
    
    public static RGBSpectrum estimateDirect(Interaction it, Point2 uScattering, Light light,
            Point2 uLight, Scene scene, Sampler sampler, boolean handleMedia)
    {
        return estimateDirect(it, uScattering, light, uLight, scene, sampler, handleMedia, false);
    }
    
    public static RGBSpectrum estimateDirect(Interaction it, Point2 uScattering, Light light,
            Point2 uLight, Scene scene, Sampler sampler, boolean handleMedia, boolean specular)
    {
        EnumSet<BxDFType> bsdfFlags = specular ? EnumSet.allOf(BxDFType.class) :
                                               EnumSet.complementOf(EnumSet.of(BxDFType.SPECULAR));
        RGBSpectrum directRadiance = new RGBSpectrum(0);
        
        // sample light source with multiple importance sampling
        var lightSample = light.sampleRadiance(it, uLight);
        RGBSpectrum radiance = lightSample.getFirst();
        Direction3 wi = lightSample.getSecond();
        double lightPdf = lightSample.getThird();
        VisibilityTester visibility = lightSample.getFourth();
        
        double scatteringPdf;
        if (lightPdf > 0 && !radiance.isBlack())
        {
            // compute BSDF or phase function's value for light sample
            RGBSpectrum f;
            if (it instanceof SurfaceInteraction)
            {
                // evaluate BSDF for light sampling strategy
                SurfaceInteraction isect = (SurfaceInteraction) it;
                f = isect.getBsdf().f(isect.getWo(), wi, bsdfFlags).times(wi.absDot(isect.getShadingGeometry().getN()));
                scatteringPdf = isect.getBsdf().pdf(isect.getWo(), wi, bsdfFlags);
            }
            else
            {
                // TODO evaluate phase function for light sampling strategy
                throw new UnsupportedOperationException("MediumInteraction TODO");
            }
            
            if(!f.isBlack())
            {
                // compute effect of visibility for light source sample
                if (handleMedia)
                {
                    radiance.timesEquals(visibility.transmittance(scene, sampler));
                }
                else if (!visibility.unoccluded(scene))
                {
                    radiance = new RGBSpectrum(0);
                }
                
                // add light's contribution to reflected radiance
                if (!radiance.isBlack())
                {
                    if (light.isDeltaLight())
                    {
                        directRadiance.plusEquals(f.times(radiance).divideBy(lightPdf));
                    }
                    else
                    {
                        double weight = powerHeuristic(1, lightPdf, 1, scatteringPdf);
                        directRadiance.plusEquals(f.times(radiance).times(weight / lightPdf));
                    }
                }
            }
        }
        // sample BSDF with multiple importance sampling
        if (!light.isDeltaLight())
        {
            RGBSpectrum f;
            boolean sampledSpecular = false;
            if (it instanceof SurfaceInteraction)
            {
                // sample scattered direction for surface interactions
                try
                {
                SurfaceInteraction isect = (SurfaceInteraction) it;
                var sample = isect.getBsdf().sampleF(isect.getWo(), uScattering, bsdfFlags);
                f = sample.getFirst();
                wi = sample.getSecond();
                scatteringPdf = sample.getThird();
                EnumSet<BxDFType> sampledType = sample.getFourth();
                f.timesEquals(wi.absDot(isect.getShadingGeometry().getN()));
                sampledSpecular = sampledType.contains(BxDFType.SPECULAR);
                } catch (NullPointerException e)
                {
                    System.out.println(":(");
                    throw e;
                }
            }
            else
            {
                // TODO sample scattered direction for medium interactions
                throw new UnsupportedOperationException("MediumInteraction TODO");
            }
            
            if (!f.isBlack() && scatteringPdf > 0)
            {
                // account for light contributions along sampled direction wi
                double weight = 1;
                if (!sampledSpecular)
                {
                    lightPdf = light.pdfRadiance(it, wi);
                    if (lightPdf == 0)
                    {
                        return directRadiance;
                    }
                    weight = powerHeuristic(1, scatteringPdf, 1, lightPdf);
                }
                // find intersection and compute transmittance
                SurfaceInteraction lightIsect;
                Ray ray = it.spawnRay(wi);
                RGBSpectrum transmittance = new RGBSpectrum(1);
                if (handleMedia)
                {
                    var mediaIntersection = scene.intersectTr(ray, sampler);
                    transmittance = mediaIntersection.getFirst();
                    lightIsect = mediaIntersection.getSecond();
                }
                else
                {
                    lightIsect = scene.intersect(ray);
                }
                
                // add light contribution from material sampling
                radiance = new RGBSpectrum(0);
                if (lightIsect != null)
                {
                    if (lightIsect.getPrimitive().getAreaLight() == light)
                    {
                        radiance = lightIsect.getEmittedRadiance(wi.times(-1));
                    }
                }
                else
                {
                    radiance = light.emittedRadiance(new RayDifferential(ray));
                }
                
                if (!radiance.isBlack())
                {
                    directRadiance.plusEquals(f.times(radiance)
                                               .times(transmittance)
                                               .times(weight / scatteringPdf));
                }
            }
        }
        return directRadiance;
    }
}
