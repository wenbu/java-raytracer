package scene.materials.functions.bxdf;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.*;
import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import scene.materials.functions.AbstractBidirectionalDistributionFunction;
import scene.materials.functions.Fresnel;
import scene.materials.functions.MicrofacetDistribution;

/**
 * An implementation of the Torrance-Sparrow model.
 */
public class MicrofacetReflection extends AbstractBidirectionalDistributionFunction
{
    private final RGBSpectrum r;
    private final MicrofacetDistribution distribution;
    private final Fresnel fresnel;

    public MicrofacetReflection(RGBSpectrum r, MicrofacetDistribution distribution, Fresnel fresnel)
    {
        super(EnumSet.of(BxDFType.REFLECTION, BxDFType.GLOSSY));
        this.r = r;
        this.distribution = distribution;
        this.fresnel = fresnel;
    }

    @Override
    public RGBSpectrum f(Direction3 wo, Direction3 wi)
    {
        double cosThetaO = absCosTheta(wo);
        double cosThetaI = absCosTheta(wi);
        Direction3 wh = wi.plus(wo);
        
        if (cosThetaI == 0 || cosThetaO == 0)
        {
            return new RGBSpectrum(0);
        }
        if (wh.x() == 0 && wh.y() == 0 && wh.z() == 0)
        {
            return new RGBSpectrum(0);
        }

        wh = wh.normalize();
        RGBSpectrum f = fresnel.evaluate(wi.dot(wh));
        return r.times(distribution.getDifferentialArea(wh) *
                       distribution.getVisibleMicrofacetFraction(wo, wi))
                .times(f)
                .divideBy(4 * cosThetaI * cosThetaO);
    }

}
