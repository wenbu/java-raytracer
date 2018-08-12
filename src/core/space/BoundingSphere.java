package core.space;

import core.math.Point3;

public class BoundingSphere
{
    private final Point3 center;
    private final double radius;
    
    public BoundingSphere(Point3 center, double radius)
    {
        this.center = center;
        this.radius = radius;
    }
    
    public Point3 getCenter()
    {
        return center;
    }
    
    public double getRadius()
    {
        return radius;
    }
}
