package scene.primitives;

import scene.geometry.Shape;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.AreaLight;
import scene.materials.Material;
import scene.materials.TransportMode;

public interface Aggregate extends Primitive
{
    @Override
    default Material getMaterial()
    {
        throw new UnsupportedOperationException("No material for Aggregate.");
    }
    
    @Override
    default Shape getShape()
    {
        throw new UnsupportedOperationException("No shape for Aggregate.");
    }

    @Override
    default void computeScatteringFunctions(SurfaceInteraction surfaceInteraction,
            TransportMode mode, boolean allowMultipleLobes)
    {
        throw new UnsupportedOperationException("No scattering functions for Aggregate.");
    }
    
    @Override
    default AreaLight getAreaLight()
    {
        throw new UnsupportedOperationException("No area light for Aggregate.");
    }
}
