package core.math;

import core.Ray;
import core.RayDifferential;
import core.space.BoundingBox3;
import core.tuple.Pair;
import core.tuple.Triple;
import scene.interactions.impl.SurfaceInteraction;
import scene.interactions.impl.SurfaceInteraction.ShadingGeometry;
import utilities.VectorUtilities;


public class Transformation
{
    private static final double[][] IDENTITY_ARRAY =
            new double[][] { { 1,0,0,0 },
                             { 0,1,0,0 },
                             { 0,0,1,0 },
                             { 0,0,0,1 } };
    public static final Transformation IDENTITY = new Transformation(IDENTITY_ARRAY);

    private final double[][] matrix;
    private final double[][] inverse;

    public Transformation()
    {
        matrix = VectorUtilities.copy(IDENTITY_ARRAY);
        inverse = VectorUtilities.copy(IDENTITY_ARRAY);
    }

    public Transformation(double[][] matrix)
    {
        this.matrix = matrix;
        inverse = VectorUtilities.inverse(matrix);
    }
    
    public Transformation(double[][] matrix, double[][] inverse)
    {
        this.matrix = matrix;
        this.inverse = inverse;
    }
    
    public Transformation(Quaternion q)
    {
        double xx = q.x() * q.x(),
               yy = q.y() * q.y(),
               zz = q.z() * q.z();
        double xy = q.x() * q.y(),
               xz = q.x() * q.z(),
               yz = q.y() * q.z();
        double wx = q.x() * q.w(),
               wy = q.y() * q.w(),
               wz = q.z() * q.w();
        
        matrix = new double[][] { { 1 - 2*(yy+zz),     2*(xy+wz),     2*(xz-wy), 0 },
                                  {     2*(xy-wz), 1 - 2*(xx+zz),     2*(yz+wx), 0 },
                                  {     2*(xz+wy),     2*(yz-wx), 1 - 2*(xx+yy), 0 },
                                  {             0,             0,             0, 1 } };
        inverse = VectorUtilities.transpose(matrix);
    }
    
    public Transformation inverse()
    {
        return new Transformation(inverse, matrix);
    }

    public static Transformation getTranslation(double x,
                                                       double y,
                                                       double z)
    {
        double[][] matrix = new double[][] { { 1,0,0,x },
                                             { 0,1,0,y },
                                             { 0,0,1,z },
                                             { 0,0,0,1 } };
        
        double[][] inverse = new double[][] { { 1,0,0,-x },
                                              { 0,1,0,-y },
                                              { 0,0,1,-z },
                                              { 0,0,0, 1 } };
        
        return new Transformation(matrix, inverse);
    }
    
    public static Transformation getUniformScale(double scale)
    {
        return getScale(scale, scale, scale);
    }
    
    public static Transformation getScale(double x, double y, double z)
    {
        double[][] matrix = new double[][] { { x,0,0,0 },
                                             { 0,y,0,0 },
                                             { 0,0,z,0 },
                                             { 0,0,0,1 } };
        
        double[][] inverse = new double[][] { { 1.0/x,     0,     0, 0 },
                                              {     0, 1.0/y,     0, 0 },
                                              {     0,     0, 1.0/z, 0 },
                                              {     0,     0,     0, 1 } };
        
        return new Transformation(matrix, inverse);
    }

    public static Transformation getRotation(double vx, double vy, double vz, double theta)
    {
        double s = Math.sin(theta);
        double c = Math.cos(theta);
        
        double mxx = c + vx * vx * (1 - c);
        double mxy = vx * vy * (1 - c) - vz * s;
        double mxz = vx * vz * (1 - c) + vy * s;
        double myx = vy * vx * (1 - c) + vz * s;
        double myy = c + vy * vy * (1 - c);
        double myz = vy * vz * (1 - c) - vx * s;
        double mzx = vz * vx * (1 - c) - vy * s;
        double mzy = vz * vy * (1 - c) + vx * s;
        double mzz = c + vz * vz * (1 - c);
        
        double[][] matrix = new double[][] { { mxx, mxy, mxz, 0 },
                                             { myx, myy, myz, 0 },
                                             { mzx, mzy, mzz, 0 },
                                             { 0,   0,   0,   1 } };

        return new Transformation(matrix, VectorUtilities.transpose(matrix));
        // the inverse of a rotation matrix is its transpose
    }
    
