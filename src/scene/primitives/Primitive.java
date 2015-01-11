package scene.primitives;

import core.Intersection;
import core.Ray;

public interface Primitive
{
    public Intersection getIntersection(Ray ray);
}
