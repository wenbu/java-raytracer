package core;

import core.math.Point3;

public class Sample
{
    private final Point3 worldPosition;
    
    public Sample(Point3 worldPosition)
    {
        this.worldPosition = worldPosition;
    }
    
    public Point3 getWorldPosition()
    {
        return worldPosition;
    }
}
