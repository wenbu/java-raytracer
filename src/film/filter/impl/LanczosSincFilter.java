package film.filter.impl;

import core.math.Direction2;
import core.math.Point2;
import film.filter.Filter;
import utilities.MathUtilities;

public class LanczosSincFilter extends Filter
{
    private final double tau;
    
    public LanczosSincFilter(Direction2 radius, double tau)
    {
        super(radius);
        this.tau = tau;
    }

    @Override
    public double evaluate(Point2 p)
    {
        return windowedSinc(p.x(), radius.x()) * windowedSinc(p.y(), radius.y());
    }
    
    private double windowedSinc(double x, double radius)
    {
        x = Math.abs(x);
        if (x > radius)
        {
            return 0;
        }
        return MathUtilities.lanczos(x, tau);
    }
}
