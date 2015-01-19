package core.space;

import core.math.Direction;
import core.math.MathUtilities;
import core.math.Point;
import core.math.VectorMath;

public class BoundingBox
{
    private final Point minPoint;
    private final Point maxPoint;

    public enum Axis
    {
        X, Y, Z
    }

    public BoundingBox()
    {
        minPoint = new Point(Point.POSITIVE_INFINITY);
        maxPoint = new Point(Point.NEGATIVE_INFINITY);
    }

    public BoundingBox(Point p)
    {
        minPoint = p;
        maxPoint = p;
    }

    public BoundingBox(Point p1, Point p2)
    {
        minPoint = new Point(Math.min(p1.x(), p2.x()),
                             Math.min(p1.y(), p2.y()),
                             Math.min(p1.z(), p2.z()));

        maxPoint = new Point(Math.max(p1.x(), p2.x()),
                             Math.max(p1.y(), p2.y()),
                             Math.max(p1.z(), p2.z()));
    }

    BoundingBox(double minX,
                double minY,
                double minZ,
                double maxX,
                double maxY,
                double maxZ)
    {
        minPoint = new Point(minX, minY, minZ);
        maxPoint = new Point(maxX, maxY, maxZ);
    }

    /**
     * Given a Point, return a new minimal BoundingBox that contains this one's
     * space as well as the Point.
     */
    public BoundingBox union(Point p)
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

    static BoundingBox union(Point minPoint1,
                             Point maxPoint1,
                             Point minPoint2,
                             Point maxPoint2)
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

    public boolean contains(Point p)
    {
        return ( p.x() >= minPoint.x() && p.x() <= maxPoint.x() &&
                 p.y() >= minPoint.y() && p.y() <= maxPoint.y() &&
                 p.z() >= minPoint.z() && p.z() <= maxPoint.z() );
    }

    public void expand(double delta)
    {
        Direction deltaVector = new Direction(delta, delta, delta);
        VectorMath.minusEquals(minPoint.getVector(), deltaVector.getVector());
        VectorMath.plusEquals(maxPoint.getVector(), deltaVector.getVector());
    }

    public double surfaceArea()
    {
        Direction diagonal = maxPoint.minus(minPoint);
        double x = diagonal.x();
        double y = diagonal.y();
        double z = diagonal.z();

        return 2.0 * ( x * y + x * z + y * z );
    }

    public double volume()
    {
        Direction diagonal = maxPoint.minus(minPoint);
        double x = diagonal.x();
        double y = diagonal.y();
        double z = diagonal.z();

        return x * y * z;
    }

    public Axis maximumExtent()
    {
        Direction diagonal = maxPoint.minus(minPoint);
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
    public Point lerp(Point p)
    {
        return lerp(p.x(), p.y(), p.z());
    }

    public Point lerp(double tx, double ty, double tz)
    {
        return new Point(MathUtilities.lerp(tx, minPoint.x(), maxPoint.x()),
                         MathUtilities.lerp(ty, minPoint.y(), maxPoint.y()),
                         MathUtilities.lerp(tz, minPoint.z(), maxPoint.z()));
    }

    public Point offset(Point p)
    {
        return offset(p.x(), p.y(), p.z());
    }

    public Point offset(double tx, double ty, double tz)
    {
        return new Point(( tx - minPoint.x() ) / ( maxPoint.x() - minPoint.x() ),
                         ( ty - minPoint.y() ) / ( maxPoint.y() - minPoint.y() ),
                         ( tz - minPoint.z() ) / ( maxPoint.z() - minPoint.z() ));
    }

    public BoundingSphere boundingSphere()
    {
        Point center = minPoint.times(0.5).plus(maxPoint.times(0.5));
        double radius = contains(center) ? center.distanceTo(maxPoint) : 0;
        return new BoundingSphere(center, radius);
    }

    public Point getMinPoint()
    {
        return minPoint;
    }

    public Point getMaxPoint()
    {
        return maxPoint;
    }
}
