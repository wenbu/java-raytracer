package scene.primitives.accelerator.bvh.tree;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import core.space.Axis;
import core.space.BoundingBox3;
import core.tuple.Pair;
import scene.primitives.Primitive;
import scene.primitives.accelerator.bvh.PrimitiveInfo;

public class MiddleTreeBuilder extends TreeBuilder
{
    private static final Logger logger = Logger.getLogger(MiddleTreeBuilder.class.getName());

    public MiddleTreeBuilder(List<Primitive> unorderedPrimitives)
    {
        super(unorderedPrimitives);
    }
    
    @Override
    protected Pair<List<PrimitiveInfo>, List<PrimitiveInfo>> partition(BuildNode node,
            Axis splitAxis, List<PrimitiveInfo> primitiveInfos, List<Primitive> orderedPrimitives,
            BoundingBox3 centroidBounds, BoundingBox3 primitiveBounds)
    {
        double pmid = (centroidBounds.getMinPoint().get(splitAxis) +
                       centroidBounds.getMaxPoint().get(splitAxis)) /
                      2;
        var partitionedPrimitiveInfos = primitiveInfos.stream()
                                                      .collect(Collectors.partitioningBy(pi -> pi.getCentroid()
                                                                                                 .get(splitAxis) < pmid));
        List<PrimitiveInfo> leftPartition = partitionedPrimitiveInfos.get(false);
        List<PrimitiveInfo> rightPartition = partitionedPrimitiveInfos.get(true);

        if (!leftPartition.isEmpty() && !rightPartition.isEmpty())
        {
            logger.fine("Partitioned " + primitiveInfos.size() +
                        " primitives into sublists of size " + leftPartition.size() + " and " +
                        rightPartition.size() + ".");
            return new Pair<>(leftPartition, rightPartition);
        } else
        {
            logger.fine("Falling back to EQUAL_COUNT partitioning.");
            return EqualCountsTreeBuilder.partition(splitAxis, primitiveInfos);
        }
    }
}
