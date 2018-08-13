package core.math;


public abstract class Vector3
{
    protected static final int MAX_INDEX = 2;
    
    protected final double[] vector;
    
    protected Vector3()
    {
        this.vector = new double[3];
    }
    
    protected Vector3(double[] vector)
    {
        this();
        for (int i = 0; i < 3; i++)
        {
            this.vector[i] = vector[i];
        }
    }
    
    protected Vector3(double x, double y, double z)
    {
        this();
        this.vector[0] = x;
        this.vector[1] = y;
        this.vector[2] = z;
    }
    
    public double[] getVector()
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
    
    public double get(int index)
    {
        switch (index)
        {
            case 0:
            case 1:
            case 2:
                return vector[index];
            default:
                throw new IllegalArgumentException("Index " + index +
                                                   " is out of range. Valid values: (0,2)");
        }
    }
    
    protected abstract double[] getHomogeneousForm();
}