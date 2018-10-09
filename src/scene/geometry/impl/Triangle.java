package scene.geometry.impl;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import core.Ray;
import core.math.CoordinateSystem;
import core.math.Direction2;
import core.math.Direction3;
import core.math.MathUtilities;
import core.math.Normal3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.space.BoundingBox;
import core.tuple.Pair;
import core.tuple.Triple;
import scene.geometry.Shape;
import scene.interactions.impl.SurfaceInteraction;

public class Triangle extends Shape
{
    private final TriangleMesh mesh;
    private final int pointIndex;
    
    public Triangle(Transformation objectToWorld, Transformation worldToObject, boolean reverseOrientation,
            TriangleMesh mesh, int triNumber)
    {
        super(objectToWorld, worldToObject, reverseOrientation);
        this.mesh =  mesh;
        pointIndex = mesh.getIndex(3 * triNumber);
    }
    
    public static List<Triangle> createTriangleMesh(Transformation objectToWorld, Transformation worldToObject,
            boolean reverseOrientation, int numTriangles, int[] vertexIndices, int numVertices, Point3[] p,
            Direction3[] s, Normal3[] n, Point2[] uv/* , Texture alphaMask */)
    {
        TriangleMesh mesh = new TriangleMesh(objectToWorld, numTriangles, vertexIndices, numVertices, p, s, n, uv);
        List<Triangle> triangles = new LinkedList<>();
        for (int i = 0; i < numTriangles; i++)
        {
            triangles.add(new Triangle(objectToWorld, worldToObject, reverseOrientation, mesh, i));
        }
        return triangles;
    }

    @Override
    public BoundingBox objectBound()
    {
        Point3 p0 = mesh.getPoint(pointIndex);
        Point3 p1 = mesh.getPoint(pointIndex + 1);
        Point3 p2 = mesh.getPoint(pointIndex + 2);

        return new BoundingBox(worldToObject.transform(p0),
                               worldToObject.transform(p1)).union(worldToObject.transform(p2));
    }
    
    @Override
    public BoundingBox worldBound()
    {
        Point3 p0 = mesh.getPoint(pointIndex);
        Point3 p1 = mesh.getPoint(pointIndex + 1);
        Point3 p2 = mesh.getPoint(pointIndex + 2);
        
        return new BoundingBox(p0, p1).union(p2);
    }

    @Override
    public Pair<Double, SurfaceInteraction> intersect(Ray ray, boolean testAlpha)
    {
        Point3 p0 = mesh.getPoint(pointIndex);
        Point3 p1 = mesh.getPoint(pointIndex + 1);
        Point3 p2 = mesh.getPoint(pointIndex + 2);
        
        var isect = doIntersectionTest(new Triple<>(p0, p1, p2), ray);
        if (isect == null)
        {
            return null;
        }
        var barycentricCoordinates = isect.getFirst();
        double b0 = barycentricCoordinates.getFirst();
        double b1 = barycentricCoordinates.getSecond();
        double b2 = barycentricCoordinates.getThird();
        double t = isect.getSecond();
        Point2[] uv = getUVs();
        var partialDerivatives = computePartialDerivatives(p0, p1, p2, uv);
        Direction3 dpdu = partialDerivatives.getFirst();
        Direction3 dpdv = partialDerivatives.getSecond();
        
        Point3 pHit = p0.times(b0).plus(p1.times(b1)).plus(p2.times(b2));
        Point2 uvHit = uv[0].times(b0).plus(uv[1].times(b1)).plus(uv[2].times(b2));
        
        // TODO: alpha test

        double xAbsSum = Math.abs(b0 * p0.x()) + Math.abs(b1 * p1.x()) + Math.abs(b2 * p2.x());
        double yAbsSum = Math.abs(b0 * p0.y()) + Math.abs(b1 * p1.y()) + Math.abs(b2 * p2.y());
        double zAbsSum = Math.abs(b0 * p0.z()) + Math.abs(b1 * p1.z()) + Math.abs(b2 * p2.z());
        Direction3 error = new Direction3(xAbsSum, yAbsSum, zAbsSum).times(MathUtilities.gamma(7));
        
        SurfaceInteraction surfaceInteraction = new SurfaceInteraction(pHit,
                                                                       error,
                                                                       uvHit,
                                                                       ray.getDirection().times(-1),
                                                                       dpdu,
                                                                       dpdv,
                                                                       new Normal3(0, 0, 0),
                                                                       new Normal3(0, 0, 0),
                                                                       ray.getTime(),
                                                                       this);
        // SurfaceInteraction initializes the normal as dpdu x dpdv. Override as meshes
        // may have bad UV parameterizations.
        Normal3 overrideNormal = new Normal3(p0.minus(p2).cross(p1.minus(p2)).normalize());
        surfaceInteraction.setN(overrideNormal);
        surfaceInteraction.getShadingGeometry().setN(overrideNormal);
        
        Normal3 ns = computeShadingNormal(b0, b1, b2, surfaceInteraction.getN());
        Direction3 ss = computeShadingTangent(b0, b1, b2, surfaceInteraction.getDpdu());
        Direction3 ts = ss.cross(ns);
        if (ts.lengthSquared() > 0)
        {
            ts.normalize();
            ss = ts.cross(ns);
        }
        else
        {
            // degenerate case
            CoordinateSystem c = new CoordinateSystem(ns);
            ss = c.getV2();
            ts = c.getV3();
        }
        
        var normalDerivatives = computeNormalPartialDerivatives(uv);
        Normal3 dndu = normalDerivatives.getFirst();
        Normal3 dndv = normalDerivatives.getSecond();
        
        surfaceInteraction.setShadingGeometry(ss, ts, dndu, dndv, true);
        
        // If interpolated normals are available, they are the most authoritative source
        // of orientation information.
        if (mesh.getN() != null)
        {
            surfaceInteraction.setN(Normal3.faceForward(surfaceInteraction.getN(),
                                                        surfaceInteraction.getShadingGeometry().getN()));
        }
        else if (reverseOrientation ^ swapsHandedness)
        {
            surfaceInteraction.setN(surfaceInteraction.getN().times(-1));
            surfaceInteraction.getShadingGeometry().setN(surfaceInteraction.getN());
        }
        return new Pair<>(t, surfaceInteraction);
    }
    
