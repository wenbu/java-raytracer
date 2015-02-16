package main;

import java.util.HashSet;
import java.util.Set;

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
import core.math.Direction;
import core.math.Point;

public class Main
{
    public static void main(String[] args)
    {
//        Sampler sampler = new GridSuperSampler(8, 8);
    	Sampler sampler = new RandomSuperSampler(4);

        Set<Primitive> geo = getGeometry();
        Set<Light> lights = getLights();

        Scene scene = new Scene(new Point(0, 0, 0),
                                new Point(-1, 1, -3),
                                new Point(1, 1, -3),
                                new Point(-1, -1, -3),
                                new Point(1, -1, -3),
                                500,
                                500,
                                sampler,
                                geo,
                                lights);
        long timeA = System.currentTimeMillis();
        scene.render();
        long timeB = System.currentTimeMillis();

        System.out.println("Rendered in " + ( timeB - timeA ) + "ms.");
    }

    private static Set<Primitive> getGeometry()
    {
        Set<Primitive> primitives = new HashSet<>();

        Material material1 = new PhongMaterial(Colors.GRAY10,
                                               Colors.MAGENTA,
                                               Colors.WHITE,
                                               Colors.BLACK,
                                               50);
        Sphere sphere1 = new Sphere(new Point(0, 0, -20), 3, material1);

        Material material2 = new PhongMaterial(Colors.GRAY10,
                                               Colors.YELLOW,
                                               Colors.WHITE,
                                               Colors.BLACK,
                                               50);
        Sphere sphere2 = new Sphere(new Point(-2, 2, -15), 1, material2);

        Material material3 = new PhongMaterial(Colors.GRAY10,
                                               Colors.CYAN,
                                               Colors.WHITE,
                                               Colors.BLACK,
                                               50);
        Sphere sphere3 = new Sphere(new Point(-2, -2, -15), 1, material3);

        Material material4 = new PhongMaterial(Colors.GRAY10,
                                               Colors.GRAY10,
                                               Colors.WHITE,
                                               Colors.GRAY50,
                                               50);
        Triangle triangle1 = new Triangle(new Point(5, 5, -17),
                                          new Point(1, 4, -20),
                                          new Point(6, -1, -20),
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

        Light light1 = new DirectionalLight(Direction.getNormalizedDirection(1,
                                                                             -1,
                                                                             -1),
                                            Colors.WHITE, 8, 0.0436);
        Light light2 = new DirectionalLight(Direction.getNormalizedDirection(1,
                                                                             1,
                                                                             -1),
                                            new Color(0.1, 0.1, 1), 8, 0.0436);

        lights.add(light1);
        lights.add(light2);

        return lights;
    }
}
