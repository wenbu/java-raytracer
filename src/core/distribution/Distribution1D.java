package core.distribution;

import java.util.Arrays;

import core.tuple.Triple;
import utilities.CollectionUtilities;

public class Distribution1D
{
    private final double[] func;
    private final double[] cdf;
    private final double funcInt;
    
    public Distribution1D(double[] f)
    {
        func = Arrays.copyOf(f, f.length);
        cdf = new double[f.length + 1];
        
        cdf[0] = 0;
        for (int i = 1; i < f.length + 1; i++)
        {
            cdf[i] = cdf[i - 1] + func[i - 1] / func.length;
        }
        
        funcInt = cdf[f.length];
        if (funcInt == 0)
        {
            for (int i = 1; i < f.length + 1; i++)
            {
                cdf[i] = (double) i / (double) f.length;
            }
        }
        else
        {
            for (int i = 1; i < f.length + 1; i++)
            {
                cdf[i] /= funcInt;
            }
        }
    }
    
    public int count()
    {
        return func.length;
    }
    
    public double getFuncInt()
    {
        return funcInt;
    }
    
    public double func(int i)
    {
        return func[i];
    }
    
    /**
     * Return <sample x, PDF(x), offset into function array of highest CDF <= u> 
     */
    public Triple<Double, Double, Integer> sampleContinuous(double u)
    {
        // find surrounding CDF segments and offset
        int offset = CollectionUtilities.findInterval(cdf, x -> x <= u);
        // compute offset along CDF segment
        double du = u - cdf[offset];
        if ((cdf[offset + 1] - cdf[offset]) > 0)
        {
            du /= (cdf[offset + 1] - cdf[offset]);
        }
        // compute PDF for sampled offset
        double pdf = func[offset] / funcInt;
        // return x corresponding to sample
        double x = (offset + du) / count();
        
        return new Triple<>(x, pdf, offset);
    }
    
    /**
     * Return <sample x, pdf, uRemapped
     */
    public Triple<Integer, Double, Double> sampleDiscrete(double u)
    {
        // find surrounding CDF segments and offset
        int offset = CollectionUtilities.findInterval(cdf, x -> x <= u);
        double pdf = discretePdf(offset);
        double uRemapped = (u - cdf[offset]) / (cdf[offset + 1] - cdf[offset]);
        return new Triple<>(offset, pdf, uRemapped);
    }
    
    public double discretePdf(int index)
    {
        return func[index] / (funcInt * count());
    }
}
