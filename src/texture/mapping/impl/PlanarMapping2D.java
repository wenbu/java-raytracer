package texture.mapping.impl;

import core.math.Direction2;
import core.math.Direction3;
import core.math.Point2;
import core.tuple.Triple;
import scene.interactions.impl.SurfaceInteraction;
import texture.mapping.TextureMapping2D;

public class PlanarMapping2D implements TextureMapping2D
{
    private final Direction3 vs;
    private final Direction3 vt;
    private final double ds;
    private final double dt;
    
    public PlanarMapping2D(Direction3 vs, Direction3 vt)
    {
        this(vs, vt, 0, 0);
    }
    
    public PlanarMapping2D(Direction3 vs, Direction3 vt, double ds, double dt)
    {
        this.vs = vs;
        this.vt = vt;
        this.ds = ds;
        this.dt = dt;
    }
    
    @Override
    public Triple<Point2, Direction2, Direction2> map(SurfaceInteraction si)
    {
        Direction3 v = new Direction3(si.getP());
        Direction2 dstdx = new Direction2(si.getDpdx().dot(vs), si.getDpdx().dot(vt));
        Direction2 dstdy = new Direction2(si.getDpdy().dot(vs), si.getDpdy().dot(vt));
        Point2 st = new Point2(ds + v.dot(vs), dt + v.dot(vt));
        
        return new Triple<>(st, dstdx, dstdy);
    }

}
