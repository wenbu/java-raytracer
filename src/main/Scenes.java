package main;

import java.io.IOException;
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
import scene.lights.AreaLight;
import scene.lights.Light;
import scene.lights.impl.DiffuseAreaLight;
import scene.lights.impl.DirectionalLight;
import scene.lights.impl.InfiniteAreaLight;
import scene.lights.impl.PointLight;
import scene.materials.Material;
import scene.materials.impl.MatteMaterial;
import scene.materials.impl.PlasticMaterial;
import scene.medium.Medium.MediumInterface;
import scene.primitives.Aggregate;
import scene.primitives.Primitive;
import scene.primitives.accelerator.bvh.BoundingVolumeHierarchy;
import scene.primitives.accelerator.bvh.BoundingVolumeHierarchy.SplitMethod;
import scene.primitives.impl.GeometricPrimitive;
import texture.Texture;
import texture.impl.CheckerboardTexture;
import texture.impl.ConstantTexture;
import texture.impl.ImageTexture;
import texture.impl.MipMap.ImageWrap;
import texture.mapping.TextureMapping2D;
import texture.mapping.impl.PlanarMapping2D;
import texture.mapping.impl.SphericalMapping2D;
import utilities.MaterialUtilities;
import utilities.MeshUtilities;

public class Scenes
{
    public static Scene fromPdb(String pdbPath)
    {
        try
        {
            PdbReader pdbReader = new PdbReader(pdbPath, 1.5);
            Transformation sceneTransform = Transformation.IDENTITY;
            List<Primitive> primitives = pdbReader.read(sceneTransform);
            List<Light> lights = new LinkedList<>();
//
//            Light light1 = new DirectionalLight(new Transformation(),
//                                                new RGBSpectrum(0.9, 0.9, 1),
//                                                new Direction3(-1, 1, 1));
//            lights.add(light1);
//            Transformation lightTransform = Transformation.getTranslation(0, 10, 0);
//            Light light2 = new PointLight(lightTransform, new MediumInterface(), new RGBSpectrum(50, 50, 50));
//            lights.add(light2);

            Light light = new InfiniteAreaLight(Transformation.IDENTITY,
                                                 new RGBSpectrum(1),
                                                 20,
                                                 "textures/vp_sky_v2_002_sm.jpg");
            lights.add(light);
            
            Aggregate geo = new BoundingVolumeHierarchy(primitives, 5, SplitMethod.SURFACE_AREA_HEURISTIC);
            
            return new Scene(geo, lights);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static Scene spheres()
    {
        List<Primitive> primitives = new LinkedList<>();
        
        Transformation sphereTransform1 = Transformation.getTranslation(-2, -10, 1.5);
        Sphere         sphere          = new Sphere(sphereTransform1, sphereTransform1.inverse(), false, 1.5);
        Material       material        = MaterialUtilities.getGlassMaterial(Colors.GRAY20, Colors.GRAY80, 0, 1.5);
        Primitive      spherePrimitive = new GeometricPrimitive(sphere, material);
        primitives.add(spherePrimitive);
        
        Transformation sphereTransform2 = Transformation.getTranslation(0, -15, 2.5);
        Sphere sphere2 = new Sphere(sphereTransform2, sphereTransform2.inverse(), false, 2.5);
        TextureMapping2D textureMapping = new SphericalMapping2D(sphereTransform2.inverse());
        Texture<RGBSpectrum> tex = new ImageTexture<>(textureMapping, "textures/UV_Grid_Sm.jpg", false, 16, ImageWrap.REPEAT, 1, true, RGBSpectrum.class);
        Material material2 = new PlasticMaterial(tex, new ConstantTexture<>(Colors.WHITE),
                                                 new ConstantTexture<>(0.4), null, false);
        Primitive spherePrimitive2 = new GeometricPrimitive(sphere2, material2);
        primitives.add(spherePrimitive2);
        
        Transformation sphereTransform3 = Transformation.getTranslation(2, -10, 1.5);
        Sphere sphere3 = new Sphere(sphereTransform3, sphereTransform3.inverse(), false, 1.5);
        Material material3 = MaterialUtilities.getMirrorMaterial(Colors.WHITE);
        Primitive spherePrimitive3 = new GeometricPrimitive(sphere3, material3);
        primitives.add(spherePrimitive3);
        
        Transformation planeTransform = Transformation.getTranslation(0, -20, 0)
                                                      .compose(Transformation.getUniformScale(2000));
        TextureMapping2D planeTextureMapping = new PlanarMapping2D(new Direction3(1, 0, 0), new Direction3(0, 1, 0), 0.002, 0.002);
        Texture<RGBSpectrum> checkerTexture = new CheckerboardTexture<>(planeTextureMapping, Colors.GRAY10, Colors.GRAY90, 1);
        Material planeMaterial = new MatteMaterial(checkerTexture, new ConstantTexture<>(0.0), null);
        List<Triangle> planeTriangles = MeshUtilities.createQuad(planeTransform);
        List<Primitive> planePrimitives = getPrimitives(planeTriangles, planeMaterial);
        primitives.addAll(planePrimitives);
        
        List<Light> lights = new LinkedList<>();
        
        Light light = new InfiniteAreaLight(Transformation.IDENTITY,
                                             new RGBSpectrum(1),
                                             20,
                                             "textures/vp_sky_v2_002_sm.jpg");
        lights.add(light);
        
        Aggregate geo = new BoundingVolumeHierarchy(primitives, 5, SplitMethod.SURFACE_AREA_HEURISTIC);
        
        return new Scene(geo, lights);
    }
    
    public static Scene getTestScene()
    {
        List<Primitive> primitives = new LinkedList<>();

        Transformation sphereTransform1 = Transformation.getTranslation(0, -20, 0);
        Sphere         sphere1          = new Sphere(sphereTransform1, sphereTransform1.inverse(), false, 3);
        Material       material1        = MaterialUtilities.getPlasticMaterial(Colors.MAGENTA, Colors.WHITE, 0.1, false);
        Primitive      spherePrimitive1 = new GeometricPrimitive(sphere1, material1);
        primitives.add(spherePrimitive1);

        Transformation sphereTransform2 = Transformation.getTranslation(-2, -15, 2);
        Sphere sphere2 = new Sphere(sphereTransform2, sphereTransform2.inverse(), false, 1);
        Material material2 = MaterialUtilities.getPlasticMaterial(Colors.YELLOW, Colors.WHITE, 0.4, false);
        Primitive spherePrimitive2 = new GeometricPrimitive(sphere2, material2);
        primitives.add(spherePrimitive2);

        Transformation sphereTransform3 = Transformation.getTranslation(-2, -15, -2);
        Sphere sphere3 = new Sphere(sphereTransform3, sphereTransform3.inverse(), false, 1);
        Material material3 = MaterialUtilities.getGlassMaterial(Colors.WHITE, Colors.WHITE, 0, 1.5);
        Primitive spherePrimitive3 = new GeometricPrimitive(sphere3, material3);
        primitives.add(spherePrimitive3);

        List<Triangle> triangles = MeshUtilities.createSingleTriangle(Transformation.IDENTITY,
                                                                      new Point3(1, -20, 4),
                                                                      new Point3(5, -17, 5),
                                                                      new Point3(6, -20, -1));
        Material material4 = MaterialUtilities.getMatteMaterial(Colors.WHITE, 1.0);
        AreaLight light3 = new DiffuseAreaLight(Transformation.IDENTITY, new MediumInterface(), new RGBSpectrum(10, 10, 10), 1, triangles.get(0));
        Primitive trianglePrimitive = new GeometricPrimitive(triangles.get(0), material4, light3, new MediumInterface());
        primitives.add(trianglePrimitive);

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
        
        Aggregate geo = new BoundingVolumeHierarchy(primitives, 5, SplitMethod.SURFACE_AREA_HEURISTIC);
        
        List<Light> lights = new LinkedList<>();

//        Light light1 = new DirectionalLight(new Transformation(),
//                                            new RGBSpectrum(0.9, 0.9, 1),
//                                            new Direction3(-1, 1, 1));
//        lights.add(light1);
        
//        Transformation lightTransform = Transformation.getTranslation(0, 2, 0);
//        Light light2 = new PointLight(lightTransform, new MediumInterface(), new RGBSpectrum(50, 50, 50));
//        lights.add(light2);
        
        lights.add(light3);

        return new Scene(geo, lights);
    }
    
    private static List<Primitive> getPrimitives(List<Triangle> triangles, Material material)
    {
        return triangles.stream().map(triangle -> new GeometricPrimitive(triangle, material)).collect(Collectors.toList());
    }
}
