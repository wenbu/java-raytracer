package texture.impl;

import core.colors.RGBSpectrum;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;

public class ScaleTexture<T> implements Texture<T>
{
    private final Texture<Double> scale;
    private final Texture<T> texture;
    
    public ScaleTexture(Texture<Double> scale, Texture<T> texture)
    {
        this.scale = scale;
        this.texture = texture;
    }
    
    @Override
    public T evaluate(SurfaceInteraction surfaceInteraction)
    {
        T t = texture.evaluate(surfaceInteraction);
        double s = scale.evaluate(surfaceInteraction);
        
        if (t instanceof RGBSpectrum)
        {
            return (T) ((RGBSpectrum) t).times(s);
        }
        else if (t instanceof Double)
        {
            return (T) Double.valueOf(((Double) t).doubleValue() * s);
        }
        else
        {
            throw new RuntimeException("Unsupported type " + t.getClass().getName());
        }
    }

}
