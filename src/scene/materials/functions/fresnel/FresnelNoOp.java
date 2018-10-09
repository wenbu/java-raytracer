package scene.materials.functions.fresnel;

import core.colors.RGBSpectrum;
import scene.materials.functions.Fresnel;

public class FresnelNoOp implements Fresnel
{

    @Override
    public RGBSpectrum evaluate(double cosI)
    {
        return new RGBSpectrum(1);
    }

}
