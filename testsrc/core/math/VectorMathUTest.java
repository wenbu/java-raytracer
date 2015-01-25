package core.math;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.junit.Rule;
import org.junit.Test;

import test.RepeatRule;
import test.RepeatRule.Repeat;

public class VectorMathUTest
{
    private static final double EPSILON = 1e-6;
    private static final Random random = new Random();

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Test
    @Repeat(times = 20)
    public void add()
    {
        double[] v1 = getRandom3Vector();
        double[] v2 = getRandom3Vector();
        double[] sum = VectorMath.add(v1, v2);

        assertThat(sum[0], is(equalTo(v1[0] + v2[0])));
        assertThat(sum[1], is(equalTo(v1[1] + v2[1])));
        assertThat(sum[2], is(equalTo(v1[2] + v2[2])));
    }

    @Test
    @Repeat(times = 20)
    public void subtract()
    {
        double[] v1 = getRandom3Vector();
        double[] v2 = getRandom3Vector();
        double[] difference = VectorMath.subtract(v1, v2);

        assertThat(difference[0], is(equalTo(v1[0] - v2[0])));
        assertThat(difference[1], is(equalTo(v1[1] - v2[1])));
        assertThat(difference[2], is(equalTo(v1[2] - v2[2])));
    }

    @Test
    @Repeat(times = 20)
    public void scalarMultiply()
    {
        double[] v = getRandom3Vector();
        double s = getRandomDouble();
        double[] product = VectorMath.multiply(v, s);

        assertThat(product[0], is(closeTo(v[0] * s, EPSILON)));
        assertThat(product[1], is(closeTo(v[1] * s, EPSILON)));
        assertThat(product[2], is(closeTo(v[2] * s, EPSILON)));
    }

    @Test
    @Repeat(times = 20)
    public void scalarDivide()
    {
        double[] v = getRandom3Vector();
        double s = 27.02;
        double[] quotient = VectorMath.divide(v, s);

        assertThat(quotient[0], is(closeTo(v[0] / s, EPSILON)));
        assertThat(quotient[1], is(closeTo(v[1] / s, EPSILON)));
        assertThat(quotient[2], is(closeTo(v[2] / s, EPSILON)));
    }

    @Test
    @Repeat(times = 20)
    public void opposite()
    {
        double[] v = getRandom3Vector();
        double[] opposite = VectorMath.opposite(v);

        assertThat(opposite[0], is(equalTo(-v[0])));
        assertThat(opposite[1], is(equalTo(-v[1])));
        assertThat(opposite[2], is(equalTo(-v[2])));
    }

    @Test
    @Repeat(times = 20)
    public void matrixVectorMultiply()
    {
        double[][] m = getRandomMatrix();
        double[] v = getRandom4Vector();
        double[] product = VectorMath.multiply(m, v);

        for (int i = 0; i < 4; i++)
        {
            assertThat(product[i],
                       is(closeTo(m[i][0] * v[0] +
                                  m[i][1] * v[1] +
                                  m[i][2] * v[2] +
                                  m[i][3] * v[3], EPSILON)));
        }
    }
    
    @Test
    @Repeat(times=20)
    public void transposeMatrixVectorMultiply()
    {
        double[][] m = getRandomMatrix();
        double[] v = getRandom4Vector();
        double[] product = VectorMath.multiplyTranspose(m, v);

        for (int i = 0; i < 4; i++)
        {
            assertThat(product[i],
                       is(closeTo(m[0][i] * v[0] +
                                  m[1][i] * v[1] +
                                  m[2][i] * v[2] +
                                  m[3][i] * v[3], EPSILON)));
        }
    }
    
    @Test
    @Repeat(times = 20)
    public void matrixMultiply()
    {
        double[][] m1 = getRandomMatrix();
        double[][] m2 = getRandomMatrix();
        double[][] product = VectorMath.multiply(m1, m2);
        
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                assertThat(product[i][j],
                           is(closeTo(m1[i][0] * m2[0][j] +
                                      m1[i][1] * m2[1][j] +
                                      m1[i][2] * m2[2][j] +
                                      m1[i][3] * m2[3][j], EPSILON)));
            }
        }
    }

    private static double[] getRandom3Vector()
    {
        return new double[] { getRandomDouble(),
                              getRandomDouble(),
                              getRandomDouble() };
    }

    private static double[] getRandom4Vector()
    {
        return new double[] { getRandomDouble(),
                              getRandomDouble(),
                              getRandomDouble(),
                              getRandomDouble() };
    }

    private static double[][] getRandomMatrix()
    {
        return new double[][] { { getRandomDouble(),
                                  getRandomDouble(),
                                  getRandomDouble(),
                                  getRandomDouble() },
                                { getRandomDouble(),
                                  getRandomDouble(),
                                  getRandomDouble(),
                                  getRandomDouble() },
                                { getRandomDouble(),
                                  getRandomDouble(),
                                  getRandomDouble(),
                                  getRandomDouble() },
                                { getRandomDouble(),
                                  getRandomDouble(),
                                  getRandomDouble(),
                                  getRandomDouble() } };
    }

    private static double getRandomDouble()
    {
        if (random.nextInt(11) <= 1) // return 0 10% of the time
            return 0;
        double mantissa = -10 + random.nextDouble() * 20; // (-10, 10)
        int exponent = random.nextInt(21) - 10; // (-10, 10)
        return mantissa * Math.pow(10.0, exponent);
    }
}
