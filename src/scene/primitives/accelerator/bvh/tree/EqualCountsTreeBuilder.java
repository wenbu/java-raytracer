package scene.primitives.accelerator.bvh.tree;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import core.space.Axis;
import core.space.BoundingBox3;
import core.tuple.Pair;
import scene.primitives.Primitive;
import scene.primitives.accelerator.bvh.PrimitiveInfo;

public class EqualCountsTreeBuilder extends TreeBuilder
{
    private static final Logger logger = Logger.getLogger(EqualCountsTreeBuilder.class.getName());

    public EqualCountsTreeBuilder(List<Primitive> unorderedPrimitives)
    {
        super(unorderedPrimitives);
    }
    
    @Override
    protected Pair<List<PrimitiveInfo>, List<PrimitiveInfo>> partition(BuildNode node,
            Axis splitAxis, List<PrimitiveInfo> primitiveInfos, List<Primitive> orderedPrimitives,
            BoundingBox3 centroidBounds, BoundingBox3 primitiveBounds)
    {
        return partition(splitAxis, primitiveInfos);
    }

    // Splitting this out into a static method as this partition method is also used by other
    // partition methods.
    static Pair<List<PrimitiveInfo>, List<PrimitiveInfo>> partition(Axis splitAxis,
            List<PrimitiveInfo> primitiveInfos)
    {
        int mid = primitiveInfos.size() / 2;
        // TODO: can be done without a full sort -- see C++ std::nth_element
        primitiveInfos.sort(Comparator.comparingDouble(pi -> pi.getCentroid().get(splitAxis)));
        List<PrimitiveInfo> leftPartition = primitiveInfos.subList(0, mid);
        List<PrimitiveInfo> rightPartition = primitiveInfos.subList(mid, primitiveInfos.size());
        logger.fine("Partitioned " + primitiveInfos.size() + " primitives into sublists of size " +
                    leftPartition.size() + " and " + rightPartition.size() + ".");
        return new Pair<>(leftPartition, rightPartition);
    }
}