    private Pair<Triple<Double, Double, Double>, Double> doIntersectionTest(Triple<Point3, Point3, Point3> points, Ray ray)
    {
        Point3 p0t = points.getFirst().minus(new Direction3(ray.getOrigin()));
        Point3 p1t = points.getSecond().minus(new Direction3(ray.getOrigin()));
        Point3 p2t = points.getThird().minus(new Direction3(ray.getOrigin()));
        
        // permute dimensions so that z has the highest magnitude
        int kz = ray.getDirection().abs().maxDimension();
        int kx = (kz + 1) % 3;
        int ky = (kx + 1) % 3;
        Direction3 d = ray.getDirection().permute(kx, ky, kz);
        p0t = p0t.permute(kx, ky, kz);
        p1t = p1t.permute(kx, ky, kz);
        p2t = p2t.permute(kx, ky, kz);
        
        // apply shear transformation to align ray direction with z+
        double Sx = -d.x() / d.z();
        double Sy = -d.y() / d.z();
        double Sz = 1.0 / d.z();
        p0t.setX(p0t.x() + Sx * p0t.z());
        p0t.setY(p0t.y() + Sy * p0t.z());
        p1t.setX(p1t.x() + Sx * p1t.z());
        p1t.setY(p1t.y() + Sy * p1t.z());
        p2t.setX(p2t.x() + Sx * p2t.z());
        p2t.setY(p2t.y() + Sy * p2t.z());
        // XXX for higher performance, consider precomputing these values and storing
        // in Ray
        
        // compute edge function coefficients
        double e0 = p1t.x() * p2t.y() - p1t.y() * p2t.x();
        double e1 = p2t.x() * p0t.y() - p2t.y() * p0t.x();
        double e2 = p0t.x() * p1t.y() - p0t.y() * p1t.x();
        // we have a valid intersection if all coefficients have the same sign and their
        // sum is nonzero
        if ((e0 < 0 || e1 < 0 || e2 < 0) && (e0 > 0 || e1 > 0 || e2 > 0))
        {
            return null;
        }
        double determinant = e0 + e1 + e2;
        if (determinant == 0)
        {
            return null;
        }
        
        // compute scaled hit distance to triangle and test against ray t range
        p0t.setZ(p0t.z() * Sz);
        p1t.setZ(p1t.z() * Sz);
        p2t.setZ(p2t.z() * Sz);
        double tScaled = e0 * p0t.z() + e1 * p1t.z() + e2 * p2t.z();
        if (determinant < 0 && (tScaled >= 0 || tScaled < ray.getTMax() * determinant))
        {
            return null;
        }
        else if (determinant > 0 && (tScaled <= 0 || tScaled > ray.getTMax() * determinant))
        {
            return null;
        }
        
        // we know there is a valid intersection at this point
        // compute barycentric coordinates and t value
        double invDet = 1 / determinant;
        double b0 = e0 * invDet;
        double b1 = e1 * invDet;
        double b2 = e2 * invDet;
        double t = tScaled * invDet;
        
        // ensure, conservatively, that t > 0
        double maxZt = new Direction3(p0t.z(), p1t.z(), p2t.z()).abs().maxComponent();
        double deltaZ = MathUtilities.gamma(3) * maxZt;
        double maxXt = new Direction3(p0t.x(), p1t.x(), p2t.x()).abs().maxComponent();
        double maxYt = new Direction3(p0t.y(), p1t.y(), p2t.y()).abs().maxComponent();
        double deltaX = MathUtilities.gamma(5) * (maxXt + maxZt);
        double deltaY = MathUtilities.gamma(5) * (maxYt + maxZt);
        double deltaE = 2 * (MathUtilities.gamma(2) * maxXt * maxYt + deltaY * maxXt + deltaX * maxYt);
        double maxE = new Direction3(e0, e1, e2).abs().maxComponent();
        double deltaT = 3 * (MathUtilities.gamma(3) * maxE * maxZt + deltaE * maxZt + deltaZ * maxE) * Math.abs(invDet);
        if (t <= deltaT)
        {
            return null;
        }
        
        Triple<Double, Double, Double> barycentricCoordinates = new Triple<>(b0, b1, b2);
        return new Pair<>(barycentricCoordinates, t);
    }
    
