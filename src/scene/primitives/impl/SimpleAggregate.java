package scene.primitives.impl;

import java.util.Set;

import core.Ray;
import core.space.BoundingBox3;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;
import scene.materials.TransportMode;
import scene.primitives.Primitive;

public class SimpleAggregate implements Primitive
{
    private final Set<Primitive> primitives;
    private BoundingBox3 worldBound;

    public SimpleAggregate(Set<Primitive> primitives)
    {
        this.primitives = primitives;
        worldBound = new BoundingBox3();
        for (Primitive primitive : primitives)
        {
            worldBound = worldBound.union(primitive.worldBound());
        }
    }

    @Override
    public BoundingBox3 worldBound()
    {
        return worldBound;
    }

    @Override
    public SurfaceInteraction intersect(Ray ray)
    {
        var boundsIntersection = worldBound.intersect(ray);
        if (boundsIntersection == null)
        {
            return null;
        }
        
        SurfaceInteraction closestInteraction = null;
        double lowestT = Double.POSITIVE_INFINITY;
        for (Primitive primitive : primitives)
        {
            SurfaceInteraction isect = primitive.intersect(ray);
            if (isect == null)
            {
                continue;
            }
            double t = ray.getTMax();
            if (t < lowestT)
            {
                lowestT = t;
                closestInteraction = isect;
            }
        }

        return closestInteraction;
    }

    @Override
    public boolean intersectP(Ray ray)
    {
        for (Primitive primitive : primitives)
        {
            if (primitive.intersectP(ray))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Material getMaterial()
    {
        throw new UnsupportedOperationException("SimpleAggregate.getMaterial");
    }

    @Override
    public void computeScatteringFunctions(SurfaceInteraction surfaceInteraction,
            TransportMode mode, boolean allowMultipleLobes)
    {
        throw new UnsupportedOperationException("SimpleAggregate.computeScatteringFunctions");
    }

}
