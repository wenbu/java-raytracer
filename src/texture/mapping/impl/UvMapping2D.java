package texture.mapping.impl;

import core.math.Direction2;
import core.math.Point2;
import core.tuple.Triple;
import scene.interactions.impl.SurfaceInteraction;
import texture.mapping.TextureMapping2D;

public class UvMapping2D implements TextureMapping2D
{
    private double su;
    private double sv;
    private double du;
    private double dv;
    
    public UvMapping2D(double su, double sv, double du, double dv)
    {
        this.su = su;
        this.sv = sv;
        this.du = du;
        this.dv = dv;
    }

    @Override
    public Triple<Point2, Direction2, Direction2> map(SurfaceInteraction si)
    {
        // compute texture differentials for (u, v) mapping
        Direction2 dstdx = new Direction2(su * si.getDudx(), sv * si.getDvdx());
        Direction2 dstdy = new Direction2(su * si.getDudy(), sv * si.getDvdy());
        
        Point2 st = new Point2(su * si.getUv().get(0) + du,
                               sv * si.getUv().get(1) + dv);
        
        return new Triple<>(st, dstdx, dstdy);
    }

}
