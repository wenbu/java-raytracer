package raytracer.impl;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import metrics.MetricsAware;
import raytracer.Raytracer;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;
import scene.materials.Material;
import scene.primitives.Primitive;
import core.Intersection;
import core.Ray;
import core.colors.Color;
import core.colors.Colors;

public class SimpleRaytracer implements Raytracer, MetricsAware
{
    private static final Logger logger = Logger.getLogger(SimpleRaytracer.class.getName());
    
    private final AtomicInteger numRaysTraced = new AtomicInteger(0);
    private final AtomicInteger numOcclusionTests = new AtomicInteger(0);
    private final AtomicLong timeSpentTracingRays = new AtomicLong(0);
    private final AtomicLong timeSpentOnOcclusionTests = new AtomicLong(0);
    
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
        long start = System.currentTimeMillis();
        try
        {
            if (ray.getDepth() >= MAX_DEPTH)
            {
                return Colors.BLACK;
            }
    
            SurfaceInteraction closestIntersection = getClosestIntersection(ray);
    
            Color color = Colors.BLACK;
            if (closestIntersection != null)
            {
                Material material = closestIntersection.getPrimitive().getMaterial();
    
                color = material.getColor(lights, closestIntersection, ray, this);
            }
            return color;
        }
        finally
        {
            long timeSpent = System.currentTimeMillis() - start;
            numRaysTraced.incrementAndGet();
            timeSpentTracingRays.addAndGet(timeSpent);
        }
    }

    private SurfaceInteraction getClosestIntersection(Ray ray)
    {
        return getClosestIntersection(ray, 0);
    }

    private SurfaceInteraction getClosestIntersection(Ray ray, double minT)
    {
        SurfaceInteraction closestIntersection = null;
        double closestDistance = Double.MAX_VALUE;

        for (Primitive geo : geometry)
        {
            SurfaceInteraction intersection = geo.intersect(ray);
            
            if (intersection == null)
            {
                continue;
            }

            double distance = intersection.getT();
            if (distance < minT)
                continue;
            if (distance < closestDistance)
            {
                closestIntersection = intersection;
                closestDistance = distance;
            }
        };
        
        return closestIntersection;
    }

    public boolean isOccluded(Ray ray)
    {
        long start = System.currentTimeMillis();
        
        try
        {
            for (Primitive geo : geometry)
            {
                SurfaceInteraction intersection = geo.intersect(ray);
    
                if (intersection == null)
                {
                    continue;
                }
    
                double distance = intersection.getT();
    
                if (distance < ray.getMinT() || distance > ray.getMaxT())
                    continue;
                return true;
            }
            return false;
        }
        finally
        {
            long timeSpent = System.currentTimeMillis() - start;
            numOcclusionTests.incrementAndGet();
            timeSpentOnOcclusionTests.addAndGet(timeSpent);
        }
    }

    @Override
    public void logMetrics()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("Number of rays traced: " + numRaysTraced.get() + "\n");
        sb.append("Number of occlusion tests performed: " + numOcclusionTests.get() + "\n");
        sb.append("\n");
        sb.append("Time spent tracing rays: " + timeSpentTracingRays.get() + "ms\n");
        sb.append("Time spent on occlusion tests: " + timeSpentOnOcclusionTests.get() + "ms\n");
        sb.append("\n");
        sb.append("Average time spent per ray traced: " + timeSpentTracingRays.get() / (double)numRaysTraced.get() + "ms\n");
        sb.append("Average time spent per occlusion test: " + timeSpentOnOcclusionTests.get() / (double)numOcclusionTests.get() + "ms\n");
        
        logger.log(Level.INFO, sb.toString());
    }
}
