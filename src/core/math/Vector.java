package core.math;


public abstract class Vector
{
    protected final double[] vector;
    
    protected Vector()
    {
        this.vector = new double[3];
    }
    
    protected Vector(double[] vector)
    {
        this();
        for (int i = 0; i < 3; i++)
        {
            this.vector[i] = vector[i];
        }
    }
    
    protected Vector(double x, double y, double z)
    {
        this();
        this.vector[0] = x;
        this.vector[1] = y;
        this.vector[2] = z;
    }
    
    double[] getVector()
    {
        return vector;
    }
    
    public double x()
    {
        return vector[0];
    }
    
    public double y()
    {
        return vector[1];
    }
    
    public double z()
    {
        return vector[2];
    }
    
    protected abstract double[] getHomogeneousForm();
}
