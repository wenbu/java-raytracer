package texture.mapping;

import core.math.Direction3;
import core.math.Point3;
import core.tuple.Triple;
import scene.interactions.impl.SurfaceInteraction;

public interface TextureMapping3D
{
    /**
     * Returns:
     * <ul>
     * <li>p in texture space</li>
     * <li>dpdx in texture space</li>
     * <li>dpdy in texture space</li>
     * </ul>
     */
    Triple<Point3, Direction3, Direction3> map(SurfaceInteraction si);
}
