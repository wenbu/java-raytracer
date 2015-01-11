package core;

import scene.materials.Material;
import core.math.Direction;
import core.math.Point;

/**
 * An Intersection stores data related to a ray intersection.
 */
public class Intersection
{
    // distance from ray origin to intersection
    private final double distance;

    private final Point position;

    private final Direction normal;

    private final Material material;

    public Intersection(double distance,
                        Point position,
                        Direction normal,
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

    public Point getPosition()
    {
        return position;
    }

    public Direction getNormal()
    {
        return normal;
    }
    
    public Material getMaterial()
    {
        return material;
    }
}
