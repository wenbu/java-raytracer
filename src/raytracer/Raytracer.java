package raytracer;

import core.Ray;
import core.colors.Color;

public interface Raytracer
{
    public Color traceRay(Ray ray);
    public boolean isOccluded(Ray ray);
}
