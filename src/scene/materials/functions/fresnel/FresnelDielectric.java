package scene.materials.functions.fresnel;

import core.colors.RGBSpectrum;
import scene.materials.functions.Fresnel;
import utilities.ReflectionUtilities;

public class FresnelDielectric implements Fresnel
{
    private final double etaI;
    private final double etaT;
    
    public FresnelDielectric(double etaI, double etaT)
    {
        this.etaI = etaI;
        this.etaT = etaT;
    }
    
    @Override
    public RGBSpectrum evaluate(double cosThetaI)
    {
        return new RGBSpectrum(ReflectionUtilities.fresnelDielectric(cosThetaI, etaI, etaT));
    }

}
