package core.math;

public class Quaternion
{
    private final Direction v;
    private double w;

    /**
     * Initialize a unit quaternion.
     */
    public Quaternion()
    {
        this.v = new Direction(0, 0, 0);
        this.w = 1;
    }

    /**
     * Initialize a quaternion with real component w and imaginary component v.
     * 
     * @param v
     *            Imaginary component.
     * @param w
     *            Real component.
     */
    public Quaternion(Direction v, double w)
    {
        this.v = v;
        this.w = w;
    }
    
    /**
     * For testing purposes.
     */
    Quaternion(double[] vals)
    {
        this.v = new Direction(vals[0], vals[1], vals[2]);
        this.w = vals[3];
    }
    
    Quaternion(double x, double y, double z, double w)
    {
        this(new double[] {x, y, z, w});
    }
    
    /**
     * Initialize a quaternion from a transformation. Adapted from pbrt.
     */
    public Quaternion(Transformation t)
    {
        double[][] m = t.getMatrix();
        double trace = m[0][0] + m[1][1] + m[2][2];
        
        if (trace > 0.0)
        {
            double s = Math.sqrt(trace + 1.0);
            w = s / 2.0;
            
            s = 0.5 / s;
            double x = (m[2][1] - m[1][2]) * s;
            double y = (m[0][2] - m[2][0]) * s;
            double z = (m[1][0] - m[0][1]) * s;
            v = new Direction(x, y, z);
        }
        else
        {
            int[] next = new int[] { 1, 2, 0 };
            double q[] = new double[3];
            int i = 0;
            
            if (m[1][1] > m[0][0])
                i = 1;
            if (m[2][2] > m[i][i])
                i = 2;
            int j = next[i];
            int k = next[j];
            
            double s = Math.sqrt((m[i][i] - (m[j][j] + m[k][k])) + 1.0);
            q[i] = s * 0.5;
            if (s != 0.0)
                s = 0.5 / s;
            q[j] = (m[j][i] + m[i][j]) * s;
            q[k] = (m[k][i] + m[i][k]) * s;
            
            v = new Direction(q[0], q[1], q[2]);
            w = (m[k][j] - m[j][k]) * s;
        }
    }
    
    public static Quaternion slerp(double t, Quaternion q1, Quaternion q2)
    {
        double cosTheta = q1.dot(q2);
        if ( cosTheta > 0.99995)
        {
            return q1.times(1.0-t).plus(q2.times(t));
        }
        else
        {
            double theta = Math.acos(cosTheta);
            double thetap = theta * t;
            Quaternion qperp = q2.minus(q1.times(cosTheta)).normalize();
            return q1.times(Math.cos(thetap)).plus(qperp.times(Math.sin(thetap)));
        }
    }

    public Quaternion plus(Quaternion other)
    {
        return new Quaternion(v.plus(other.v), w + other.w);
    }

    public Quaternion minus(Quaternion other)
    {
        return new Quaternion(v.minus(other.v), w - other.w);
    }

    public Quaternion times(double scalar)
    {
        return new Quaternion(v.times(scalar), w * scalar);
    }

    public Quaternion divide(double scalar)
    {
        return new Quaternion(v.divide(scalar), w / scalar);
    }
    
    public Quaternion plusEquals(Quaternion other)
    {
        v.plusEquals(other.v);
        w += other.w;
        return this;
    }
    
    public Quaternion minusEquals(Quaternion other)
    {
        v.minusEquals(other.v);
        w -= other.w;
        return this;
    }
    
    public Quaternion timesEquals(double scalar)
    {
        v.timesEquals(scalar);
        w *= scalar;
        return this;
    }
    
    public Quaternion divideEquals(double scalar)
    {
        v.divideEquals(scalar);
        w /= scalar;
        return this;
    }
    
    public double dot(Quaternion other)
    {
        return v.dot(other.v) + w * other.w;
    }
    
    public Quaternion normalized()
    {
        return divide(Math.sqrt(magnitude()));
    }
    
    public Quaternion normalize()
    {
        return divideEquals(Math.sqrt(magnitude()));
    }
    
    public double magnitude()
    {
        return this.dot(this);
    }
    
    public Transformation getTransformation()
    {
        return new Transformation(this);
    }
    
    public double x()
    {
        return v.x();
    }
    
    public double y()
    {
        return v.y();
    }
    
    public double z()
    {
        return v.z();
    }
    
    public double w()
    {
        return w;
    }
    
    @Override
    public String toString()
    {
        return String.format("Quaternion[<%f, %f, %f>, %f]", x(), y(), z(), w);
    }
}
