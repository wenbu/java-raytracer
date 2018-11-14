package scene.materials.impl;

import core.colors.RGBSpectrum;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;
import scene.materials.TransportMode;
import scene.materials.functions.BidirectionalScatteringDistributionFunction;
import scene.materials.functions.Fresnel;
import scene.materials.functions.MicrofacetDistribution;
import scene.materials.functions.bxdf.LambertianReflection;
import scene.materials.functions.bxdf.MicrofacetReflection;
import scene.materials.functions.fresnel.FresnelDielectric;
import scene.materials.functions.microfacet.TrowbridgeReitzDistribution;
import texture.Texture;

public class PlasticMaterial implements Material
{
    private final Texture<RGBSpectrum> kd;
    private final Texture<RGBSpectrum> ks;
    private final Texture<Double> roughness;
    private final Texture<Double> bumpMap;
    private final boolean remapRoughness;

    public PlasticMaterial(Texture<RGBSpectrum> kd, Texture<RGBSpectrum> ks,
            Texture<Double> roughness, Texture<Double> bumpMap, boolean remapRoughness)
    {
        this.kd = kd;
        this.ks = ks;
        this.roughness = roughness;
        this.bumpMap = bumpMap;
        this.remapRoughness = remapRoughness;
    }

    @Override
    public void computeScatteringFunctions(SurfaceInteraction si, TransportMode mode,
            boolean allowMultipleLobes)
    {
        if (bumpMap != null)
        {
            bump(bumpMap, si);
        }

        BidirectionalScatteringDistributionFunction bsdf = new BidirectionalScatteringDistributionFunction(si);
        
        RGBSpectrum kd = this.kd.evaluate(si).clamp();
        if (!kd.isBlack())
        {
            bsdf.addBxdf(new LambertianReflection(kd));
        }
        
        RGBSpectrum ks = this.ks.evaluate(si).clamp();
        if (!ks.isBlack())
        {
            Fresnel fresnel = new FresnelDielectric(1, 1.5);
            double rough = roughness.evaluate(si);
            if (remapRoughness)
            {
                rough = TrowbridgeReitzDistribution.roughnessToAlpha(rough);
            }
            MicrofacetDistribution distribution = new TrowbridgeReitzDistribution(rough, rough);
            bsdf.addBxdf(new MicrofacetReflection(ks, distribution, fresnel));
        }
        si.setBsdf(bsdf);
    }
}
