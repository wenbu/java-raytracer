package core.math;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static test.TestUtils.getRandomDouble;
import static test.TestUtils.EPSILON;

import org.junit.Rule;
import org.junit.Test;

import test.RepeatRule;
import test.RepeatRule.Repeat;

public class QuaternionUTest
{
    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Test
    @Repeat(times = 20)
    public void testQuaternionAdd()
    {
        double[] qv1 = getRandomQuaternionVals();
        double[] qv2 = getRandomQuaternionVals();
        
        Quaternion q1 = new Quaternion(qv1);
        Quaternion q2 = new Quaternion(qv2);
        Quaternion sum = q1.plus(q2);
        
        assertThat(sum.x(), is(closeTo(qv1[0] + qv2[0], EPSILON)));
        assertThat(sum.y(), is(closeTo(qv1[1] + qv2[1], EPSILON)));
        assertThat(sum.z(), is(closeTo(qv1[2] + qv2[2], EPSILON)));
        assertThat(sum.w(), is(closeTo(qv1[3] + qv2[3], EPSILON)));
    }
    
    @Test
    @Repeat(times = 20)
    public void testQuaternionPlusEquals()
    {
        double[] qv1 = getRandomQuaternionVals();
        double[] qv2 = getRandomQuaternionVals();
        
        Quaternion q1 = new Quaternion(qv1);
        Quaternion q2 = new Quaternion(qv2);
        q1.plusEquals(q2);
        
        assertThat(q1.x(), is(closeTo(qv1[0] + qv2[0], EPSILON)));
        assertThat(q1.y(), is(closeTo(qv1[1] + qv2[1], EPSILON)));
        assertThat(q1.z(), is(closeTo(qv1[2] + qv2[2], EPSILON)));
        assertThat(q1.w(), is(closeTo(qv1[3] + qv2[3], EPSILON)));
    }
    
    @Test
    @Repeat(times = 20)
    public void testQuaternionSubtract()
    {
        double[] qv1 = getRandomQuaternionVals();
        double[] qv2 = getRandomQuaternionVals();
        
        Quaternion q1 = new Quaternion(qv1);
        Quaternion q2 = new Quaternion(qv2);
        Quaternion difference = q1.minus(q2);
        
        assertThat(difference.x(), is(closeTo(qv1[0] - qv2[0], EPSILON)));
        assertThat(difference.y(), is(closeTo(qv1[1] - qv2[1], EPSILON)));
        assertThat(difference.z(), is(closeTo(qv1[2] - qv2[2], EPSILON)));
        assertThat(difference.w(), is(closeTo(qv1[3] - qv2[3], EPSILON)));
    }
    
    @Test
    @Repeat(times = 20)
    public void testQuaternionMinusEquals()
    {
        double[] qv1 = getRandomQuaternionVals();
        double[] qv2 = getRandomQuaternionVals();
        
        Quaternion q1 = new Quaternion(qv1);
        Quaternion q2 = new Quaternion(qv2);
        q1.minusEquals(q2);
        
        assertThat(q1.x(), is(closeTo(qv1[0] - qv2[0], EPSILON)));
        assertThat(q1.y(), is(closeTo(qv1[1] - qv2[1], EPSILON)));
        assertThat(q1.z(), is(closeTo(qv1[2] - qv2[2], EPSILON)));
        assertThat(q1.w(), is(closeTo(qv1[3] - qv2[3], EPSILON)));
    }
    
    @Test
    @Repeat(times = 20)
    public void testQuaternionMultiply()
    {
        double[] qv1 = getRandomQuaternionVals();
        double s = getRandomDouble();
        
        Quaternion q1 = new Quaternion(qv1);
        Quaternion product = q1.times(s);
        
        assertThat(product.x(), is(closeTo(qv1[0] * s, EPSILON)));
        assertThat(product.y(), is(closeTo(qv1[1] * s, EPSILON)));
        assertThat(product.z(), is(closeTo(qv1[2] * s, EPSILON)));
        assertThat(product.w(), is(closeTo(qv1[3] * s, EPSILON)));
    }
    
