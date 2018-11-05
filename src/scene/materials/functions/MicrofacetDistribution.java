package scene.materials.functions;

import static utilities.MathUtilities.*;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.*;
import core.math.Direction3;
import core.math.Point2;

public abstract class MicrofacetDistribution
{
    protected final boolean sampleVisibleArea;
    
    protected MicrofacetDistribution(boolean sampleVisibleArea)
    {
        this.sampleVisibleArea = sampleVisibleArea;
    }
    /**
     * Return the differential area of microfacets oriented with the given
     * normal vector <code>wh</code>.
     * <br/>
     * This function is also called <code>D</code>.
     * @param wh normal vector
     * @return differential area of microfacets oriented with <code>wh</code>.
     */
    public abstract double getDifferentialArea(Direction3 wh);
    
    /**
     * Return the invisible masked (backfacing) microfacet area per
     * visible microfacet area.
     * <br/>
     * This function is also called <code>lambda</code>.
     * @param w normal vector
     * @return masked microfacet area per visible microfacet area
     */
    public abstract double getMaskedArea(Direction3 w);
    
    /**
     * Return the fraction of microfacets visible from the given
     * direction.
     * <br/>
     * This function is also called <code>G1</code>.
     * @param w viewing direction
     * @return fraction of microfacets visible from w.
     */
    public double getVisibleMicrofacetFraction(Direction3 w)
    {
        return 1 / (1 + getMaskedArea(w));
    }
    
    /**
     * Return the (approximate) fraction of microfacets visible from both given
     * directions.
     * <br/>
     * This function is also called <code>G</code>;
     * @param wo given direction 1
     * @param wi given direction 2
     * @return fraction of microfacets visible from both wo and wi.
     */
    public double getVisibleMicrofacetFraction(Direction3 wo, Direction3 wi)
    {
        return 1 / (1 + getMaskedArea(wo) + getMaskedArea(wi));
    }
    
    /**
     * Sample the normal vector distribution.
     * @param wo outgoing direction
     * @param u sample
     * @return
     */
    public abstract Direction3 sampleNormalDistribution(Direction3 wo, Point2 u);
    
    public double pdf(Direction3 wo, Direction3 wh)
    {
        if (sampleVisibleArea)
        {
            return getDifferentialArea(wh) * getVisibleMicrofacetFraction(wo) * wo.absDot(wh) /
                   absCosTheta(wo);
        }
        else
        {
            return getDifferentialArea(wh) * absCosTheta(wh);
        }
    }
}
