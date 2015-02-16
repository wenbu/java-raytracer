package scene;

import film.Film;
import film.impl.ToneMappingFilm;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import camera.Camera;
import raytracer.Raytracer;
import raytracer.impl.SimpleRaytracer;
import sampler.Sampler;
import scene.lights.Light;
import scene.primitives.Primitive;
import core.Pixel;
import core.Ray;
import core.Sample;
import core.colors.Color;
import core.math.Direction;
import core.math.Point;

public class Scene
{
    // (0,0) at LL
    private final Point imageLL;

    private final int outputX;
    private final int outputY;

    private final Direction dx;
    private final Direction dy;

    private final Sampler sampler;
    private final Camera camera;
    private final Raytracer raytracer;
    private final Film film;
    
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
    public Scene(Point eyePosition,
                 Point imageUL,
                 Point imageUR,
                 Point imageLL,
                 Point imageLR,
                 int outputX,
                 int outputY,
                 Sampler sampler,
                 Set<Primitive> geometry,
                 Set<Light> lights)
    {
        this.camera = new Camera(eyePosition);
        this.sampler = sampler;
        
        this.imageLL = imageLL;

        this.outputX = outputX;
        this.outputY = outputY;

        dx = (imageLR.minus(imageLL)).times(1.0 / outputX);
        dy = (imageUL.minus(imageLL)).times(1.0 / outputY);
        
        raytracer = new SimpleRaytracer(geometry, lights);
        film = new ToneMappingFilm(outputX, outputY);
    }

    public void render()
    {
        for (int x = 0; x < outputX; x++)
        {
            for (int y = 0; y < outputY; y++)
            {
//                Pixel pixel = getPixel(x, y);
//                Set<Sample> samples = sampler.getSamples(pixel);
//                for (Sample sample : samples)
//                {
//                    Ray cameraRay = camera.getRay(sample); 
//                    Color color = raytracer.traceRay(cameraRay);
//                    film.registerSample(x, y, color);
//                }
            	executor.submit(new PixelRenderTask(x, y));
            }
        }
        executor.shutdown();
        try
		{
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e)
		{
			System.out.println("interrupted");
			e.printStackTrace();
		}
        film.imageComplete();
    }
    
    private class PixelRenderTask implements Runnable
    {
    	private final int x;
    	private final int y;
    	
    	public PixelRenderTask(int x, int y)
    	{
    		this.x = x;
    		this.y = y;
    	}
    	
		@Override
		public void run()
		{
			Pixel pixel = getPixel(x, y);
			Set<Sample> samples = sampler.getSamples(pixel);
            for (Sample sample : samples)
            {
                Ray cameraRay = camera.getRay(sample); 
                Color color = raytracer.traceRay(cameraRay);
                film.registerSample(x, y, color);
            }
		}
    }

    private Pixel getPixel(int pixelX, int pixelY)
    {
        double x = pixelX;
        double y = pixelY;

        Point pixelLL = imageLL.plus(dx.times(x)).plus(dy.times(y));
        Point pixelLR = pixelLL.plus(dx);
        Point pixelUL = pixelLL.plus(dy);
        Point pixelUR = pixelLL.plus(dx).plus(dy);

        return new Pixel(pixelUL, pixelUR, pixelLL, pixelLR);
    }
}
