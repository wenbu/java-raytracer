package texture.mapping;

import core.math.Direction2;
import core.math.Point2;
import core.tuple.Triple;
import scene.interactions.impl.SurfaceInteraction;

public interface TextureMapping2D
{
    /**
     * Returns:
     * <ul>
     * <li>the (s, t) texture coordinates</li>
     * <li>dstdx</li>
     * <li>dstdy</li>
     * </ul>
     */
    Triple<Point2, Direction2, Direction2> map(SurfaceInteraction si);
}
