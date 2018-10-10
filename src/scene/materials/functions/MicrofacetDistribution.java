package scene.materials.functions;

import core.math.Direction3;

public interface MicrofacetDistribution
{
    /**
     * Return the differential area of microfacets oriented with the given
     * normal vector <code>wh</code>.
     * <br/>
     * This function is also called <code>D</code>.
     * @param wh normal vector
     * @return differential area of microfacets oriented with <code>wh</code>.
     */
    double getDifferentialArea(Direction3 wh);
    
    /**
     * Return the invisible masked (backfacing) microfacet area per
     * visible microfacet area.
     * <br/>
     * This function is also called <code>lambda</code>.
     * @param w normal vector
     * @return masked microfacet area per visible microfacet area
     */
    double getMaskedArea(Direction3 w);
    
    /**
     * Return the fraction of microfacets visible from the given
     * direction.
     * <br/>
     * This function is also called <code>G1</code>.
     * @param w viewing direction
     * @return fraction of microfacets visible from w.
     */
    default double getVisibleMicrofacetFraction(Direction3 w)
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
    default double getVisibleMicrofacetFraction(Direction3 wo, Direction3 wi)
    {
        return 1 / (1 + getMaskedArea(wo) + getMaskedArea(wi));
    }
}