    private Pair<Direction3, Direction3> computePartialDerivatives(Point3 p0, Point3 p1, Point3 p2, Point2[] uv)
    {
        Direction3 dpdu, dpdv;
        Direction2 duv02 = uv[0].minus(uv[2]);
        Direction2 duv12 = uv[1].minus(uv[2]);
        Direction3 dp02 = p0.minus(p2);
        Direction3 dp12 = p1.minus(p2);
        double determinant = duv02.x() * duv12.y() - duv02.y() * duv12.x();
        if (determinant == 0)
        {
            // handle degenerate case -- choose arbitrary orthonormal coordinate system
            // about normal
            Direction3 n = p2.minus(p0).cross(p1.minus(p0)).normalize();
            CoordinateSystem c = new CoordinateSystem(n);
            dpdu = c.getV2();
            dpdv = c.getV3();
        }
        else
        {
            double invDet = 1 / determinant;
            dpdu = dp02.times(duv12.y()).minus(dp12.times(duv02.y())).times(invDet);
            dpdv = dp02.times(-duv12.x()).plus(dp12.times(duv02.x())).times(invDet);
        }
        return new Pair<>(dpdu, dpdv);
    }
    
    private Normal3 computeShadingNormal(double b0, double b1, double b2, Normal3 existingNormal)
    {
        if (mesh.getN() != null)
        {
            return mesh.getN()[pointIndex]    .times(b0) .plus(
                   mesh.getN()[pointIndex + 1].times(b1)).plus(
                   mesh.getN()[pointIndex + 2].times(b2)).normalize();
        }
        else
        {
            return existingNormal;
        }
    }
    
    private Direction3 computeShadingTangent(double b0, double b1, double b2, Direction3 existingDpdu)
    {
        if (mesh.getS() != null)
        {
            return mesh.getS()[pointIndex]    .times(b0) .plus(
                   mesh.getS()[pointIndex + 1].times(b1)).plus(
                   mesh.getS()[pointIndex + 2].times(b2)).normalize();
        }
        else
        {
            return existingDpdu;
        }
    }
    
    private Pair<Normal3, Normal3> computeNormalPartialDerivatives(Point2[] uv)
    {
        if (mesh.getN() != null)
        {
            Direction2 duv02 = uv[0].minus(uv[2]);
            Direction2 duv12 = uv[1].minus(uv[2]);
            Normal3 dn1 = mesh.getN()[pointIndex]    .minus(mesh.getN()[pointIndex + 2]);
            Normal3 dn2 = mesh.getN()[pointIndex + 1].minus(mesh.getN()[pointIndex + 2]);
            double determinant = duv02.x() * duv12.y() - duv02.y() * duv12.x();
            if (determinant == 0)
            {
                // degenerate case
                Direction3 dn = new Direction3(mesh.getN()[pointIndex + 2].minus(
                                               mesh.getN()[pointIndex]    )).cross(
                                new Direction3(mesh.getN()[pointIndex + 1].minus(
                                               mesh.getN()[pointIndex]    )));
                if (dn.lengthSquared() == 0)
                {
                    return new Pair<>(new Normal3(0, 0, 0), new Normal3(0, 0, 0));
                }
                else
                {
                    CoordinateSystem c = new CoordinateSystem(dn);
                    return new Pair<>(new Normal3(c.getV2()), new Normal3(c.getV3()));
                }
            }
            else
            {
                double invDet = 1 / determinant;
                Normal3 dndu = dn1.times( duv12.y()).minus(dn2.times(duv02.y())).times(invDet);
                Normal3 dndv = dn1.times(-duv12.x()).minus(dn2.times(duv02.x())).times(invDet);
                return new Pair<>(dndu, dndv);
            }
        }
        else
        {
            return new Pair<>(new Normal3(0, 0, 0), new Normal3(0, 0, 0));
        }
    }
    private Point2[] getUVs()
    {
        Point2[] uv;
        Point2[] meshUV = mesh.getUVs();
        if (meshUV != null)
        {
            uv = Arrays.copyOf(meshUV, 3);
        }
        else
        {
            uv = new Point2[] { new Point2(0, 0), new Point2(1, 0), new Point2(1, 1) };
        }
        return uv;
    }

    @Override
    public double surfaceArea()
    {
        Point3 p0 = mesh.getPoint(pointIndex);
        Point3 p1 = mesh.getPoint(pointIndex + 1);
        Point3 p2 = mesh.getPoint(pointIndex + 2);
        
        return 0.5 * p1.minus(p0).cross(p2.minus(p0)).length();
    }

}
