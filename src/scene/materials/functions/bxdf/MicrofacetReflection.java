package scene.materials.functions.bxdf;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.*;
import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Normal3;
import core.math.Point2;
import core.tuple.Quadruple;
import scene.materials.functions.AbstractBidirectionalDistributionFunction;
import scene.materials.functions.Fresnel;
import scene.materials.functions.MicrofacetDistribution;
import scene.materials.functions.AbstractBidirectionalDistributionFunction.BxDFType;

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

    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, EnumSet<BxDFType>> sample_f(
            Direction3 wo, Point2 sample)
    {
        // sample microfacet orientation wh and reflected direction wi
        Direction3 wh = distribution.sampleNormalDistribution(wo, sample);
        Direction3 wi = reflect(wo, new Normal3(wh));
        if (!sameHemisphere(wo, wi))
        {
            return new Quadruple<>(new RGBSpectrum(0), wi, 0.0, type);
        }
        // compute pdf of wi for microfacet reflection
        double pdf = distribution.pdf(wo, wh) / (4 * wo.dot(wh));
        
        return new Quadruple<>(f(wo, wi), wi, pdf, type);
    }
}
