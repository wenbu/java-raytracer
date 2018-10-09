package scene.materials.functions;

import core.colors.RGBSpectrum;

public interface Fresnel
{
    RGBSpectrum evaluate(double cosI);
}
