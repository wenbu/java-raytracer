package texture.impl;

import core.colors.RGBSpectrum;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;

public class MixTexture<T> implements Texture<T>
{
    private final Texture<T> texture1;
    private final Texture<T> texture2;
    private final Texture<Double> amount;
    
    public MixTexture(Texture<T> texture1, Texture<T> texture2, Texture<Double> amount)
    {
        this.texture1 = texture1;
        this.texture2 = texture2;
        this.amount = amount;
    }
    
    @Override
    public T evaluate(SurfaceInteraction surfaceInteraction)
    {
        T t1 = texture1.evaluate(surfaceInteraction);
        T t2 = texture2.evaluate(surfaceInteraction);
        double amt = amount.evaluate(surfaceInteraction);
        
        // is there some way to avoid type checking and casting like this?
        if (t1 instanceof RGBSpectrum)
        {
            RGBSpectrum s1 = (RGBSpectrum) t1;
            RGBSpectrum s2 = (RGBSpectrum) t2;
            return (T) s1.times(1 - amt).plus(s2.times(amt));
        }
        else if (t1 instanceof Double)
        {
            double d1 = ((Double) t1).doubleValue();
            double d2 = ((Double) t2).doubleValue();
            return (T) Double.valueOf((1 - amt) * d1 + amt * d2);
        }
        else
        {
            throw new RuntimeException("Unsupported type " + t1.getClass().getName());
        }
    }

}
