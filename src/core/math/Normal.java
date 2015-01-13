package core.math;

public class Normal extends Vector
{
    private double[] homogeneous;
    
    public Normal(double x, double y, double z)
    {
        super(x, y, z);
        homogeneous = new double[] {x, y, z, 0};
    }
    
    public Normal(Direction direction)
    {
        this(direction.vector);
    }
    
    Normal(double[] vector)
    {
        super(vector);
        homogeneous = new double[] {vector[0], vector[1], vector[2], 0};
    }
    
    public static Normal getNormalizedNormal(Normal normal)
    {
        double x = normal.x();
        double y = normal.y();
        double z = normal.z();
        
        return getNormalizedNormal(x, y, z);
    }
    
    public static Normal getNormalizedNormal(double x, double y, double z)
    {
        double length = VectorMath.getLength(x, y, z);
        return new Normal(x / length, y / length, z / length);
    }
    
    /**
     * @param normal
     * @param direction
     * @return normal or -normal; whichever is in the same hemisphere as
     * direction
     */
    public static Normal faceForward(Normal normal, Direction direction)
    {
        return (normal.dot(direction) < 0.0) ? normal.opposite() : normal;
    }
    
    public Normal opposite()
    {
        return new Normal(VectorMath.opposite(vector));
    }
    
    public Normal plus(Normal other)
    {
        return new Normal(VectorMath.add(vector, other.getVector()));
    }

    public Normal minus(Normal other)
    {
        return new Normal(VectorMath.subtract(vector, other.getVector()));
    }

    public Normal times(double scalar)
    {
        return new Normal(VectorMath.multiply(vector, scalar));
    }

    public Normal divide(double scalar)
    {
        return times(1.0 / scalar);
    }
    
    public double dot(Direction other)
    {
        return VectorMath.dotProduct(vector, other.vector);
    }
    
    public double dot(Normal other)
    {
        return VectorMath.dotProduct(vector, other.vector);
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

    public String toString()
    {
        return String.format("Normal [<%f, %f, %f>, length=%f]",
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
