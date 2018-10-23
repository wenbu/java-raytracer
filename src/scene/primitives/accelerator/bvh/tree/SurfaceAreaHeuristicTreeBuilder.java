package scene.primitives.accelerator.bvh.tree;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import core.space.Axis;
import core.space.BoundingBox3;
import core.tuple.Pair;
import scene.primitives.Primitive;
import scene.primitives.accelerator.bvh.PrimitiveInfo;

public class SurfaceAreaHeuristicTreeBuilder extends TreeBuilder
{
    private static final Logger logger = Logger.getLogger(SurfaceAreaHeuristicTreeBuilder.class.getName());

    private static final int NUM_BUCKETS = 12;
    
    // Relative cost of intersection (i.e. not splitting) for deciding whether to partition or create a leaf.
    private static final double INTERSECTION_COST = 1;
    
    // Relative cost of traversal (i.e. splitting) for deciding whether to partition or create a leaf.
    private static final double TRAVERSAL_COST = 1;
    
    // The number of PrimitiveInfos to partition at which we decide that it is no longer worth the
    // additional computation to apply the surface area heuristic and fall back to something
    // simpler.
    private static final int FALLBACK_SIZE = 2;
    
    public SurfaceAreaHeuristicTreeBuilder(List<Primitive> unorderedPrimitives,
            int maxPrimitivesPerNode)
    {
        super(unorderedPrimitives, maxPrimitivesPerNode);
    }

    @Override
    protected Pair<List<PrimitiveInfo>, List<PrimitiveInfo>> partition(BuildNode node,
            Axis splitAxis, List<PrimitiveInfo> primitiveInfos, List<Primitive> orderedPrimitives,
            BoundingBox3 centroidBounds, BoundingBox3 primitiveBounds)
    {
        if (primitiveInfos.size() <= FALLBACK_SIZE)
        {
            return EqualCountsTreeBuilder.partition(splitAxis, primitiveInfos);
        }
        else
        {
            BucketInfo[] buckets = initializeBuckets(primitiveInfos, splitAxis, centroidBounds);

            double[] splitCosts = estimateBucketSplitCosts(buckets, primitiveBounds);

            // find bucket split that minimizes cost
            double minSplitCost = splitCosts[0];
            int minCostSplitBucket = 0;
            for (int i = 1; i < splitCosts.length; i++)
            {
                if (splitCosts[i] < minSplitCost)
                {
                    minSplitCost = splitCosts[i];
                    minCostSplitBucket = i;
                }
            }

            // create leaf or split
            double createLeafCost = INTERSECTION_COST * primitiveInfos.size();
            if (primitiveInfos.size() > maxPrimitivesPerNode || minSplitCost < createLeafCost)
            {
                final BoundingBox3 finalCentroidBounds = new BoundingBox3(centroidBounds);
                final int finalSplitBucket = minCostSplitBucket;
                var splitPrimitiveInfos = primitiveInfos.stream()
                                                        .collect(Collectors.partitioningBy(pi ->
                                                        {
                                                            int b = (int) (NUM_BUCKETS *
                                                                           finalCentroidBounds.offset(pi.getCentroid())
                                                                                              .get(splitAxis));
                                                            if (b == NUM_BUCKETS)
                                                            {
                                                                b = NUM_BUCKETS - 1;
                                                            }
                                                            return b <= finalSplitBucket;
                                                        }));
                List<PrimitiveInfo> leftPartition = splitPrimitiveInfos.get(false);
                List<PrimitiveInfo> rightPartition = splitPrimitiveInfos.get(true);
                logger.fine("Partitioned " + primitiveInfos.size() +
                            " primitives into sublists of size " + leftPartition.size() + " and " +
                            rightPartition.size() + ".");
                return new Pair<>(leftPartition, rightPartition);
            }
            else
            {
                initLeaf(node, primitiveInfos, orderedPrimitives, primitiveBounds);
                return null;
            }
        }
    }
    
    private BucketInfo[] initializeBuckets(List<PrimitiveInfo> primitiveInfos, Axis splitAxis, BoundingBox3 centroidBounds)
    {
        BucketInfo[] buckets = new BucketInfo[NUM_BUCKETS];

        // initialize BucketInfos
        for (int i = 0; i < NUM_BUCKETS; i++)
        {
            buckets[i] = new BucketInfo();
        }
        
        for (PrimitiveInfo primitiveInfo : primitiveInfos)
        {
            int b = (int) (NUM_BUCKETS *
                           centroidBounds.offset(primitiveInfo.getCentroid()).get(splitAxis));
            if (b == NUM_BUCKETS)
            {
                b = NUM_BUCKETS - 1;
            }
            buckets[b].count++;
            buckets[b].bounds = buckets[b].bounds.union(primitiveInfo.getBounds());
        }
        
        return buckets;
    }
    
    private double[] estimateBucketSplitCosts(BucketInfo[] buckets, BoundingBox3 primitiveBounds)
    {
        // compute costs for splitting after each bucket
        // traversal cost relative to intersection cost is arbitrarily set to 1/8.
        double[] costs = new double[NUM_BUCKETS - 1];
        for (int i = 0; i < costs.length; i++)
        {
            BoundingBox3 b0 = new BoundingBox3();
            BoundingBox3 b1 = new BoundingBox3();
            int count0 = 0;
            int count1 = 0;
            for (int j = 0; j <= i; j++)
            {
                b0 = b0.union(buckets[j].bounds);
                count0 += buckets[j].count;
            }
            for (int j = i + 1; j < NUM_BUCKETS; j++)
            {
                b1 = b1.union(buckets[j].bounds);
                count1 += buckets[j].count;
            }
            costs[i] = TRAVERSAL_COST + (count0 * b0.surfaceArea() + count1 * b1.surfaceArea()) /
                                        primitiveBounds.surfaceArea();
        }
        return costs;
    }

    private static class BucketInfo
    {
        private int count = 0;
        private BoundingBox3 bounds = new BoundingBox3();
    }
}