    public static Transformation getRotation(Direction3 rotationAxis, double rotationAngle)
    {
        rotationAxis = rotationAxis.normalized();
        return getRotation(rotationAxis.x(),
                           rotationAxis.y(),
                           rotationAxis.z(),
                           rotationAngle);
    }
    
    public static Transformation getLookAt(Point3 position, Point3 target, Direction3 up)
    {
        Direction3 direction = Direction3.getNormalizedDirection(position, target);
        Direction3 left = Direction3.getNormalizedDirection(Direction3.getNormalizedDirection(up)
                                                                      .cross(direction));
        Direction3 newUp = direction.cross(left);
        
        double[][] matrix = new double[][] { { left.x(), newUp.x(), direction.x(), position.x() },
                                             { left.y(), newUp.y(), direction.y(), position.y() },
                                             { left.z(), newUp.z(), direction.z(), position.z() },
                                             {        0,         0,             0,            1 } };
        
        return new Transformation(VectorUtilities.inverse(matrix), matrix);
    }

    public Point3 transform(Point3 other)
    {
        double[] v = VectorUtilities.multiply(matrix, other.getHomogeneousForm());
        
        // ensure w = 1
        // need epsilon?
        if(v[3] != 1.0) v = VectorUtilities.divide(v, v[3]);
        
        return new Point3(v);
    }
    
    public Pair<Point3, Direction3> transformWithError(Point3 other)
    {
        double[] v = VectorUtilities.multiply(matrix, other.getHomogeneousForm());
        double[] error = VectorUtilities.getMultiplyError(matrix, other.getHomogeneousForm());
        if(v[3] != 1.0) v = VectorUtilities.divide(v, v[3]);
        
        return new Pair<>(new Point3(v), new Direction3(error));
    }
    
    public Pair<Point3, Direction3> transformWithError(Point3 other, Direction3 existingError)
    {
        double[] v = VectorUtilities.multiply(matrix, other.getHomogeneousForm());
        double[] error = VectorUtilities.getMultiplyError(matrix, other.getHomogeneousForm(), existingError.getHomogeneousForm());
        if(v[3] != 1.0) v = VectorUtilities.divide(v, v[3]);
        
        return new Pair<>(new Point3(v), new Direction3(error));
    }
    
    public Direction3 transform(Direction3 other)
    {
        return new Direction3(VectorUtilities.multiply(matrix, other.getHomogeneousForm()));
    }
    
    public Pair<Direction3, Direction3> transformWithError(Direction3 other)
    {
        double[] v = VectorUtilities.multiply(matrix, other.getHomogeneousForm());
        double[] error = VectorUtilities.getMultiplyError(matrix, other.getHomogeneousForm());
        
        return new Pair<>(new Direction3(v), new Direction3(error));
    }
    
    public Pair<Direction3, Direction3> transformWithError(Direction3 other, Direction3 existingError)
    {
        double[] v = VectorUtilities.multiply(matrix, other.getHomogeneousForm());
        double[] error = VectorUtilities.getMultiplyError(matrix, other.getHomogeneousForm(), existingError.getHomogeneousForm());
        
        return new Pair<>(new Direction3(v), new Direction3(error));
    }
    
    public Normal3 transform(Normal3 other)
    {
        return new Normal3(VectorUtilities.multiplyTranspose(inverse, other.getHomogeneousForm()));
    }
    
