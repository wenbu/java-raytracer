package core.math;

import core.Ray;
import core.space.BoundingBox;


public class Transformation
{
    public static final double[][] IDENTITY =
            new double[][] { { 1,0,0,0 },
                             { 0,1,0,0 },
                             { 0,0,1,0 },
                             { 0,0,0,1 } };
    
    private final double[][] matrix;
    private final double[][] inverse;

    public Transformation()
    {
        matrix = VectorMath.copy(IDENTITY);
        inverse = VectorMath.copy(IDENTITY);
    }

    public Transformation(double[][] matrix)
    {
        this.matrix = matrix;
        inverse = VectorMath.inverse(matrix);
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
        inverse = VectorMath.transpose(matrix);
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

        return new Transformation(matrix, VectorMath.transpose(matrix));
        // the inverse of a rotation matrix is its transpose
    }
    
    public static Transformation getRotation(Direction rotationAxis, double rotationAngle)
    {
        return getRotation(rotationAxis.x(),
                           rotationAxis.y(),
                           rotationAxis.z(),
                           rotationAngle);
    }
    
    public static Transformation getLookAt(Point position, Point target, Direction up)
    {
        Direction direction = Direction.getNormalizedDirection(position, target);
        Direction left = Direction.getNormalizedDirection(Direction.getNormalizedDirection(up).cross(direction));
        Direction newUp = direction.cross(left);
        
        double[][] matrix = new double[][] { { left.x(), newUp.x(), direction.x(), position.x() },
                                             { left.y(), newUp.y(), direction.y(), position.y() },
                                             { left.z(), newUp.z(), direction.z(), position.z() },
                                             {        0,         0,             0,            1 } };
        
        return new Transformation(VectorMath.inverse(matrix), matrix);
    }

    public Point transform(Point other)
    {
        double[] v = VectorMath.multiply(matrix, other.getHomogeneousForm());
        
        // ensure w = 1
        // need epsilon?
        if(v[3] != 1.0) v = VectorMath.divide(v, v[3]);
        
        return new Point(v);
    }
    
    public Direction transform(Direction other)
    {
        return new Direction(VectorMath.multiply(matrix, other.getHomogeneousForm()));
    }
    
    public Normal transform(Normal other)
    {
        return new Normal(VectorMath.multiplyTranspose(inverse, other.getHomogeneousForm()));
    }
    
    public Ray transform(Ray other)
    {
        Point newOrigin = transform(other.getOrigin());
        Direction newDirection = transform(other.getDirection());
        
        return new Ray(newOrigin,
                       newDirection,
                       other.getMinT(),
                       other.getMaxT(),
                       other.getDepth());
    }
    
    /**
     * Returns true if this transformation flips the handedness of the coordinate system.
     */
    public boolean swapsHandedness()
    {
        return VectorMath.get3x3Determinant(matrix) < 0;
    }
    
    /**
     * Transforms all eight corner vertices and computes a new BoundingBox
     * that contains them.
     */
    public BoundingBox transform(BoundingBox other)
    {
        double[] min = other.getMinPoint().getVector();
        double[] max = other.getMaxPoint().getVector();
        
        double[][] transformed = new double[8][];
        
        transformed[0] = VectorMath.multiply(matrix, new double[] { min[0], min[1], min[2], 1 });
        transformed[1] = VectorMath.multiply(matrix, new double[] { min[0], min[1], max[2], 1 });
        transformed[2] = VectorMath.multiply(matrix, new double[] { min[0], max[1], min[2], 1 });
        transformed[3] = VectorMath.multiply(matrix, new double[] { min[0], max[1], max[2], 1 });
        transformed[4] = VectorMath.multiply(matrix, new double[] { max[0], min[1], min[2], 1 });
        transformed[5] = VectorMath.multiply(matrix, new double[] { max[0], min[1], max[2], 1 });
        transformed[6] = VectorMath.multiply(matrix, new double[] { max[0], max[1], min[2], 1 });
        transformed[7] = VectorMath.multiply(matrix, new double[] { max[0], max[1], max[2], 1 });
        
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
        
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public Transformation compose(Transformation m2)
    {
        return new Transformation(VectorMath.multiply(matrix, m2.matrix),
                                  VectorMath.multiply(m2.inverse, inverse));
    }
    
    double[][] getMatrix()
    {
        return matrix;
    }
    
    @Override
    public String toString()
    {
        return VectorMath.matrixToString(matrix);
    }
}
