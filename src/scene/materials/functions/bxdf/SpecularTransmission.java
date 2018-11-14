package scene.materials.functions.bxdf;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.absCosTheta;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.cosTheta;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.refract;

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

public class SpecularTransmission extends AbstractBidirectionalDistributionFunction
{
    // transmission scale factor
    private final RGBSpectrum t;
    // index of refraction above surface
    private final double etaA;
    // index of refraction below surface
    private final double etaB;
    
    private final Fresnel fresnel;
    
    private final TransportMode mode;
    
    public SpecularTransmission(RGBSpectrum t, double etaA, double etaB, TransportMode mode)
    {
        super(EnumSet.of(BxDFType.TRANSMISSION, BxDFType.SPECULAR));
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
        boolean entering = cosTheta(wo) > 0;
        double etaI = entering ? etaA : etaB;
        double etaT = entering ? etaB : etaA;
        
        Direction3 wi = refract(wo, Normal3.faceForward(new Normal3(0, 0, 1), wo), etaI / etaT);
        if (wi == null)
        {
            return new Quadruple<>(new RGBSpectrum(0), new Direction3(0, 0, 0), 0.0, EnumSet.noneOf(BxDFType.class));
        }
        
        double pdf = 1;
        RGBSpectrum ft = t.times(new RGBSpectrum(1).minus(fresnel.evaluate(cosTheta(wi))));
        
        // account for non-symmetry with transmission to different medium
        if (mode == TransportMode.RADIANCE)
        {
            ft = ft.times((etaI * etaI) / (etaT * etaT));
        }
        return new Quadruple<>(ft.divideBy(absCosTheta(wi)), wi, pdf, type);
    }

    @Override
    public double pdf(Direction3 wo, Direction3 wi)
    {
        return 0;
    }
}
