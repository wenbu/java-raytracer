package utilities;

import core.math.Direction3;
import core.math.Normal3;
import core.math.Point3;

public class GeometryUtilities
{
    public static final double SHADOW_EPSILON = 0.001;
    
    public static Point3 offsetRayOrigin(Point3 p, Direction3 pError, Normal3 n, Direction3 w)
    {
        double d = n.abs().dot(pError);
        Direction3 offset = new Direction3(n).times(d);
        if (w.dot(n) < 0)
        {
            offset = offset.times(-1);
        }
        Point3 po = p.plus(offset);
        
        // round po away from p
        double[] ps = new double[] { po.x(), po.y(), po.z() };
        for (int i = 0; i < 3; i++)
        {
            if (offset.get(i) > 0)
            {
                ps[i] = Math.nextUp(ps[i]);
            }
            else if (offset.get(i) < 0)
            {
                ps[i] = Math.nextDown(ps[i]);
            }
        }
        po = new Point3(ps[0], ps[1], ps[2]);
        return po;
    }
    
    public static Direction3 sphericalDirection(double sinTheta, double cosTheta, double phi)
    {
        return new Direction3(sinTheta * Math.cos(phi), sinTheta * Math.sin(phi), cosTheta);
    }
    
    public static Direction3 sphericalDirection(double sinTheta, double cosTheta, double phi,
            Direction3 x, Direction3 y, Direction3 z)
    {
        return x.times(sinTheta * Math.cos(phi))
                .plus(y.times(sinTheta * Math.sin(phi)))
                .plus(z.times(cosTheta));
    }
    
    public static double sphericalTheta(Direction3 v)
    {
        return Math.acos(MathUtilities.clamp(v.z(), -1, 1));
    }
    
    public static double sphericalPhi(Direction3 v)
    {
        double p = Math.atan2(v.y(), v.x());
        return (p < 0) ? (p + 2 * Math.PI) : p;
    }
}
