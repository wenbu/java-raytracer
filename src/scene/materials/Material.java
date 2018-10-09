package scene.materials;

import scene.interactions.impl.SurfaceInteraction;

public interface Material
{
    void computeScatteringFunctions(SurfaceInteraction si, TransportMode mode, boolean allowMultipleLobes);
}
