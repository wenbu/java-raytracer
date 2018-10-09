package film.filter.impl;

import core.math.Direction2;
import core.math.Point2;
import film.filter.Filter;

public class BoxFilter extends Filter
{
    public BoxFilter(Direction2 radius)
    {
        super(radius);
    }
    
    @Override
    public double evaluate(Point2 p)
    {
        return 1;
    }

}
