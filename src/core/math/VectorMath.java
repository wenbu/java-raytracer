package core.math;


public class VectorMath
{
    static double getLength(double x, double y, double z)
    {
        return Math.sqrt(x * x + y * y + z * z);
    }

    static double dotProduct(double[] v1, double[] v2)
    {
        if (v1.length != 3 || v2.length != 3)
            throw new IllegalArgumentException("array size is not 3");

        return v1[0] * v2[0] +
               v1[1] * v2[1] +
               v1[2] * v2[2];
    }

    /*
     * Copy initializers
     */
    
    static double[] copy(double[] v)
    {
        double[] copy = new double[v.length];
        for (int i = 0; i < v.length; i++)
        {
            copy[i] = v[i];
        }
        return copy;
    }

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

    public static double[] add(double[] v1, double[] v2)
    {
        if (v1.length != v2.length)
            throw new IllegalArgumentException("array sizes do not match");

        double[] sum = new double[v1.length];
        for (int i = 0; i < v1.length; i++)
        {
            sum[i] = v1[i] + v2[i];
        }

        return sum;
    }

    public static double[] subtract(double[] v1, double[] v2)
    {
        if (v1.length != v2.length)
            throw new IllegalArgumentException("array sizes do not match");

        double[] difference = new double[v1.length];
        for (int i = 0; i < v1.length; i++)
        {
            difference[i] = v1[i] - v2[i];
        }

        return difference;
    }

    public static double[] opposite(double[] v)
    {
        double[] opposite = new double[v.length];
        for (int i = 0; i < v.length; i++)
        {
            opposite[i] = -v[i];
        }

        return opposite;
    }

    public static double[] multiply(double[] v, double s)
    {
        double[] product = new double[v.length];
        for (int i = 0; i < v.length; i++)
        {
            product[i] = s * v[i];
        }

        return product;
    }
    
    public static double[] divide(double[] v, double s)
    {
        return multiply(v, 1.0/s);
    }
    
    public static double[][] inverse(double[][] m)
    {
        // TODO
        throw new UnsupportedOperationException("matrix inverse not yet implemented");
    }
    
    public static double[][] transpose(double[][] m)
    {
        return new double[][] { { m[0][0], m[1][0], m[2][0], m[3][0] },
                                { m[0][1], m[1][1], m[2][1], m[3][1] },
                                { m[0][2], m[1][2], m[2][2], m[3][2] },
                                { m[0][3], m[1][3], m[2][3], m[3][3] } };
    }
    
    /*
     * Operators that modify an existing double[].
     */
    
    /**
     * Component-wise addition of v2 to v1.
     * The length of v2 must be at least that of v1.
     */
    public static void plusEquals(double[] v1, double[] v2)
    {
        if (!(v2.length >= v1.length))
        {
            throw new IllegalArgumentException("v2 is too short for v1: "+v1.length+
                                               " vs "+v2.length);
        }
        
        for (int i =  0; i < v1.length; i++)
        {
            v1[i] += v2[i];
        }
    }
    
    /**
     * Component-wise addition of v2 to v1.
     * The length of v2 must be at least that of v1.
     */
    public static void minusEquals(double[] v1, double[] v2)
    {
        if (!(v2.length >= v1.length))
        {
            throw new IllegalArgumentException("v2 is too short for v1: "+v1.length+
                                               " vs "+v2.length);
        }
        
        for (int i =  0; i < v1.length; i++)
        {
            v1[i] -= v2[i];
        }
    }
    
    /**
     * 
     * @param m a 4x4 matrix
     * @param v a 4-vector (homogeneous)
     * @return the product m * v
     */
    public static double[] multiply(double[][] m, double[] v)
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
    
    /**
     * 
     * @param m a 4x4 matrix
     * @param v a 4-vector (homogeneous)
     * @return the product mT * v
     */
    public static double[] multiplyTranspose(double[][] m, double[] v)
    {
        if (v.length != 4 || m.length != 4)
            throw new IllegalArgumentException("array size is not 4");
        
        double[] product = new double[4];
        for (int i = 0; i < 4; i++)
        {
            product[i] = 0;
            for (int j = 0; j < 4; j++)
            {
                product[i] += m[j][i] * v[j];
            }
        }
        
        return product;
    }
    
    public static double[][] multiply(double[][] a, double[][] b)
    {
        if (a.length != 4 || b.length != 4)
            throw new IllegalArgumentException("array size is not 4");
        
        double[][] product = new double[4][4];
        
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                product[i][j] = 0;
                for (int k = 0; k < 4; k++)
                {
                    product[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        
        return product;
    }
}
