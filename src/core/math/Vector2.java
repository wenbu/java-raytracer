package core.math;

public abstract class Vector2
{
    protected final double[] vector;
    
    protected Vector2()
    {
        this.vector = new double[2];
    }
    
    protected Vector2(double[] vector)
    {
        this();
        for (int i = 0; i < 2; i++)
        {
            this.vector[i] = vector[i];
        }
    }
    
    protected Vector2(double x, double y)
    {
        this();
        this.vector[0] = x;
        this.vector[1] = y;
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
    
    public double get(int index)
    {
        switch(index)
        {
            case 0:
            case 1:
                return vector[index];
            default:
                throw new IllegalArgumentException("Index " + index + " is out of range. Valid values: (0,1)");    
        }
    }
    
    protected abstract double[] getHomogeneousForm();
}
