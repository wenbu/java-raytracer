package scene;

import java.util.List;
import java.util.logging.Logger;

import core.Ray;
import core.colors.RGBSpectrum;
import core.space.BoundingBox3;
import core.tuple.Pair;
import sampler.Sampler;
import scene.interactions.impl.SurfaceInteraction;
import scene.lights.Light;
import scene.primitives.Aggregate;

public class Scene
{
    private static final Logger logger = Logger.getLogger(Scene.class.getName());

    private Aggregate aggregate;
    private List<Light> lights;
    private BoundingBox3 worldBound;
    
    public Scene(Aggregate aggregate, List<Light> lights)
    {
        this.aggregate = aggregate;
        this.lights = lights;
        this.worldBound = aggregate.worldBound();
        
        for (Light light : lights)
        {
            light.preprocess(this);
        }
    }
    
    public BoundingBox3 worldBound()
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
    
    public Pair<RGBSpectrum, SurfaceInteraction> intersectTr(Ray ray, Sampler sampler)
    {
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
            if (hitSurface.getPrimitive().getMaterial() != null)
            {
                return new Pair<>(transmittance, hitSurface);
            }
            ray = hitSurface.spawnRay(ray.getDirection());
        }
    }
    
    public List<Light> getLights()
    {
        return lights;
    }
}
