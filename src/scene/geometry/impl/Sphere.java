package scene.geometry.impl;

import static core.math.MathUtilities.clamp;
import static core.math.MathUtilities.quadratic;

import core.Ray;
import core.math.Direction3;
import core.math.Normal3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.space.BoundingBox;
import core.tuple.Pair;
import core.tuple.Triple;
import scene.geometry.Shape;
import scene.interactions.impl.SurfaceInteraction;

public class Sphere extends Shape
{
    private final double radius;
    private final double zMin;
    private final double zMax;
    private final double thetaMin;
    private final double thetaMax;
    private final double phiMax;
    
    public Sphere(Transformation objectToWorld, Transformation worldToObject, boolean reverseOrientation, double radius)
    {
        this(objectToWorld, worldToObject, reverseOrientation, radius, -radius, radius, 360);
    }
    
    /**
     * @param phiMax is in degrees
     */
    public Sphere(Transformation objectToWorld, Transformation worldToObject, boolean reverseOrientation, double radius,
            double zMin, double zMax, double phiMax)
    {
        super(objectToWorld, worldToObject, reverseOrientation);
        this.radius = radius;
        this.zMin = clamp(Math.min(zMin, zMax), -radius, radius);
        this.zMax = clamp(Math.max(zMin, zMax), -radius, radius);
        this.thetaMin = Math.acos(clamp(zMin / radius, -1, 1));
        this.thetaMax = Math.acos(clamp(zMax / radius, -1, 1));
        this.phiMax = Math.toRadians(clamp(phiMax, 0, 360));
    }

    @Override
    public BoundingBox objectBound()
    {
        // TODO: can compute tighter bound if phiMax < 3pi/2
        return new BoundingBox(new Point3(-radius, -radius, zMin),
                               new Point3( radius,  radius, zMax));
    }

    @Override
    public Pair<Double, SurfaceInteraction> intersect(Ray ray, boolean testAlpha)
    {
        Ray objectSpaceRay = worldToObject.transform(ray);

        Pair<Double, Double> quadraticSolution = getQuadraticIntersectionSolution(objectSpaceRay);
        if (quadraticSolution == null)
        {
            return null;
        }
        Triple<Point3, Double, Double> closestValidIntersection = getClosestValidIntersection(quadraticSolution, objectSpaceRay);
        if (closestValidIntersection == null)
        {
            return null;
        }
        
        // found hit -- compute parametric representation
        Point3 pHit = closestValidIntersection.getFirst();
        double tShapeHit = closestValidIntersection.getSecond();
        double phi = closestValidIntersection.getThird();
        
        double px = pHit.x();
        double py = pHit.y();
        double pz = pHit.z();
        double u = phi / phiMax;
        double theta = Math.acos(clamp(pz / radius, -1, 1));
        double v = (theta - thetaMin) / (thetaMax / thetaMin);
        
        double zRadius = Math.sqrt(px * px + py * py);
        double invZRadius = 1 / zRadius;
        double cosPhi = px * invZRadius;
        double sinPhi = py * invZRadius;
        Direction3 dpdu = new Direction3(-phiMax * py, phiMax * px, 0);
        Direction3 dpdv = new Direction3(pz * cosPhi,
                                         pz * sinPhi,
                                         -radius * Math.sin(theta)).times(thetaMax - thetaMin);
        Direction3 d2pduu = new Direction3(px, py, 0).times(-phiMax * phiMax);
        Direction3 d2pduv = new Direction3(-sinPhi, cosPhi, 0).times((thetaMax - thetaMin) * pHit.z() * phiMax);
        Direction3 d2pdvv = new Direction3(px, py, pz).times(-(thetaMax - thetaMin) * (thetaMax - thetaMin));
        double E = dpdu.dot(dpdu);
        double F = dpdu.dot(dpdv);
        double G = dpdv.dot(dpdv);
        Direction3 N = dpdu.cross(dpdv).normalize();
        double e = N.dot(d2pduu);
        double f = N.dot(d2pduv);
        double g = N.dot(d2pdvv);
        double invEGF2 = 1 / (E * G - F * F);
        Normal3 dndu = new Normal3(dpdu.times((f * F - e * G) * invEGF2).plus(
                                   dpdv.times((e * F - f * E) * invEGF2) ));
        Normal3 dndv = new Normal3(dpdu.times((g * F - f * G) * invEGF2).plus(
                                   dpdv.times((f * F - g * E) * invEGF2) ));
        // TODO
        Direction3 pError = new Direction3(0, 0, 0);
        SurfaceInteraction isect = objectToWorld.transform(new SurfaceInteraction(pHit,
                                                                                  pError,
                                                                                  new Point2(u, v),
                                                                                  ray.getDirection().times(-1),
                                                                                  dpdu,
                                                                                  dpdv,
                                                                                  dndu,
                                                                                  dndv,
                                                                                  ray.getTime(),
                                                                                  this));
        return new Pair<>(tShapeHit, isect);
    }
    
