package core.math;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class CoordinateSystem
{
    private final Direction v1;
    private final Direction v2;
    private final Direction v3;
    
    public CoordinateSystem(Direction v1, Direction v2, Direction v3)
    {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }
    
    public CoordinateSystem(Direction v)
    {
        v1 = v;
        
        if (abs(v.x()) > abs(v.y()))
        {
            double invLen = 1.0 / sqrt(v.x()*v.x() + v.z()*v.z());
            v2 = new Direction (-v.z() * invLen, 0, v.x() * invLen);
        }
        else
        {
            double invLen = 1.0 / sqrt(v.y()*v.y() + v.z()*v.z());
            v2 = new Direction(0, v.z() * invLen, -v.y() * invLen);
        }
        v3 = v1.cross(v2);
    }

    public Direction getV1()
    {
        return v1;
    }

    public Direction getV2()
    {
        return v2;
    }

    public Direction getV3()
    {
        return v3;
    }
}
