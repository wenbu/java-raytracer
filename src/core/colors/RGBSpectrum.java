package core.colors;

import utilities.MathUtilities;

public class RGBSpectrum extends CoefficientSpectrum
{
    private double r;
    private double g;
    private double b;

    public RGBSpectrum()
    {
        this(0, 0, 0);
    }
    
    public RGBSpectrum(double v)
    {
        this(v, v, v);
    }
    
    public RGBSpectrum(double r, double g, double b)
    {
        super(new double[] {r, g, b});
        this.r = r;
        this.g = g;
        this.b = b;
    }

    RGBSpectrum(double[] newSamples)
    {
        super(newSamples);
        this.r = newSamples[0];
        this.g = newSamples[1];
        this.b = newSamples[2];
    }

    public double r()
    {
        return r;
    }

    public double g()
    {
        return g;
    }

    public double b()
    {
        return b;
    }
    
    // XXX this is RGB, not XYZ
    @Override
    public double[] toXYZ()
    {
        return new double[] {r, g, b};
    }
    
    
    public RGBSpectrum plus(RGBSpectrum s2)
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = samples[i] + s2.samples[i];
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum plus(double s)
    {
        return plus(new RGBSpectrum(s));
    }
    
    public RGBSpectrum minus(RGBSpectrum s2)
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = samples[i] - s2.samples[i];
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum minus(double s)
    {
        return minus(new RGBSpectrum(s));
    }
    
    public RGBSpectrum times(RGBSpectrum s2)
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = samples[i] * s2.samples[i];
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum times(double s)
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = samples[i] * s;
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum divideBy(RGBSpectrum s2)
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = samples[i] / s2.samples[i];
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum divideBy(double s)
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = samples[i] / s;
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum negative()
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = -samples[i];
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum sqrt()
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = Math.sqrt(samples[i]);
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum pow(double s)
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = Math.pow(samples[i], s);
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum exp(double s)
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = Math.exp(s);
        }
        return new RGBSpectrum(newSamples);
    }
    
    public RGBSpectrum clamp()
    {
        return clamp(0, Double.POSITIVE_INFINITY);
    }
    
    public RGBSpectrum clamp(double low, double high)
    {
        double[] newSamples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            newSamples[i] = MathUtilities.clamp(samples[i], low, high);
        }
        return new RGBSpectrum(newSamples);
    }
    
    @Override
    public String toString()
    {
        return String.format("Color [%.3f, %.3f, %.3f]", r, g, b);
    }
}
