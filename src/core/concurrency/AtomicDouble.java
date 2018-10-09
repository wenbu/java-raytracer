package core.concurrency;

public class AtomicDouble
{
    private volatile double value;
    
    public AtomicDouble()
    {
        this(0);
    }
    
    public AtomicDouble(double value)
    {
        this.value = value;
    }
    
    public synchronized void add(double x)
    {
        value += x;
    }
    
    public double getValue()
    {
        return value;
    }
}
