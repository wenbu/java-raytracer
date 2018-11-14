package texture.impl;

import core.colors.RGBSpectrum;
import core.math.Normal3;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;

public class NormalTexture implements Texture<RGBSpectrum>
{
    @Override
    public RGBSpectrum evaluate(SurfaceInteraction surfaceInteraction)
    {
        Normal3 normal = surfaceInteraction.getShadingGeometry().getN().normalized().timesEquals(0.5).plusEquals(new Normal3(0.5, 0.5, 0.5));
        return new RGBSpectrum(normal.x(), normal.y(), normal.z());
    }
}
