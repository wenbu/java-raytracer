package scene.primitives.accelerator.bvh.tree;

import core.space.Axis;
import core.space.BoundingBox3;

class BuildNode
{
    public BoundingBox3 bounds;
    public BuildNode[] children = new BuildNode[2];
    public Axis splitAxis;
    public int firstPrimitiveOffset;
    public int numPrimitives;
    
    public void initLeaf(int first, int n, BoundingBox3 b)
    {
        firstPrimitiveOffset = first;
        numPrimitives = n;
        bounds = b;
    }
    
    public void initInterior(Axis axis, BuildNode c0, BuildNode c1)
    {
        children[0] = c0;
        children[1] = c1;
        bounds = c0.bounds.union(c1.bounds);
        splitAxis = axis;
        numPrimitives = 0;
    }
    
    @Override
    public String toString()
    {
        if (numPrimitives == 0)
        {
            return "BuildNode(intr) [c0=" + children[0] + ", c1=" + children[1] + "]";
        }
        else
        {
            return "BuildNode(leaf) [numPrimitives=" + numPrimitives +", offset=" + firstPrimitiveOffset + "]";
        }
    }
}