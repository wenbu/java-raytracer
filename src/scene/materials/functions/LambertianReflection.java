package scene.materials.functions;

import java.util.EnumSet;
import java.util.Set;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point2;
import core.tuple.Pair;
import core.tuple.Quadruple;
import core.tuple.Triple;

public class LambertianReflection extends AbstractBidirectionalDistributionFunction
{
    private RGBSpectrum r;
    
    public LambertianReflection(RGBSpectrum r)
    {
        super(EnumSet.of(BxDFType.REFLECTION, BxDFType.DIFFUSE));
        this.r = r;
    }

    @Override
    public RGBSpectrum f(Direction3 wo, Direction3 wi)
    {
        return r.divideBy(Math.PI);
    }

    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, EnumSet<BxDFType>> sample_f(Direction3 wo, Point2 sample)
    {
        // TODO Auto-generated method stub
        return null;
    }

 // hemispherical-directional reflectance
    // total reflection in a given direction due to constant illumination over the hemisphere
    public Pair<RGBSpectrum, Set<Point2>> rho(Direction3 wo, int nSamples)
    {
        return new Pair<>(r, null);
    }
    // hemispherical-hemispherical reflectance
    // fraction of incident light reflected when incident light is same from all directions
    public Triple<RGBSpectrum, Set<Point2>, Set<Point2>> rho(int nSamples)
    {
        return new Triple<>(r, null, null);
    }
}
