package core;

import core.math.Direction3;
import core.math.Point3;
import scene.medium.Medium;

public class RayDifferential extends Ray
{
    private boolean hasDifferentials = false;
    private Point3 rxOrigin = new Point3();
    private Point3 ryOrigin = new Point3();
    private Direction3 rxDirection = new Direction3();
    private Direction3 ryDirection = new Direction3();
    
    public RayDifferential()
    {
        super();
    }
    
    public RayDifferential(Point3 origin, Direction3 direction)
    {
        this(origin, direction, Double.MAX_VALUE, 0, null);
    }
    
    public RayDifferential(Point3 origin, Direction3 direction, double tMax, double time, Medium medium)
    {
        super(origin, direction, tMax, time, medium);
    }
    
    public RayDifferential(Ray ray)
    {
        super(ray.getOrigin(), ray.getDirection(), ray.getTMax(), ray.getTime(), ray.getMedium());
    }
    
    public void scaleDifferentials(double s)
    {
        rxOrigin = origin.plus(rxOrigin.minus(origin).times(s));
        ryOrigin = origin.plus(ryOrigin.minus(origin).times(s));
        rxDirection = direction.plus(rxDirection.minus(direction).times(s));
        ryDirection = direction.plus(ryDirection.minus(direction).times(s));
    }

    public boolean hasDifferentials()
    {
        return hasDifferentials;
    }

    public void setHasDifferentials(boolean hasDifferentials)
    {
        this.hasDifferentials = hasDifferentials;
    }

    public Point3 getRxOrigin()
    {
        return rxOrigin;
    }

    public void setRxOrigin(Point3 rxOrigin)
    {
        this.rxOrigin = rxOrigin;
    }

    public Point3 getRyOrigin()
    {
        return ryOrigin;
    }

    public void setRyOrigin(Point3 ryOrigin)
    {
        this.ryOrigin = ryOrigin;
    }

    public Direction3 getRxDirection()
    {
        return rxDirection;
    }

    public void setRxDirection(Direction3 rxDirection)
    {
        this.rxDirection = rxDirection;
    }

    public Direction3 getRyDirection()
    {
        return ryDirection;
    }

    public void setRyDirection(Direction3 ryDirection)
    {
        this.ryDirection = ryDirection;
    }
}
