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
import film.filter.impl.BoxFilter;
import integrator.Integrator;
import integrator.impl.WhittedIntegrator;
import sampler.Sampler;
import sampler.impl.StratifiedSampler;
import scene.Scene;
import scene.medium.Medium;

public class Main
{
    // TODO: scene parser
    public static void main(String[] args)
    {
        Sampler sampler = new StratifiedSampler(2, 2, false, 1);

        Point2 resolution = new Point2(800, 800);
        Film film = new Film(resolution,
                             new BoundingBox2(0, 0, 1, 1),
                             new BoxFilter(new Direction2(0.5, 0.5)),
                             100,
                             "placeholder",
                             1);
        Medium medium = null;
        Transformation cameraTransform = Transformation.getLookAt(new Point3(0, 0, 0),
                                                                  new Point3(0, -20, 0),
                                                                  new Direction3(0, 0, 1));
        // getLookAt returns the world->camera transform.
        Camera camera = new PerspectiveCamera(cameraTransform.inverse(),
                                              new BoundingBox2(-1, -1, 1, 1),
                                              0,
                                              1,
                                              0,
                                              3,
                                              2 * Math.toDegrees(Math.atan(1.0/3.0)),
                                              film,
                                              medium);
//        Camera camera = new OrthographicCamera(cameraTransform.inverse(),
//                                               new BoundingBox2(-5, -5, 5, 5),
//                                               0,
//                                               1,
//                                               0,
//                                               0,
//                                               film,
//                                               medium);

        Integrator integrator = new WhittedIntegrator(sampler, camera, 5);
        Scene scene = Scenes.getTestScene();

        integrator.render(scene);
    }
}
