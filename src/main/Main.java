package main;

import java.util.HashSet;
import java.util.Set;

import metrics.MetricsManager;
import raytracer.impl.SimpleRaytracer;
import sampler.Sampler;
import sampler.impl.RandomSuperSampler;
import scene.Scene;
import scene.lights.Light;
import scene.lights.impl.DirectionalLight;
import scene.materials.Material;
import scene.materials.impl.PhongMaterial;
import scene.primitives.Primitive;
import scene.primitives.impl.Sphere;
import scene.primitives.impl.Triangle;
import core.colors.Color;
import core.colors.Colors;
import core.math.Direction3;
import core.math.Point3;
import film.impl.ToneMappingFilm;

public class Main
{
    public static void main(String[] args)
    {
        // Sampler sampler = new GridSuperSampler(8, 8);
        Sampler sampler = new RandomSuperSampler(4);

        int outputX = 1200;
        int outputY = 1200;
        
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
                                1200,
                                1200,
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
        Sphere sphere1 = new Sphere(new Point3(0, 0, -20), 3, material1);

        Material material2 = new PhongMaterial(Colors.GRAY10,
                                               Colors.YELLOW,
                                               Colors.WHITE,
                                               Colors.BLACK,
                                               50);
        Sphere sphere2 = new Sphere(new Point3(-2, 2, -15), 1, material2);

        Material material3 = new PhongMaterial(Colors.GRAY10,
                                               Colors.CYAN,
                                               Colors.WHITE,
                                               Colors.BLACK,
                                               50);
        Sphere sphere3 = new Sphere(new Point3(-2, -2, -15), 1, material3);

        Material material4 = new PhongMaterial(Colors.GRAY10,
                                               Colors.GRAY10,
                                               Colors.WHITE,
                                               Colors.GRAY50,
                                               50);
        Triangle triangle1 = new Triangle(new Point3(5, 5, -17),
                                          new Point3(1, 4, -20),
                                          new Point3(6, -1, -20),
                                          material4);
        primitives.add(sphere1);
        primitives.add(sphere2);
        primitives.add(sphere3);
        primitives.add(triangle1);

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
