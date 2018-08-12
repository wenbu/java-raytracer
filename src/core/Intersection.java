package core;

import scene.materials.Material;
import core.math.Direction3;
import core.math.Point3;

/**
 * An Intersection stores data related to a ray intersection.
 */
public class Intersection
{
    // distance from ray origin to intersection
    private final double distance;

    private final Point3 position;

    private final Direction3 normal;

    private final Material material;

    public Intersection(double distance,
                        Point3 position,
                        Direction3 normal,
                        Material material)
    {
        this.distance = distance;
        this.position = position;
        this.normal = normal;
        this.material = material;
    }

    public double getDistance()
    {
        return distance;
    }

    public Point3 getPosition()
    {
        return position;
    }

    public Direction3 getNormal()
    {
        return normal;
    }
    
    public Material getMaterial()
    {
        return material;
    }
}
