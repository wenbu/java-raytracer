package scene.geometry.impl;

import java.util.Arrays;

import core.math.Direction3;
import core.math.Normal3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;

public class TriangleMesh
{
    private final int numTriangles;
    private final int[] vertexIndices;
    private final Point3[] p;
    private final Normal3[] n;
    private final Direction3[] s;
    private final Point2[] uv;
    // private final Texture alphaMask;

    public TriangleMesh(Transformation objectToWorld, int numTriangles, int[] vertexIndices, Point3[] P,
            Direction3[] S, Normal3[] N, Point2[] UV/* , Texture alphaMask */)
    {
        this.numTriangles = numTriangles;
        this.vertexIndices = vertexIndices;
        
        p = new Point3[P.length];
        for (int i = 0; i < P.length; i++)
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
            n = new Normal3[P.length];
            for (int i = 0; i < P.length; i++)
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
            s = new Direction3[P.length];
            for (int i = 0; i < P.length; i++)
            {
                s[i] = objectToWorld.transform(S[i]);
            }
        }
        else
        {
            s = null;
        }
    }
    
    public Point3 getPoint(int index)
    {
        return p[vertexIndices[index]];
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
