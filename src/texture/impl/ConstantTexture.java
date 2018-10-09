package texture.impl;

import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;

public class ConstantTexture<T> implements Texture<T>
{
    private final T value;
    
    public ConstantTexture(T value)
    {
        this.value = value;
    }

    @Override
    public T evaluate(SurfaceInteraction surfaceInteraction)
    {
        return value;
    }
}
