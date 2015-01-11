package scene.primitives.impl;

import scene.materials.Material;
import scene.primitives.Primitive;
import core.Intersection;
import core.Ray;
import core.math.Direction;
import core.math.Point;
import core.math.VectorMath;

public class Sphere implements Primitive
{
    private final Point center;
    private final double radius;
    private final Material material;

    public Sphere(Point center, double radius, Material material)
    {
        this.center = center;
        this.radius = radius;
        this.material = material;
    }

    @Override
    public Intersection getIntersection(Ray ray)
    {
        Point rayOrigin = ray.getOrigin();
        Direction rayDirection = ray.getDirection();

        Direction positionDifference = rayOrigin.minus(center);

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

        Point intersectionPoint = rayOrigin.plus(rayDirection.times(closer));

        Direction normal = VectorMath.normalized(intersectionPoint.minus(center));

        return new Intersection(closer, intersectionPoint, normal, material);
    }

}
