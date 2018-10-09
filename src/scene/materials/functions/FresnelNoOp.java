package scene.materials.functions;

import core.colors.RGBSpectrum;

public class FresnelNoOp implements Fresnel
{

    @Override
    public RGBSpectrum evaluate(double cosI)
    {
        return new RGBSpectrum(1);
    }

}
