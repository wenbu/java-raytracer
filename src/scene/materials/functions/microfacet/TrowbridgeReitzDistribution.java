package scene.materials.functions.microfacet;

import static utilities.GeometryUtilities.*;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.*;
import core.math.Direction3;
import core.math.Point2;
import core.tuple.Pair;
import scene.materials.functions.MicrofacetDistribution;

public class TrowbridgeReitzDistribution extends MicrofacetDistribution
{
    private final double alphax;
    private final double alphay;

    public TrowbridgeReitzDistribution(double alphax, double alphay)
    {
        this(alphax, alphay, true);
    }
    
    public TrowbridgeReitzDistribution(double alphax, double alphay, boolean sampleVisibleArea)
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
        double e = (cos2Phi(wh) / (alphax * alphax) + sin2Phi(wh) / (alphay * alphay)) * tan2Theta;
        return 1 / (Math.PI * alphax * alphay * cos4Theta * (1 + e) * (1 + e));
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
        double alpha2Tan2Theta = (alpha * absTanTheta) * (alpha * absTanTheta);
        return (-1 + Math.sqrt(1.f + alpha2Tan2Theta)) / 2;
    }

    // XXX there has to be a better place for this
    public static double roughnessToAlpha(double roughness)
    {
        roughness = Math.max(roughness, 1e-3);
        double x = Math.log(roughness);
        return 1.62142 + 0.819955 * x + 0.1734 * x * x + 0.0171201 * x * x * x +
               0.000640711 * x * x * x * x;
    }

    @Override
    public Direction3 sampleNormalDistribution(Direction3 wo, Point2 u)
    {
        Direction3 wh;
        if (!sampleVisibleArea)
        {
            double cosTheta = 0;
            double phi = 2 * Math.PI * u.get(1);
            if (alphax == alphay)
            {
                double tanTheta2 = alphax * alphax * u.get(0) / (1 - u.get(0));
                cosTheta = 1 / Math.sqrt(1 + tanTheta2);
            }
            else
            {
                phi = Math.atan(alphay / alphax * Math.tan(2 * Math.PI * u.get(1) + 0.5 * Math.PI));
                if (u.get(1) > 0.5)
                {
                    phi += Math.PI;
                }
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);
                double alphax2 = alphax * alphax;
                double alphay2 = alphay * alphay;
                double alpha2 = 1 / (cosPhi * cosPhi / alphax2 + sinPhi * sinPhi / alphay2);
                double tanTheta2 = alpha2 * u.get(0) / (1 - u.get(0));
                cosTheta = 1 / Math.sqrt(1 + tanTheta2);
            }
            double sinTheta = Math.sqrt(Math.max(0, 1 - cosTheta * cosTheta));
            wh = sphericalDirection(sinTheta, cosTheta, phi);
            if (!sameHemisphere(wo, wh))
            {
                wh.timesEquals(-1);
            }
        }
        else
        {
            boolean flip = wo.z() < 0;
            wh = trowbridgeReitzSample(flip? wo.times(-1) : wo, alphax, alphay, u.get(0), u.get(1));
            if (flip)
            {
                wh.timesEquals(-1);
            }
        }
        return wh;
    }
    
    private static Direction3 trowbridgeReitzSample(Direction3 wi, double alphax, double alphay, double u1, double u2)
    {
        // stretch wi
        Direction3 wiStretched = new Direction3(alphax * wi.x(), alphay * wi.y(), wi.z()).normalize();
        
        // simulate P22_wi(slopex, slopey, 1, 1)
        var sample = trowbridgeReitzSample11(cosTheta(wiStretched), u1, u1);
        double slopex = sample.getFirst();
        double slopey = sample.getSecond();
        
        // rotate
        double tmp = cosPhi(wiStretched) * slopex - sinPhi(wiStretched) * slopey;
        slopey = sinPhi(wiStretched) * slopex + cosPhi(wiStretched) * slopey;
        slopex = tmp;
        
        // unstretch
        slopex = alphax * slopex;
        slopey = alphay * slopey;
        
        // compute normal
        return new Direction3(-slopex, slopey, 1).normalize();
    }
    
    private static Pair<Double, Double> trowbridgeReitzSample11(double cosTheta, double u1, double u2)
    {
        // special case -- normal incidence
        if (cosTheta > 0.9999)
        {
            double r = Math.sqrt(u1 / (1 - u1));
            double phi = 6.28318530718 * u2;
            return new Pair<>(r * Math.cos(phi), r * Math.sin(phi));
        }
        
        double sinTheta = Math.sqrt(Math.max(0, 1 - cosTheta * cosTheta));
        double tanTheta = sinTheta / cosTheta;
        double a = 1 / tanTheta;
        double G1 = 2 / (1 + Math.sqrt(1 + 1 / (a * a)));
        
        // sample slopex
        double A = 2 * u1 / G1 - 1;
        double tmp = 1 / (A * A - 1);
        if (tmp > 1e10)
        {
            tmp = 1e10;
        }
        double B = tanTheta;
        double D = Math.sqrt(Math.max(B * B * tmp * tmp - (A * A - B * B) * tmp, 0));
        double slopex1 = B * tmp - D;
        double slopex2 = B * tmp + D;
        double slopex = (A < 0 || slopex2 > 1 / tanTheta) ? slopex1 : slopex2;
        
        // sample slopey
        double S;
        if (u2 > 0.5)
        {
            S = 1;
            u2 = 2 * (u2 - 0.5);
        }
        else
        {
            S = -1;
            u2 = 2 * (0.5 - u2);
        }
        double z = (u2 * (u2 * (u2 * 0.27385 - 0.73369) + 0.46341)) /
                   (u2 * (u2 * (u2 * 0.093073 + 0.309420) - 1) + 0.597999);
        double slopey = S * z * Math.sqrt(1 + slopex * slopex);
        
        return new Pair<>(slopex, slopey);
    }
}
