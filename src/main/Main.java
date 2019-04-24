package main;

import camera.Camera;
import camera.impl.PerspectiveCamera;
import core.math.Direction2;
import core.math.Direction3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.space.BoundingBox2;
import film.Film;
import film.filter.impl.MitchellFilter;
import integrator.Integrator;
import integrator.SamplerIntegrator;
import integrator.impl.DirectLightingIntegrator;
import integrator.impl.PathIntegrator;
import sampler.Sampler;
import sampler.impl.StratifiedSampler;
import scene.Scene;

public class Main
{
    // TODO: scene parser
    public static void main(String[] args) throws InterruptedException
    {
        Sampler sampler = new StratifiedSampler(2, 2, true, 4);

        Point2 resolution = new Point2(512, 512);
        Film film = getFilm(resolution);
        
        Point3 cameraPosition = new Point3(0, 0, 0);
        Point3 cameraLookAt = new Point3(0, -20, 0);
        Direction3 cameraUp = new Direction3(0, 0, 1);
        Camera camera = getCamera(cameraPosition, cameraLookAt, cameraUp, resolution, 40, film);

        Integrator integrator = new PathIntegrator(5, camera, sampler);
        Scene scene = Scenes.cornellBox();

        Runtime.getRuntime().addShutdownHook(new Thread(integrator::shutdownNow));
        integrator.render(scene);
    }
    
    private static Film getFilm(Point2 resolution)
    {
        Film film = new Film(resolution,
                             new BoundingBox2(0, 0, 1, 1),
                             new MitchellFilter(new Direction2(1, 1), 0.6, 0.2),
                             100,
                             "img",
                             "img-" + System.currentTimeMillis(),
                             1);
        return film;
    }
    
    private static Camera getCamera(Point3 cameraPosition, Point3 cameraLookAt, Direction3 cameraUp, Point2 resolution, double fov, Film film)
    {
        Transformation cameraTransform = Transformation.getLookAt(cameraPosition,
                                                                  cameraLookAt,
                                                                  cameraUp);

        double resX = resolution.x();
        double resY = resolution.y();
        BoundingBox2 screenWindow;
        if (resX < resY)
        {
            screenWindow = new BoundingBox2(-1, -resY / resX, 1, resY / resX);
        }
        else
        {
            screenWindow = new BoundingBox2(-resX / resY, -1, resX / resY, 1);
        }

        // getLookAt returns the world->camera transform.
        Camera camera = new PerspectiveCamera(cameraTransform.inverse(),
                                              screenWindow,
                                              0,
                                              1,
                                              0,
                                              3,
                                              fov,
                                              film,
                                              null);
        return camera;
    }
}
