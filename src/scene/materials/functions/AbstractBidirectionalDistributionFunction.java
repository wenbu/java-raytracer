package scene.materials.functions;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.absCosTheta;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.sameHemisphere;
import static utilities.SamplingUtilities.cosineSampleHemisphere;
import static utilities.SamplingUtilities.uniformHemispherePdf;
import static utilities.SamplingUtilities.uniformSampleHemisphere;

import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point2;
import core.tuple.Quadruple;

public abstract class AbstractBidirectionalDistributionFunction
{
    public enum BxDFType
    {
        REFLECTION, TRANSMISSION, DIFFUSE, GLOSSY, SPECULAR
    }

    // Type constraints:
    // - At least one of REFLECTION, TRANSMISSION
    // - Exactly one of DIFFUSE, GLOSSY, SPECULAR
    protected final EnumSet<BxDFType> type;

    public AbstractBidirectionalDistributionFunction(EnumSet<BxDFType> type)
    {
        this.type = type;
    }

    public boolean matchesTypes(EnumSet<BxDFType> types)
    {
        return types.containsAll(this.type);
    }

    public boolean hasType(BxDFType type)
    {
        return this.type.contains(type);
    }

    public EnumSet<BxDFType> getType()
    {
        return type;
    }

    // Return the value of this distribution function for the two given directions.
    public abstract RGBSpectrum f(Direction3 wo, Direction3 wi);

    // For e.g. perfectly specular materials, light is only scattered from one
    // incident direction to one outgoing direction. This handles both this delta
    // distribution and Monte Carlo sampling.
    public Quadruple<RGBSpectrum, Direction3, Double, EnumSet<BxDFType>> sample_f(
            Direction3 wo, Point2 sample)
    {
        Direction3 wi = cosineSampleHemisphere(sample);
        if (wo.z() < 0)
        {
            wi.setZ(-wi.z());
        }
        double pdf = pdf(wo, wi);
        return new Quadruple<>(f(wo, wi), wi, pdf, type);
    }

    // hemispherical-directional reflectance
    // total reflection in a given direction due to constant illumination over the
    // hemisphere
    public RGBSpectrum rho(Direction3 wo, Point2[] samples)
    {
        RGBSpectrum r = new RGBSpectrum(0, 0, 0);
        for (Point2 sample : samples)
        {
            var sample_f = sample_f(wo, sample);
            double pdf = sample_f.getThird();
            if (pdf > 0)
            {
                RGBSpectrum f = sample_f.getFirst();
                Direction3 wi = sample_f.getSecond();
                r = r.plus(f.times(absCosTheta(wi) / pdf));
            }
        }
        return r.divideBy(samples.length);
    }

    // hemispherical-hemispherical reflectance
    // fraction of incident light reflected when incident light is same from all
    // directions
    public RGBSpectrum rho(Point2[] u1, Point2[] u2)
    {
        assert u1.length == u2.length : "u1 and u2 length must be equal; u1.length = " + u1.length +
                                        "; u2.length = " + u2.length;
        RGBSpectrum r = new RGBSpectrum(0, 0, 0);
        for (int i = 0; i < u1.length; i++)
        {
            Direction3 wo = uniformSampleHemisphere(u1[i]);
            double pdfo = uniformHemispherePdf();
            var sample_f = sample_f(wo, u2[i]);
            double pdfi = sample_f.getThird();
            if (pdfi > 0)
            {
                RGBSpectrum f = sample_f.getFirst();
                Direction3 wi = sample_f.getSecond();
                r = r.plus(f.times(absCosTheta(wi) * absCosTheta(wo) / (pdfo * pdfi)));
            }
        }
        
        return r.divideBy(Math.PI * u1.length);
    }

    public double pdf(Direction3 wo, Direction3 wi)
    {
        return sameHemisphere(wo, wi) ? absCosTheta(wi) * (1 / Math.PI) : 0;
    }
}
