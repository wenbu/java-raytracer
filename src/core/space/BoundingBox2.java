package core.space;

import java.util.Iterator;
import java.util.NoSuchElementException;

import core.math.Direction2;
import core.math.Point2;

public class BoundingBox2 implements Iterable<Point2>
{
    private final Point2 minPoint;
    private final Point2 maxPoint;
    
    public BoundingBox2()
    {
        minPoint = new Point2(Point2.POSITIVE_INFINITY);
        maxPoint = new Point2(Point2.NEGATIVE_INFINITY);
    }
    
    public BoundingBox2(Point2 p)
    {
        minPoint = p;
        maxPoint = p;
    }
    
    public BoundingBox2(Point2 p1, Point2 p2)
    {
        minPoint = new Point2(Math.min(p1.x(), p2.x()),
                              Math.min(p1.y(), p2.y()));
        maxPoint = new Point2(Math.max(p1.x(), p2.x()),
                              Math.max(p1.y(), p2.y()));
    }
    
    public BoundingBox2(double minX, double minY, double maxX, double maxY)
    {
        minPoint = new Point2(minX, minY);
        maxPoint = new Point2(maxX, maxY);
    }
    
    public BoundingBox2(BoundingBox2 other)
    {
        minPoint = new Point2(other.minPoint);
        maxPoint = new Point2(other.maxPoint);
    }
    
    public Point2 get(int index)
    {
        if (index == 0)
        {
            return minPoint;
        }
        else if (index == 1)
        {
            return maxPoint;
        }
        else
        {
            throw new IllegalArgumentException("Provided index " + index + " out of range. Value values: 0, 1");
        }
    }
    
    public BoundingBox2 union(Point2 p)
    {
        return union(minPoint, maxPoint, p, p);
    }
    
    public BoundingBox2 union(BoundingBox2 other)
    {
        return union(minPoint, maxPoint, other.minPoint, other.maxPoint);
    }
    
    static BoundingBox2 union(Point2 minPoint1, Point2 maxPoint1, Point2 minPoint2, Point2 maxPoint2)
    {
       return new BoundingBox2(Math.min(minPoint1.x(), maxPoint1.x()),
                               Math.min(minPoint1.y(), maxPoint1.y()),
                               Math.max(minPoint2.x(), maxPoint2.x()),
                               Math.max(minPoint2.y(), maxPoint2.y()));   
    }
    
    public BoundingBox2 intersect(BoundingBox2 other)
    {
        Point2 pMin = Point2.max(minPoint, other.minPoint);
        Point2 pMax = Point2.min(maxPoint, other.maxPoint);
        return new BoundingBox2(pMin.x(), pMin.y(), pMax.x(), pMax.y());
    }
    
    public boolean overlaps(BoundingBox2 other)
    {
        boolean xOverlaps = (maxPoint.x() >= other.minPoint.x()) &&
                            (minPoint.x() <= other.maxPoint.x());
        boolean yOverlaps = (maxPoint.y() >= other.minPoint.y()) &&
                            (minPoint.y() <= other.maxPoint.x());
        return xOverlaps && yOverlaps;
    }
    
    public boolean contains(Point2 p)
    {
        return ( p.x() >= minPoint.x() && p.x() <= maxPoint.x() &&
                 p.y() >= minPoint.y() && p.y() <= maxPoint.y());
    }
    
    public boolean containsExclusive(Point2 p)
    {
        return ( p.x() >= minPoint.x() && p.x() < maxPoint.x() &&
                 p.y() >= minPoint.y() && p.y() < maxPoint.y());
    }
    
    public Direction2 diagonal()
    {
        return maxPoint.minus(minPoint);
    }
    
    public double area()
    {
        Direction2 diagonal = diagonal();
        return diagonal.x() * diagonal.y();
    }
    
    public int integerArea()
    {
        int intMinX = (int) minPoint.x();
        if (intMinX < minPoint.x())
        {
            intMinX += 1;
        }
        int intMinY = (int) minPoint.y();
        if (intMinY < minPoint.y())
        {
            intMinY += 1;
        }
        int intMaxX = (int) maxPoint.x();
        if (intMaxX < maxPoint.x())
        {
            intMaxX += 1;
        }
        int intMaxY = (int) maxPoint.y();
        if (intMaxY < maxPoint.y())
        {
            intMaxY += 1;
        }
        return (intMaxX - intMinX) * (intMaxY - intMinY);
    }
    
    public int maximumExtent()
    {
        Direction2 diagonal = diagonal();
        if (diagonal.x() > diagonal.y())
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }
    
    @Override
    public String toString()
    {
        return "BoundingBox2[" + minPoint + ", " + maxPoint + "]";
    }

    /**
     * Returns an iterator that iterates over all integral Point2s contained in
     * this BoundingBox2.
     */
    @Override
    public Iterator<Point2> iterator()
    {
        return new BoundingBoxIterator(this);
    }
    
    // Not thread-safe.
    private static class BoundingBoxIterator implements Iterator<Point2>
    {
        private final BoundingBox2 bounds;
        private final int initialX;
        private int curX;
        private int curY;
        
        public BoundingBoxIterator(BoundingBox2 bounds)
        {
            this.bounds = bounds;
            
            double actualStartX = bounds.minPoint.x();
            curX = (int) actualStartX;
            if (curX < actualStartX)
            {
                curX += 1;
            }
            initialX = curX;
            // decrement here so the increment in next() returns the
            // right value
            curX -= 1;
            
            double actualStartY = bounds.minPoint.y();
            curY = (int) actualStartY;
            if (curY < actualStartY)
            {
                curY += 1;
            }
        }
        
        @Override
        public boolean hasNext()
        {
            if (curY + 1 < bounds.maxPoint.y())
            {
                return true;
            }
            if (curX + 1 < bounds.maxPoint.x())
            {
                return true;
            }
            return false;
        }

        @Override
        public Point2 next()
        {
            curX += 1;
            if (curX >= bounds.maxPoint.x())
            {
                curX = initialX;
                curY += 1;
                if (curY >= bounds.maxPoint.y())
                {
                    throw new NoSuchElementException();
                }
            }
            return new Point2(curX, curY);
        }
        
    }
}
