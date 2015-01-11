package core;

import core.math.Point;

public class Sample
{
    private final Point worldPosition;
    
    public Sample(Point worldPosition)
    {
        this.worldPosition = worldPosition;
    }
    
    public Point getWorldPosition()
    {
        return worldPosition;
    }
}