    public Pair<Normal3, Direction3> transformWithError(Normal3 other)
    {
        double[] v = VectorUtilities.multiply(matrix, other.getHomogeneousForm());
        double[] error = VectorUtilities.getMultiplyError(matrix, other.getHomogeneousForm());
        
        return new Pair<>(new Normal3(v), new Direction3(error));
    }
    
    public Pair<Normal3, Direction3> transformWithError(Normal3 other, Direction3 existingError)
    {
        double[] v = VectorUtilities.multiply(matrix, other.getHomogeneousForm());
        double[] error = VectorUtilities.getMultiplyError(matrix, other.getHomogeneousForm(), existingError.getHomogeneousForm());
        
        return new Pair<>(new Normal3(v), new Direction3(error));
    }
    
    public Ray transform(Ray other)
    {
        var origin = transformWithError(other.getOrigin());
        Point3 newOrigin = origin.getFirst();
        Direction3 originError = origin.getSecond();
        
        Direction3 newDirection = transform(other.getDirection());
        
        double lengthSquared = newDirection.lengthSquared();
        double tMax = other.getTMax();
        if (lengthSquared > 0)
        {
            double dt = newDirection.abs().dot(originError) / lengthSquared;
            newOrigin = newOrigin.plus(newDirection.times(dt));
            tMax -= dt;
        }
        
        return new Ray(newOrigin,
                       newDirection,
                       tMax,
                       other.getTime(),
                       other.getMedium());
    }
    
    public RayDifferential transform(RayDifferential r)
    {
        Ray transformedRay = this.transform(new Ray(r));
        RayDifferential ret = new RayDifferential(transformedRay);
        ret.setHasDifferentials(r.hasDifferentials());
        if (r.hasDifferentials())
        {
            ret.setRxOrigin(this.transform(r.getRxOrigin()));
            ret.setRyOrigin(this.transform(r.getRyOrigin()));
            ret.setRxDirection(this.transform(r.getRxDirection()));
            ret.setRyDirection(this.transform(r.getRyDirection()));
        }
        return ret;
    }
    
    public Triple<Ray, Direction3, Direction3> transformWithError(Ray ray)
    {
        var origin = transformWithError(ray.getOrigin());
        Point3 newOrigin = origin.getFirst();
        Direction3 originError = origin.getSecond();
        
        var direction = transformWithError(ray.getDirection());
        Direction3 newDirection = direction.getFirst();
        Direction3 directionError = direction.getSecond();

        double lengthSquared = newDirection.lengthSquared();
        double tMax = ray.getTMax();
        if (lengthSquared > 0)
        {
            double dt = newDirection.abs().dot(originError) / lengthSquared;
            newOrigin = newOrigin.plus(newDirection.times(dt));
            tMax -= dt;
        }
        
        Ray transformedRay = new Ray(newOrigin, newDirection, tMax, ray.getTime(), ray.getMedium());
        
        return new Triple<>(transformedRay, originError, directionError);
    }
    
    public Triple<Ray, Direction3, Direction3> transformWithError(Ray ray, Direction3 existingOriginError,
            Direction3 existingDirectionError)
    {
        var origin = transformWithError(ray.getOrigin(), existingOriginError);
        Point3 newOrigin = origin.getFirst();
        Direction3 newOriginError = origin.getSecond();
        
        var direction = transformWithError(ray.getDirection(), existingDirectionError);
        Direction3 newDirection = direction.getFirst();
        Direction3 newDirectionError = direction.getSecond();

        double lengthSquared = newDirection.lengthSquared();
        double tMax = ray.getTMax();
        if (lengthSquared > 0)
        {
            double dt = newDirection.abs().dot(newOriginError) / lengthSquared;
            newOrigin = newOrigin.plus(newDirection.times(dt));
            tMax -= dt;
        }
        
        Ray transformedRay = new Ray(newOrigin, newDirection, tMax, ray.getTime(), ray.getMedium());

        return new Triple<>(transformedRay, newOriginError, newDirectionError);
    }
    
