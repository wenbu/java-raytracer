package texture.mapping.impl;

import core.math.Direction3;
import core.math.Point3;
import core.math.Transformation;
import core.tuple.Triple;
import scene.interactions.impl.SurfaceInteraction;
import texture.mapping.TextureMapping3D;

public class TransformMapping3D implements TextureMapping3D
{
    private final Transformation worldToTexture;
    
    public TransformMapping3D(Transformation worldToTexture)
    {
        this.worldToTexture = worldToTexture;
    }
    
    @Override
    public Triple<Point3, Direction3, Direction3> map(SurfaceInteraction si)
    {
        Point3 p = worldToTexture.transform(si.getP());
        Direction3 dpdx = worldToTexture.transform(si.getDpdx());
        Direction3 dpdy = worldToTexture.transform(si.getDpdy());
        return new Triple<>(p, dpdx, dpdy);
    }

}
