package core.space;

import core.math.Point;

public class BoundingSphere
{
    private final Point center;
    private final double radius;
    
    public BoundingSphere(Point center, double radius)
    {
        this.center = center;
        this.radius = radius;
    }
    
    public Point getCenter()
    {
        return center;
    }
    
    public double getRadius()
    {
        return radius;
    }
}
