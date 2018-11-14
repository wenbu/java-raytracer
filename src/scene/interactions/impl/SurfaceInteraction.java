package scene.interactions.impl;

import core.RayDifferential;
import core.colors.RGBSpectrum;
import core.math.Direction3;
import utilities.MatrixUtilities;
import core.math.Normal3;
import core.math.Point2;
import core.math.Point3;
import scene.geometry.Shape;
import scene.interactions.Interaction;
import scene.lights.AreaLight;
import scene.materials.TransportMode;
import scene.materials.functions.BidirectionalScatteringDistributionFunction;
import scene.primitives.Primitive;

public class SurfaceInteraction extends Interaction
{
    private Point2 uv;
    private final Direction3 dpdu, dpdv;
    private final Normal3 dndu, dndv;
    private final Shape shape;
    private final ShadingGeometry shadingGeometry;
    private Primitive primitive = null;
    private BidirectionalScatteringDistributionFunction bsdf = null;
    
    private Direction3 dpdx, dpdy;
    private double dudx, dudy, dvdx, dvdy;
    // private BSSRDF bssrdf = null;

    public SurfaceInteraction(Point3 p, Direction3 error, Point2 uv, Direction3 wo, Direction3 dpdu, Direction3 dpdv,
            Normal3 dndu, Normal3 dndv, double t, Shape shape)
    {
        super(p, new Normal3(dpdu.cross(dpdv).normalize()), error, wo, t, null);
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
        super(p, n, error, wo, t, null);
        this.uv = uv;
        this.dpdu = dpdu;
        this.dpdv = dpdv;
        this.dndu = dndu;
        this.dndv = dndv;
        this.shape = shape;
        this.shadingGeometry = new ShadingGeometry(shadingN, shadingDndu, shadingDndv, shadingDpdu, shadingDpdv);
    }

    /**
     * Shallow copy constructor.
     */
    public SurfaceInteraction(SurfaceInteraction s)
    {
        super(s.p, s.n, s.error, s.wo, s.t, s.mediumInterface);
        this.uv = s.uv;
        this.dpdu = s.dpdu;
        this.dpdv = s.dpdv;
        this.dndu = s.dndu;
        this.dndv = s.dndv;
        this.shape = s.shape;
        this.shadingGeometry = s.shadingGeometry;
        this.primitive = s.primitive;
        this.bsdf = s.bsdf;
        this.dpdx = s.dpdx;
        this.dpdy = s.dpdy;
        this.dudx = s.dudx;
        this.dudy = s.dudy;
        this.dvdx = s.dvdx;
        this.dvdy = s.dvdy;
    }
    
    public RGBSpectrum getEmittedRadiance(Direction3 w)
    {
        AreaLight areaLight = primitive.getAreaLight();
        if (areaLight != null)
        {
            return areaLight.radiance(this, w);
        }
        else
        {
            return new RGBSpectrum(0);
        }
    }
    
    public void computeScatteringFunctions(RayDifferential ray)
    {
        computeScatteringFunctions(ray, TransportMode.RADIANCE, false);
    }
    
    public void computeScatteringFunctions(RayDifferential ray, TransportMode mode, boolean allowMultipleLobes)
    {
        computeDifferentials(ray);
        primitive.computeScatteringFunctions(this, mode, allowMultipleLobes);
    }
    
    public void computeDifferentials(RayDifferential ray)
    {
        if (ray.hasDifferentials())
        {
            // estimate screen space change in p and (u, v)
            // use plane through p tangent to surface
            // compute auxiliary intersection points with plane
            double d = n.dot(new Direction3(p.x(), p.y(), p.z()));
            double tx = -(n.dot(new Direction3(ray.getRxOrigin())) - d) / n.dot(ray.getRxDirection());
            double ty = -(n.dot(new Direction3(ray.getRyOrigin())) - d) / n.dot(ray.getRyDirection());
            Point3 px = ray.getRxOrigin().plus(ray.getRxDirection().times(tx));
            Point3 py = ray.getRyOrigin().plus(ray.getRyDirection().times(ty));
            
            dpdx = px.minus(p);
            dpdy = py.minus(p);
            
            // compute (u, v) offsets at auxiliary points
            // choose two dimensions for ray offset computation
            int dim[] = new int[2];
            if (Math.abs(n.x()) > Math.abs(n.y()) && Math.abs(n.x()) > Math.abs(n.z()))
            {
                dim[0] = 1; dim[1] = 2;
            }
            else if (Math.abs(n.y()) > Math.abs(n.z()))
            {
                dim[0] = 0; dim[1] = 2;
            }
            else
            {
                dim[0] = 0; dim[1] = 1;
            }
            
            // initialize A, Bx, By matrices for offset computation
            double[][] A = { { dpdu.get(dim[0]), dpdv.get(dim[0]) },
                             { dpdu.get(dim[1]), dpdv.get(dim[1]) } };
            double[] Bx = { px.get(dim[0]) - p.get(dim[0]), px.get(dim[1]) - p.get(dim[1]) };
            double[] By = { py.get(dim[0]) - p.get(dim[0]), py.get(dim[1]) - p.get(dim[1]) };
            
            var solutionX = MatrixUtilities.solveLinearSystem2x2(A, Bx);
            if (solutionX == null)
            {
                dudx = 0;
                dvdx = 0;
            }
            else
            {
                dudx = solutionX.getFirst();
                dvdx = solutionX.getSecond();
            }
            var solutionY = MatrixUtilities.solveLinearSystem2x2(A, By);
            if (solutionY == null)
            {
                dudy = 0;
                dvdy = 0;
            }
            else
            {
                dudy = solutionY.getFirst();
                dvdy = solutionY.getSecond();
            }
        }
        else
        {
            dudx = 0;
            dvdx = 0;
            dudy = 0;
            dvdy = 0;
            dpdx = new Direction3(0, 0, 0);
            dpdy = new Direction3(0, 0, 0);
        }
    }

    public void setUv(Point2 uv)
    {
        this.uv = uv;
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
    
    public double getDudx()
    {
        return dudx;
    }
    
    public double getDudy()
    {
        return dudy;
    }
    
    public double getDvdx()
    {
        return dvdx;
    }
    
    public double getDvdy()
    {
        return dvdy;
    }
    
    public Direction3 getDpdx()
    {
        return dpdx;
    }
    
    public Direction3 getDpdy()
    {
        return dpdy;
    }

    public Shape getShape()
    {
        return shape;
    }
    
    public void setBsdf(BidirectionalScatteringDistributionFunction bsdf)
    {
        this.bsdf = bsdf;
    }
    
    public BidirectionalScatteringDistributionFunction getBsdf()
    {
        return bsdf;
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
    
    public void setPrimitive(Primitive primitive)
    {
        this.primitive = primitive;
    }
    
    public Primitive getPrimitive()
    {
        return primitive;
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
