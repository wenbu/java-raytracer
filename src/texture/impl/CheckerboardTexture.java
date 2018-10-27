package texture.impl;

import core.math.Point2;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;
import texture.mapping.TextureMapping2D;

public class CheckerboardTexture<T> implements Texture<T>
{
    private final TextureMapping2D textureMapping;
    private final T value1;
    private final T value2;
    private final double scale;
    
    public CheckerboardTexture(TextureMapping2D textureMapping, T value1, T value2, double scale)
    {
        this.textureMapping = textureMapping;
        this.value1 = value1;
        this.value2 = value2;
        this.scale = scale;
    }

    @Override
    public T evaluate(SurfaceInteraction surfaceInteraction)
    {
        // TODO antialiasing
        Point2 st = textureMapping.map(surfaceInteraction).getFirst();
        double s = st.get(0);
        double t = st.get(1);
        
        if (s < 0)
        {
            s = -s + 1;
        }
        if (t < 0)
        {
            t = -t + 1;
        }
        
        if (((int) (s / scale)) % 2 == ((int) (t / scale)) % 2)
        {
            return value1;
        }
        else
        {
            return value2;
        }
    }
}
