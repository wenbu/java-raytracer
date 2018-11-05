package scene.geometry;

import core.Ray;
import core.math.Direction3;
import core.math.Point2;
import core.math.Transformation;
import core.space.BoundingBox3;
import core.tuple.Pair;
import scene.interactions.Interaction;
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
    
    /**
     * @param u sample
     * @return an {@link Interaction} specifying the sampled point. The position
     *         <code>p</code> and normal <code>n</code> should be set, as well as
     *         the rounding error <code>pError</code>.
     */
    public abstract Interaction sample(Point2 u);
    
    /**
     * An alternative sample method, which allows an {@link Interaction} specifying
     * the point from which the shape surface is being integrated over to be passed
     * in. This allows shape implementations to sample only the portion of the shape
     * that is potentially visible from that point.
     * 
     * @param interaction specifying the point from which the shape surface is being
     *                    integrated over
     * @param u sample
     * @return an {@link Interaction} specifying the sampled point. The position
     *         <code>p</code> and normal <code>n</code> should be set, as well as
     *         the rounding error <code>pError</code>.
     */
    public Interaction sample(Interaction interaction, Point2 u)
    {
        return sample(u);
    }
    
    /**
     * A default implementation for pdf, assuming uniform sampling over the surface.
     * @param interaction
     * @return
     */
    public double pdf(Interaction interaction)
    {
        return 1 / surfaceArea();
    }
    
    /**
     * PDF with respect to solid angle from a reference point.
     * @param ref
     * @param wi
     * @return
     */
    public double pdf(Interaction ref, Direction3 wi)
    {
        // intersect sample ray with area light geometry
        Ray ray = ref.spawnRay(wi);
        var intersection = intersect(ray, false);
        if (intersection == null)
        {
            return 0;
        }
        SurfaceInteraction isectLight = intersection.getSecond();
        
        // convert light sample weight to solid angle measure
        double pdf = ref.getP().distanceSquared(isectLight.getP()) /
                     (isectLight.getN().absDot(wi.times(-1)) * surfaceArea());
        return pdf;
    }
}
