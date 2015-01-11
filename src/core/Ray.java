package core;

import core.math.Direction;
import core.math.Point;

public class Ray
{
    private final Point origin;
    private final Direction direction;
    
    // Maximum t value that is relevant for this Ray.
    private final double maxT;

    public Ray(Point origin, Direction direction)
    {
        this(origin, direction, Double.MAX_VALUE);
    }

    public Ray(Point origin, Point destination)
    {
        this(origin, destination, Double.MAX_VALUE);
    }
    
    public Ray(Point origin, Direction direction, double maxT)
    {
    	this.origin = origin;
    	this.direction = direction;
    	this.maxT = maxT;
    }
    
    public Ray(Point origin, Point destination, double maxT)
    {
    	this.origin = origin;
        this.direction = Direction.getNormalizedDirection(origin, destination);
        this.maxT = maxT;
    }

    public Point getOrigin()
    {
        return origin;
    }

    public Direction getDirection()
    {
        return direction;
    }
    
    public double getMaxT()
    {
    	return maxT;
    }
}
