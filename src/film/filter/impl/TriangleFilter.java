package film.filter.impl;

import core.math.Direction2;
import core.math.Point2;
import film.filter.Filter;

public class TriangleFilter extends Filter
{
    public TriangleFilter(Direction2 radius)
    {
        super(radius);
    }
    
    @Override
    public double evaluate(Point2 p)
    {
        return Math.max(0, radius.x() - Math.abs(p.x())) *
               Math.max(0, radius.y() - Math.abs(p.y()));
    }
}
