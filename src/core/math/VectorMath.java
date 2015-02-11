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
    
    public static double[][] multiply(double[][] m, double s)
    {
        double[][] product = new double[m.length][];
        for (int i = 0; i < m.length; i++)
        {
            product[i] = new double[m[i].length];
            for (int j = 0; j < m[i].length; j++)
            {
                product[i][j] = m[i][j] * s;
            }
        }
        return product;
    }
    
    public static double[][] inverse(double[][] m)
    {
        if (!isSquare(m))
            throw new IllegalArgumentException("matrix is not square");
        if (m.length == 2)
            return get2x2inverse(m);
        else if (m.length == 3)
            return get3x3inverse(m);
        else if (m.length == 4)
            return get4x4inverse(m);
        else
            throw new IllegalArgumentException("illegal matrix size: " + m.length);
    }
    
    private static double[][] get2x2inverse(double[][] m)
    {
        double determinant = get2x2Determinant(m);
        if (determinant == 0)
            throw new IllegalArgumentException("matrix is not invertible:\n"+matrixToString(m));
        double inverseDeterminant = 1.0/determinant;
        
        return multiply(new double[][] { {  m[1][1], -m[0][1] },
                                         { -m[1][0],  m[0][0] } },
                        inverseDeterminant);
    }
    
    private static double[][] get3x3inverse(double[][] m)
    {
        double determinant = get3x3Determinant(m);
        if (determinant == 0)
            throw new IllegalArgumentException("matrix is not invertible:\n"+matrixToString(m));
        
         double n00 = m[1][1] * m[2][2] - m[1][2] * m[2][1];
         double n01 = m[0][2] * m[2][1] - m[0][1] * m[2][2];
         double n02 = m[0][1] * m[1][2] - m[0][2] * m[1][1];
         
         double n10 = m[1][2] * m[2][0] - m[1][0] * m[2][2];
         double n11 = m[0][0] * m[2][2] - m[0][2] * m[2][0];
         double n12 = m[0][2] * m[1][0] - m[0][0] * m[1][2];
         
         double n20 = m[1][0] * m[2][1] - m[1][1] * m[2][0];
         double n21 = m[0][1] * m[2][0] - m[0][0] * m[2][1];
         double n22 = m[0][0] * m[1][1] - m[0][1] * m[1][0];
         
         return multiply(new double[][] { { n00, n01, n02 },
                                          { n10, n11, n12 },
                                          { n20, n21, n22 } },
                         1.0/determinant);
    }
    
    private static double[][] get4x4inverse(double[][] m)
    {
        double det = get4x4Determinant(m);
        if (det == 0)
            throw new IllegalArgumentException("matrix is not invertible:\n"+matrixToString(m));
        
        // ew
        double n00 = m[1][1]*m[2][2]*m[3][3] + m[1][2]*m[2][3]*m[3][1] + m[1][3]*m[2][1]*m[3][2] -
                     m[1][1]*m[2][3]*m[3][2] - m[1][2]*m[2][1]*m[3][3] - m[1][3]*m[2][2]*m[3][1];
        double n01 = m[0][1]*m[2][3]*m[3][2] + m[0][2]*m[2][1]*m[3][3] + m[0][3]*m[2][2]*m[3][1] -
                     m[0][1]*m[2][2]*m[3][3] - m[0][2]*m[2][3]*m[3][1] - m[0][3]*m[2][1]*m[3][2];
        double n02 = m[0][1]*m[1][2]*m[3][3] + m[0][2]*m[1][3]*m[3][1] + m[0][3]*m[1][1]*m[3][2] -
                     m[0][1]*m[1][3]*m[3][2] - m[0][2]*m[1][1]*m[3][3] - m[0][3]*m[1][2]*m[3][1];
        double n03 = m[0][1]*m[1][3]*m[2][2] + m[0][2]*m[1][1]*m[2][3] + m[0][3]*m[1][2]*m[2][1] -
                     m[0][1]*m[1][2]*m[2][3] - m[0][2]*m[1][3]*m[2][1] - m[0][3]*m[1][1]*m[2][2];
        
        double n10 = m[1][0]*m[2][3]*m[3][2] + m[1][2]*m[2][0]*m[3][3] + m[1][3]*m[2][2]*m[3][0] -
                     m[1][0]*m[2][2]*m[3][3] - m[1][2]*m[2][3]*m[3][0] - m[1][3]*m[2][0]*m[3][2];
        double n11 = m[0][0]*m[2][2]*m[3][3] + m[0][2]*m[2][3]*m[3][0] + m[0][3]*m[2][0]*m[3][2] -
                     m[0][0]*m[2][3]*m[3][2] - m[0][2]*m[2][0]*m[3][3] - m[0][3]*m[2][2]*m[3][0];
        double n12 = m[0][0]*m[1][3]*m[3][2] + m[0][2]*m[1][0]*m[3][3] + m[0][3]*m[1][2]*m[3][0] -
                     m[0][0]*m[1][2]*m[3][3] - m[0][2]*m[1][3]*m[3][0] - m[0][3]*m[1][0]*m[3][2];
        double n13 = m[0][0]*m[1][2]*m[2][3] + m[0][2]*m[1][3]*m[2][0] + m[0][3]*m[1][0]*m[2][2] -
                     m[0][0]*m[1][3]*m[2][2] - m[0][2]*m[1][0]*m[2][3] - m[0][3]*m[1][2]*m[2][0];

        double n20 = m[1][0]*m[2][1]*m[3][3] + m[1][1]*m[2][3]*m[3][0] + m[1][3]*m[2][0]*m[3][1] -
                     m[1][0]*m[2][3]*m[3][1] - m[1][1]*m[2][0]*m[3][3] - m[1][3]*m[2][1]*m[3][0];
        double n21 = m[0][0]*m[2][3]*m[3][1] + m[0][1]*m[2][0]*m[3][3] + m[0][3]*m[2][1]*m[3][0] -
                     m[0][0]*m[2][1]*m[3][3] - m[0][1]*m[2][3]*m[3][0] - m[0][3]*m[2][0]*m[3][1];
        double n22 = m[0][0]*m[1][1]*m[3][3] + m[0][1]*m[1][3]*m[3][0] + m[0][3]*m[1][0]*m[3][1] -
                     m[0][0]*m[1][3]*m[3][1] - m[0][1]*m[1][0]*m[3][3] - m[0][3]*m[1][1]*m[3][0];
        double n23 = m[0][0]*m[1][3]*m[2][1] + m[0][1]*m[1][0]*m[2][3] + m[0][3]*m[1][1]*m[2][0] -
                     m[0][0]*m[1][1]*m[2][3] - m[0][1]*m[1][3]*m[2][0] - m[0][3]*m[1][0]*m[2][1];
        
        double n30 = m[1][0]*m[2][2]*m[3][1] + m[1][1]*m[2][0]*m[3][2] + m[1][2]*m[2][1]*m[3][0] -
                     m[1][0]*m[2][1]*m[3][2] - m[1][1]*m[2][2]*m[3][0] - m[1][2]*m[2][0]*m[3][1];
        double n31 = m[0][0]*m[2][1]*m[3][2] + m[0][1]*m[2][2]*m[3][0] + m[0][2]*m[2][0]*m[3][1] -
                     m[0][0]*m[2][2]*m[3][1] - m[0][1]*m[2][0]*m[3][2] - m[0][2]*m[2][1]*m[3][0];
        double n32 = m[0][0]*m[1][2]*m[3][1] + m[0][1]*m[1][0]*m[3][2] + m[0][2]*m[1][1]*m[3][0] -
                     m[0][0]*m[1][1]*m[3][2] - m[0][1]*m[1][2]*m[3][0] - m[0][2]*m[1][0]*m[3][1];
        double n33 = m[0][0]*m[1][1]*m[2][2] + m[0][1]*m[1][2]*m[2][0] + m[0][2]*m[1][0]*m[2][1] -
                     m[0][0]*m[1][2]*m[2][1] - m[0][1]*m[1][0]*m[2][2] - m[0][2]*m[1][1]*m[2][0];

        return multiply(new double[][] { { n00, n01, n02, n03 },
                                         { n10, n11, n12, n13 },
                                         { n20, n21, n22, n23 },
                                         { n30, n31, n32, n33 } },
                        1.0/det);
    }
    
    public static double[][] transpose(double[][] m)
    {
        return new double[][] { { m[0][0], m[1][0], m[2][0], m[3][0] },
                                { m[0][1], m[1][1], m[2][1], m[3][1] },
                                { m[0][2], m[1][2], m[2][2], m[3][2] },
                                { m[0][3], m[1][3], m[2][3], m[3][3] } };
    }
    
    public static double get2x2Determinant(double[][] m)
    {
        return m[0][0] * m[1][1] - m[0][1] * m[1][0];
    }
    
    public static double get3x3Determinant(double[][] m)
    {
        return m[0][0]*(m[1][1]*m[2][2]-m[1][2]*m[2][1]) -
               m[0][1]*(m[1][0]*m[2][2]-m[1][2]*m[2][0]) +
               m[0][2]*(m[1][0]*m[2][1]-m[1][1]*m[2][0]);
    }
    
    public static double get4x4Determinant(double[][] m)
    {
        return m[0][3]*m[1][2]*m[2][1]*m[3][0] - m[0][2]*m[1][3]*m[2][1]*m[3][0] -
               m[0][3]*m[1][1]*m[2][2]*m[3][0] + m[0][1]*m[1][3]*m[2][2]*m[3][0] +
               m[0][2]*m[1][1]*m[2][3]*m[3][0] - m[0][1]*m[1][2]*m[2][3]*m[3][0] -
               m[0][3]*m[1][2]*m[2][0]*m[3][1] + m[0][2]*m[1][3]*m[2][0]*m[3][1] +
               m[0][3]*m[1][0]*m[2][2]*m[3][1] - m[0][0]*m[1][3]*m[2][2]*m[3][1] -
               m[0][2]*m[1][0]*m[2][3]*m[3][1] + m[0][0]*m[1][2]*m[2][3]*m[3][1] +
               m[0][3]*m[1][1]*m[2][0]*m[3][2] - m[0][1]*m[1][3]*m[2][0]*m[3][2] -
               m[0][3]*m[1][0]*m[2][1]*m[3][2] + m[0][0]*m[1][3]*m[2][1]*m[3][2] +
               m[0][1]*m[1][0]*m[2][3]*m[3][2] - m[0][0]*m[1][1]*m[2][3]*m[3][2] -
               m[0][2]*m[1][1]*m[2][0]*m[3][3] + m[0][1]*m[1][2]*m[2][0]*m[3][3] +
               m[0][2]*m[1][0]*m[2][1]*m[3][3] - m[0][0]*m[1][2]*m[2][1]*m[3][3] -
               m[0][1]*m[1][0]*m[2][2]*m[3][3] + m[0][0]*m[1][1]*m[2][2]*m[3][3];
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
    
    public static void timesEquals(double[] v, double s)
    {
        for (int i = 0; i < v.length; i++)
        {
            v[i] *= s;
        }
    }
    
    public static void divideEquals(double[] v, double s)
    {
        timesEquals(v, 1.0/s);
    }
    
    /**
     * 
     * @param m a 4x4 matrix
     * @param v a 4-vector (homogeneous)
     * @return the product m * v
     */
    public static double[] multiply(double[][] m, double[] v)
    {
        if (!isSquare(m))
            throw new IllegalArgumentException("matrix is not square");
        
        if (m.length != v.length)
            throw new IllegalArgumentException("dimension mismatch");
        int d = m.length;
        double[] product = new double[d];
        for (int i = 0; i < d; i++)
        {
            product[i] = 0;
            for (int j = 0; j < d; j++)
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
        if (!isSquare(m))
            throw new IllegalArgumentException("matrix is not square");
        
        if (m.length != v.length)
            throw new IllegalArgumentException("dimension mismatch");
        int d = m.length;
        double[] product = new double[d];
        for (int i = 0; i < d; i++)
        {
            product[i] = 0;
            for (int j = 0; j < d; j++)
            {
                product[i] += m[j][i] * v[j];
            }
        }
        
        return product;
    }
    
    /**
     * Multiplies two square matrices.
     * @param a
     * @param b
     * @return
     */
    public static double[][] multiply(double[][] a, double[][] b)
    {
        if (!isSquare(a) || !isSquare(b))
            throw new IllegalArgumentException("matrices are not square");
        
        if (a.length != b.length)
            throw new IllegalArgumentException("matrix sizes are not equal");
        
        int d = a.length;
        double[][] product = new double[d][d];
        
        for (int i = 0; i < d; i++)
        {
            for (int j = 0; j < d; j++)
            {
                product[i][j] = 0;
                for (int k = 0; k < d; k++)
                {
                    product[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        
        return product;
    }
    
    public static boolean isSquare(double[][] m)
    {
        int width = m.length;
        for (int i = 0; i < width; i++)
        {
            if (m[i].length != width)
                return false;
        }
        return true;
    }
    
    public static String matrixToString(double[][] m)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.length; i++)
        {
            sb.append("[ ");
            for (int j = 0; j < m[i].length; j++)
            {
                sb.append(String.format("%.2f", m[i][j]));
                if (j != m[i].length-1)
                    sb.append(", ");
            }
            sb.append(" ]\n");
        }
        return sb.toString();
    }
}
