package core.math;

public class Normal3 extends Vector3
{
    private double[] homogeneous;
    
    public Normal3(double x, double y, double z)
    {
        super(x, y, z);
        homogeneous = new double[] {x, y, z, 0};
    }
    
    public Normal3(Direction3 direction)
    {
        this(direction.getVector());
    }
    
    Normal3(double[] vector)
    {
        super(vector);
        homogeneous = new double[] {vector[0], vector[1], vector[2], 0};
    }
    
    public static Normal3 getNormalizedNormal(Normal3 normal)
    {
        double x = normal.x();
        double y = normal.y();
        double z = normal.z();
        
        return getNormalizedNormal(x, y, z);
    }
    
    public static Normal3 getNormalizedNormal(double x, double y, double z)
    {
        double length = VectorMath.getLength(x, y, z);
        return new Normal3(x / length, y / length, z / length);
    }
    
    /**
     * @param normal
     * @param direction
     * @return normal or -normal; whichever is in the same hemisphere as
     * direction
     */
    public static Normal3 faceForward(Normal3 normal, Direction3 direction)
    {
        return (normal.dot(direction) < 0.0) ? normal.opposite() : normal;
    }
    
    public Normal3 opposite()
    {
        return new Normal3(VectorMath.opposite(vector));
    }
    
    public Normal3 plus(Normal3 other)
    {
        return new Normal3(VectorMath.add(vector, other.getVector()));
    }

    public Normal3 minus(Normal3 other)
    {
        return new Normal3(VectorMath.subtract(vector, other.getVector()));
    }

    public Normal3 times(double scalar)
    {
        return new Normal3(VectorMath.multiply(vector, scalar));
    }

    public Normal3 divide(double scalar)
    {
        return times(1.0 / scalar);
    }
    
    public double dot(Direction3 other)
    {
        return VectorMath.dotProduct(vector, other.vector);
    }
    
    public double dot(Normal3 other)
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
