package core.math;


public class VectorMath
{
    public static Direction normalized(Direction v)
    {
        return v.divide(getLength(v));
    }

    public static double getLength(Direction v)
    {
        double[] vector = v.getVector();

        double x = vector[0];
        double y = vector[1];
        double z = vector[2];

        return getLength(x, y, z);
    }

    static double getLength(double x, double y, double z)
    {
        return Math.sqrt(x * x + y * y + z * z);
    }

    static double dotProduct(double[] v1, double[] v2)
    {
        if (v1.length != 4 || v2.length != 4)
            throw new IllegalArgumentException("array size is not 4");

        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2] + v1[3] * v2[3];
    }

    /*
     * Copy initializers
     */

    static double[][] copy(double[][] m)
    {
        double[][] copy = new double[m.length][];
        for (int i = 0; i < m.length; i++)
        {
            copy[i] = m[i].clone();
        }
        return copy;
    }

    /*
     * Operators that return a new double[].
     */

    static double[] add(double[] v1, double[] v2)
    {
        if (v1.length != 4 || v2.length != 4)
            throw new IllegalArgumentException("array size is not 4");

        double[] sum = new double[4];
        for (int i = 0; i < 4; i++)
        {
            sum[i] = v1[i] + v2[i];
        }

        return sum;
    }

    static double[] subtract(double[] v1, double[] v2)
    {
        if (v1.length != 4 || v2.length != 4)
            throw new IllegalArgumentException("array size is not 4");

        double[] difference = new double[4];
        for (int i = 0; i < 4; i++)
        {
            difference[i] = v1[i] - v2[i];
        }

        return difference;
    }

    static double[] opposite(double[] v)
    {
        if (v.length != 4)
            throw new IllegalArgumentException("array size is not 4");

        double[] opposite = new double[4];
        for (int i = 0; i < 4; i++)
        {
            opposite[i] = -v[i];
        }

        return opposite;
    }

    static double[] multiply(double[] v, double s)
    {
        if (v.length != 4)
            throw new IllegalArgumentException("array size is not 4");

        double[] product = new double[4];
        for (int i = 0; i < 4; i++)
        {
            product[i] = s * v[i];
        }

        return product;
    }
    
    static double[] multiply(double[][] m, double[] v)
    {
        if (v.length != 4 || m.length != 4)
            throw new IllegalArgumentException("array size is not 4");
        
        double[] product = new double[4];
        for (int i = 0; i < 4; i++)
        {
            product[i] = 0;
            for (int j = 0; j < 4; j++)
            {
                product[i] += m[i][j] * v[j];
            }
        }
        
        return product;
    }
}
