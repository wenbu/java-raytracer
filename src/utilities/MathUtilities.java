package utilities;

import core.colors.RGBSpectrum;
import core.tuple.Pair;

public class MathUtilities
{
    public static final double MACHINE_EPSILON = Math.ulp(1.0) / 2;
    public static final double INV_PI = 1 / Math.PI;
    public static final double INV_2PI = 1 / (2 * Math.PI);
    public static final double INV_4PI = 1 / (4 * Math.PI);
    public static final double PI_OVER_2 = Math.PI / 2;
    public static final double PI_OVER_4 = Math.PI / 4;
    public static final double INV_LOG2 = 1.442695040888963387004650940071;
    
    public static double erf(double x)
    {
        final double a1 = 0.254829592f;
        final double a2 = -0.284496736f;
        final double a3 = 1.421413741f;
        final double a4 = -1.453152027f;
        final double a5 = 1.061405429f;
        final double p = 0.3275911f;
        
        // save sign(x)
        int sign = x < 0 ? -1 : 1;
        
        // A&S formula 7.1.26
        double t = 1 / (1 + p * x);
        double y = 1 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);
        
        return sign * y;
    }
    
    public static double invErf(double x)
    {
        x = clamp(x, -0.99999, 0.99999);
        double w = -Math.log((1 - x) * (1 - x));
        double p;
        if (w < 5)
        {
            w -= 2.5;
            p = 2.81022636e-08f;
            p = 3.43273939e-07f + p * w;
            p = -3.5233877e-06f + p * w;
            p = -4.39150654e-06f + p * w;
            p = 0.00021858087f + p * w;
            p = -0.00125372503f + p * w;
            p = -0.00417768164f + p * w;
            p = 0.246640727f + p * w;
            p = 1.50140941f + p * w;
        }
        else
        {
            w = Math.sqrt(w) - 3;
            p = -0.000200214257f;
            p = 0.000100950558f + p * w;
            p = 0.00134934322f + p * w;
            p = -0.00367342844f + p * w;
            p = 0.00573950773f + p * w;
            p = -0.0076224613f + p * w;
            p = 0.00943887047f + p * w;
            p = 1.00167406f + p * w;
            p = 2.83297682f + p * w;
        }
        return p * x;
    }
    public static double log2(double x)
    {
        return Math.log(x) * INV_LOG2;
    }
    
    public static int log2Int(int x)
    {
        return 31 - Integer.numberOfLeadingZeros(x);
    }
    
    public static double lanczos(double x)
    {
        return lanczos(x, 2);
    }
    
    public static double lanczos(double x, double tau)
    {
        x = Math.abs(x);
        double lanczos = MathUtilities.sinc(x / tau);
        return MathUtilities.sinc(x) * lanczos;
    }
    
    public static double sinc(double x)
    {
        x = Math.abs(x);
        if (x < 1e-5)
        {
            return 1;
        }
        return Math.sin(Math.PI * x) / (Math.PI * x);
    }
    
    public static boolean isPowerOf2(int t)
    {
        return (t != 0) && ((t & (t - 1)) == 0);
    }
    
    public static int nextPower2(int t)
    {
        int highestOneBit = Integer.highestOneBit(t);
        if (t == highestOneBit)
        {
            return t;
        }
        return highestOneBit << 1;
    }
    
    public static double lerp(double t, double v1, double v2)
    {
        return (1.0 - t) * v1 + t * v2;
    }
    
    public static RGBSpectrum lerp(double t, RGBSpectrum v1, RGBSpectrum v2)
    {
        return v1.times(1.0 - t).plus(v2.times(t));
    }
    
    public static int clamp(int val, int low, int high)
    {
        if (val < low) return low;
        else if (val > high) return high;
        else return val;
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
