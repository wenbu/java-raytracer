package raytracer.impl;

import java.util.Set;

import raytracer.Raytracer;
import scene.lights.Light;
import scene.materials.Material;
import scene.primitives.Primitive;
import core.Intersection;
import core.Ray;
import core.colors.Color;
import core.colors.Colors;

public class SimpleRaytracer implements Raytracer
{
    private Set<Primitive> geometry;
    private Set<Light> lights;
    
    // TODO: make configurable
    private static final int MAX_DEPTH = 4;

    public SimpleRaytracer(Set<Primitive> geometry, Set<Light> lights)
    {
        this.geometry = geometry;
        this.lights = lights;
    }

    @Override
    public Color traceRay(Ray ray)
    {
        return traceRay(ray, 0);
    }
    
    @Override
    public Color traceRay(Ray ray, int depth)
    {
        if (depth >= MAX_DEPTH)
            return Colors.BLACK;
        
        Intersection closestIntersection = getClosestIntersection(ray);

        Color color = Colors.BLACK;
        if (closestIntersection != null)
        {
            Material material = closestIntersection.getMaterial();

            color = material.getColor(lights, closestIntersection, ray, this, depth);
        }
        return color;
    }

    private Intersection getClosestIntersection(Ray ray)
    {
        return getClosestIntersection(ray, 0);
    }

    private Intersection getClosestIntersection(Ray ray, double minT)
    {
        Intersection closestIntersection = null;
        double closestDistance = Double.MAX_VALUE;

        for (Primitive geo : geometry)
        {
            Intersection intersection = geo.getIntersection(ray);

            if (intersection == null)
            {
                continue;
            }

            double distance = intersection.getDistance();
            if (distance < minT)
                continue;
            if (distance < closestDistance)
            {
                closestIntersection = intersection;
                closestDistance = distance;
            }
        }
        return closestIntersection;
    }

    public boolean isOccluded(Ray ray)
    {
    	for (Primitive geo : geometry)
    	{
    		Intersection intersection = geo.getIntersection(ray);
    		
    		if (intersection == null)
    		{
    			continue;
    		}
    		
    		double distance = intersection.getDistance();
    		
    		if (distance < 1.0e-5 || distance > ray.getMaxT())
    			continue;
    		return true;
    	}
    	return false;
    }
}
