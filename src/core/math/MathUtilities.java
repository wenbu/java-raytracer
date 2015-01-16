package core.math;

public class MathUtilities
{
    public static double lerp(double t, double v1, double v2)
    {
        return (1.0 - t) * v1 + t * v2;
    }
    
    public static double clamp(double val, double low, double high)
    {
        if (val < low) return low;
        else if (val > high) return high;
        else return val;
    }
}
