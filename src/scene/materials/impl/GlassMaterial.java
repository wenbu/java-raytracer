package scene.materials.impl;

import core.colors.RGBSpectrum;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;
import scene.materials.TransportMode;
import scene.materials.functions.BidirectionalScatteringDistributionFunction;
import scene.materials.functions.Fresnel;
import scene.materials.functions.MicrofacetDistribution;
import scene.materials.functions.bxdf.FresnelSpecular;
import scene.materials.functions.bxdf.MicrofacetReflection;
import scene.materials.functions.bxdf.MicrofacetTransmission;
import scene.materials.functions.bxdf.SpecularReflection;
import scene.materials.functions.bxdf.SpecularTransmission;
import scene.materials.functions.fresnel.FresnelDielectric;
import scene.materials.functions.microfacet.TrowbridgeReitzDistribution;
import texture.Texture;

public class GlassMaterial implements Material
{
    private final Texture<RGBSpectrum> kr;
    private final Texture<RGBSpectrum> kt;
    private final Texture<Double> uRoughness;
    private final Texture<Double> vRoughness;
    private final Texture<Double> index;
    private final Texture<Double> bump;
    private final boolean remapRoughness;
    
    public GlassMaterial(Texture<RGBSpectrum> kr, Texture<RGBSpectrum> kt,
            Texture<Double> uRoughness, Texture<Double> vRoughness, Texture<Double> index,
            Texture<Double> bump, boolean remapRoughness)
    {
        this.kr = kr;
        this.kt = kt;
        this.uRoughness = uRoughness;
        this.vRoughness = vRoughness;
        this.index = index;
        this.bump = bump;
        this.remapRoughness = remapRoughness;
    }
    
    @Override
    public void computeScatteringFunctions(SurfaceInteraction si, TransportMode mode,
            boolean allowMultipleLobes)
    {
        // TODO bump
        
        double eta = index.evaluate(si);
        double uRough = uRoughness.evaluate(si);
        double vRough = vRoughness.evaluate(si);
        RGBSpectrum r = kr.evaluate(si).clamp();
        RGBSpectrum t = kt.evaluate(si).clamp();
        
        BidirectionalScatteringDistributionFunction bsdf = new BidirectionalScatteringDistributionFunction(si, eta);
        si.setBsdf(bsdf);
        
        if (r.isBlack() && t.isBlack())
        {
            return;
        }
        
        boolean isSpecular = uRough == 0 && vRough == 0;
        if (isSpecular && allowMultipleLobes)
        {
            bsdf.addBxdf(new FresnelSpecular(r, t, 1, eta, mode));
        }
        else
        {
            if (remapRoughness)
            {
                uRough = TrowbridgeReitzDistribution.roughnessToAlpha(uRough);
                vRough = TrowbridgeReitzDistribution.roughnessToAlpha(vRough);
            }
            MicrofacetDistribution distribution = isSpecular ? null :
                                                             new TrowbridgeReitzDistribution(uRough, vRough);
            if (!r.isBlack())
            {
                Fresnel fresnel = new FresnelDielectric(1, eta);
                if (isSpecular)
                {
                    bsdf.addBxdf(new SpecularReflection(r, fresnel));
                }
                else
                {
                    bsdf.addBxdf(new MicrofacetReflection(r, distribution, fresnel));
                }
            }
            
            if (!t.isBlack())
            {
                if (isSpecular)
                {
                    bsdf.addBxdf(new SpecularTransmission(t, 1, eta, mode));
                }
                else
                {
                    bsdf.addBxdf(new MicrofacetTransmission(t, distribution, 1.0, eta, mode));
                }
            }
        }
    }
}
