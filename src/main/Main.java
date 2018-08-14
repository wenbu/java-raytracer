package main;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import core.colors.Color;
import core.colors.Colors;
import core.math.Direction3;
import core.math.Point3;
import core.math.Transformation;
import film.impl.ToneMappingFilm;
import metrics.MetricsManager;
import raytracer.impl.SimpleRaytracer;
import sampler.Sampler;
import sampler.impl.RandomSuperSampler;
import scene.Scene;
import scene.geometry.impl.Sphere;
import scene.geometry.impl.Triangle;
import scene.lights.Light;
import scene.lights.impl.DirectionalLight;
import scene.materials.Material;
import scene.materials.impl.PhongMaterial;
import scene.primitives.Primitive;
import scene.primitives.impl.GeometricPrimitive;

public class Main
{
    public static void main(String[] args)
    {
        // Sampler sampler = new GridSuperSampler(8, 8);
        Sampler sampler = new RandomSuperSampler(1);

        int outputX = 400;
        int outputY = 400;
        
        Set<Primitive> geo = getGeometry();
        Set<Light> lights = getLights();
        SimpleRaytracer raytracer = new SimpleRaytracer(geo, lights);
        ToneMappingFilm film = new ToneMappingFilm(outputX, outputY);

        MetricsManager metricsManager = new MetricsManager();
        
        Scene scene = new Scene(new Point3(0, 0, 0),
                                new Point3(-1, 1, -3),
                                new Point3(1, 1, -3),
                                new Point3(-1, -1, -3),
                                new Point3(1, -1, -3),
                                400,
                                400,
                                sampler,
                                raytracer,
                                film);
        
        metricsManager.registerMetricsAwareEntities(raytracer, film, scene);
        long timeA = System.currentTimeMillis();
        scene.render();
        long timeB = System.currentTimeMillis();

        System.out.println("Rendered in " + ( timeB - timeA ) + "ms.");
        metricsManager.logMetrics();
    }

    private static Set<Primitive> getGeometry()
    {
        Set<Primitive> primitives = new HashSet<>();

        Material material1 = new PhongMaterial(Colors.GRAY10,
                                               Colors.MAGENTA,
                                               Colors.WHITE,
                                               Colors.BLACK,
                                               50);
        Sphere sphere1 = new Sphere(Transformation.getTranslation(0, 0, -20),
                                    Transformation.getTranslation(0, 0, 20),
                                    false,
                                    3);
        Primitive spherePrimitive1 = new GeometricPrimitive(sphere1, material1);

        Material material2 = new PhongMaterial(Colors.GRAY10,
                                               Colors.YELLOW,
                                               Colors.WHITE,
                                               Colors.BLACK,
                                               50);
        Sphere sphere2 = new Sphere(Transformation.getTranslation(-2, 2, -15),
                                    Transformation.getTranslation(2, -2, 15),
                                    false,
                                    1);
        Primitive spherePrimitive2 = new GeometricPrimitive(sphere2, material2);
        
        Material material3 = new PhongMaterial(Colors.GRAY10,
                                               Colors.CYAN,
                                               Colors.WHITE,
                                               Colors.BLACK,
                                               50);
        Sphere sphere3 = new Sphere(Transformation.getTranslation(-2, -2, -15),
                                    Transformation.getTranslation(2, 2, 15),
                                    false,
                                    1);
        Primitive spherePrimitive3 = new GeometricPrimitive(sphere3, material3);

        Material material4 = new PhongMaterial(Colors.GRAY10,
                                               Colors.GRAY10,
                                               Colors.WHITE,
                                               Colors.GRAY50,
                                               50);
        List<Triangle> mesh = Triangle.createTriangleMesh(new Transformation(),
                                                          new Transformation(),
                                                          false,
                                                          1,
                                                          new int[] { 0, 1, 2 },
                                                          3,
                                                          new Point3[] { new Point3(5, 5, -17),
                                                                         new Point3(1, 4, -20),
                                                                         new Point3(6, -1, -20) },
                                                          null,
                                                          null,
                                                          null);
        List<Primitive> meshPrimitives = mesh.stream()
                                             .map(t -> new GeometricPrimitive(t, material4))
                                             .collect(Collectors.toList());
        primitives.add(spherePrimitive1);
        primitives.add(spherePrimitive2);
        primitives.add(spherePrimitive3);
        primitives.addAll(meshPrimitives);

        return primitives;
    }

    private static Set<Light> getLights()
    {
        Set<Light> lights = new HashSet<>();

        Light light1 = new DirectionalLight(Direction3.getNormalizedDirection(1,
                                                                             -1,
                                                                             -1),
                                            Colors.WHITE,
                                            8,
                                            0.0436);
        Light light2 = new DirectionalLight(Direction3.getNormalizedDirection(1,
                                                                             1,
                                                                             -1),
                                            new Color(0.1, 0.1, 1),
                                            8,
                                            0.0436);

        lights.add(light1);
        lights.add(light2);

        return lights;
    }
}
