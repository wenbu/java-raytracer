package core.math;

public class Point extends Vector
{
    public static final Point POSITIVE_INFINITY = new Point(Double.POSITIVE_INFINITY,
                                                            Double.POSITIVE_INFINITY,
                                                            Double.POSITIVE_INFINITY);
    public static final Point NEGATIVE_INFINITY = new Point(Double.NEGATIVE_INFINITY,
                                                            Double.NEGATIVE_INFINITY,
                                                            Double.NEGATIVE_INFINITY);
    private double[] homogeneous;

    public Point(double x, double y, double z)
    {
        super(x, y, z);
        homogeneous = new double[] { x, y, z, 1 };
    }

    Point(double[] vector)
    {
        super(vector);
        homogeneous = new double[] { vector[0], vector[1], vector[2], 1 };
    }

    public Point plus(Vector other)
    {
        return new Point(VectorMath.add(vector, other.getVector()));
    }
    
    public void plusEquals(Direction other)
    {
        vector[0] += other.x();
        vector[1] += other.y();
        vector[2] += other.z();
    }

    public Direction minus(Point other)
    {
        return new Direction(VectorMath.subtract(vector, other.getVector()));
    }
    
    public Point times(double scalar)
    {
        return new Point(vector[0] * scalar,
                         vector[1] * scalar,
                         vector[2] * scalar);
    }
    
    public double distanceTo(Point other)
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
