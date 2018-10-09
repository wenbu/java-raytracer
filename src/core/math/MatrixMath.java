package core.math;

import core.tuple.Pair;

public class MatrixMath
{
    public static Pair<Double, Double> solveLinearSystem2x2(double a[][], double b[])
    {
        if (a.length != 2)
        {
            throw new IllegalArgumentException("a.length = " + a.length + "; must be 2");
        }
        if (a[0].length != 2)
        {
            throw new IllegalArgumentException("a[0].length = " + a[0].length + "; must be 2");
        }
        if (b.length != 2)
        {
            throw new IllegalArgumentException("b.length = 2" + b.length + "; must be 2");
        }
        
        double determinant = VectorMath.get2x2Determinant(a);
        if (Math.abs(determinant) < 1e-10)
        {
            return null;
        }
        
        double x0 = (a[1][1] * b[0] - a[0][1] * b[1]) / determinant;
        double x1 = (a[0][0] * b[1] - a[1][0] * b[0]) / determinant;
        
        if (x0 == Double.NaN || x1 == Double.NaN)
        {
            return null;
        }
        
        return new Pair<>(x0, x1);
    }
}
