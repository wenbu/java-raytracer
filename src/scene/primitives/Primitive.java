package scene.primitives;

import core.Ray;
import core.space.BoundingBox;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;

public interface Primitive
{
    BoundingBox worldBound();
    SurfaceInteraction intersect(Ray ray);
    boolean intersectP(Ray ray);
    // AreaLight getAreaLight();
    // TODO: use pbrt Material when done
    Material getMaterial();
    // void computeScatteringFunctions(SurfaceInteraction surfaceInteraction,
    //                                 MemoryArena arena,
    //                                 TransportMode mode,
    //                                 boolean allowMultipleLobes);
}
