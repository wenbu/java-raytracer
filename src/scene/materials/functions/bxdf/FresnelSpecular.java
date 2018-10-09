package scene.materials.functions.bxdf;

import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
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
        // TODO Auto-generated method stub
        return null;
    }

}
