package texture.mapping.impl;

import core.math.Direction2;
import core.math.Direction3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.tuple.Triple;
import scene.interactions.impl.SurfaceInteraction;
import texture.mapping.TextureMapping2D;
import utilities.MathUtilities;

public class CylindricalMapping2D implements TextureMapping2D
{
    private final Transformation worldToTexture;
    
    public CylindricalMapping2D(Transformation worldToTexture)
    {
        this.worldToTexture = worldToTexture;
    }

    @Override
    public Triple<Point2, Direction2, Direction2> map(SurfaceInteraction si)
    {
        Point2 st = cylinder(si.getP());
        // compute texture coordinate differentials
        double delta = 0.1;
        Point2 stDeltaX = cylinder(si.getP().plus(si.getDpdx().times(delta)));
        Point2 stDeltaY = cylinder(si.getP().plus(si.getDpdy().times(delta)));
        Direction2 dstdx = stDeltaX.minus(st).divide(delta);
        Direction2 dstdy = stDeltaY.minus(st).divide(delta);
        
        return new Triple<>(st, dstdx, dstdy);
    }

    private Point2 cylinder(Point3 p)
    {
        Direction3 v = worldToTexture.transform(p).minus(new Point3(0, 0, 0)).normalize();
        return new Point2((Math.PI + Math.atan2(v.y(), v.x())) * MathUtilities.INV_2PI, v.z());
    }
}
