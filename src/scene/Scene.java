package scene;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import metrics.MetricsAware;
import sampler.Sampler;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;
import scene.primitives.Primitive;
import camera.Camera;
import core.Pixel;
import core.Ray;
import core.Sample;
import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point3;
import core.space.BoundingBox;
import core.tuple.Pair;
import film.Film;

public class Scene //implements MetricsAware
{
    private static final Logger logger = Logger.getLogger(Scene.class.getName());

    private Primitive aggregate;
    private List<Light> lights;
    private BoundingBox worldBound;
    
    public Scene(Primitive aggregate, List<Light> lights)
    {
        this.aggregate = aggregate;
        this.lights = lights;
        this.worldBound = aggregate.worldBound();
        
        for (Light light : lights)
        {
            light.preprocess(this);
        }
    }
    
    public BoundingBox worldBound()
    {
        return worldBound;
    }
    
    public SurfaceInteraction intersect(Ray ray)
    {
        return aggregate.intersect(ray);
    }
    
    public boolean intersectP(Ray ray)
    {
        return aggregate.intersectP(ray);
    }
    
    public RGBSpectrum intersectTr(Ray ray, Sampler sampler, SurfaceInteraction surfaceInteraction)
    {
        // XXX This depends on intersect() properly updating tMax. Not sure if that's working.
        RGBSpectrum transmittance = new RGBSpectrum(1, 1, 1);
        
        while(true)
        {
            SurfaceInteraction hitSurface = intersect(ray);
            
            // Accumulate beam transmittance for ray segment
            if (ray.getMedium() != null)
            {
                transmittance = transmittance.times(ray.getMedium().transmittance(ray, sampler));
            }
            
            // Initialize next ray segment or halt transmittance computation
            if (hitSurface == null)
            {
                return null;
            }
            if (surfaceInteraction.getPrimitive().getMaterial() != null)
            {
                return transmittance;
            }
            ray = surfaceInteraction.spawnRay(ray.getDirection());
        }
    }
    
    public List<Light> getLights()
    {
        return lights;
    }
}
