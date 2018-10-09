package scene.materials.functions;

import java.util.EnumSet;
import java.util.Set;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point2;
import core.tuple.Pair;
import core.tuple.Quadruple;
import core.tuple.Triple;

public class ScaledBidirectionalDistributionFunction extends AbstractBidirectionalDistributionFunction
{
    private final RGBSpectrum scale;
    private final AbstractBidirectionalDistributionFunction bxdf;

    public ScaledBidirectionalDistributionFunction(AbstractBidirectionalDistributionFunction bxdf, RGBSpectrum scale)
    {
        super(bxdf.type);
        this.bxdf = bxdf;
        this.scale = scale;
    }

    @Override
    public RGBSpectrum f(Direction3 wo, Direction3 wi)
    {
        return bxdf.f(wo, wi).times(scale);
    }

    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, EnumSet<BxDFType>> sample_f(Direction3 wo, Point2 sample)
    {
        var unscaled = bxdf.sample_f(wo, sample);
        return new Quadruple<>(unscaled.getFirst().times(scale),
                               unscaled.getSecond(),
                               unscaled.getThird(),
                               unscaled.getFourth());
    }

    @Override
    public RGBSpectrum rho(Direction3 wo, Point2[] samples)
    {
        RGBSpectrum unscaled = bxdf.rho(wo, samples);
        return unscaled.times(scale);
    }

    @Override
    public RGBSpectrum rho(Point2[] u1, Point2[] u2)
    {
        RGBSpectrum unscaled = bxdf.rho(u1, u2);
        return unscaled.times(scale);
    }

}
