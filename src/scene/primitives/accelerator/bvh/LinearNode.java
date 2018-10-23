package scene.primitives.accelerator.bvh;

import core.space.Axis;
import core.space.BoundingBox3;

public class LinearNode
{
    private BoundingBox3 bounds;
    private int offset; // primitive offset for leaf nodes, secondChild offset for interior nodes
    private int numPrimitives; // 0 -> interior node
    private Axis axis;

    public BoundingBox3 getBounds()
    {
        return bounds;
    }

    public void setBounds(BoundingBox3 bounds)
    {
        this.bounds = bounds;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public int getNumPrimitives()
    {
        return numPrimitives;
    }

    public void setNumPrimitives(int numPrimitives)
    {
        this.numPrimitives = numPrimitives;
    }

    public Axis getAxis()
    {
        return axis;
    }

    public void setAxis(Axis axis)
    {
        this.axis = axis;
    }
}