package scene.materials.functions.bxdf;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.cosTheta;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.*;

import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Normal3;
import core.math.Point2;
import core.tuple.Quadruple;
import scene.materials.TransportMode;
import scene.materials.functions.AbstractBidirectionalDistributionFunction;
import scene.materials.functions.MicrofacetDistribution;
import scene.materials.functions.AbstractBidirectionalDistributionFunction.BxDFType;
import scene.materials.functions.fresnel.FresnelDielectric;

public class MicrofacetTransmission extends AbstractBidirectionalDistributionFunction
{
    private final RGBSpectrum t;
    private final MicrofacetDistribution distribution;
    private final double etaA;
    private final double etaB;
    private final FresnelDielectric fresnel;
    private final TransportMode mode;

    public MicrofacetTransmission(RGBSpectrum t, MicrofacetDistribution distribution, double etaA,
            double etaB, TransportMode mode)
    {
        super(EnumSet.of(BxDFType.TRANSMISSION, BxDFType.GLOSSY));
        this.t = t;
        this.distribution = distribution;
        this.etaA = etaA;
        this.etaB = etaB;
        this.fresnel = new FresnelDielectric(etaA, etaB);
        this.mode = mode;
    }

    @Override
    public RGBSpectrum f(Direction3 wo, Direction3 wi)
    {
        if (sameHemisphere(wo, wi))
        {
            return new RGBSpectrum(0);
        }
        
        double cosThetaO = cosTheta(wo);
        double cosThetaI = cosTheta(wi);
        if (cosThetaI == 0 || cosThetaO == 0)
        {
            return new RGBSpectrum(0);
        }
        
        double eta = cosTheta(wo) > 0 ? (etaB / etaA) : (etaA / etaB);
        Direction3 wh = wo.plus(wi.times(eta)).normalize();
        if (wh.z() < 0)
        {
            wh = wh.opposite();
        }
        
        RGBSpectrum f = fresnel.evaluate(wo.dot(wh));
        
        double sqrtDenominator = wo.dot(wh) + eta * wi.dot(wh);
        double factor = (mode == TransportMode.RADIANCE) ? (1 / eta) : 1;

        return (new RGBSpectrum(1).minus(f)).times(t)
                                            .times(Math.abs(distribution.getDifferentialArea(wh) *
                                                            distribution.getVisibleMicrofacetFraction(wo, wi) *
                                                            eta * eta * wi.absDot(wh) *
                                                            wo.absDot(wh) * factor * factor /
                                                            (cosThetaI * cosThetaO * sqrtDenominator *
                                                             sqrtDenominator)));
    }

    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, EnumSet<BxDFType>> sample_f(
            Direction3 wo, Point2 sample)
    {
        Direction3 wh = distribution.sampleNormalDistribution(wo,  sample);
        double eta = cosTheta(wo) > 0 ? (etaA / etaB) : (etaB / etaA);
        Direction3 wi = refract(wo, new Normal3(wh), eta);
        if (wi == null)
        {
            return new Quadruple<>(new RGBSpectrum(0), wi, 0.0, type);
        }
        
        double pdf = pdf(wo, wi);
        return new Quadruple<>(f(wo, wi), wi, pdf, type);
    }
    
    @Override
    public double pdf(Direction3 wo, Direction3 wi)
    {
        if (sameHemisphere(wo, wi))
        {
            return 0;
        }
        
        // compute wh from wo, wi for microfacet transmission
        double eta = cosTheta(wo) > 0 ? (etaB / etaA) : (etaA / etaB);
        Direction3 wh = wo.plus(wi.times(eta)).normalize();
        
        // compute change of variables dwhdwi for microfacet transmission
        double sqrtDenom = wo.dot(wh) + eta * wi.dot(wh);
        double dwhdwi = Math.abs((eta * eta * wi.dot(wh)) / (sqrtDenom * sqrtDenom));
        
        return distribution.pdf(wo, wh) * dwhdwi;
    }
}