    /**
     * Returns true if this transformation flips the handedness of the coordinate system.
     */
    public boolean swapsHandedness()
    {
        return VectorUtilities.get3x3Determinant(matrix) < 0;
    }
    
    /**
     * Transforms all eight corner vertices and computes a new BoundingBox3
     * that contains them.
     */
    public BoundingBox3 transform(BoundingBox3 other)
    {
        double[] min = other.getMinPoint().getVector();
        double[] max = other.getMaxPoint().getVector();
        
        double[][] transformed = new double[8][];
        
        transformed[0] = VectorUtilities.multiply(matrix, new double[] { min[0], min[1], min[2], 1 });
        transformed[1] = VectorUtilities.multiply(matrix, new double[] { min[0], min[1], max[2], 1 });
        transformed[2] = VectorUtilities.multiply(matrix, new double[] { min[0], max[1], min[2], 1 });
        transformed[3] = VectorUtilities.multiply(matrix, new double[] { min[0], max[1], max[2], 1 });
        transformed[4] = VectorUtilities.multiply(matrix, new double[] { max[0], min[1], min[2], 1 });
        transformed[5] = VectorUtilities.multiply(matrix, new double[] { max[0], min[1], max[2], 1 });
        transformed[6] = VectorUtilities.multiply(matrix, new double[] { max[0], max[1], min[2], 1 });
        transformed[7] = VectorUtilities.multiply(matrix, new double[] { max[0], max[1], max[2], 1 });
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;
        for(double[] v : transformed)
        {
            if (v[0] < minX) minX = v[0];
            if (v[0] > maxX) maxX = v[0];
            if (v[1] < minY) minY = v[1];
            if (v[1] > maxY) maxY = v[1];
            if (v[2] < minZ) minZ = v[2];
            if (v[2] > maxZ) maxZ = v[2];
        }
        
        return new BoundingBox3(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public SurfaceInteraction transform(SurfaceInteraction other)
    {
        Point3 transformedP = this.transform(other.getP());
        Direction3 transformedError = this.transform(other.getError());
        
        Direction3 transformedWo = this.transform(other.getWo()).normalize();
        Normal3 transformedN = this.transform(other.getN()).normalize();
        Direction3 transformedDpdu = this.transform(other.getDpdu());
        Direction3 transformedDpdv = this.transform(other.getDpdv());
        Normal3 transformedDndu = this.transform(other.getDndu());
        Normal3 transformedDndv = this.transform(other.getDndv());
        
        ShadingGeometry shadingGeo = other.getShadingGeometry();
        Normal3 transformedShadingN = this.transform(shadingGeo.getN()).normalize();
        Direction3 transformedShadingDpdu = this.transform(shadingGeo.getDpdu());
        Direction3 transformedShadingDpdv = this.transform(shadingGeo.getDpdv());
        Normal3 transformedShadingDndu = this.transform(shadingGeo.getDndu());
        Normal3 transformedShadingDndv = this.transform(shadingGeo.getDndv());

        return new SurfaceInteraction(transformedP,
                                      transformedError,
                                      other.getUv(),
                                      transformedWo,
                                      transformedN,
                                      transformedDpdu,
                                      transformedDpdv,
                                      transformedDndu,
                                      transformedDndv,
                                      other.getT(),
                                      other.getShape(),
                                      transformedShadingN,
                                      transformedShadingDndu,
                                      transformedShadingDndv,
                                      transformedShadingDpdu,
                                      transformedShadingDpdv);
    }
    
    public Transformation compose(Transformation m2)
    {
        return new Transformation(VectorUtilities.multiply(matrix, m2.matrix),
                                  VectorUtilities.multiply(m2.inverse, inverse));
    }
    
    double[][] getMatrix()
    {
        return matrix;
    }
    
    @Override
    public String toString()
    {
        return VectorUtilities.matrixToString(matrix);
    }
}
