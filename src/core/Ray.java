package core;

import core.math.Direction3;
import core.math.Point3;

public class Ray
{
    // configurable?
    private static final double EPSILON = 1e-5;

    private final Point3 origin;
    private final Direction3 direction;

    // Minimum t value that is relevant for this Ray.
    private final double minT;

    // Maximum t value that is relevant for this Ray.
    private final double maxT;

    private final int depth;

    public Ray(Point3 origin, Direction3 direction)
    {
        this(origin, direction, EPSILON, Double.MAX_VALUE, 0);
    }

    public Ray(Point3 origin, Point3 destination)
    {
        this(origin, destination, EPSILON, Double.MAX_VALUE, 0);
    }

    public Ray(Point3 origin,
               Direction3 direction,
               Ray parent,
               double minT,
               double maxT)
    {
        this(origin, direction, minT, maxT, parent.getDepth() + 1);
    }

    public Ray(Point3 origin,
               Direction3 direction,
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

    public Ray(Point3 origin,
               Point3 destination,
               double minT,
               double maxT,
               int depth)
    {
        this.origin = origin;
        this.direction = Direction3.getNormalizedDirection(origin, destination);
        this.minT = minT;
        this.maxT = maxT;
        this.depth = depth;
    }

    public Point3 getOrigin()
    {
        return origin;
    }

    public Direction3 getDirection()
    {
        return direction;
    }
    
    /**
     * @param t
     * @return the Point at origin + t * direction
     */
    public Point3 pointAt(double t)
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
