package core.math;

public class Direction extends Vector
{
    private double[] homogeneous;
    
    public Direction(double x, double y, double z)
    {
        super(x, y, z);
        homogeneous = new double[] {x, y, z, 0};
    }
    
    public Direction(Normal normal)
    {
        this(normal.vector);
    }

    Direction(double[] vector)
    {
        super(vector);
        homogeneous = new double[] {vector[0], vector[1], vector[2], 0};
    }

    public static Direction getNormalizedDirection(Direction direction)
    {
        double x = direction.x();
        double y = direction.y();
        double z = direction.z();

        return getNormalizedDirection(x, y, z);
    }

    public static Direction getNormalizedDirection(double x, double y, double z)
    {
        double length = VectorMath.getLength(x, y, z);
        return new Direction(x / length, y / length, z / length);
    }

    public static Direction getNormalizedDirection(Point source,
                                                   Point destination)
    {
        return getNormalizedDirection(destination.x() - source.x(),
                                      destination.y() - source.y(),
                                      destination.z() - source.z());
    }

    public Direction opposite()
    {
        return new Direction(VectorMath.opposite(vector));
    }

    public Direction plus(Direction other)
    {
        return new Direction(VectorMath.add(vector, other.getVector()));
    }

    public Direction minus(Direction other)
    {
        return new Direction(VectorMath.subtract(vector, other.getVector()));
    }

    public Direction times(double scalar)
    {
        return new Direction(VectorMath.multiply(vector, scalar));
    }

    public Direction divide(double scalar)
    {
        return times(1.0 / scalar);
    }
    
    public Direction plusEquals(Direction other)
    {
        VectorMath.plusEquals(vector, other.vector);
        return this;
    }
    
    public Direction minusEquals(Direction other)
    {
        VectorMath.minusEquals(vector, other.vector);
        return this;
    }
    
    public Direction timesEquals(double scalar)
    {
        VectorMath.timesEquals(vector, scalar);
        return this;
    }
    
    public Direction divideEquals(double scalar)
    {
        VectorMath.divideEquals(vector, scalar);
        return this;
    }

    public double dot(Direction other)
    {
        return VectorMath.dotProduct(vector, other.vector);
    }
    
    public double dot(Normal other)
    {
        return VectorMath.dotProduct(vector, other.vector);
    }

    public Direction cross(Direction other)
    {
        double x1 = vector[0];
        double y1 = vector[1];
        double z1 = vector[2];
        double x2 = other.vector[0];
        double y2 = other.vector[1];
        double z2 = other.vector[2];

        return new Direction(y1 * z2 - z1 * y2,
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
    
    public Direction normalized()
    {
        return divide(length());
    }
    
    public Direction normalize()
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
