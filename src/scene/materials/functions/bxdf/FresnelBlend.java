package scene.materials.functions.bxdf;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.absCosTheta;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.reflect;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.sameHemisphere;
import static utilities.MathUtilities.INV_PI;
import static utilities.SamplingUtilities.cosineSampleHemisphere;

import java.util.EnumSet;
import java.util.function.Function;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Normal3;
import core.math.Point2;
import core.tuple.Quadruple;
import scene.materials.functions.AbstractBidirectionalDistributionFunction;
import scene.materials.functions.MicrofacetDistribution;

/**
 * Implementation of the Ashikhmin/Shirley model
 */
public class FresnelBlend extends AbstractBidirectionalDistributionFunction
{
    private final RGBSpectrum rd;
    private final RGBSpectrum rs;
    private final MicrofacetDistribution distribution;
    
    private static final Function<Double, Double> pow5 = x -> (x * x) * (x * x) * x;
    
    public FresnelBlend(RGBSpectrum rd, RGBSpectrum rs, MicrofacetDistribution distribution)
    {
        super(EnumSet.of(BxDFType.REFLECTION, BxDFType.GLOSSY));
        this.rd = rd;
        this.rs = rs;
        this.distribution = distribution;
    }

    @Override
    public RGBSpectrum f(Direction3 wo, Direction3 wi)
    {
        RGBSpectrum diffuse = rd.times((28 / (23 * Math.PI)))
                                .times(new RGBSpectrum(1).minus(rs))
                                .times(1 - pow5.apply(1 - 0.5 * absCosTheta(wi)))
                                .times(1 - pow5.apply(1 - 0.5 * absCosTheta(wo)));
        Direction3 wh = wi.plus(wo);
        
        if (wh.x() == 0 && wh.y() == 0 && wh.z() == 0)
        {
            return new RGBSpectrum(0);
        }
        
        RGBSpectrum specular = schlickFresnel(wi.dot(wh)).times(distribution.getDifferentialArea(wh) /
                                                                (4 * wi.absDot(wh) *
                                                                 Math.max(absCosTheta(wi),
                                                                          absCosTheta(wo))));
        return diffuse.plus(specular);
    }
    
    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, EnumSet<BxDFType>> sample_f(
            Direction3 wo, Point2 uOrig)
    {
        Point2 u = uOrig;
        Direction3 wi;
        if (u.get(0) < 0.5)
        {
            u.set(0, 2 * u.get(0));
            // cosine-sample hemisphere, flipping direction if needed
            wi = cosineSampleHemisphere(u);
            if (wo.z() < 0)
            {
                wi.setZ(wi.get(2) * -1);
            }
        }
        else
        {
            u.set(0, 2 * (u.get(0) - 0.5));
            // sample microfacet orientation wh and reflected direction wi
            Direction3 wh = distribution.sampleNormalDistribution(wo, u);
            wi = reflect(wo, new Normal3(wh));
            if (!sameHemisphere(wo, wi))
            {
                return new Quadruple<>(new RGBSpectrum(0), wi, 0.0, type);
            }
        }
        double pdf = pdf(wo, wi);
        return new Quadruple<>(f(wo, wi), wi, pdf, type);
    }
    
    @Override
    public double pdf(Direction3 wo, Direction3 wi)
    {
        if (!sameHemisphere(wo, wi))
        {
            return 0;
        }
        Direction3 wh = wo.plus(wi).normalize();
        double pdf_wh = distribution.pdf(wo, wh);
        return 0.5 * (absCosTheta(wi) * INV_PI + pdf_wh / (4 * wo.dot(wh)));
    }

    private RGBSpectrum schlickFresnel(double cosTheta)
    {
        
        return rs.plus(pow5.apply(1 - cosTheta)).times(new RGBSpectrum(1).minus(rs));
    }
}
