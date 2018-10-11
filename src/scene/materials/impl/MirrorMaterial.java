package scene.materials.impl;

import core.colors.RGBSpectrum;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;
import scene.materials.TransportMode;
import scene.materials.functions.BidirectionalScatteringDistributionFunction;
import scene.materials.functions.Fresnel;
import scene.materials.functions.bxdf.SpecularReflection;
import scene.materials.functions.fresnel.FresnelNoOp;
import texture.Texture;

public class MirrorMaterial implements Material
{
    private final Texture<RGBSpectrum> kr;
    private final Texture<Double> bump;
    
    public MirrorMaterial(Texture<RGBSpectrum> kr, Texture<Double> bump)
    {
        this.kr = kr;
        this.bump = bump;
    }
    
    @Override
    public void computeScatteringFunctions(SurfaceInteraction si, TransportMode mode,
            boolean allowMultipleLobes)
    {
        // TODO bumpmapping

        BidirectionalScatteringDistributionFunction bsdf = new BidirectionalScatteringDistributionFunction(si);
        
        RGBSpectrum kr = this.kr.evaluate(si).clamp();
        if (!kr.isBlack())
        {
            Fresnel fresnel = new FresnelNoOp();
            bsdf.addBxdf(new SpecularReflection(kr, fresnel));
        }
        
        si.setBsdf(bsdf);
    }

}
