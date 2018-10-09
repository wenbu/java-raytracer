package scene.materials.functions;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.absCosTheta;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.cosTheta;

import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point2;
import core.tuple.Quadruple;

public class SpecularReflection extends AbstractBidirectionalDistributionFunction
{
    private final RGBSpectrum r;
    private final Fresnel fresnel;
    
    public SpecularReflection(RGBSpectrum r, Fresnel fresnel)
    {
        super(EnumSet.of(BxDFType.REFLECTION, BxDFType.SPECULAR));
        this.r = r;
        this.fresnel = fresnel;
    }
    
    @Override
    public RGBSpectrum f(Direction3 wo, Direction3 wi)
    {
        return new RGBSpectrum(0);
    }

    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, EnumSet<BxDFType>> sample_f(Direction3 wo, Point2 sample)
    {
        Direction3 wi = new Direction3(-wo.x(), -wo.y(), wo.z());
        double pdf = 1;
        RGBSpectrum reflectedColor = r.times(fresnel.evaluate(cosTheta(wi))).divideBy(absCosTheta(wi));
        return new Quadruple<>(reflectedColor, wi, pdf, type);
    }
}
