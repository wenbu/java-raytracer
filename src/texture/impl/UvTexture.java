package texture.impl;

import core.colors.RGBSpectrum;
import core.math.Direction2;
import core.math.Point2;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;
import texture.mapping.TextureMapping2D;

public class UvTexture implements Texture<RGBSpectrum>
{
    private final TextureMapping2D mapping;

    public UvTexture(TextureMapping2D mapping)
    {
        this.mapping = mapping;
    }

    @Override
    public RGBSpectrum evaluate(SurfaceInteraction surfaceInteraction)
    {
        var p = mapping.map(surfaceInteraction);
        Point2 st = p.getFirst();
        Direction2 dstdx = p.getSecond();
        Direction2 dstdy = p.getThird();
        double[] rgb = new double[] { st.get(0) - Math.floor(st.get(0)),
                                      st.get(1) - Math.floor(st.get(1)), 0 };
        return RGBSpectrum.fromRGB(rgb);
    }

}
