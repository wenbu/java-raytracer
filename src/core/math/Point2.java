package core.math;

public class Point2 extends Vector2
{
    public static final Point2 POSITIVE_INFINITY = new Point2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    public static final Point2 NEGATIVE_INFINITY = new Point2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    
    private double[] homogeneous;
    
    public Point2(double x, double y)
    {
        super(x, y);
        homogeneous = new double[] { x, y, 1 };
    }
    
    public Point2(Point2 p)
    {
        this(p.x(), p.y());
    }
    
    public Point2(Point3 p)
    {
        this(p.x(), p.y());
    }
    
    Point2(double[] vector)
    {
        super(vector);
        homogeneous = new double[] { vector[0], vector[1], 1 };
    }
    
    public Point2 plus(Vector2 other)
    {
        return new Point2(VectorMath.add(vector, other.getVector()));
    }
    
    public void plusEquals(Direction2 other)
    {
        VectorMath.plusEquals(vector, other.getVector());
        VectorMath.plusEquals(homogeneous, other.getVector(), MAX_INDEX);
    }
    
    public Direction2 minus(Point2 other)
    {
        return new Direction2(VectorMath.subtract(vector, other.getVector()));
    }
    
    public Point2 minus(Direction2 other)
    {
        return new Point2(VectorMath.subtract(vector, other.getVector()));
    }
    
    public Point2 times(double scalar)
    {
        return new Point2(vector[0] * scalar,
                          vector[1] * scalar);
    }
    
    public Point2 floor()
    {
        return new Point2(Math.floor(vector[0]), Math.floor(vector[1]));
    }
    
    public Point2 ceil()
    {
        return new Point2(Math.ceil(vector[0]), Math.ceil(vector[1]));
    }
    
    public double distanceTo(Point2 other)
    {
        double[] v = VectorMath.subtract(vector, other.getVector());
        return VectorMath.getLength(v[0], v[1]);
    }
    
    public static Point2 max(Point2 p1, Point2 p2)
    {
        return new Point2(Math.max(p1.x(), p2.x()), Math.max(p1.y(), p2.y()));
    }
    
    public static Point2 min(Point2 p1, Point2 p2)
    {
        return new Point2(Math.min(p1.x(), p2.x()), Math.min(p1.y(), p2.y()));
    }
    
    @Override
    public String toString()
    {
        return String.format("Point [%f, %f]", vector[0], vector[1]);
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof Point2))
        {
            return false;
        }
        Point2 p2 = (Point2) other;
        return this.x() == p2.x() && this.y() == p2.y();
    }
    
    @Override
    protected double[] getHomogeneousForm()
    {
        return homogeneous;
    }
}
