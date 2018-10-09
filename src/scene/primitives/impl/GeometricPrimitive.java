package scene.primitives.impl;

import core.Ray;
import core.space.BoundingBox;
import scene.geometry.Shape;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;
import scene.materials.TransportMode;
import scene.medium.Medium.MediumInterface;
import scene.primitives.Primitive;

public class GeometricPrimitive implements Primitive
{
    private final Shape shape;
    private final Material material;
    // private final AreaLight areaLight;
    private final MediumInterface mediumInterface;
    
    public GeometricPrimitive(Shape shape, Material material, MediumInterface mediumInterface)
    {
        this.shape = shape;
        this.material = material;
        this.mediumInterface = mediumInterface;
    }

    @Override
    public BoundingBox worldBound()
    {
        return shape.worldBound();
    }

    @Override
    public SurfaceInteraction intersect(Ray ray)
    {
        var intersection = shape.intersect(ray);
        if (intersection == null)
        {
            return null;
        }
        
        double tHit = intersection.getFirst();
        SurfaceInteraction surfaceInteraction = intersection.getSecond();
        
        ray.setTMax(tHit);
        surfaceInteraction.setPrimitive(this);
        if (mediumInterface.isMediumTransition())
        {
            surfaceInteraction.setMediumInterface(mediumInterface);
        }
        else
        {
            surfaceInteraction.setMediumInterface(new MediumInterface(ray.getMedium()));
        }
        return surfaceInteraction;
    }
    
    @Override
    public void computeScatteringFunctions(SurfaceInteraction isect, TransportMode mode, boolean allowMultipleLobes)
    {
        if (material != null)
        {
            material.computeScatteringFunctions(isect, mode, allowMultipleLobes);   
        }
    }

    @Override
    public boolean intersectP(Ray ray)
    {
        return shape.intersectP(ray);
    }

    @Override
    public Material getMaterial()
    {
        return material;
    }

}
