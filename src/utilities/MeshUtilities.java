package utilities;

import java.util.List;

import core.math.Point3;
import core.math.Transformation;
import scene.geometry.impl.Triangle;

public class MeshUtilities
{
    public static List<Triangle> createSingleTriangle(Transformation objectToWorld, Point3 p1,
            Point3 p2, Point3 p3)
    {
        return Triangle.createTriangleMesh(objectToWorld,
                                           objectToWorld.inverse(),
                                           false,
                                           1,
                                           new int[] { 0, 1, 2 },
                                           new Point3[] { p1, p2, p3 },
                                           null,
                                           null,
                                           null);
    }
    
    /**
     * Create a unit cube with the specified transformation.
     */
    public static List<Triangle> createCube(Transformation objectToWorld, boolean reverseOrientation)
    {
        Point3[] points = new Point3[] { new Point3(-0.5, -0.5, -0.5),
                                         new Point3(-0.5, -0.5,  0.5),
                                         new Point3(-0.5,  0.5, -0.5),
                                         new Point3(-0.5,  0.5,  0.5),
                                         new Point3( 0.5, -0.5, -0.5),
                                         new Point3( 0.5, -0.5,  0.5),
                                         new Point3( 0.5,  0.5, -0.5),
                                         new Point3( 0.5,  0.5,  0.5) };
        
        int[] vertexIndices = new int[] { 0, 1, 3, 0, 3, 2, 1, 5, 3, 3, 5, 7, 2, 3, 7, 2, 7, 6, 2,
                                          6, 4, 0, 2, 4, 0, 4, 1, 1, 4, 5, 4, 6, 7, 4, 7, 5 };
        
        return Triangle.createTriangleMesh(objectToWorld, objectToWorld.inverse(), reverseOrientation, 12, vertexIndices, points, null, null, null);
    }
}
