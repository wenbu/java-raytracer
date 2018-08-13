package core.math;

import core.tuple.Pair;

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
    
    public static Pair<Double, Double> quadratic(double a, double b, double c)
    {
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0)
        {
            return null;
        }

        double discriminantRoot = Math.sqrt(discriminant);
        double q;
        if (b < 0)
        {
            q = -0.5 * (b - discriminantRoot);
        }
        else
        {
            q = -0.5 * (b + discriminantRoot);
        }

        double t0 = q / a;
        double t1 = c / q;
        return new Pair<>(Math.min(t0, t1), Math.max(t0, t1));
    }
}
