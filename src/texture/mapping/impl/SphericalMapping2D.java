package texture.mapping.impl;

import core.math.Direction2;
import core.math.Direction3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.tuple.Triple;
import scene.interactions.impl.SurfaceInteraction;
import texture.mapping.TextureMapping2D;
import utilities.GeometryUtilities;
import utilities.MathUtilities;

public class SphericalMapping2D implements TextureMapping2D
{
    private final Transformation worldToTexture;
    
    public SphericalMapping2D(Transformation worldToTexture)
    {
        this.worldToTexture = worldToTexture;
    }

    @Override
    public Triple<Point2, Direction2, Direction2> map(SurfaceInteraction si)
    {
        Point2 st = sphere(si.getP());
        // compute texture coordinate differentials
        double delta = 0.1;
        Point2 stDeltaX = sphere(si.getP().plus(si.getDpdx().times(delta)));
        Point2 stDeltaY = sphere(si.getP().plus(si.getDpdy().times(delta)));
        Direction2 dstdx = stDeltaX.minus(st).divide(delta);
        Direction2 dstdy = stDeltaY.minus(st).divide(delta);
        
        // handle sphere mapping discontinuity -- seam at t=1
        if (dstdx.get(1) > 0.5)
        {
            dstdx.set(1, 1 - dstdx.get(1));
        }
        else if (dstdx.get(1) < -0.5)
        {
            dstdx.set(1, -(dstdx.get(1) + 1));
        }
        if (dstdy.get(1) > 0.5)
        {
            dstdy.set(1, 1 - dstdy.get(1));
        }
        else if (dstdy.get(1) < -0.5)
        {
            dstdy.set(1, -(dstdy.get(1) + 1));
        }
        return new Triple<>(st, dstdx, dstdy);
    }

    private Point2 sphere(Point3 p)
    {
        Direction3 v = worldToTexture.transform(p).minus(new Point3(0, 0, 0)).normalize();
        double theta = GeometryUtilities.sphericalTheta(v);
        double phi = GeometryUtilities.sphericalPhi(v);
        return new Point2(theta * MathUtilities.INV_PI, phi * MathUtilities.INV_PI);
    }
}
