package main;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import camera.Camera;
import camera.impl.PerspectiveCamera;
import core.colors.Colors;
import core.colors.RGBSpectrum;
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
import scene.geometry.impl.Sphere;
import scene.lights.Light;
import scene.lights.impl.DirectionalLight;
import scene.materials.Material;
import scene.materials.impl.MatteMaterial;
import scene.medium.Medium;
import scene.medium.Medium.MediumInterface;
import scene.primitives.Primitive;
import scene.primitives.impl.GeometricPrimitive;
import scene.primitives.impl.SimpleAggregate;
import texture.impl.ConstantTexture;

public class Main
{
    // TODO: scene parser
    public static void main(String[] args)
    {
        // XXX more for camera?
        Sampler sampler = new StratifiedSampler(1, 1, false, 1);
        
        Point2 resolution = new Point2(400, 400);
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
                                              30,
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

        Primitive geo = getGeometry();
        List<Light> lights = getLights();
        
        Integrator integrator = new WhittedIntegrator(sampler, camera, 5);
        Scene scene = new Scene(geo, lights);
        
        long timeA = System.currentTimeMillis();
        integrator.render(scene);
        long timeB = System.currentTimeMillis();

        System.out.println("Rendered in " + ( timeB - timeA ) + "ms.");
    }

    private static Primitive getGeometry()
    {
        Set<Primitive> primitives = new HashSet<>();
        
        Transformation sphereTransform1 = Transformation.getTranslation(0, -20, 0);
        Sphere sphere1 = new Sphere(sphereTransform1, sphereTransform1.inverse(), false, 3);
        Material material1 = new MatteMaterial(new ConstantTexture<RGBSpectrum>(Colors.MAGENTA),
                                              new ConstantTexture<Double>(0.1),
                                              null);
        Primitive spherePrimitive1 = new GeometricPrimitive(sphere1, material1, new MediumInterface());
        primitives.add(spherePrimitive1);
        
        Transformation sphereTransform2 = Transformation.getTranslation(-2, -15, 2);
        Sphere sphere2 = new Sphere(sphereTransform2, sphereTransform2.inverse(), false, 1);
        Material material2 = new MatteMaterial(new ConstantTexture<RGBSpectrum>(Colors.YELLOW),
                                               new ConstantTexture<Double>(0.1),
                                               null);
        Primitive spherePrimitive2 = new GeometricPrimitive(sphere2, material2, new MediumInterface());
        primitives.add(spherePrimitive2);
        
        Transformation sphereTransform3 = Transformation.getTranslation(-2, -15, -2);
        Sphere sphere3 = new Sphere(sphereTransform3, sphereTransform3.inverse(), false, 1);
        Material material3 = new MatteMaterial(new ConstantTexture<RGBSpectrum>(Colors.CYAN),
                                               new ConstantTexture<Double>(0.1),
                                               null);
        Primitive spherePrimitive3 = new GeometricPrimitive(sphere3, material3, new MediumInterface());
        primitives.add(spherePrimitive3);
        
        return new SimpleAggregate(primitives);
    }

    private static List<Light> getLights()
    {
        List<Light> lights = new LinkedList<>();

        Light light1 = new DirectionalLight(new Transformation(),
                                            new RGBSpectrum(1, 0.1, 0.1),
                                            new Direction3(-1, 1, 1));
        Light light2 = new DirectionalLight(new Transformation(),
                                            new RGBSpectrum(0.1, 0.1, 1),
                                            new Direction3(-1, 1, -1));

        lights.add(light1);
        lights.add(light2);

        return lights;
    }
}
