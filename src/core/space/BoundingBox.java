package core.space;

import core.Ray;
import core.math.Direction3;
import core.math.MathUtilities;
import core.math.Point3;
import core.math.VectorMath;
import core.tuple.Pair;

public class BoundingBox
{
    private final Point3 minPoint;
    private final Point3 maxPoint;

    public enum Axis
    {
        X, Y, Z
    }

    public BoundingBox()
    {
        minPoint = new Point3(Point3.POSITIVE_INFINITY);
        maxPoint = new Point3(Point3.NEGATIVE_INFINITY);
    }

    public BoundingBox(Point3 p)
    {
        minPoint = p;
        maxPoint = p;
    }

    public BoundingBox(Point3 p1, Point3 p2)
    {
        minPoint = new Point3(Math.min(p1.x(), p2.x()),
                             Math.min(p1.y(), p2.y()),
                             Math.min(p1.z(), p2.z()));

        maxPoint = new Point3(Math.max(p1.x(), p2.x()),
                             Math.max(p1.y(), p2.y()),
                             Math.max(p1.z(), p2.z()));
    }

    public BoundingBox(double minX,
                       double minY,
                       double minZ,
                       double maxX,
                       double maxY,
                       double maxZ)
    {
        minPoint = new Point3(minX, minY, minZ);
        maxPoint = new Point3(maxX, maxY, maxZ);
    }

    /**
     * Given a Point, return a new minimal BoundingBox that contains this one's
     * space as well as the Point.
     */
    public BoundingBox union(Point3 p)
    {
        return union(minPoint, maxPoint, p, p);
    }

    /**
     * Given another BoundingBox, return a new minimal BoundingBox that contains
     * the union of their volumes.
     */
    public BoundingBox union(BoundingBox other)
    {
        return union(minPoint, maxPoint, other.minPoint, other.maxPoint);
    }

    static BoundingBox union(Point3 minPoint1,
                             Point3 maxPoint1,
                             Point3 minPoint2,
                             Point3 maxPoint2)
    {
        return new BoundingBox(Math.min(minPoint1.x(), minPoint2.x()),
                               Math.min(minPoint1.y(), minPoint2.y()),
                               Math.min(minPoint1.z(), minPoint2.z()),
                               Math.max(maxPoint1.x(), maxPoint2.x()),
                               Math.max(maxPoint1.y(), maxPoint2.y()),
                               Math.max(maxPoint1.z(), maxPoint2.z()));
    }

    public boolean overlaps(BoundingBox other)
    {
        boolean xOverlaps = ( maxPoint.x() >= other.minPoint.x() ) &&
                            ( minPoint.x() <= other.maxPoint.x() );
        boolean yOverlaps = ( maxPoint.y() >= other.minPoint.y() ) &&
                            ( minPoint.y() <= other.maxPoint.x() );
        boolean zOverlaps = ( maxPoint.z() >= other.minPoint.z() ) &&
                            ( minPoint.z() <= other.maxPoint.z() );

        return ( xOverlaps && yOverlaps && zOverlaps );
    }

    public boolean contains(Point3 p)
    {
        return ( p.x() >= minPoint.x() && p.x() <= maxPoint.x() &&
                 p.y() >= minPoint.y() && p.y() <= maxPoint.y() &&
                 p.z() >= minPoint.z() && p.z() <= maxPoint.z() );
    }

    public void expand(double delta)
    {
        Direction3 deltaVector = new Direction3(delta, delta, delta);
        VectorMath.minusEquals(minPoint.getVector(), deltaVector.getVector());
        VectorMath.plusEquals(maxPoint.getVector(), deltaVector.getVector());
    }

    public double surfaceArea()
    {
        Direction3 diagonal = maxPoint.minus(minPoint);
        double x = diagonal.x();
        double y = diagonal.y();
        double z = diagonal.z();

        return 2.0 * ( x * y + x * z + y * z );
    }

    public double volume()
    {
        Direction3 diagonal = maxPoint.minus(minPoint);
        double x = diagonal.x();
        double y = diagonal.y();
        double z = diagonal.z();

        return x * y * z;
    }

    public Axis maximumExtent()
    {
        Direction3 diagonal = maxPoint.minus(minPoint);
        if (diagonal.x() > diagonal.y() && diagonal.x() > diagonal.z())
            return Axis.X;
        else if (diagonal.y() > diagonal.z())
            return Axis.Y;
        else
            return Axis.Z;
    }

    /**
     * @param p
     *            a Point in the domain [0,1]^3
     */
    public Point3 lerp(Point3 p)
    {
        return lerp(p.x(), p.y(), p.z());
    }

    public Point3 lerp(double tx, double ty, double tz)
    {
        return new Point3(MathUtilities.lerp(tx, minPoint.x(), maxPoint.x()),
                         MathUtilities.lerp(ty, minPoint.y(), maxPoint.y()),
                         MathUtilities.lerp(tz, minPoint.z(), maxPoint.z()));
    }

    public Point3 offset(Point3 p)
    {
        return offset(p.x(), p.y(), p.z());
    }

    public Point3 offset(double tx, double ty, double tz)
    {
        return new Point3(( tx - minPoint.x() ) / ( maxPoint.x() - minPoint.x() ),
                         ( ty - minPoint.y() ) / ( maxPoint.y() - minPoint.y() ),
                         ( tz - minPoint.z() ) / ( maxPoint.z() - minPoint.z() ));
    }

    public BoundingSphere boundingSphere()
    {
        Point3 center = minPoint.times(0.5).plus(maxPoint.times(0.5));
        double radius = contains(center) ? center.distanceTo(maxPoint) : 0;
        return new BoundingSphere(center, radius);
    }

    public Point3 getMinPoint()
    {
        return minPoint;
    }

    public Point3 getMaxPoint()
    {
        return maxPoint;
    }
    
    /**
     * Perform an intersection test against the specified ray.
     * 
     * @param ray
     * @return a pair (t0, t1) containing the intersection's parametric range,
     *         or null if there is no intersection.
     */
    public Pair<Double, Double> intersect(Ray ray)
    {
        double t0 = 0;
        double t1 = ray.getMaxT();
        
        for (int i = 0; i < 3; i++)
        {
            double inverseRayDirection = 1/ray.getDirection().get(i);
            double tNear = (minPoint.get(i) - ray.getOrigin().get(i)) * inverseRayDirection;
            double tFar = (maxPoint.get(i) - ray.getOrigin().get(i)) * inverseRayDirection;
            
            if (tNear > tFar)
            {
                double temp = tNear;
                tNear = tFar;
                tFar = temp;
            }
            
            t0 = tNear > t0 ? tNear : t0;
            t1 = tFar < t1 ? tFar : t1;
            
            if (t0 > t1)
            {
                return null;
            }
        }
        
        return new Pair<>(t0, t1);
    }
    
    /**
     * Perform an intersection test, with some precomputed values.
     * @param ray
     * @param inverseDirection 1/ray.direction
     * @param dirIsNegative 1 if direction is negative in that axis, 0 otherwise
     * @return
     */
    public Pair<Double, Double> intersect(Ray ray, Direction3 inverseDirection, int[] dirIsNegative)
    {
        // TODO
        return null;
    }
}
