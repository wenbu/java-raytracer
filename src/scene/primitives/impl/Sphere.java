package scene.primitives.impl;

import scene.materials.Material;
import scene.primitives.Primitive;
import core.Intersection;
import core.Ray;
import core.math.Direction3;
import core.math.Point3;

public class Sphere implements Primitive
{
    private final Point3 center;
    private final double radius;
    private final Material material;

    public Sphere(Point3 center, double radius, Material material)
    {
        this.center = center;
        this.radius = radius;
        this.material = material;
    }

    @Override
    public Intersection getIntersection(Ray ray)
    {
        Point3 rayOrigin = ray.getOrigin();
        Direction3 rayDirection = ray.getDirection();

        Direction3 positionDifference = rayOrigin.minus(center);

        double a = rayDirection.dot(rayDirection);
        double b = rayDirection.times(2).dot(positionDifference);
        double c = positionDifference.dot(positionDifference)
                - (radius * radius);

        double discriminant = (b * b) - (4 * a * c);
        if (discriminant < 0)
        {   
            return null;
        }
        double sqrtDiscriminant = Math.sqrt(discriminant);

        double t1 = ((-b) - sqrtDiscriminant) / (2 * a);
        double t2 = ((-b) + sqrtDiscriminant) / (2 * a);

        double closer = t1 < t2 ? t1 : t2;

        Point3 intersectionPoint = rayOrigin.plus(rayDirection.times(closer));

        Direction3 normal = Direction3.getNormalizedDirection(intersectionPoint.minus(center));

        return new Intersection(closer, intersectionPoint, normal, material);
    }

}
