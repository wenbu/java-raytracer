package scene.interactions;

import core.Ray;
import core.math.Direction3;
import core.math.Normal3;
import core.math.Point3;
import utilities.GeometryUtilities;
import scene.medium.Medium;
import scene.medium.Medium.MediumInterface;

public class Interaction
{
    public final Point3 p;
    public final double t;
    public final Direction3 error;
    public final Direction3 wo;
    public MediumInterface mediumInterface;

    public Normal3 n;

    public Interaction(Point3 p, Normal3 n, Direction3 pError)
    {
        this(p, n, pError, null, 0, null);
    }
    
    public Interaction(Point3 p, double t, MediumInterface mediumInterface)
    {
        this(p, null, null, null, t, mediumInterface);
    }

    public Interaction(Point3 p, Direction3 wo, double t, MediumInterface mediumInterface)
    {
        this(p, null, null, wo, t, mediumInterface);
    }

    public Interaction(Point3 p, Normal3 n, Direction3 error, Direction3 wo, double t, MediumInterface mediumInterface)
    {
        this.p = p;
        this.n = n;
        this.error = error;
        this.wo = wo;
        this.t = t;
        this.mediumInterface = mediumInterface;
    }

    public Ray spawnRay(Direction3 direction)
    {
        Point3 origin = GeometryUtilities.offsetRayOrigin(p, error, n, direction);
        return new Ray(origin, direction, Double.POSITIVE_INFINITY, t, getMedium(direction));
    }

    public Ray spawnRayTo(Point3 p2)
    {
        Point3 origin = GeometryUtilities.offsetRayOrigin(p, error, n, p2.minus(p));
        Direction3 direction = p2.minus(origin);
        return new Ray(origin, direction, 1 - GeometryUtilities.SHADOW_EPSILON, t, getMedium(direction));
    }

    public Ray spawnRayTo(Interaction interaction)
    {
        Point3 p2 = interaction.getP();
        Point3 origin = GeometryUtilities.offsetRayOrigin(p, error, n, p2.minus(p));
        Direction3 direction = p2.minus(origin);
        return new Ray(origin, direction, 1 - GeometryUtilities.SHADOW_EPSILON, t, getMedium(direction));
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

    public void setN(Normal3 n)
    {
        this.n = n;
    }

    public Normal3 getN()
    {
        return n;
    }

    public void setMediumInterface(MediumInterface mediumInterface)
    {
        this.mediumInterface = mediumInterface;
    }

    public MediumInterface getMediumInterface()
    {
        return mediumInterface;
    }

    public Medium getMedium(Direction3 w)
    {
        assert (n != null);
        return w.dot(n) > 0 ? mediumInterface.getOutside() : mediumInterface.getInside();
    }

    public Medium getMedium()
    {
        assert (mediumInterface.getInside() == mediumInterface.getOutside());
        return mediumInterface.getInside();
    }
}
