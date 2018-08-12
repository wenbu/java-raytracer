package core.math;

public class Direction3 extends Vector3
{
    private double[] homogeneous;
    
    public Direction3(double x, double y, double z)
    {
        super(x, y, z);
        homogeneous = new double[] {x, y, z, 0};
    }
    
    public Direction3(Normal3 normal)
    {
        this(normal.vector);
    }

    Direction3(double[] vector)
    {
        super(vector);
        homogeneous = new double[] {vector[0], vector[1], vector[2], 0};
    }

    public static Direction3 getNormalizedDirection(Direction3 direction)
    {
        double x = direction.x();
        double y = direction.y();
        double z = direction.z();

        return getNormalizedDirection(x, y, z);
    }

    public static Direction3 getNormalizedDirection(double x, double y, double z)
    {
        double length = VectorMath.getLength(x, y, z);
        return new Direction3(x / length, y / length, z / length);
    }

    public static Direction3 getNormalizedDirection(Point3 source,
                                                   Point3 destination)
    {
        return getNormalizedDirection(destination.x() - source.x(),
                                      destination.y() - source.y(),
                                      destination.z() - source.z());
    }

    public Direction3 opposite()
    {
        return new Direction3(VectorMath.opposite(vector));
    }

    public Direction3 plus(Direction3 other)
    {
        return new Direction3(VectorMath.add(vector, other.getVector()));
    }

    public Direction3 minus(Direction3 other)
    {
        return new Direction3(VectorMath.subtract(vector, other.getVector()));
    }

    public Direction3 times(double scalar)
    {
        return new Direction3(VectorMath.multiply(vector, scalar));
    }

    public Direction3 divide(double scalar)
    {
        return new Direction3(VectorMath.divide(vector, scalar));
    }
    
    public Direction3 plusEquals(Direction3 other)
    {
        VectorMath.plusEquals(vector, other.getVector());
        VectorMath.plusEquals(homogeneous, other.getVector(), MAX_INDEX);
        return this;
    }
    
    public Direction3 minusEquals(Direction3 other)
    {
        VectorMath.minusEquals(vector, other.getVector());
        VectorMath.minusEquals(homogeneous, other.getVector(), MAX_INDEX);
        return this;
    }
    
    public Direction3 timesEquals(double scalar)
    {
        VectorMath.timesEquals(vector, scalar);
        VectorMath.timesEquals(homogeneous, scalar, MAX_INDEX);
        return this;
    }
    
    public Direction3 divideEquals(double scalar)
    {
        VectorMath.divideEquals(vector, scalar);
        VectorMath.divideEquals(homogeneous, scalar, MAX_INDEX);
        return this;
    }

    public double dot(Direction3 other)
    {
        return VectorMath.dotProduct(vector, other.vector);
    }
    
    public double dot(Normal3 other)
    {
        return VectorMath.dotProduct(vector, other.vector);
    }

    public Direction3 cross(Direction3 other)
    {
        double x1 = vector[0];
        double y1 = vector[1];
        double z1 = vector[2];
        double x2 = other.vector[0];
        double y2 = other.vector[1];
        double z2 = other.vector[2];

        return new Direction3(y1 * z2 - z1 * y2,
                             z1 * x2 - x1 * z2,
                             x1 * y2 - y1 * x2);
    }

    public double length()
    {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared()
    {
        return x() * x() +
               y() * y() +
               z() * z();
    }
    
    public Direction3 normalized()
    {
        return divide(length());
    }
    
    public Direction3 normalize()
    {
        return divideEquals(length());
    }

    public String toString()
    {
        return String.format("Direction [<%f, %f, %f>, length=%f]",
                             vector[0],
                             vector[1],
                             vector[2],
                             length());
    }

    @Override
    protected double[] getHomogeneousForm()
    {
        return homogeneous;
    }
}
