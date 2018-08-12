package camera;

import core.Ray;
import core.Sample;
import core.math.Point3;

public class Camera
{
    private final Point3 position;
    
    public Camera(Point3 position)
    {
        this.position = position;
    }
    
    public Ray getRay(Sample sample)
    {
        Point3 samplePosition = sample.getWorldPosition();
        
        return new Ray(position, samplePosition);
    }
}
