package core.math;


public class VectorMath
{
    public static Direction normalized(Direction v)
    {
        Direction n = v.divide(v.length());
        if (n.length() > 1.1)
        {
            System.out.println(v);
            System.out.println(n);
        }
        return n;
    }

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
        if (v1.length != v2.length)
            throw new IllegalArgumentException("array sizes do not match");

        double[] sum = new double[v1.length];
        for (int i = 0; i < v1.length; i++)
        {
            sum[i] = v1[i] + v2[i];
        }

        return sum;
    }

    static double[] subtract(double[] v1, double[] v2)
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

    static double[] opposite(double[] v)
    {
        double[] opposite = new double[v.length];
        for (int i = 0; i < v.length; i++)
        {
            opposite[i] = -v[i];
        }

        return opposite;
    }

    static double[] multiply(double[] v, double s)
    {
        double[] product = new double[v.length];
        for (int i = 0; i < v.length; i++)
        {
            product[i] = s * v[i];
        }

        return product;
    }
    
    /*
     * Operators that modify an existing double[].
     */
    
    /**
     * Component-wise addition of v2 to v1.
     * The length of v2 must be at least that of v1.
     */
    static void plusEquals(double[] v1, double[] v2)
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
    static void minusEquals(double[] v1, double[] v2)
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
     * @return the product mv
     */
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
