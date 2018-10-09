package scene.materials.functions;

import static core.math.MathUtilities.INV_PI;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.absCosTheta;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.cosPhi;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.sinPhi;
import static scene.materials.functions.BidirectionalScatteringDistributionFunction.sinTheta;

import java.util.EnumSet;

import core.colors.RGBSpectrum;
import core.math.Direction3;

public class OrenNayar extends AbstractBidirectionalDistributionFunction
{
    private RGBSpectrum r;
    private double a;
    private double b;
    
    public OrenNayar(RGBSpectrum r, double sigma)
    {
        super(EnumSet.of(BxDFType.REFLECTION, BxDFType.DIFFUSE));
        this.r = r;
        
        sigma = Math.toRadians(sigma);
        double sigmaSquared = sigma * sigma;
        this.a = 1 - (sigmaSquared / (2 * (sigmaSquared + 0.33)));
        this.b = 0.45 * sigmaSquared / (sigmaSquared + 0.09);
    }

    @Override
    public RGBSpectrum f(Direction3 wo, Direction3 wi)
    {
        double sinThetaI = sinTheta(wi);
        double sinThetaO = sinTheta(wo);
        
        // compute cosine term
        double maxCos = 0;
        if (sinThetaI > 1e-4 && sinThetaO > 1e-4)
        {
            double sinPhiI = sinPhi(wi);
            double cosPhiI = cosPhi(wi);
            double sinPhiO = sinPhi(wo);
            double cosPhiO = cosPhi(wo);
            double dCos = cosPhiI * cosPhiO + sinPhiI * sinPhiO;
            maxCos = Math.max(0, dCos);
        }
        
        // compute sine and tangent terms
        double sinAlpha;
        double tanBeta;
        if (absCosTheta(wi) > absCosTheta(wo))
        {
            sinAlpha = sinThetaO;
            tanBeta = sinThetaI / absCosTheta(wi);
        }
        else
        {
            sinAlpha = sinThetaI;
            tanBeta = sinThetaO / absCosTheta(wo);
        }
        
        double scalingFactor = INV_PI * (a + b * maxCos * sinAlpha * tanBeta);
        return r.times(scalingFactor);
    }
}
