package core.math;

public class Direction2 extends Vector2
{
    private double[] homogeneous;
    
    public Direction2(double x, double y)
    {
        super(x, y);
        homogeneous = new double[] {x, y, 0};
    }
    
    public Direction2(Normal2 normal)
    {
        this(normal.vector);
    }
    
    Direction2(double[] vector)
    {
        super(vector);
        homogeneous = new double[] {vector[0], vector[1], 0};
    }
    
    public static Direction2 getNormalizedDirection(Direction2 direction)
    {
        double x = direction.x();
        double y = direction.y();
        
        return getNormalizedDirection(x, y);
    }
    
    public static Direction2 getNormalizedDirection(double x, double y)
    {
        double length = VectorMath.getLength(x, y);
        return new Direction2(x / length, y / length);
    }
    
    public static Direction2 getNormalizedDirection(Point2 source, Point2 destination)
    {
        return getNormalizedDirection(destination.x() - source.x(), destination.y() - source.y());
    }
    
    public Direction2 opposite()
    {
        return new Direction2(VectorMath.opposite(vector));
    }
    
    public Direction2 plus(Direction2 other)
    {
        return new Direction2(VectorMath.add(vector, other.getVector()));
    }
    
    public Direction2 minus(Direction2 other)
    {
        return new Direction2(VectorMath.subtract(vector, other.getVector()));
    }
    
    public Direction2 times(double scalar)
    {
        return new Direction2(VectorMath.multiply(vector, scalar));
    }
    
    public Direction2 divide(double scalar)
    {
        return new Direction2(VectorMath.divide(vector, scalar));
    }

    public Direction2 plusEquals(Direction2 other)
    {
        VectorMath.plusEquals(vector, other.getVector());
        return this;
    }
    
    public Direction2 minusEquals(Direction2 other)
    {
        VectorMath.minusEquals(vector, other.getVector());
        return this;
    }
    
    public Direction2 timesEquals(double scalar)
    {
        VectorMath.timesEquals(vector, scalar);
        return this;
    }
    
    public Direction2 divideEquals(double scalar)
    {
        VectorMath.divideEquals(vector, scalar);
        return this;
    }
    
    public double dot(Direction2 other)
    {
        return VectorMath.dotProduct(vector, other.getVector());
    }
    
    public double dot(Normal2 other)
    {
        return VectorMath.dotProduct(vector, other.getVector());
    }
    
    public double length()
    {
        return Math.sqrt(lengthSquared());
    }
    
    public double lengthSquared()
    {
        return x() * x() + y() * y();
    }
    
    public Direction2 normalized()
    {
        return divide(length());
    }
    
    public Direction2 normalize()
    {
        return divideEquals(length());
    }
    
    public String toString()
    {
        return String.format("Direction [<%f, %f>, length=%f]", vector[0], vector[1], length());
    }
    
    @Override
    protected double[] getHomogeneousForm()
    {
        return homogeneous;
    }

}
