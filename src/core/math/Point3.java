package core.math;

public class Point3 extends Vector3
{
    public static final Point3 POSITIVE_INFINITY = new Point3(Double.POSITIVE_INFINITY,
                                                            Double.POSITIVE_INFINITY,
                                                            Double.POSITIVE_INFINITY);
    public static final Point3 NEGATIVE_INFINITY = new Point3(Double.NEGATIVE_INFINITY,
                                                            Double.NEGATIVE_INFINITY,
                                                            Double.NEGATIVE_INFINITY);
    
    private double[] homogeneous;

    public Point3(double x, double y, double z)
    {
        super(x, y, z);
        homogeneous = new double[] { x, y, z, 1 };
    }
    
    public Point3(Point3 p)
    {
        this(p.x(), p.y(), p.z());
    }

    Point3(double[] vector)
    {
        super(vector);
        homogeneous = new double[] { vector[0], vector[1], vector[2], 1 };
    }

    public Point3 plus(Vector3 other)
    {
        return new Point3(VectorMath.add(vector, other.getVector()));
    }
    
    public Point3 plusEquals(Direction3 other)
    {
        VectorMath.plusEquals(vector, other.getVector());
        VectorMath.plusEquals(homogeneous, other.getVector(), MAX_INDEX);
        return this;
    }

    public Direction3 minus(Point3 other)
    {
        return new Direction3(VectorMath.subtract(vector, other.getVector()));
    }
    
    public Point3 times(double scalar)
    {
        return new Point3(vector[0] * scalar,
                         vector[1] * scalar,
                         vector[2] * scalar);
    }
    
    public double distanceTo(Point3 other)
    {
        double[] v = VectorMath.subtract(vector, other.vector);
        return VectorMath.getLength(v[0], v[1], v[2]);
    }

    @Override
    public String toString()
    {
        return String.format("Point [%f, %f, %f]",
                             vector[0],
                             vector[1],
                             vector[2]);
    }

    @Override
    protected double[] getHomogeneousForm()
    {
        return homogeneous;
    }
}
