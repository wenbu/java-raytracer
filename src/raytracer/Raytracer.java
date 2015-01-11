package raytracer;

import core.Ray;
import core.colors.Color;

public interface Raytracer
{
    public Color traceRay(Ray ray);
    public Color traceRay(Ray ray, int depth);
    public boolean isOccluded(Ray ray);
}
