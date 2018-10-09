package scene.materials.impl;

import core.colors.RGBSpectrum;
import utilities.MathUtilities;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;
import scene.materials.TransportMode;
import scene.materials.functions.BidirectionalScatteringDistributionFunction;
import scene.materials.functions.bxdf.LambertianReflection;
import scene.materials.functions.bxdf.OrenNayar;
import texture.Texture;

public class MatteMaterial implements Material
{
    private final Texture<RGBSpectrum> kd;
    private final Texture<Double> sigma;
    private final Texture<Double> bump;

    public MatteMaterial(Texture<RGBSpectrum> kd, Texture<Double> roughness, Texture<Double> bump)
    {
        this.kd = kd;
        this.sigma = roughness;
        this.bump = bump;
    }

    @Override
    public void computeScatteringFunctions(SurfaceInteraction si, TransportMode mode,
            boolean allowMultipleLobes)
    {
        if (bump != null)
        {
            // TODO bumpmapping
        }
        
        BidirectionalScatteringDistributionFunction bsdf = new BidirectionalScatteringDistributionFunction(si);
        RGBSpectrum r = kd.evaluate(si).clamp();
        double sig = MathUtilities.clamp(sigma.evaluate(si), 0, 90);
        if (!r.isBlack())
        {
            if (sig == 0)
            {
                bsdf.addBxdf(new LambertianReflection(r));
            }
            else
            {
                bsdf.addBxdf(new OrenNayar(r, sig));
            }
        }
        si.setBsdf(bsdf);
    }
}
