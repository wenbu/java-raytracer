package core.distribution;

import static utilities.MathUtilities.*;
import java.util.Arrays;

import core.math.Point2;
import core.tuple.Pair;

public class Distribution2D
{
    private Distribution1D[] pConditionalV;
    private Distribution1D pMarginal;
    
    public Distribution2D(double[] func, int nu, int nv)
    {
        pConditionalV = new Distribution1D[nv];
        
        for (int v = 0; v < nv; v++)
        {
            // compute conditional p(u|v) for v
            pConditionalV[v] = new Distribution1D(Arrays.copyOfRange(func, v * nu, v * nu + nu));
        }
        // compute marginal p(u)
        double[] marginalFunc = new double[nv];
        for (int v = 0; v < nv; v++)
        {
            marginalFunc[v] = pConditionalV[v].getFuncInt();
        }
        pMarginal = new Distribution1D(marginalFunc);
    }
    
    public Pair<Point2, Double> sampleContinuous(Point2 u)
    {
        var marginal = pMarginal.sampleContinuous(u.get(1));
        double d1 = marginal.getFirst();
        double pdf1 = marginal.getSecond();
        int index = marginal.getThird();
        var conditional = pConditionalV[index].sampleContinuous(u.get(0));
        double d0 = conditional.getFirst();
        double pdf0 = conditional.getSecond();
        double pdf = pdf0 * pdf1;
        return new Pair<>(new Point2(d0, d1), pdf);
    }
    
    public double pdf(Point2 p)
    {
        int iu = clamp((int) (p.get(0) * pConditionalV[0].count()), 0, pConditionalV[0].count() - 1);
        int iv = clamp((int) (p.get(1) * pMarginal.count()), 0, pMarginal.count() - 1);
        return pConditionalV[iv].func(iu) / pMarginal.getFuncInt();
    }
}
