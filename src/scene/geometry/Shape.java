package scene.geometry;

import core.Ray;
import core.math.Transformation;
import core.space.BoundingBox3;
import core.tuple.Pair;
import scene.interactions.impl.SurfaceInteraction;

public abstract class Shape
{
    protected final Transformation objectToWorld;
    protected final Transformation worldToObject;
    protected final boolean reverseOrientation;
    protected final boolean swapsHandedness;
    
    private static int shapeCounter = 0;
    private final String debugName;
    
    public Shape(Transformation objectToWorld, Transformation worldToObject, boolean reverseOrientation)
    {
        this.objectToWorld = objectToWorld;
        this.worldToObject = worldToObject;
        this.reverseOrientation = reverseOrientation;
        this.swapsHandedness = objectToWorld.swapsHandedness();
        this.debugName = this.getClass().getSimpleName() + shapeCounter++;
    }
    
    public boolean isOrientationReversed()
    {
        return reverseOrientation;
    }
    
    public boolean isHandednessSwapped()
    {
        return swapsHandedness;
    }
    
    /**
     * @return BoundingBox3 in this Shape's object space.
     */
    public abstract BoundingBox3 objectBound();
    
    /**
     * @return BoundingBox3 in world space.
     */
    public BoundingBox3 worldBound()
    {
        return objectToWorld.transform(objectBound());
    }
    
    public Pair<Double, SurfaceInteraction> intersect(Ray ray)
    {
        return intersect(ray, true);
    }
    
    public abstract Pair<Double, SurfaceInteraction> intersect(Ray ray, boolean testAlpha);
    
    public boolean intersectP(Ray ray)
    {
        return intersectP(ray, true);
    }
    
    /*
     * Shape implementations should override this as there are often far more efficient
     * ways of computing this.
     */
    public boolean intersectP(Ray ray, boolean testAlpha)
    {
        return intersect(ray, testAlpha) != null;
    }
    
    public abstract double surfaceArea();
    
    @Override
    public String toString()
    {
        return debugName;
    }
}
