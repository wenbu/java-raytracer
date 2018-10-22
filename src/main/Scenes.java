package main;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import core.colors.Colors;
import core.colors.RGBSpectrum;
import core.math.Direction3;
import core.math.Point3;
import core.math.Transformation;
import scene.Scene;
import scene.geometry.impl.Sphere;
import scene.geometry.impl.Triangle;
import scene.lights.Light;
import scene.lights.impl.DirectionalLight;
import scene.lights.impl.PointLight;
import scene.materials.Material;
import scene.medium.Medium;
import scene.medium.Medium.MediumInterface;
import scene.primitives.Aggregate;
import scene.primitives.Primitive;
import scene.primitives.accelerator.bvh.BoundingVolumeHierarchy;
import scene.primitives.accelerator.bvh.BoundingVolumeHierarchy.SplitMethod;
import scene.primitives.impl.GeometricPrimitive;
import utilities.MaterialUtilities;
import utilities.MeshUtilities;

public class Scenes
{
    public static Scene spheres()
    {
        List<Primitive> primitives = new LinkedList<>();
        
        Transformation sphereTransform = Transformation.getTranslation(0, -20, 0);
        Sphere         sphere          = new Sphere(sphereTransform, sphereTransform.inverse(), false, 3);
        Material       material        = MaterialUtilities.getPlasticMaterial(Colors.WHITE, Colors.WHITE, 0.1, false);
        Primitive      spherePrimitive = new GeometricPrimitive(sphere, material, new Medium.MediumInterface());
        primitives.add(spherePrimitive);
        
        Transformation sphereTransform2 = Transformation.getTranslation(-2, -15, 2);
        Sphere sphere2 = new Sphere(sphereTransform2, sphereTransform2.inverse(), false, 1);
        Material material2 = MaterialUtilities.getPlasticMaterial(Colors.YELLOW, Colors.WHITE, 0.001, false);
        Primitive spherePrimitive2 = new GeometricPrimitive(sphere2, material2, new Medium.MediumInterface());
        primitives.add(spherePrimitive2);
        
        Transformation sphereTransform3 = Transformation.getTranslation(-2, -15, -2);
        Sphere sphere3 = new Sphere(sphereTransform3, sphereTransform3.inverse(), false, 1);
        Material material3 = MaterialUtilities.getPlasticMaterial(Colors.CYAN, Colors.WHITE, 0.25, false);
        Primitive spherePrimitive3 = new GeometricPrimitive(sphere3, material3, new Medium.MediumInterface());
        primitives.add(spherePrimitive3);
        
        Transformation sphereTransform4 = Transformation.getTranslation(2, -15, -2);
        Sphere sphere4 = new Sphere(sphereTransform4, sphereTransform4.inverse(), false, 1);
        Material material4 = MaterialUtilities.getPlasticMaterial(Colors.MAGENTA, Colors.WHITE, 0.5, false);
        Primitive spherePrimitive4 = new GeometricPrimitive(sphere4, material4, new Medium.MediumInterface());
        primitives.add(spherePrimitive4);
        
        Transformation sphereTransform5 = Transformation.getTranslation(2, -15, 2);
        Sphere sphere5 = new Sphere(sphereTransform5, sphereTransform5.inverse(), false, 1);
        Material material5 = MaterialUtilities.getPlasticMaterial(Colors.GRAY50, Colors.WHITE, 0.999, false);
        Primitive spherePrimitive5 = new GeometricPrimitive(sphere5, material5, new Medium.MediumInterface());
        primitives.add(spherePrimitive5);
        
        List<Light> lights = new LinkedList<>();

        Light light1 = new DirectionalLight(new Transformation(),
                                            new RGBSpectrum(0.9, 0.9, 1),
                                            new Direction3(-1, 1, 1));
        lights.add(light1);
        
        Aggregate geo = new BoundingVolumeHierarchy(primitives, 5, SplitMethod.SURFACE_AREA_HEURISTIC);
        
        return new Scene(geo, lights);
    }
    public static Scene getTestScene()
    {
        List<Primitive> primitives = new LinkedList<>();

        Transformation sphereTransform1 = Transformation.getTranslation(0, -20, 0);
        Sphere         sphere1          = new Sphere(sphereTransform1, sphereTransform1.inverse(), false, 3);
        Material       material1        = MaterialUtilities.getPlasticMaterial(Colors.MAGENTA, Colors.WHITE, 0.1, false);
        Primitive      spherePrimitive1 = new GeometricPrimitive(sphere1, material1, new Medium.MediumInterface());
        primitives.add(spherePrimitive1);

        Transformation sphereTransform2 = Transformation.getTranslation(-2, -15, 2);
        Sphere sphere2 = new Sphere(sphereTransform2, sphereTransform2.inverse(), false, 1);
        Material material2 = MaterialUtilities.getPlasticMaterial(Colors.YELLOW, Colors.WHITE, 0.4, false);
        Primitive spherePrimitive2 = new GeometricPrimitive(sphere2, material2, new Medium.MediumInterface());
        primitives.add(spherePrimitive2);

        Transformation sphereTransform3 = Transformation.getTranslation(-2, -15, -2);
        Sphere sphere3 = new Sphere(sphereTransform3, sphereTransform3.inverse(), false, 1);
        Material material3 = MaterialUtilities.getPlasticMaterial(Colors.CYAN, Colors.WHITE, 0.6, false);
        Primitive spherePrimitive3 = new GeometricPrimitive(sphere3, material3, new Medium.MediumInterface());
        primitives.add(spherePrimitive3);

        List<Triangle> triangles = MeshUtilities.createSingleTriangle(Transformation.IDENTITY,
                                                                      new Point3(5, -17, 5),
                                                                      new Point3(1, -20, 4),
                                                                      new Point3(6, -20, -1));
        Material material4 = MaterialUtilities.getMirrorMaterial(Colors.GRAY30);
        List<Primitive> trianglePrimitives1 = getPrimitives(triangles, material4);
        primitives.addAll(trianglePrimitives1);

        Transformation cubeTransform = Transformation.getTranslation(2, -15, -2)
                                                     .compose(Transformation.getRotation(new Direction3(1, 1, 1),
                                                                                         60))
                                                     .compose(Transformation.getUniformScale(Math.sqrt(2)));

        Material material5 = MaterialUtilities.getPlasticMaterial(Colors.GRAY50, Colors.WHITE, 0.01, false);
        List<Triangle> cubeTriangles = MeshUtilities.createCube(cubeTransform, false);
        List<Primitive> cubePrimitives = getPrimitives(cubeTriangles, material5);
        primitives.addAll(cubePrimitives);
        
        Transformation planeTransform1 = Transformation.getTranslation(0, -20, -5).compose(Transformation.getUniformScale(20));
        Material material6 = MaterialUtilities.getMatteMaterial(Colors.WHITE, 0.5);
        List<Triangle> planeTriangles1 = MeshUtilities.createQuad(planeTransform1);
        List<Primitive> planePrimitives1 = getPrimitives(planeTriangles1, material6);
        primitives.addAll(planePrimitives1);

        Transformation planeTransform2 = Transformation.getTranslation(0, -30, 5)
                                                       .compose(Transformation.getRotation(new Direction3(1, 0, 0),
                                                                                           90))
                                                       .compose(Transformation.getUniformScale(20));
        List<Triangle> planeTriangles2 = MeshUtilities.createQuad(planeTransform2);
        List<Primitive> planePrimitives2 = getPrimitives(planeTriangles2, material6);
        primitives.addAll(planePrimitives2);
        
        //Aggregate geo = new SimpleAggregate(primitives);
        Aggregate geo = new BoundingVolumeHierarchy(primitives, 5, SplitMethod.EQUAL_COUNTS);
        
        List<Light> lights = new LinkedList<>();

        Light light1 = new DirectionalLight(new Transformation(),
                                            new RGBSpectrum(0.9, 0.9, 1),
                                            new Direction3(-1, 1, 1));
        lights.add(light1);
        
        Transformation lightTransform = Transformation.getTranslation(0, 2, 0);
        Light light2 = new PointLight(lightTransform, new MediumInterface(), new RGBSpectrum(50, 50, 50));
        lights.add(light2);

        return new Scene(geo, lights);
    }
    
    private static List<Primitive> getPrimitives(List<Triangle> triangles, Material material)
    {
        return triangles.stream().map(triangle -> new GeometricPrimitive(triangle, material, new MediumInterface())).collect(Collectors.toList());
    }
}
