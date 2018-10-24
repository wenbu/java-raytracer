package film.filter.impl;

import core.math.Direction2;
import core.math.Point2;
import film.filter.Filter;

public class GaussianFilter extends Filter
{
    private final double alpha;
    private final double expX;
    private final double expY;
    
    public GaussianFilter(Direction2 radius, double alpha)
    {
        super(radius);
        this.alpha = alpha;
        expX = Math.exp(-alpha * radius.x() * radius.x());
        expY = Math.exp(-alpha * radius.y() * radius.y());
    }
    @Override
    public double evaluate(Point2 p)
    {
        return gaussian(p.x(), expX) * gaussian(p.y(), expY);
    }

    private double gaussian(double d, double expv)
    {
        return Math.max(0, Math.exp(-alpha * d * d) - expv);
    }
}
