package core;

import core.math.Direction;
import core.math.Point;

public class Ray
{
    // configurable?
    private static final double EPSILON = 1e-5;

    private final Point origin;
    private final Direction direction;

    // Minimum t value that is relevant for this Ray.
    private final double minT;

    // Maximum t value that is relevant for this Ray.
    private final double maxT;

    private final int depth;

    public Ray(Point origin, Direction direction)
    {
        this(origin, direction, EPSILON, Double.MAX_VALUE, 0);
    }

    public Ray(Point origin, Point destination)
    {
        this(origin, destination, EPSILON, Double.MAX_VALUE, 0);
    }

    public Ray(Point origin,
               Direction direction,
               Ray parent,
               double minT,
               double maxT)
    {
        this(origin, direction, minT, maxT, parent.getDepth() + 1);
    }

    public Ray(Point origin,
               Direction direction,
               double minT,
               double maxT,
               int depth)
    {
        this.origin = origin;
        this.direction = direction;
        this.minT = minT;
        this.maxT = maxT;
        this.depth = depth;
    }

    public Ray(Point origin,
               Point destination,
               double minT,
               double maxT,
               int depth)
    {
        this.origin = origin;
        this.direction = Direction.getNormalizedDirection(origin, destination);
        this.minT = minT;
        this.maxT = maxT;
        this.depth = depth;
    }

    public Point getOrigin()
    {
        return origin;
    }

    public Direction getDirection()
    {
        return direction;
    }
    
    /**
     * @param t
     * @return the Point at origin + t * direction
     */
    public Point pointAt(double t)
    {
        return origin.plus(direction.times(t));
    }

    public double getMinT()
    {
        return minT;
    }

    public double getMaxT()
    {
        return maxT;
    }

    public int getDepth()
    {
        return depth;
    }
}
