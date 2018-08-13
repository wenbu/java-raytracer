package scene.interactions.impl;

import core.math.Direction3;
import core.math.Normal3;
import core.math.Point2;
import core.math.Point3;
import scene.interactions.Interaction;
import scene.shapes.Shape;

public class SurfaceInteraction extends Interaction
{
    private final Point2 uv;
    private final Direction3 dpdu, dpdv;
    private final Normal3 dndu, dndv;
    private final Shape shape;
    private final ShadingGeometry shadingGeometry;

    public SurfaceInteraction(Point3 p, Direction3 error, Point2 uv, Direction3 wo, Direction3 dpdu, Direction3 dpdv,
            Normal3 dndu, Normal3 dndv, double t, Shape shape)
    {
        super(p, new Normal3(dpdu.cross(dpdv).normalize()), error, wo, t);
        this.uv = uv;
        this.dpdu = dpdu;
        this.dpdv = dpdv;
        this.dndu = dndu;
        this.dndv = dndv;
        this.shape = shape;
        this.shadingGeometry = new ShadingGeometry(n, dndu, dndv, dpdu, dpdv);
        
        if (shouldReverseNormal())
        {
            n = n.times(-1);
            shadingGeometry.setN(n);
        }
    }
    
    public SurfaceInteraction(Point3 p, Direction3 error, Point2 uv, Direction3 wo, Normal3 n, Direction3 dpdu,
            Direction3 dpdv, Normal3 dndu, Normal3 dndv, double t, Shape shape, Normal3 shadingN, Normal3 shadingDndu,
            Normal3 shadingDndv, Direction3 shadingDpdu, Direction3 shadingDpdv)
    {
        super(p, n, error, wo, t);
        this.uv = uv;
        this.dpdu = dpdu;
        this.dpdv = dpdv;
        this.dndu = dndu;
        this.dndv = dndv;
        this.shape = shape;
        this.shadingGeometry = new ShadingGeometry(shadingN, shadingDndu, shadingDndv, shadingDpdu, shadingDpdv);
    }
    
    public Point2 getUv()
    {
        return uv;
    }

    public Direction3 getDpdu()
    {
        return dpdu;
    }

    public Direction3 getDpdv()
    {
        return dpdv;
    }

    public Normal3 getDndu()
    {
        return dndu;
    }

    public Normal3 getDndv()
    {
        return dndv;
    }

    public Shape getShape()
    {
        return shape;
    }

    public ShadingGeometry getShadingGeometry()
    {
        return shadingGeometry;
    }

    public void setShadingGeometry(Direction3 dpdus, Direction3 dpdvs, Normal3 dndus, Normal3 dndvs, boolean orientationIsAuthoritative)
    {
        shadingGeometry.setN(Normal3.getNormalizedNormal(dpdus.cross(dpdvs)));
        if (shouldReverseNormal())
        {
            shadingGeometry.setN(shadingGeometry.getN().times(-1));
        }
        
        if (orientationIsAuthoritative)
        {
            n = Normal3.faceForward(n, shadingGeometry.getN());
        }
        else
        {
            shadingGeometry.setN(Normal3.faceForward(shadingGeometry.getN(), n));
        }
        
        shadingGeometry.setDpdu(dpdus);
        shadingGeometry.setDpdv(dpdvs);
        shadingGeometry.setDndu(dndus);
        shadingGeometry.setDndv(dndvs);
    }
    
    private boolean shouldReverseNormal()
    {
        return shape != null && (shape.isHandednessSwapped() ^ shape.isOrientationReversed());
    }
    
    /*
     * Second instance of the surface normal and partial derivatives
     * to represent possible perturbed values (e.g. bumpmapping,
     * interpolated vertex normals)
     */
    public class ShadingGeometry
    {
        private Normal3 n, dndu, dndv;
        private Direction3 dpdu, dpdv;
        
        public ShadingGeometry(Normal3 n, Normal3 dndu, Normal3 dndv, Direction3 dpdu, Direction3 dpdv)
        {
            this.n = n;
            this.dndu = dndu;
            this.dndv = dndv;
            this.dpdu = dpdu;
            this.dpdv = dpdv;
        }

        public Normal3 getN()
        {
            return n;
        }

        public void setN(Normal3 n)
        {
            this.n = n;
        }

        public Normal3 getDndu()
        {
            return dndu;
        }

        public void setDndu(Normal3 dndu)
        {
            this.dndu = dndu;
        }

        public Normal3 getDndv()
        {
            return dndv;
        }

        public void setDndv(Normal3 dndv)
        {
            this.dndv = dndv;
        }

        public Direction3 getDpdu()
        {
            return dpdu;
        }

        public void setDpdu(Direction3 dpdu)
        {
            this.dpdu = dpdu;
        }

        public Direction3 getDpdv()
        {
            return dpdv;
        }

        public void setDpdv(Direction3 dpdv)
        {
            this.dpdv = dpdv;
        }
    }
}