    @Test
    @Repeat(times = 20)
    public void testQuaternionTimesEquals()
    {
        double[] qv1 = getRandomQuaternionVals();
        double s = getRandomDouble();
        
        Quaternion q1 = new Quaternion(qv1);
        q1.timesEquals(s);
        
        assertThat(q1.x(), is(closeTo(qv1[0] * s, EPSILON)));
        assertThat(q1.y(), is(closeTo(qv1[1] * s, EPSILON)));
        assertThat(q1.z(), is(closeTo(qv1[2] * s, EPSILON)));
        assertThat(q1.w(), is(closeTo(qv1[3] * s, EPSILON)));
    }
    
    @Test
    @Repeat(times = 20)
    public void testQuaternionDivide()
    {
        double[] qv1 = getRandomQuaternionVals();
        double s = getRandomDouble();
        
        Quaternion q1 = new Quaternion(qv1);
        Quaternion quotient = q1.divide(s);
        
        assertThat(quotient.x(), is(closeTo(qv1[0] / s, EPSILON)));
        assertThat(quotient.y(), is(closeTo(qv1[1] / s, EPSILON)));
        assertThat(quotient.z(), is(closeTo(qv1[2] / s, EPSILON)));
        assertThat(quotient.w(), is(closeTo(qv1[3] / s, EPSILON)));
    }
    
    @Test
    @Repeat(times = 20)
    public void testQuaternionDivideEquals()
    {
        double[] qv1 = getRandomQuaternionVals();
        double s = getRandomDouble();
        
        Quaternion q1 = new Quaternion(qv1);
        q1.divideEquals(s);
        
        assertThat(q1.x(), is(closeTo(qv1[0] / s, EPSILON)));
        assertThat(q1.y(), is(closeTo(qv1[1] / s, EPSILON)));
        assertThat(q1.z(), is(closeTo(qv1[2] / s, EPSILON)));
        assertThat(q1.w(), is(closeTo(qv1[3] / s, EPSILON)));
    }
    
    @Test
    @Repeat(times = 20)
    public void testQuaternionDot()
    {
        double[] qv1 = getRandomQuaternionVals();
        double[] qv2 = getRandomQuaternionVals();
        
        Quaternion q1 = new Quaternion(qv1);
        Quaternion q2 = new Quaternion(qv2);
        double dot = q1.dot(q2);
        
        assertThat(dot, is(closeTo(qv1[0] * qv2[0] +
                                   qv1[1] * qv2[1] +
                                   qv1[2] * qv2[2] +
                                   qv1[3] * qv2[3], EPSILON)));
    }
    
    @Test
    @Repeat(times = 20)
    public void testQuaternionNormalize()
    {
        double[] qv = getRandomQuaternionVals();
        Quaternion q = new Quaternion(qv);
        Quaternion qq = q.normalized();
        assertThat(qq.magnitude(), is(closeTo(1.0, EPSILON)));
    }
    
    @Test
    public void testQuaternionSlerp()
    {
        Transformation t1 = Transformation.getRotation(0.0, 1.0, 0.0, Math.PI/2.0);
        Quaternion q1 = new Quaternion(t1);
        
        Transformation t2 = Transformation.getRotation(0.0, 1.0, 0.0, Math.PI);
        Quaternion q2 = new Quaternion(t2);
        
        Transformation expectedTransform = Transformation.getRotation(0.0, 1.0, 0.0, Math.PI * 0.75);
        Quaternion expectedQuaternion = new Quaternion(expectedTransform);
        
        Quaternion slerpQuaternion = Quaternion.slerp(0.5, q1, q2);
        
        assertThat(slerpQuaternion.x(), is(closeTo(expectedQuaternion.x(), EPSILON)));
        assertThat(slerpQuaternion.y(), is(closeTo(expectedQuaternion.y(), EPSILON)));
        assertThat(slerpQuaternion.z(), is(closeTo(expectedQuaternion.z(), EPSILON)));
        assertThat(slerpQuaternion.w(), is(closeTo(expectedQuaternion.w(), EPSILON)));
    }
    
    private double[] getRandomQuaternionVals()
    {
        return new double[] { getRandomDouble(),
                              getRandomDouble(),
                              getRandomDouble(),
                              getRandomDouble() };
    }
}
