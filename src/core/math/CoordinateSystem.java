package core.math;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class CoordinateSystem
{
    private final Direction3 v1;
    private final Direction3 v2;
    private final Direction3 v3;
    
    public CoordinateSystem(Direction3 v1, Direction3 v2, Direction3 v3)
    {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }
    
    public CoordinateSystem(Vector3 v)
    {
        v1 = new Direction3(v);
        
        if (abs(v.x()) > abs(v.y()))
        {
            double invLen = 1.0 / sqrt(v.x()*v.x() + v.z()*v.z());
            v2 = new Direction3 (-v.z() * invLen, 0, v.x() * invLen);
        }
        else
        {
            double invLen = 1.0 / sqrt(v.y()*v.y() + v.z()*v.z());
            v2 = new Direction3(0, v.z() * invLen, -v.y() * invLen);
        }
        v3 = v1.cross(v2);
    }

    public Direction3 getV1()
    {
        return v1;
    }

    public Direction3 getV2()
    {
        return v2;
    }

    public Direction3 getV3()
    {
        return v3;
    }
}
