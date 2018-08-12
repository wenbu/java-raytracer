package core.math;

public class Normal2 extends Vector2
{
    private double[] homogeneous;

    public Normal2(double x, double y)
    {
        super(x, y);
        homogeneous = new double[] {x, y, 0};
    }
    
    public Normal2(Direction2 direction)
    {
        this(direction.getVector());
    }
    
    Normal2(double[] vector)
    {
        super(vector);
        homogeneous = new double[] {vector[0], vector[1], 0};
    }
    
    public static Normal2 getNormalizedNormal(Normal2 normal)
    {
        double x = normal.x();
        double y = normal.y();
        
        return getNormalizedNormal(x, y);
    }
    
    public static Normal2 getNormalizedNormal(double x, double y)
    {
        double length = VectorMath.getLength(x, y);
        return new Normal2(x / length, y / length);
    }
    
    public Normal2 opposite()
    {
        return new Normal2(VectorMath.opposite(vector));
    }
    
    public Normal2 plus(Normal2 other)
    {
        return new Normal2(VectorMath.add(vector, other.getVector()));
    }
    
    public Normal2 minus(Normal2 other)
    {
        return new Normal2(VectorMath.subtract(vector, other.getVector()));
    }

    public Normal2 times(double scalar)
    {
        return new Normal2(VectorMath.multiply(vector, scalar));
    }

    public Normal2 divide(double scalar)
    {
        return times(1.0 / scalar);
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
        return x() * x() +
               y() * y();
    }

    public String toString()
    {
        return String.format("Normal [<%f, %f>, length=%f]",
                             vector[0],
                             vector[1],
                             length());
    }
    
    @Override
    protected double[] getHomogeneousForm()
    {
        return homogeneous;
    }

}
