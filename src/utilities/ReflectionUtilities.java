package utilities;

import core.colors.RGBSpectrum;
import core.math.MathUtilities;

public class ReflectionUtilities
{
    // Index of refraction (η) for dielectrics is real-valued.
    public static double fresnelDielectric(double cosThetaI, double etaI, double etaT)
    {
        cosThetaI = MathUtilities.clamp(cosThetaI, -1, 1);
        
        // swap indices of refraction if needed
        boolean entering = cosThetaI >  0;
        if (!entering)
        {
            var tmp = etaI;
            etaI = etaT;
            etaT = tmp;
            cosThetaI = Math.abs(cosThetaI);
        }
        
        // compute cosThetaT with Snell's law
        double sinThetaI = Math.sqrt(Math.max(0, 1 - cosThetaI * cosThetaI));
        double sinThetaT = etaI / etaT * sinThetaI;
        // handle total internal reflection
        if (sinThetaI >= 1)
        {
            return 1;
        }
        double cosThetaT = Math.sqrt(Math.max(0, 1 - sinThetaT * sinThetaT));

        double rParl = ((etaT * cosThetaI) / (etaI * cosThetaT)) / ((etaT * cosThetaI) + (etaI * cosThetaT));
        double rPerp = ((etaI * cosThetaI) / (etaT * cosThetaT)) / ((etaI * cosThetaI) + (etaT * cosThetaT));
        return (rParl * rParl + rPerp * rPerp) / 2;
    }
    
    // Index of refraction of conductors is complex (ῆ = η + ik) and generally varies by wavelength.
    // k is the absorption coefficient.
    public static RGBSpectrum fresnelConductor(double cosThetaI, RGBSpectrum etaI, RGBSpectrum etaT, RGBSpectrum k)
    {
        cosThetaI = MathUtilities.clamp(cosThetaI, -1, 1);
        RGBSpectrum eta = etaT.divideBy(etaI);
        RGBSpectrum etaK = k.divideBy(etaI);

        double cosThetaI2 = cosThetaI * cosThetaI;
        double sinThetaI2 = 1 - cosThetaI2;
        RGBSpectrum eta2 = eta.times(eta);
        RGBSpectrum etaK2 = etaK.times(etaK);

        RGBSpectrum t0 = eta2.minus(etaK2).minus(sinThetaI2);
        RGBSpectrum a2b2 = t0.times(t0).plus(eta2.times(etaK2).times(4)).sqrt();
        RGBSpectrum t1 = a2b2.times(cosThetaI2);
        RGBSpectrum a = a2b2.plus(t0).times(0.5).sqrt();
        RGBSpectrum t2 = a.times(2 * cosThetaI);
        RGBSpectrum rs = t1.minus(t2).divideBy(t1.plus(t2));

        RGBSpectrum t3 = a2b2.times(cosThetaI2).plus(sinThetaI2 * sinThetaI2);
        RGBSpectrum t4 = t2.times(sinThetaI2);
        RGBSpectrum rp = rs.times(t3.minus(t4)).divideBy(t3.plus(t4));

        return rp.plus(rs).times(0.5);
    }
}