    @Override
    public boolean intersectP(Ray ray, boolean testAlpha)
    {
        Ray objectSpaceRay = worldToObject.transform(ray);

        Pair<Double, Double> quadraticSolution = getQuadraticIntersectionSolution(objectSpaceRay);
        if (quadraticSolution == null)
        {
            return false;
        }
        Triple<Point3, Double, Double> closestValidIntersection = getClosestValidIntersection(quadraticSolution, objectSpaceRay);
        if (closestValidIntersection == null)
        {
            return false;
        }
        return true;
    }
    
    private Pair<Double, Double> getQuadraticIntersectionSolution(Ray objectSpaceRay)
    {
        double ox = objectSpaceRay.getOrigin().x();
        double oy = objectSpaceRay.getOrigin().y();
        double oz = objectSpaceRay.getOrigin().z();
        double dx = objectSpaceRay.getDirection().x();
        double dy = objectSpaceRay.getDirection().y();
        double dz = objectSpaceRay.getDirection().z();
        
        // compute quadratic sphere coefficients
        double a = dx * dx + dy * dy + dz * dz;
        double b = 2 * (dx * ox + dy * oy + dz * oz);
        double c = ox * ox + oy * oy + oz * oz - radius * radius;
        
        // solve quadratic equation
        return quadratic(a, b, c);
    }
    
    // returns intersection point, parametric t of intersection, phi at intersection
    private Triple<Point3, Double, Double> getClosestValidIntersection(Pair<Double, Double> t, Ray ray)
    {
        double t0 = t.getFirst();
        double t1 = t.getSecond();
        
        // check t0 and t1 for nearest intersection with quadric
        // valid values are 0 < t < ray.tMax
        if (t0 > ray.getTMax() || t1 <= 0)
        {
            return null;
        }
        
        double tShapeHit = t0;
        if (tShapeHit <= 0)
        {
            tShapeHit = t1;
            if (tShapeHit > ray.getTMax())
            {
                return null;
            }
        }
        
        // compute hit position (in object space) and phi
        Pair<Point3, Double> positionAndPhi = computeHitPositionAndPhi(tShapeHit, ray);
        Point3 pHit = positionAndPhi.getFirst();
        double phi = positionAndPhi.getSecond();
        
        // test intersection against z and phi
        if ((zMin > -radius && pHit.z() < zMin) ||
            (zMax <  radius && pHit.z() > zMax) ||
            phi > phiMax)
        {
            if (tShapeHit == t1)
            {
                return null;
            }
            if (t1 > ray.getTMax())
            {
                return null;
            }
            tShapeHit = t1;
            
            positionAndPhi = computeHitPositionAndPhi(tShapeHit, ray);
            pHit = positionAndPhi.getFirst();
            phi = positionAndPhi.getSecond();
            if ((zMin > -radius && pHit.z() < zMin) ||
                (zMax <  radius && pHit.z() > zMax) ||
                phi > phiMax)
            {
                return null;
            }
        }
        return new Triple<>(pHit, tShapeHit, phi);
    }
    
    private Pair<Point3, Double> computeHitPositionAndPhi(double t, Ray ray)
    {
        Point3 pHit = ray.pointAt(t);
        if (pHit.x() == 0 && pHit.y() == 0)
        {
            pHit.setX(1e-5 * radius);
        }
        double phi = Math.atan2(pHit.y(), pHit.x());
        if (phi < 0)
        {
            phi += 2 * Math.PI;
        }
        
        return new Pair<>(pHit, phi);
    }

    @Override
    public double surfaceArea()
    {
        return phiMax * radius * (zMax - zMin);
    }
}
