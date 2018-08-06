package scene.shapes;

import core.math.Transformation;
import core.space.BoundingBox;

public abstract class Shape
{
    protected final Transformation objectToWorld;
    protected final boolean reverseOrientation;
    
    public Shape(Transformation objectToWorld, boolean reverseOrientation)
    {
        this.objectToWorld = objectToWorld;
        this.reverseOrientation = reverseOrientation;
    }
    
    /**
     * @return BoundingBox in this Shape's object space.
     */
    public abstract BoundingBox objectBound();
    
    /**
     * @return BoundingBox in world space.
     */
    public BoundingBox worldBound()
    {
        return objectToWorld.transform(objectBound());
    }
}
