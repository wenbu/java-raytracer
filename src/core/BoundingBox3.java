package core;

import core.math.Direction;
import core.math.Point;

public class BoundingBox3
{
    private final Point pMin;
    private final Point pMax;

    public BoundingBox3(Point p1, Point p2)
    {
        this.pMin = new Point(Math.min(p1.x(), p2.x()),
                              Math.min(p1.y(), p2.y()),
                              Math.min(p1.z(), p2.z()));
        this.pMax = new Point(Math.max(p1.x(), p2.x()),
                              Math.max(p1.y(), p2.y()),
                              Math.max(p1.z(), p2.z()));
    }
    
    public Point getMin()
    {
        return pMin;
    }
    
    public Point getMax()
    {
        return pMax;
    }
    
    private Point get(int i)
    {
        if (i == 0)
        {
            return pMin;
        }
        else if (i == 1)
        {
            return pMax;
        }
        else
        {
            throw new IllegalArgumentException("Illegal input to get: " + i);
        }
    }
    
    public Point getCorner(int corner)
    {
        return new Point(get(corner & 1).x(),
                         get((corner & 2) != 0 ? 0 : 1).y(),
                         get((corner & 4) != 0 ? 0 : 1).z());
    }
    
    public BoundingBox3 union(Point p)
    {
        return new BoundingBox3(new Point(Math.min(this.pMin.x(), p.x()),
                                          Math.min(this.pMin.y(), p.y()),
                                          Math.min(this.pMin.z(), p.z())),
                                new Point(Math.max(this.pMax.x(), p.x()),
                                          Math.max(this.pMax.y(), p.y()),
                                          Math.max(this.pMax.z(), p.z())));
    }
    
    public BoundingBox3 union(BoundingBox3 other)
    {
        return new BoundingBox3(new Point(Math.min(this.pMin.x(), other.pMin.x()),
                                          Math.min(this.pMin.y(), other.pMin.y()),
                                          Math.min(this.pMin.z(), other.pMin.z())),
                                new Point(Math.max(this.pMax.x(), other.pMax.x()),
                                          Math.max(this.pMax.y(), other.pMax.y()),
                                          Math.max(this.pMax.z(), other.pMax.z())));
    }
    
    public BoundingBox3 intersect(BoundingBox3 other)
    {
        return new BoundingBox3(new Point(Math.max(this.pMin.x(), other.pMin.x()),
                                          Math.max(this.pMin.y(), other.pMin.y()),
                                          Math.max(this.pMin.z(), other.pMin.z())),
                                new Point(Math.min(this.pMax.x(), other.pMax.x()),
                                          Math.min(this.pMax.y(), other.pMax.y()),
                                          Math.min(this.pMax.z(), other.pMax.z())));
    }
    
    public boolean overlaps(BoundingBox3 other)
    {
        boolean xOverlaps = (this.pMin.x() >= other.pMin.x()) && (this.pMax.x() <= other.pMax.x());
        boolean yOverlaps = (this.pMin.y() >= other.pMin.y()) && (this.pMax.y() <= other.pMax.y());
        boolean zOverlaps = (this.pMin.z() >= other.pMin.z()) && (this.pMax.z() <= other.pMax.z());
        
        return xOverlaps && yOverlaps && zOverlaps;
    }
    
    public boolean contains(Point p)
    {
        return p.x() >= pMin.x() && p.x() <= pMax.x() && p.y() >= pMin.y() && p.y() <= pMax.y() && p.z() >= pMin.z() &&
               p.z() <= pMax.z();
    }
    
    public boolean containsExclusive(Point p)
    {
        return p.x() >= pMin.x() && p.x() < pMax.x() && p.y() >= pMin.y() && p.y() < pMax.y() && p.z() >= pMin.z() &&
                p.z() < pMax.z();
    }
    
    public BoundingBox3 expand(Direction delta)
    {
        return new BoundingBox3(pMin.plus(delta.opposite()), pMax.plus(delta));
    }
    
    public Direction diagonal()
    {
        return pMax.minus(pMin);
    }
    
    public double surfaceArea()
    {
        Direction diagonal = diagonal();
        return 2 * (diagonal.x() * diagonal.y() + diagonal.x() * diagonal.z() + diagonal.y() * diagonal.z());
    }
    
    public double volume()
    {
        Direction diagonal = diagonal();
        return diagonal.x() * diagonal.y() * diagonal.z();
    }
    
    public int maximumExtent()
    {
        Direction diagonal = diagonal();
        
        if (diagonal.x() > diagonal.y() && diagonal.x() > diagonal.z())
        {
            return 0;
        }
        else if (diagonal.y() > diagonal.z())
        {
            return 1;
        }
        else
        {
            return 2;
        }
    }
    
    public Point lerp(Point t)
    {
        throw new UnsupportedOperationException("TODO");
    }
}
