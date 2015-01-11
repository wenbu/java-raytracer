package scene.lights.impl;

import java.util.HashSet;
import java.util.Set;

import scene.lights.Light;
import core.Intersection;
import core.Ray;
import core.colors.Color;
import core.math.Point;

public class PointLight extends Light
{
    private final Point position;
    
    public PointLight(Point position, Color color)
    {
        super(color);
        this.position = position;
    }
    
    public PointLight(Point position, Color ambientColor, Color diffuseColor, Color specularColor)
    {
        super(ambientColor, diffuseColor, specularColor);
        this.position = position;
    }
    
    @Override
    public Set<Ray> getLightRay(Intersection intersection)
    {
        Set<Ray> lightRays = new HashSet<>();
        Point origin = intersection.getPosition();
        lightRays.add(new Ray(origin, position));
        return lightRays;
    }
}
