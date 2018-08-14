package scene.materials;

import java.util.Set;

import raytracer.Raytracer;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;
import core.Intersection;
import core.Ray;
import core.colors.Color;

public interface Material
{
    public Color getColor(Set<Light> lights, SurfaceInteraction intersection,
            Ray cameraRay, Raytracer raytracer);
}
