package texture;

import scene.interactions.impl.SurfaceInteraction;

public interface Texture<T>
{
    T evaluate(SurfaceInteraction surfaceInteraction);
}
