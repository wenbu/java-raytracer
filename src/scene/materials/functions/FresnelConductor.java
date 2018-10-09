package scene.materials.functions;

import core.colors.RGBSpectrum;
import utilities.ReflectionUtilities;

public class FresnelConductor implements Fresnel
{
    private final RGBSpectrum etaI;
    private final RGBSpectrum etaT;
    private final RGBSpectrum k;
    
    public FresnelConductor(RGBSpectrum etaI, RGBSpectrum etaT, RGBSpectrum k)
    {
        this.etaI = etaI;
        this.etaT = etaT;
        this.k = k;
    }

    @Override
    public RGBSpectrum evaluate(double cosThetaI)
    {
        return ReflectionUtilities.fresnelConductor(Math.abs(cosThetaI), etaI, etaT, k);
    }

}
