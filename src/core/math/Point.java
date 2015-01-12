package core.math;


public class Point extends Vector
{
    private double[] homogeneous;
    
    public Point(double x, double y, double z)
    {
        super(x, y, z);
        homogeneous = new double[] {x, y, z, 1};
    }
    
    Point(double[] vector)
    {
        super(vector);
        homogeneous = new double[] {vector[0], vector[1], vector[2], 1};
    }
    
    public Point plus(Direction other)
    {
        return new Point(VectorMath.add(vector, other.getVector()));
    }
    
    public Direction minus(Point other)
    {
        return new Direction(VectorMath.subtract(vector, other.getVector()));
    }
    
    @Override public String toString()
    {
        return String.format("Point [%f, %f, %f]", vector[0], vector[1], vector[2]);
    }

    @Override
    protected double[] getHomogeneousForm()
    {
        return homogeneous;
    }
}
