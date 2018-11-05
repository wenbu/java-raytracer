package scene.materials.functions.bxdf;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.*;
import static utilities.ReflectionUtilities.*;
import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Normal3;
import core.math.Point2;
import core.tuple.Quadruple;
import scene.materials.TransportMode;
import scene.materials.functions.AbstractBidirectionalDistributionFunction;
import scene.materials.functions.Fresnel;
import scene.materials.functions.fresnel.FresnelDielectric;

public class FresnelSpecular extends AbstractBidirectionalDistributionFunction
{
    private final RGBSpectrum r;
    private final RGBSpectrum t;
    private final double etaA;
    private final double etaB;
    private final Fresnel fresnel;
    private final TransportMode mode;
    
    public FresnelSpecular(RGBSpectrum r, RGBSpectrum t, double etaA, double etaB, TransportMode mode)
    {
        super(EnumSet.of(BxDFType.REFLECTION, BxDFType.TRANSMISSION, BxDFType.SPECULAR));
        this.r = r;
        this.t = t;
        this.etaA = etaA;
        this.etaB = etaB;
        this.fresnel = new FresnelDielectric(etaA, etaB);
        this.mode = mode;
        
    }
    @Override
    public RGBSpectrum f(Direction3 wo, Direction3 wi)
    {
        return new RGBSpectrum(0);
    }

    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, EnumSet<BxDFType>> sample_f(Direction3 wo, Point2 sample)
    {
        double f = fresnelDielectric(cosTheta(wo), etaA, etaB);
        Direction3 wi;
        if (sample.get(0) < f)
        {
            // compute specular reflection
            wi = new Direction3(-wo.x(), -wo.y(), wo.z());
            EnumSet<BxDFType> sampledType = EnumSet.of(BxDFType.SPECULAR, BxDFType.REFLECTION);
            double pdf = f;
            return new Quadruple<>(r.times(f).divideEquals(absCosTheta(wi)), wi, pdf, sampledType);
        }
        else
        {
            // compute specular transmission
            boolean entering = cosTheta(wo) > 0;
            double etaI = entering ? etaA : etaB;
            double etaT = entering ? etaB : etaA;
            wi = refract(wo, Normal3.faceForward(new Normal3(0, 0, 1), wo), etaI / etaT);
            if (wi == null)
            {
                return new Quadruple<>(new RGBSpectrum(0), wi, 0.0, type);
            }
            RGBSpectrum ft = t.times(1 - f);
            // account for non-symmetry with transmission to different medium
            if (mode == TransportMode.RADIANCE)
            {
                ft.timesEquals((etaI * etaI) / (etaT * etaT));
            }
            
            EnumSet<BxDFType> sampledType = EnumSet.of(BxDFType.SPECULAR, BxDFType.TRANSMISSION);
            double pdf = 1 - f;
            return new Quadruple<>(ft.divideEquals(absCosTheta(wi)), wi, pdf, sampledType);
        }
    }

}
