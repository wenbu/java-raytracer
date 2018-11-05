package scene.materials.functions.microfacet;

import static scene.materials.functions.BidirectionalScatteringDistributionFunction.*;
import static utilities.GeometryUtilities.*;
import static utilities.MathUtilities.*;

import core.math.Direction3;
import core.math.Point2;
import core.tuple.Pair;
import scene.materials.functions.MicrofacetDistribution;

public class BeckmannDistribution extends MicrofacetDistribution
{
    private final double alphax;
    private final double alphay;
    
    public BeckmannDistribution(double alphax, double alphay)
    {
        this(alphax, alphay, true);
    }
    
    public BeckmannDistribution(double alphax, double alphay, boolean sampleVisibleArea)
    {
        super(sampleVisibleArea);
        this.alphax = alphax;
        this.alphay = alphay;
    }
    
    @Override
    public double getDifferentialArea(Direction3 wh)
    {
        double tan2Theta = tan2Theta(wh);
        if (Double.isInfinite(tan2Theta))
        {
            return 0;
        }
        
        double cos4Theta = cos2Theta(wh) * cos2Theta(wh);
        return Math.exp(-tan2Theta *
                        (cos2Phi(wh) / (alphax * alphax) + sin2Phi(wh) / (alphay * alphay))) /
               (Math.PI * alphax * alphay * cos4Theta);
    }

    @Override
    public double getMaskedArea(Direction3 w)
    {
        double absTanTheta = Math.abs(tanTheta(w));
        if (Double.isInfinite(absTanTheta))
        {
            return 0;
        }
        
        double alpha = Math.sqrt(cos2Phi(w) * alphax * alphax + sin2Phi(w) * alphay * alphay);
        double a = 1 / (alpha * absTanTheta);
        if (a >= 1.6f)
        {
            return 0;
        }
        return (1 - 1.259f * a + 0.396f * a * a) / (3.535f * a + 2.181f * a * a);
    }

    @Override
    public Direction3 sampleNormalDistribution(Direction3 wo, Point2 u)
    {
        Direction3 wh;
        if (!sampleVisibleArea)
        {
            // sample full normal distribution
            // compute tan^2(theta) and phi
            double tan2Theta;
            double phi;
            double logSample = Math.log(u.get(0));
            if (Double.isInfinite(logSample))
            {
                logSample = 0;
            }
            
            if (alphax == alphay)
            {
                tan2Theta = -alphax * alphax * logSample;
                phi = u.get(1) * 2 * Math.PI;
            }
            else
            {
                // compute tan2Theta and phi for anisotropic distribution
                phi = Math.atan(alphay / alphax * Math.tan(2 * Math.PI * u.get(1) + 0.5 * Math.PI));
                if (u.get(1) > 0.5)
                {
                    phi += Math.PI;
                }
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);
                double alphax2 = alphax * alphax;
                double alphay2 = alphay * alphay;
                tan2Theta = -logSample / (cosPhi * cosPhi / alphax2 + sinPhi * sinPhi / alphay2);
            }
            // map sampled Beckmann angles to normal distribution
            double cosTheta = 1 / (Math.sqrt(1 + tan2Theta));
            double sinTheta = Math.sqrt(Math.max(0, 1 - cosTheta * cosTheta));
            wh = sphericalDirection(sinTheta, cosTheta, phi);
            if (!sameHemisphere(wo, wh))
            {
                wh.timesEquals(-1);
            }
        }
        else
        {
            // sample visible area of normals
            boolean flip = wo.z() < 0;
            wh = beckmannSample(flip? wo.times(-1) : wo, alphax, alphay, u.get(0), u.get(1));
            if (flip)
            {
                wh.timesEquals(-1);
            }
        }
        return wh;
    }

    private static Direction3 beckmannSample(Direction3 wi, double alphax, double alphay, double u1, double u2)
    {
        // stretch wi
        Direction3 wiStretched = new Direction3(alphax * wi.x(), alphay * wi.y(), wi.z()).normalize();
        
        // simulate P22_wi(x_slope, y_slope, 1, 1);
        var beckmannSample = beckmannSample11(cosTheta(wiStretched), u1, u2);
        double slopex = beckmannSample.getFirst();
        double slopey = beckmannSample.getSecond();
        
        // rotate
        double tmp = cosPhi(wiStretched) * slopex - sinPhi(wiStretched) * slopey;
        slopey = sinPhi(wiStretched) * slopex + cosPhi(wiStretched) * slopey;
        slopex = tmp;
        
        // unstretch
        slopex = alphax * slopex;
        slopey = alphay * slopey;
        
        // compute normal
        return new Direction3(-slopex, -slopey, 1).normalize();
    }
    
    private static Pair<Double, Double> beckmannSample11(double cosThetaI, double u1, double u2)
    {
        // special case -- normal incidence
        if (cosThetaI > 0.9999)
        {
            double r = Math.sqrt(-Math.log(1 - u1));
            double sinPhi = Math.sin(2 * Math.PI * u2);
            double cosPhi = Math.cos(2 * Math.PI * u2);
            return new Pair<>(r * cosPhi, r * sinPhi);
        }
        
        double sinThetaI = Math.sqrt(Math.max(0, 1 - cosThetaI * cosThetaI));
        double tanThetaI = sinThetaI / cosThetaI;
        double cotThetaI = 1 / tanThetaI;
        
        double a = -1;
        double c = erf(cotThetaI);
        double samplex = Math.max(u1, 1e-6);
        
        double thetaI = Math.acos(cosThetaI);
        double fit = 1 + thetaI * (0.876 + thetaI * (0.4265 - 0.0594 * thetaI));
        double b = c - (1 + c) * Math.pow(1 - samplex, fit);
        
        // normalization factor for CDF
        final double sqrtPiInv = 1 / Math.sqrt(Math.PI);
        double normalization = 1 / (1 + c + sqrtPiInv * tanThetaI * Math.exp(-cotThetaI * cotThetaI));
        
        for (int it = 0; it < 10; it++)
        {
            // bisection criterion -- the boolean expression is to check for NaNs at
            // little additional cost
            if (!(b >= a && b <= c))
            {
                b = 0.5 * (a + c);
            }
            
            // evaluate CDF and derivatives (i.e. density function)
            double invErf = invErf(b);
            double value = normalization *
                           (1 + b + sqrtPiInv * tanThetaI * Math.exp(-invErf * invErf)) - samplex;
            double derivative = normalization * (1 - invErf * tanThetaI);
            
            if (Math.abs(value) < 1e-5)
            {
                break;
            }
            
            // update bisection intervals
            if (value > 0)
            {
                c = b;
            }
            else
            {
                a = b;
            }
            
            b -= value / derivative;
            
        }
        
        // convert back to slope
        double slopex = invErf(b);
        
        // simulate y component
        double slopey = invErf(2 * Math.max(u2, 1e-6) - 1.0);
        
        // XXX inf/nan checks?
        
        return new Pair<>(slopex, slopey);
    }
}
