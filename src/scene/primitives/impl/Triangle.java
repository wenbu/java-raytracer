package scene.primitives.impl;

import core.Intersection;
import core.Ray;
import core.math.Direction;
import core.math.Point;
import scene.materials.Material;
import scene.primitives.Primitive;

public class Triangle implements Primitive
{
    private final Point v1;
    private final Direction normal;
    private final Material material;

    private final Direction e1;
    private final Direction e2;

    private final boolean isDoubleSided;

    // TODO: configurable?
    private static final double EPSILON = 1e-5;

    public Triangle(Point v1, Point v2, Point v3, Material material)
    {
        this(v1, v2, v3, material, false);
    }

    public Triangle(Point v1,
                    Point v2,
                    Point v3,
                    Material material,
                    boolean doubleSided)
    {
        this.v1 = v1;
        this.material = material;

        e1 = v2.minus(v1);
        e2 = v3.minus(v1);

        normal = Direction.getNormalizedDirection(e1.cross(e2));

        this.isDoubleSided = doubleSided;
    }

    /*
     * http://www.cs.virginia.edu/~gfx/Courses/2003/ImageSynthesis/papers/
     * Acceleration/Fast%20MinimumStorage%20RayTriangle%20Intersection.pdf
     * 
     * @see scene.primitives.Primitive#getIntersection(core.Ray)
     */
    @Override
    public Intersection getIntersection(Ray ray)
    {
        Point rayOrigin = ray.getOrigin();
        Direction rayDirection = ray.getDirection();

        // begin calculating determinant; also used for U parameter
        Direction pvec = rayDirection.cross(e2);

        // if determinant ~= 0, ray lies in plane of triangle
        double determinant = e1.dot(pvec);

        double t = 0;
        if (this.isDoubleSided)
        {
            if (determinant > -EPSILON && determinant < EPSILON)
                return null;

            double inverseDeterminant = 1.0 / determinant;

            Direction tvec = rayOrigin.minus(v1);
            double u = tvec.dot(pvec) * inverseDeterminant;

            if (u < 0 || u > 1)
                return null;

            Direction qvec = tvec.cross(e1);
            double v = rayDirection.dot(qvec) * inverseDeterminant;
            if (v < 0 || u + v > 1)
                return null;

            t = e2.dot(qvec) * inverseDeterminant;
        }
        else
        {
            if (determinant < EPSILON)
                return null;

            Direction tvec = rayOrigin.minus(v1);
            double u = tvec.dot(pvec);
            if (u < 0 || u > determinant)
                return null;

            Direction qvec = tvec.cross(e1);
            double v = rayDirection.dot(qvec);

            if (v < 0 || u + v > determinant)
                return null;

            t = e2.dot(qvec) / determinant;
        }
        Point intersectionPoint = rayOrigin.plus(rayDirection.times(t));
        return new Intersection(t, intersectionPoint, normal, material);
    }

}
