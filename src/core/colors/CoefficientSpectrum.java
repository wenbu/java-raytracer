package core.colors;

public abstract class CoefficientSpectrum
{
    protected final int numSpectrumSamples;
    protected double[] samples;
    
    public CoefficientSpectrum(int numSpectrumSamples, double initialValue)
    {
        this.numSpectrumSamples = numSpectrumSamples;
        samples = new double[numSpectrumSamples];
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            samples[i] = initialValue;
        }
    }
    
    CoefficientSpectrum(double[] samples)
    {
        numSpectrumSamples = samples.length;
        this.samples = samples;
    }
    
    public abstract double[] toXYZ();
    
    public double getSample(int i)
    {
        return samples[i];
    }
    
    public boolean isBlack()
    {
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            if (samples[i] != 0)
            {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof CoefficientSpectrum))
        {
            return false;
        }
        CoefficientSpectrum s2 = (CoefficientSpectrum) other;
        if (numSpectrumSamples != s2.numSpectrumSamples)
        {
            return false;
        }
        for (int i = 0; i < numSpectrumSamples; i++)
        {
            if (samples[i] != s2.samples[i])
            {
                return false;
            }
        }
        return true;
    }
}
