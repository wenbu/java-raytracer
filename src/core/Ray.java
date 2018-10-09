package core;

import core.math.Direction3;
import core.math.Point3;
import scene.medium.Medium;

public class Ray
{
    protected final Point3 origin;
    protected final Direction3 direction;

    private double tMax;
    private double time;
    private Medium medium;

    public Ray()
    {
        this(new Point3(), new Direction3(), Double.MAX_VALUE, 0, null);
    }
    
    public Ray(Point3 origin, Direction3 direction)
    {
        this(origin, direction, Double.MAX_VALUE, 0, null);
    }

    public Ray(Point3 origin,
               Direction3 direction,
               double tMax,
               double time,
               Medium medium)
    {
        this.origin = origin;
        this.direction = direction;
        
        this.tMax = tMax;
        this.time = time;
        
        this.medium = medium;
    }

    public Ray(Ray other)
    {
        this.origin = other.origin;
        this.direction = other.direction;
        this.tMax = other.tMax;
        this.time = other.time;
        this.medium = other.medium;
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
    
    public void setTime(double time)
    {
        this.time = time;
    }
    
    public double getTime()
    {
        return time;
    }
    
    public void setTMax(double tMax)
    {
        this.tMax = tMax;
    }
    
    public double getTMax()
    {
        return tMax;
    }
    
    public void setMedium(Medium medium)
    {
        this.medium = medium;
    }
    
    public Medium getMedium()
    {
        return medium;
    }
    
    @Override
    public String toString()
    {
        return "Ray [o=" + origin + ", d=" + direction + "]";
    }
}
