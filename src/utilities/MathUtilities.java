package utilities;

import core.tuple.Pair;

public class MathUtilities
{
    public static final double MACHINE_EPSILON = Math.ulp(1.0) / 2;
    public static final double INV_PI = 1 / Math.PI;
    public static final double INV_2PI = 1 / (2 * Math.PI);
    public static final double PI_OVER_2 = Math.PI / 2;
    public static final double PI_OVER_4 = Math.PI / 4;
    
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
    
    public static double gamma(int n)
    {
        return (n * MACHINE_EPSILON) / (1 - n * MACHINE_EPSILON);
    }
    
    public static double gammaCorrect(double n)
    {
        if (n <= 0.0031308f)
        {
            return 12.92f * n;
        }
        return 1.055f * Math.pow(n, (1.f/2.4f)) - 0.055f;
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
    
    public static Pair<eDouble, eDouble> quadratic(eDouble a, eDouble b, eDouble c)
    {
        double discriminant = b.value * b.value - 4 * a.value * c.value;
        if (discriminant < 0)
        {
            return null;
        }
        double discriminantRoot = Math.sqrt(discriminant);
        eDouble eDiscriminantRoot = new eDouble(discriminantRoot, MACHINE_EPSILON * discriminantRoot);
        
        eDouble q;
        if (b.value < 0)
        {
            q = b.minus(eDiscriminantRoot).times(-0.5);
        }
        else
        {
            q = b.plus(eDiscriminantRoot).times(-0.5);
        }
        eDouble t0 = q.divide(a);
        eDouble t1 = c.divide(q);
        if (t0.value < t1.value)
        {
            return new Pair<>(t0, t1);
        }
        else
        {
            return new Pair<>(t1, t0);
        }
    }
    
    public static class eDouble
    {
        double value;
        double low;
        double high;
        
        public eDouble()
        {
            this(0, 0);
        }
        
        public eDouble(double value)
        {
            this(value, 0);
        }
        
        public eDouble(double value, double error)
        {
            this.value = value;
            if (error == 0)
            {
                low = high = value;
            }
            else
            {
                low = Math.nextDown(value - error);
                high = Math.nextUp(value + error);
            }
        }
        
        public eDouble(eDouble other)
        {
            this.value = other.value;
            this.low = other.low;
            this.high = other.high;
        }
        
        public double getValue()
        {
            return value;
        }
        
        public eDouble plus(eDouble f)
        {
            eDouble r = new eDouble();
            r.value = value + f.value;
            r.low = Math.nextDown(lowerBound() + f.lowerBound());
            r.high = Math.nextUp(upperBound() + f.upperBound());
            return r;
        }
        
        public eDouble minus(eDouble f)
        {
            eDouble r = new eDouble();
            r.value = value - f.value;
            r.low = Math.nextDown(lowerBound() - f.upperBound());
            r.high = Math.nextUp(upperBound() - f.lowerBound());
            return r;
        }
        
        public eDouble times(double f)
        {
            return times(new eDouble(f, 0));
        }
        
        public eDouble times(eDouble f)
        {
            eDouble r = new eDouble();
            r.value = value * f.value;
            double p0 = lowerBound() * f.lowerBound();
            double p1 = upperBound() * f.lowerBound();
            double p2 = lowerBound() * f.upperBound();
            double p3 = upperBound() * f.upperBound();
            r.low = Math.nextDown(Math.min(Math.min(p0, p1), Math.min(p2, p3)));
            r.high = Math.nextUp(Math.max(Math.max(p0, p1), Math.max(p2, p3)));
            return r;
        }
        
        public eDouble divide(double f)
        {
            return divide(new eDouble(f, 0));
        }
        
        public eDouble divide(eDouble f)
        {
            eDouble r = new eDouble();
            r.value = value / f.value;
            if (f.low < 0 && f.high > 0)
            {
                r.low = Double.NEGATIVE_INFINITY;
                r.high = Double.POSITIVE_INFINITY;
            }
            else
            {
                double p0 = lowerBound() / f.lowerBound();
                double p1 = upperBound() / f.lowerBound();
                double p2 = lowerBound() / f.upperBound();
                double p3 = upperBound() / f.upperBound();
                r.low = Math.nextDown(Math.min(Math.min(p0, p1), Math.min(p2, p3)));
                r.high = Math.nextUp(Math.max(Math.max(p0, p1), Math.max(p2, p3)));
            }
            return r;
        }
        
        public eDouble sqrt()
        {
            eDouble r = new eDouble();
            r.value = Math.sqrt(value);
            r.low = Math.nextDown(Math.sqrt(low));
            r.high = Math.nextUp(Math.sqrt(high));
            return r;
        }
        
        public eDouble abs()
        {
            if (low >= 0)
            {
                return this;
            }
            else if (high <= 0)
            {
                return negative();
            }
            else
            {
                eDouble r = new eDouble();
                r.value = Math.abs(value);
                r.low = 0;
                r.high = Math.max(-low, high);
                return r;
            }
        }
        
        public eDouble negative()
        {
            eDouble r = new eDouble();
            r.value = -value;
            r.low = -high;
            r.high = -low;
            return r;
        }
        
        @Override
        public boolean equals(Object other)
        {
            if (!(other instanceof eDouble))
            {
                return false;
            }
            else
            {
                return value == ((eDouble)other).value;
            }
        }
        
        public double getAbsoluteError()
        {
            return high - low;
        }
        
        public double upperBound()
        {
            return high;
        }
        
        public double lowerBound()
        {
            return low;
        }
    }
}
