package scene;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import metrics.MetricsAware;
import raytracer.Raytracer;
import sampler.Sampler;
import camera.Camera;
import core.Pixel;
import core.Ray;
import core.Sample;
import core.colors.Color;
import core.math.Direction;
import core.math.Point;
import film.Film;

public class Scene implements MetricsAware
{
    private static final Logger logger = Logger.getLogger(Scene.class.getName());
    
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
    
//    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    
    private long timeSpentRendering = 0L;
   
    public Scene(Point eyePosition,
                 Point imageUL,
                 Point imageUR,
                 Point imageLL,
                 Point imageLR,
                 int outputX,
                 int outputY,
                 Sampler sampler,
                 Raytracer raytracer,
                 Film film)
    {
        this.camera = new Camera(eyePosition);
        this.sampler = sampler;
        
        this.imageLL = imageLL;

        this.outputX = outputX;
        this.outputY = outputY;

        dx = (imageLR.minus(imageLL)).times(1.0 / outputX);
        dy = (imageUL.minus(imageLL)).times(1.0 / outputY);
        
        this.raytracer = raytracer;
        this.film = film;
    }

    public void render()
    {
        long start = System.currentTimeMillis();
        
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
        finally
        {
            timeSpentRendering = System.currentTimeMillis() - start;
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

    @Override
    public void logMetrics()
    {
        logger.log(Level.INFO, "Time spent rendering: " + timeSpentRendering + "ms");
    }
}
