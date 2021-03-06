package core.math;

import utilities.VectorUtilities;

public class Point3 extends Vector3
{
    public static final Point3 POSITIVE_INFINITY = new Point3(Double.POSITIVE_INFINITY,
                                                            Double.POSITIVE_INFINITY,
                                                            Double.POSITIVE_INFINITY);
    public static final Point3 NEGATIVE_INFINITY = new Point3(Double.NEGATIVE_INFINITY,
                                                            Double.NEGATIVE_INFINITY,
                                                            Double.NEGATIVE_INFINITY);
    
    private double[] homogeneous;

    public Point3()
    {
        this(0, 0, 0);
    }
    
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
        return new Point3(VectorUtilities.add(vector, other.getVector()));
    }
    
    public Point3 plusEquals(Direction3 other)
    {
        VectorUtilities.plusEquals(vector, other.getVector());
        VectorUtilities.plusEquals(homogeneous, other.getVector(), MAX_INDEX);
        return this;
    }

    public Direction3 minus(Point3 other)
    {
        return new Direction3(VectorUtilities.subtract(vector, other.getVector()));
    }
    
    public Point3 minus(Direction3 other)
    {
        return new Point3(VectorUtilities.subtract(vector, other.getVector()));
    }
    
    public Point3 times(double scalar)
    {
        return new Point3(vector[0] * scalar,
                          vector[1] * scalar,
                          vector[2] * scalar);
    }
    
    public Point3 timesEquals(double scalar)
    {
        VectorUtilities.timesEquals(this.getVector(), scalar);
        VectorUtilities.timesEquals(this.getVector(), scalar, MAX_INDEX);
        return this;
    }
    
    public Point3 divideBy(double scalar)
    {
        return new Point3(vector[0] / scalar,
                          vector[1] / scalar,
                          vector[2] / scalar);
    }
    
    public Point3 abs()
    {
        return new Point3(VectorUtilities.abs(vector));
    }
    
    public double distanceSquared(Point3 other)
    {
        return (this.minus(other)).lengthSquared();
    }
    
    public double distanceTo(Point3 other)
    {
        double[] v = VectorUtilities.subtract(vector, other.vector);
        return VectorUtilities.getLength(v[0], v[1], v[2]);
    }
    
    public Point3 permute(int x, int y, int z)
    {
        return new Point3(VectorUtilities.permute(vector, x, y, z));
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
