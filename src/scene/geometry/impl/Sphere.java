package scene.geometry.impl;

import static core.math.MathUtilities.clamp;
import static core.math.MathUtilities.quadratic;

import core.Ray;
import core.math.Direction3;
import core.math.MathUtilities;
import core.math.MathUtilities.eDouble;
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
        var transformedRay = worldToObject.transformWithError(ray);
        Ray objectSpaceRay = transformedRay.getFirst();
        Direction3 originError = transformedRay.getSecond();
        Direction3 directionError = transformedRay.getThird();

        Pair<eDouble, eDouble> quadraticSolution = getQuadraticIntersectionSolution(objectSpaceRay,
                                                                                    originError,
                                                                                    directionError);
        if (quadraticSolution == null)
        {
            return null;
        }
        Triple<Point3, eDouble, Double> closestValidIntersection = getClosestValidIntersection(quadraticSolution, objectSpaceRay);
        if (closestValidIntersection == null)
        {
            return null;
        }
        
        // found hit -- compute parametric representation
        Point3 pHit = closestValidIntersection.getFirst();
        eDouble tShapeHit = closestValidIntersection.getSecond();
        double phi = closestValidIntersection.getThird();
        
        double px = pHit.x();
        double py = pHit.y();
        double pz = pHit.z();
        double u = phi / phiMax;
        double theta = Math.acos(clamp(pz / radius, -1, 1));
        double v = (theta - thetaMin) / (thetaMax - thetaMin);
        
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
        
        Direction3 pError = new Direction3(pHit).abs().timesEquals(MathUtilities.gamma(5));
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
        double tHit = tShapeHit.getValue();
        return new Pair<>(tHit, isect);
    }
    
    @Override
    public boolean intersectP(Ray ray, boolean testAlpha)
    {
        var transformedRay = worldToObject.transformWithError(ray);
        Ray objectSpaceRay = transformedRay.getFirst();
        Direction3 originError = transformedRay.getSecond();
        Direction3 directionError = transformedRay.getThird();

        var quadraticSolution = getQuadraticIntersectionSolution(objectSpaceRay,
                                                                 originError,
                                                                 directionError);
        if (quadraticSolution == null)
        {
            return false;
        }
        var closestValidIntersection = getClosestValidIntersection(quadraticSolution, objectSpaceRay);
        if (closestValidIntersection == null)
        {
            return false;
        }
        return true;
    }
    
    private Pair<eDouble, eDouble> getQuadraticIntersectionSolution(Ray objectSpaceRay,
            Direction3 originError, Direction3 directionError)
    {
        eDouble ox = new eDouble(objectSpaceRay.getOrigin().x(), originError.x());
        eDouble oy = new eDouble(objectSpaceRay.getOrigin().y(), originError.y());
        eDouble oz = new eDouble(objectSpaceRay.getOrigin().z(), originError.z());
        eDouble dx = new eDouble(objectSpaceRay.getDirection().x(), directionError.x());
        eDouble dy = new eDouble(objectSpaceRay.getDirection().y(), directionError.y());
        eDouble dz = new eDouble(objectSpaceRay.getDirection().z(), directionError.z());
        
        // compute quadratic sphere coefficients
        eDouble a = new eDouble(dx.times(dx).plus(dy.times(dy)).plus(dz.times(dz)));
        eDouble b = new eDouble(dx.times(ox).plus(dy.times(oy)).plus(dz.times(oz)).times(2));
        eDouble c = new eDouble(ox.times(ox).plus(oy.times(oy)).plus(oz.times(oz)).minus(new eDouble(radius * radius)));
        
        // solve quadratic equation
        return quadratic(a, b, c);
    }
    
    // returns intersection point, parametric t of intersection, phi at intersection
    private Triple<Point3, eDouble, Double> getClosestValidIntersection(Pair<eDouble, eDouble> t, Ray ray)
    {
        eDouble t0 = t.getFirst();
        eDouble t1 = t.getSecond();
        
        // check t0 and t1 for nearest intersection with quadric
        // valid values are 0 < t < ray.tMax
        if (t0.upperBound() > ray.getTMax() || t1.lowerBound() <= 0)
        {
            return null;
        }
        
        eDouble tShapeHit = t0;
        if (tShapeHit.lowerBound() <= 0)
        {
            tShapeHit = t1;
            if (tShapeHit.upperBound() > ray.getTMax())
            {
                return null;
            }
        }
        
        // compute hit position (in object space) and phi
        Pair<Point3, Double> positionAndPhi = computeHitPositionAndPhi(tShapeHit.getValue(), ray);
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
            if (t1.upperBound() > ray.getTMax())
            {
                return null;
            }
            tShapeHit = t1;
            
            positionAndPhi = computeHitPositionAndPhi(tShapeHit.getValue(), ray);
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
        pHit = pHit.times(radius / pHit.distanceTo(new Point3()));
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
