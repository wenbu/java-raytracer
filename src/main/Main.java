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
import integrator.impl.DirectLightingIntegrator;
import integrator.impl.DirectLightingIntegrator.LightStrategy;
import sampler.Sampler;
import sampler.impl.StratifiedSampler;
import scene.Scene;

public class Main
{
    // TODO: scene parser
    public static void main(String[] args)
    {
        Sampler sampler = new StratifiedSampler(10, 10, true, 10);

        Point2 resolution = new Point2(640, 360);
        Film film = getFilm(resolution);
        
        Point3 cameraPosition = new Point3(0, 0, 2);
        Point3 cameraLookAt = new Point3(0, -20, 0);
        Direction3 cameraUp = new Direction3(0, 0, 1);
        Camera camera = getCamera(cameraPosition, cameraLookAt, cameraUp, resolution, 40, film);

        Integrator integrator = new DirectLightingIntegrator(sampler, camera, LightStrategy.UNIFORM_SAMPLE_ALL, 5);
        Scene scene = Scenes.getTestScene();

        integrator.render(scene);
    }
    
    private static Film getFilm(Point2 resolution)
    {
        Film film = new Film(resolution,
                             new BoundingBox2(0, 0, 1, 1),
                             new MitchellFilter(new Direction2(1, 1), 0.6, 0.2),
                             100,
                             "placeholder",
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
