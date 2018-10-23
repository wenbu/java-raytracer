package scene.primitives.accelerator.bvh;

import core.math.Point3;
import core.space.BoundingBox3;

public class PrimitiveInfo
{
    private final int primitiveNumber;
    private final BoundingBox3 bounds;
    private Point3 centroid;
    
    public PrimitiveInfo(int primitiveNumber, BoundingBox3 bounds)
    {
        this.primitiveNumber = primitiveNumber;
        this.bounds = bounds;
        this.centroid = bounds.getMinPoint().times(0.5).plus(bounds.getMaxPoint().times(0.5));
    }

    public int getPrimitiveNumber()
    {
        return primitiveNumber;
    }

    public BoundingBox3 getBounds()
    {
        return bounds;
    }

    public Point3 getCentroid()
    {
        return centroid;
    }
    
    public void setCentroid(Point3 centroid)
    {
        this.centroid = centroid;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bounds == null) ? 0 : bounds.hashCode());
        result = prime * result + ((centroid == null) ? 0 : centroid.hashCode());
        result = prime * result + primitiveNumber;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrimitiveInfo other = (PrimitiveInfo) obj;
        if (bounds == null)
        {
            if (other.bounds != null)
                return false;
        } else if (!bounds.equals(other.bounds))
            return false;
        if (centroid == null)
        {
            if (other.centroid != null)
                return false;
        } else if (!centroid.equals(other.centroid))
            return false;
        if (primitiveNumber != other.primitiveNumber)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "PrimitiveInfo [primitiveNumber=" + primitiveNumber + ", bounds=" + bounds +
               ", centroid=" + centroid + "]";
    }
}