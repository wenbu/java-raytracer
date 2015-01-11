package camera;

import core.Ray;
import core.Sample;
import core.math.Point;

public class Camera
{
    private final Point position;
    
    public Camera(Point position)
    {
        this.position = position;
    }
    
    public Ray getRay(Sample sample)
    {
        Point samplePosition = sample.getWorldPosition();
        
        return new Ray(position, samplePosition);
    }
}
