package scene.primitives;

import core.Ray;
import core.space.BoundingBox3;
import scene.geometry.Shape;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;
import scene.materials.TransportMode;

public interface Primitive
{
    BoundingBox3 worldBound();
    SurfaceInteraction intersect(Ray ray);
    boolean intersectP(Ray ray);
    // AreaLight getAreaLight();
    Material getMaterial();
    Shape getShape();
    void computeScatteringFunctions(SurfaceInteraction surfaceInteraction,
                                    TransportMode mode,
                                    boolean allowMultipleLobes);
}
