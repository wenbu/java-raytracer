package texture.impl;

import core.colors.RGBSpectrum;
import core.math.Point2;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;
import texture.mapping.TextureMapping2D;

public class BilinearInterpolatedTexture<T> implements Texture<T>
{
    private final TextureMapping2D mapping;
    private final T v00;
    private final T v01;
    private final T v10;
    private final T v11;

    public BilinearInterpolatedTexture(TextureMapping2D mapping, T v00, T v01, T v10, T v11)
    {
        this.mapping = mapping;
        this.v00 = v00;
        this.v01 = v01;
        this.v10 = v10;
        this.v11 = v11;
    }

    @Override
    public T evaluate(SurfaceInteraction surfaceInteraction)
    {
        Point2 st = mapping.map(surfaceInteraction).getFirst();
        double s = st.get(0);
        double t = st.get(1);

        if (v00 instanceof RGBSpectrum)
        {
            RGBSpectrum c00 = (RGBSpectrum) v00;
            RGBSpectrum c01 = (RGBSpectrum) v01;
            RGBSpectrum c10 = (RGBSpectrum) v10;
            RGBSpectrum c11 = (RGBSpectrum) v11;

            RGBSpectrum r = c00.times((1 - s) * (1 - t))
                          .plus(c01.times((1 - s) * t))
                          .plus(c10.times(s * (1 - t)))
                          .plus(c11.times(s * t));
            return (T) r;
        }
        else if (v00 instanceof Double)
        {
            double d00 = ((Double) v00).doubleValue();
            double d01 = ((Double) v01).doubleValue();
            double d10 = ((Double) v10).doubleValue();
            double d11 = ((Double) v11).doubleValue();

            return (T) Double.valueOf((1 - s) * (1 - t) * d00 + (1 - s) * t * d01 +
                                      s * (1 - t) * d10 + s * t * d11);
        }
        else
        {
            throw new RuntimeException("Unsupported type " + v00.getClass().getName());
        }
    }
}
