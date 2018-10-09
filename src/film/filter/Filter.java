package film.filter;

import core.math.Direction2;
import core.math.Point2;

public abstract class Filter
{
    protected final Direction2 radius;
    protected final Direction2 invRadius;
    
    public Filter(Direction2 radius)
    {
        this.radius = radius;
        this.invRadius = new Direction2(1/radius.x(), 1/radius.y());
    }
    
    public Direction2 getRadius()
    {
        return radius;
    }
    
    public abstract double evaluate(Point2 p);
}
