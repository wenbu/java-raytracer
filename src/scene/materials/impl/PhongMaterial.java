package scene.materials.impl;

import java.util.HashSet;
import java.util.Set;

import raytracer.Raytracer;
import scene.lights.Light;
import scene.materials.Material;
import core.Intersection;
import core.Ray;
import core.colors.Color;
import core.colors.Colors;
import core.math.Direction3;
import core.math.Point3;

public class PhongMaterial implements Material
{
    private final Color ambientColor;
    private final Color diffuseColor;
    private final Color specularColor;
    private final Color reflectionColor;
    private final double cosinePower;

    public PhongMaterial(Color ambientColor,
                         Color diffuseColor,
                         Color specularColor,
                         Color reflectionColor,
                         double cosinePower)
    {
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.reflectionColor = reflectionColor;
        this.cosinePower = cosinePower;
    }

    @Override
    public Color getColor(Set<Light> lights,
                          Intersection intersection,
                          Ray cameraRay,
                          Raytracer raytracer)
    {
        Set<Color> colorsPerLight = new HashSet<>();

        for (Light light : lights)
        {
            Color color = new Color();
            Set<Ray> lightRays = light.getLightRay(intersection);
            for (Ray lightRay : light.getLightRay(intersection))
            {

                if (raytracer.isOccluded(lightRay))
                    continue;

                Direction3 surfaceNormal = intersection.getNormal();

                color.add(getAmbientTerm(light.getAmbientColor()));
                color.add(getDiffuseTerm(lightRay,
                                         surfaceNormal,
                                         light.getDiffuseColor()));
                color.add(getSpecularTerm(lightRay,
                                          surfaceNormal,
                                          cameraRay,
                                          light.getSpecularColor()));
                color.add(getReflectionTerm(cameraRay,
                                            intersection.getPosition(),
                                            surfaceNormal,
                                            raytracer));
            }
            color.divideBy(lightRays.size());
            colorsPerLight.add(color);
        }
        return Colors.sum(colorsPerLight);
    }

    private Color getAmbientTerm(Color lightColor)
    {
        return ambientColor.times(lightColor);
    }

    private Color getDiffuseTerm(Ray lightRay,
                                 Direction3 normal,
                                 Color lightColor)
    {
        if (diffuseColor.equals(Colors.BLACK))
            return Colors.BLACK;
        Direction3 lightDirection = lightRay.getDirection();
        double dotProduct = lightDirection.dot(normal);

        if (dotProduct < 0)
            return Colors.BLACK;

        return diffuseColor.times(lightColor).multiplyBy(dotProduct);
    }

    private Color getSpecularTerm(Ray lightRay,
                                  Direction3 normal,
                                  Ray cameraRay,
                                  Color lightColor)
    {
        if (specularColor.equals(Colors.BLACK))
            return Colors.BLACK;
        // cameraRay is the ray from the camera into the scene; to get the one
        // we want we need to reverse it
        Direction3 cameraDirection = cameraRay.getDirection().opposite();
        Direction3 lightDirection = lightRay.getDirection();
        Direction3 reflectedLightDirection = getReflectionVector(lightDirection,
                                                                normal);
        double dotProduct = reflectedLightDirection.dot(cameraDirection);
        double diffuseDotProduct = lightDirection.dot(normal);

        if (dotProduct < 0 || diffuseDotProduct < 0)
            return Colors.BLACK;

        return specularColor.times(lightColor).multiplyBy(Math.pow(dotProduct,
                                                              cosinePower));
    }

    private Color getReflectionTerm(Ray cameraRay,
                                    Point3 point,
                                    Direction3 normal,
                                    Raytracer raytracer)
    {
        if (reflectionColor.equals(Colors.BLACK))
            return Colors.BLACK;
        Direction3 cameraDirection = cameraRay.getDirection();
        Direction3 reflectedCameraDirection = getReflectionVector(cameraDirection,
                                                                 normal).opposite();
        Ray reflectedRay = new Ray(point, reflectedCameraDirection,
                                   cameraRay, 1e-5, Double.MAX_VALUE);
        Color reflectionColor = raytracer.traceRay(reflectedRay);
        return reflectionColor.times(this.reflectionColor);
    }

    /**
     * @param v
     * @param normal
     * @return v reflected about normal
     */
    private Direction3 getReflectionVector(Direction3 v, Direction3 normal)
    {
        return normal.times(2 * v.dot(normal)).minus(v);
    }
}
