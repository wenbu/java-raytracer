package scene.primitives;

import core.Intersection;
import core.Ray;

@Deprecated
public interface Primitive
{
    public Intersection getIntersection(Ray ray);
}
