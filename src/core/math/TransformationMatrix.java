package core.math;


public class TransformationMatrix
{
    public static final double[][] IDENTITY =
            new double[][] { { 1,0,0,0 },
                             { 0,1,0,0 },
                             { 0,0,1,0 },
                             { 0,0,0,1 } };
    
    private final double[][] matrix;
    private final double[][] inverse;

    public TransformationMatrix()
    {
        matrix = VectorMath.copy(IDENTITY);
        inverse = VectorMath.copy(IDENTITY);
    }

    public TransformationMatrix(double[][] matrix)
    {
        this.matrix = matrix;
        inverse = VectorMath.inverse(matrix);
    }
    
    public TransformationMatrix(double[][] matrix, double[][] inverse)
    {
        this.matrix = matrix;
        this.inverse = inverse;
    }

    public static TransformationMatrix getTranslation(double x,
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
        
        return new TransformationMatrix(matrix, inverse);
    }
    
    public static TransformationMatrix getScale(double x, double y, double z)
    {
        double[][] matrix = new double[][] { { x,0,0,0 },
                                             { 0,y,0,0 },
                                             { 0,0,z,0 },
                                             { 0,0,0,1 } };
        
        double[][] inverse = new double[][] { { 1.0/x,     0,     0, 0 },
                                              {     0, 1.0/y,     0, 0 },
                                              {     0,     0, 1.0/z, 0 },
                                              {     0,     0,     0, 1 } };
        
        return new TransformationMatrix(matrix, inverse);
    }

    public static TransformationMatrix getRotation(double vx, double vy, double vz, double theta)
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

        return new TransformationMatrix(matrix, VectorMath.transpose(matrix));
        // the inverse of a rotation matrix is its transpose
    }
    
    public static TransformationMatrix getRotation(Direction rotationAxis, double rotationAngle)
    {
    	return getRotation(rotationAxis.x(), rotationAxis.y(), rotationAxis.z(), rotationAngle);
    }

    public Point times(Point other)
    {
        return new Point(VectorMath.multiply(matrix, other.getHomogeneousForm()));
    }
    
    public Direction times(Direction other)
    {
        return new Direction(VectorMath.multiply(matrix, other.getHomogeneousForm()));
    }

    public static void main(String[] args)
    {
        Point point = new Point(1, 2, 3);
        Direction direction = new Direction(1,2,3);
        Direction rotationAxis = Direction.getNormalizedDirection(1, 0, 0);
        TransformationMatrix matrix = new TransformationMatrix();
        matrix = getRotation(rotationAxis, Math.PI/2);

        System.out.println("Matrix:\n" + matrix.matrix);
        System.out.println("Point:     " + point.vector);
        System.out.println("Direction: "+direction.vector);

        System.out.println("ResultP:   " + matrix.times(point).vector);
        System.out.println("ResultD:   " + matrix.times(direction).vector);
    }
}
