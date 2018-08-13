package scene.interactions;

import core.math.Direction3;
import core.math.Normal3;
import core.math.Point3;

public abstract class Interaction
{
    public final Point3 p;
    public final double t;
    public final Direction3 error;
    public final Direction3 wo;
    
    public Normal3 n;
    
    public Interaction(Point3 p, Normal3 n, Direction3 error, Direction3 wo, double t/*MediumInterface medium*/)
    {
        this.p = p;
        this.n = n;
        this.error = error;
        this.wo = wo;
        this.t = t;
    }

    public Point3 getP()
    {
        return p;
    }

    public double getT()
    {
        return t;
    }

    public Direction3 getError()
    {
        return error;
    }

    public Direction3 getWo()
    {
        return wo;
    }

    public Normal3 getN()
    {
        return n;
    }
}
