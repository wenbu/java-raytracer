package scene.materials.impl;

import java.util.Set;

import core.Ray;
import core.colors.Color;
import raytracer.Raytracer;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;
import scene.materials.Material;

public class DebugMaterial implements Material
{
    private final Color color;
    
    public DebugMaterial(Color color)
    {
        this.color = color;
    }

    @Override
    public Color getColor(Set<Light> lights,
                          SurfaceInteraction intersection,
                          Ray cameraRay,
                          Raytracer raytracer)
    {
        return color;
    }

}
