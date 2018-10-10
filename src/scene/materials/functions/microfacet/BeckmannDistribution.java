package scene.materials.functions.microfacet;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.*;

import core.math.Direction3;
import scene.materials.functions.MicrofacetDistribution;

public class BeckmannDistribution implements MicrofacetDistribution
{
    private final double alphax;
    private final double alphay;
    
    public BeckmannDistribution(double alphax, double alphay)
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
        return Math.exp(-tan2Theta *
                        (cos2Phi(wh) / (alphax * alphax) + sin2Phi(wh) / (alphay * alphay))) /
               (Math.PI * alphax * alphay * cos4Theta);
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
        double a = 1 / (alpha * absTanTheta);
        if (a >= 1.6f)
        {
            return 0;
        }
        return (1 - 1.259f * a + 0.396f * a * a) / (3.535f * a + 2.181f * a * a);
    }

}
