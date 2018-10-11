package main;

import camera.Camera;
import camera.impl.PerspectiveCamera;
import core.colors.Colors;
import core.colors.RGBSpectrum;
import core.math.*;
import core.space.BoundingBox2;
import film.Film;
import film.filter.impl.BoxFilter;
import integrator.Integrator;
import integrator.impl.WhittedIntegrator;
import sampler.Sampler;
import sampler.impl.StratifiedSampler;
import scene.Scene;
import scene.geometry.impl.Sphere;
import scene.geometry.impl.Triangle;
import scene.lights.Light;
import scene.lights.impl.DirectionalLight;
import scene.materials.Material;
import scene.materials.impl.MatteMaterial;
import scene.materials.impl.MirrorMaterial;
import scene.materials.impl.PlasticMaterial;
import scene.medium.Medium;
import scene.medium.Medium.MediumInterface;
import scene.primitives.Primitive;
import scene.primitives.impl.GeometricPrimitive;
import scene.primitives.impl.SimpleAggregate;
import texture.impl.ConstantTexture;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        Primitive geo = getGeometry();
        List<Light> lights = getLights();

        Integrator integrator = new WhittedIntegrator(sampler, camera, 5);
        Scene scene = new Scene(geo, lights);

        long timeA = System.currentTimeMillis();
        integrator.render(scene);
        long timeB = System.currentTimeMillis();

        System.out.println("Rendered in " + (timeB - timeA) + "ms.");
    }

    private static Primitive getGeometry()
    {
        Set<Primitive> primitives = new HashSet<>();

        Transformation sphereTransform1 = Transformation.getTranslation(0, -20, 0);
        Sphere sphere1 = new Sphere(sphereTransform1, sphereTransform1.inverse(), false, 3);
//        Material material1 = new MatteMaterial(new ConstantTexture<>(Colors.MAGENTA),
//                                               new ConstantTexture<>(0.1),
//                                               null);
        Material material1 = new PlasticMaterial(new ConstantTexture<>(Colors.MAGENTA),
                                                 new ConstantTexture<>(Colors.WHITE),
                                                 new ConstantTexture<>(0.1),
                                                 null,
                                                 false);
//        Material material1 = new MirrorMaterial(new ConstantTexture<>(Colors.GRAY70), null);
        Primitive spherePrimitive1 = new GeometricPrimitive(sphere1, material1, new MediumInterface());
        primitives.add(spherePrimitive1);

        Transformation sphereTransform2 = Transformation.getTranslation(-2, -15, 2);
        Sphere sphere2 = new Sphere(sphereTransform2, sphereTransform2.inverse(), false, 1);
//        Material material2 = new MatteMaterial(new ConstantTexture<>(Colors.YELLOW),
//                                               new ConstantTexture<>(0.1),
//                                               null);
        Material material2 = new PlasticMaterial(new ConstantTexture<>(Colors.YELLOW),
                                                 new ConstantTexture<>(Colors.WHITE),
                                                 new ConstantTexture<>(0.4),
                                                 null,
                                                 false);
//        Material material2 = new MirrorMaterial(new ConstantTexture<>(Colors.GRAY70), null);
        Primitive spherePrimitive2 = new GeometricPrimitive(sphere2, material2, new MediumInterface());
        primitives.add(spherePrimitive2);

        Transformation sphereTransform3 = Transformation.getTranslation(-2, -15, -2);
        Sphere sphere3 = new Sphere(sphereTransform3, sphereTransform3.inverse(), false, 1);
//        Material material3 = new MatteMaterial(new ConstantTexture<>(Colors.CYAN),
//                                               new ConstantTexture<>(0.1),
//                                               null);
        Material material3 = new PlasticMaterial(new ConstantTexture<>(Colors.CYAN),
                                                 new ConstantTexture<>(Colors.WHITE),
                                                 new ConstantTexture<>(0.6),
                                                 null,
                                                 false);
//        Material material3 = new MirrorMaterial(new ConstantTexture<>(Colors.GRAY70), null);
        Primitive spherePrimitive3 = new GeometricPrimitive(sphere3, material3, new MediumInterface());
        primitives.add(spherePrimitive3);

        Transformation triangleTransform = Transformation.IDENTITY;
        List<Triangle> triangles = Triangle.createTriangleMesh(triangleTransform,
                                                               triangleTransform.inverse(),
                                                               false,
                                                               1,
                                                               new int[]{0},
                                                               3,
                                                               new Point3[]{ new Point3(5, -17, 5),
                                                                             new Point3(1, -20, 4),
                                                                             new Point3(6, -20, -1) },
                                                               null,
                                                               null,
                                                               null);
//        Material material4 = new MatteMaterial(new ConstantTexture<>(Colors.GRAY10), new ConstantTexture<>(0.1), null);
//        Material material4 = new PlasticMaterial(new ConstantTexture<>(Colors.GRAY10),
//                                                 new ConstantTexture<>(Colors.WHITE),
//                                                 new ConstantTexture<>(0.001),
//                                                 null,
//                                                 false);
        Material material4 = new MirrorMaterial(new ConstantTexture<>(Colors.GRAY70), null);
        List<Primitive> trianglePrimitives = triangles.stream()
                                                      .map(t -> new GeometricPrimitive(t,
                                                                                       material4,
                                                                                       new MediumInterface()))
                                                      .collect(Collectors.toList());
        primitives.addAll(trianglePrimitives);

        return new SimpleAggregate(primitives);
    }

    private static List<Light> getLights()
    {
        List<Light> lights = new LinkedList<>();

        // TODO investigate why the sign of y is not what is expected
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
