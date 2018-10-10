package scene.materials.functions.microfacet;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.*;
import core.math.Direction3;
import scene.materials.functions.MicrofacetDistribution;

public class TrowbridgeReitzDistribution implements MicrofacetDistribution
{
    private final double alphax;
    private final double alphay;

    public TrowbridgeReitzDistribution(double alphax, double alphay)
    {
        this.alphax = alphax;
        this.alphay = alphay;
    }

    @Override
    public double getDifferentialArea(Direction3 wh)
    {
        double tan2Theta = tan2Theta(wh);
        if (Double.isInfinite(tan2Theta))
        {
            return 0;
        }

        double cos4Theta = cos2Theta(wh) * cos2Theta(wh);
        double e = (cos2Phi(wh) / (alphax * alphax) + sin2Phi(wh) / (alphay * alphay)) * tan2Theta;
        return 1 / (Math.PI * alphax * alphay * cos4Theta * (1 + e) * (1 + e));
    }

    @Override
    public double getMaskedArea(Direction3 w)
    {
        double absTanTheta = Math.abs(tanTheta(w));
        if (Double.isInfinite(absTanTheta))
        {
            return 0;
        }
        
        double alpha = Math.sqrt(cos2Phi(w) * alphax * alphax + sin2Phi(w) * alphay * alphay);
        double alpha2Tan2Theta = (alpha * absTanTheta) * (alpha * absTanTheta);
        return (-1 + Math.sqrt(1.f + alpha2Tan2Theta)) / 2;
    }

}
