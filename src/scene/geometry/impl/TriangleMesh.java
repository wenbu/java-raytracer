package scene.geometry.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import core.math.Direction3;
import core.math.Normal3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;

public class TriangleMesh
{
    private final int numTriangles;
    private final int numVertices;
    private final List<Integer> vertexIndices;
    private final Point3[] p;
    private final Normal3[] n;
    private final Direction3[] s;
    private final Point2[] uv;
    // private final Texture alphaMask;

    public TriangleMesh(Transformation objectToWorld, int numTriangles, int[] vertexIndices, int numVertices, Point3[] P,
            Direction3[] S, Normal3[] N, Point2[] UV/* , Texture alphaMask */)
    {
        this.numTriangles = numTriangles;
        this.numVertices = numVertices;
        this.vertexIndices = new ArrayList<>(numTriangles * 3);
        for (int i = 0; i < vertexIndices.length; i++)
        {
            this.vertexIndices.add(i);
        }
        
        p = new Point3[numVertices];
        for (int i = 0; i < numVertices; i++)
        {
            p[i] = objectToWorld.transform(P[i]);
        }
        
        if (UV != null)
        {
            uv = Arrays.copyOf(UV, UV.length);
        }
        else
        {
            uv = null;
        }
        
        if (N != null)
        {
            n = new Normal3[numVertices];
            for (int i = 0; i < numVertices; i++)
            {
                n[i] = objectToWorld.transform(N[i]);
            }
        }
        else
        {
            n = null;
        }
        
        if (S != null)
        {
            s = new Direction3[numVertices];
            for (int i = 0; i < numVertices; i++)
            {
                s[i] = objectToWorld.transform(S[i]);
            }
        }
        else
        {
            s = null;
        }
    }
    
    public int getIndex(int triangleNumber)
    {
        return vertexIndices.get(triangleNumber);
    }
    
    public Point3 getPoint(int index)
    {
        return p[index];
    }
    
    public Point2[] getUVs()
    {
        return uv;
    }
    
    public Normal3[] getN()
    {
        return n;
    }
    
    public Direction3[] getS()
    {
        return s;
    }
}
