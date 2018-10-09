package core.math;

import utilities.VectorUtilities;

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
    
    public static Normal3 getNormalizedNormal(Vector3 v)
    {
        double x = v.x();
        double y = v.y();
        double z = v.z();
        
        return getNormalizedNormal(x, y, z);
    }
    
    private static Normal3 getNormalizedNormal(double x, double y, double z)
    {
        double length = VectorUtilities.getLength(x, y, z);
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
    
    public static Normal3 faceForward(Normal3 normal, Normal3 direction)
    {
        return (normal.dot(direction) < 0.0) ? normal.opposite() : normal;
    }
    
    public Normal3 opposite()
    {
        return new Normal3(VectorUtilities.opposite(vector));
    }
    
    public Normal3 plus(Normal3 other)
    {
        return new Normal3(VectorUtilities.add(vector, other.getVector()));
    }

    public Normal3 minus(Normal3 other)
    {
        return new Normal3(VectorUtilities.subtract(vector, other.getVector()));
    }

    public Normal3 times(double scalar)
    {
        return new Normal3(VectorUtilities.multiply(vector, scalar));
    }

    public Normal3 divide(double scalar)
    {
        return times(1.0 / scalar);
    }
    
    public Normal3 plusEquals(Normal3 other)
    {
        VectorUtilities.plusEquals(vector, other.getVector());
        VectorUtilities.plusEquals(homogeneous, other.getVector(), MAX_INDEX);
        return this;
    }
    
    public Normal3 minusEquals(Normal3 other)
    {
        VectorUtilities.minusEquals(vector, other.getVector());
        VectorUtilities.minusEquals(homogeneous, other.getVector(), MAX_INDEX);
        return this;
    }
    
    public Normal3 timesEquals(double scalar)
    {
        VectorUtilities.timesEquals(vector, scalar);
        VectorUtilities.timesEquals(homogeneous, scalar, MAX_INDEX);
        return this;
    }
    
    public Normal3 divideEquals(double scalar)
    {
        VectorUtilities.divideEquals(vector, scalar);
        VectorUtilities.divideEquals(homogeneous, scalar, MAX_INDEX);
        return this;
    }
    
    public double dot(Direction3 other)
    {
        return VectorUtilities.dotProduct(vector, other.vector);
    }
    
    public double dot(Normal3 other)
    {
        return VectorUtilities.dotProduct(vector, other.vector);
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
    
    public Normal3 normalized()
    {
        return divide(length());
    }
    
    public Normal3 normalize()
    {
        return divideEquals(length());
    }
    
    public Normal3 abs()
    {
        return new Normal3(VectorUtilities.abs(vector));
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
