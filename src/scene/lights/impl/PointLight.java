package scene.lights.impl;

import java.util.HashSet;
import java.util.Set;

import core.Ray;
import core.colors.Color;
import core.math.Point3;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;

public class PointLight extends Light
{
    private final Point3 position;
    
    public PointLight(Point3 position, Color color)
    {
        super(color);
        this.position = position;
    }
    
    public PointLight(Point3 position, Color ambientColor, Color diffuseColor, Color specularColor)
    {
        super(ambientColor, diffuseColor, specularColor);
        this.position = position;
    }
    
    @Override
    public Set<Ray> getLightRay(SurfaceInteraction intersection)
    {
        Set<Ray> lightRays = new HashSet<>();
        Point3 origin = intersection.getP();
        lightRays.add(new Ray(origin, position));
        return lightRays;
    }
}
