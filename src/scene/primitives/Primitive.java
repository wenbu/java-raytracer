package scene.primitives;

import core.Ray;
import core.space.BoundingBox;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;
import scene.materials.TransportMode;

public interface Primitive
{
    BoundingBox worldBound();
    SurfaceInteraction intersect(Ray ray);
    boolean intersectP(Ray ray);
    // AreaLight getAreaLight();
    Material getMaterial();
    void computeScatteringFunctions(SurfaceInteraction surfaceInteraction,
                                    TransportMode mode,
                                    boolean allowMultipleLobes);
}
