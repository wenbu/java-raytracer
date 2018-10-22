package scene.primitives.accelerator.bvh;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import core.Ray;
import core.math.Direction3;
import core.space.Axis;
import core.space.BoundingBox3;
import core.tuple.Pair;
import scene.geometry.Shape;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.Material;
import scene.materials.TransportMode;
import scene.primitives.Aggregate;
import scene.primitives.Primitive;

public class BoundingVolumeHierarchy implements Aggregate
{
    public static enum SplitMethod
    {
        SURFACE_AREA_HEURISTIC, HLBVH, MIDDLE, EQUAL_COUNTS;
    }

    private static final Logger logger = Logger.getLogger(BoundingVolumeHierarchy.class.getName());
    
    private List<Primitive> primitives;
    private final int maxPrimitivesPerNode;
    private final SplitMethod splitMethod;
    
    private LinearBvhNode[] nodes;
    
    public BoundingVolumeHierarchy(List<Primitive> primitives, int maxPrimitivesPerNode, SplitMethod splitMethod)
    {
        this.primitives = primitives;
        this.maxPrimitivesPerNode = Math.min(255, maxPrimitivesPerNode);
        this.splitMethod = splitMethod;
        
        if (primitives.isEmpty())
        {
            return;
        }
        
        // build BVH from primitives
        List<PrimitiveInfo> primitiveInfos = new ArrayList<>(primitives.size());
        int i = 0;
        for (Primitive primitive : primitives)
        {
            primitiveInfos.add(new PrimitiveInfo(i, primitive.worldBound()));
            i++;
        }
        
        List<Primitive> orderedPrimitives = new LinkedList<>();
        BuildNode buildTreeRoot;
        int numBuildNodes;
        if (splitMethod == SplitMethod.HLBVH)
        {
            throw new UnsupportedOperationException("TODO");
        }
        else
        {
            var tree = recursiveBuild(primitiveInfos, 0, primitives.size(), 0, orderedPrimitives);
            buildTreeRoot = tree.getFirst();
            numBuildNodes = tree.getSecond();
        }
        this.primitives = orderedPrimitives;
        
        nodes = new LinearBvhNode[numBuildNodes];
        for (int j = 0; j < numBuildNodes; j++)
        {
            nodes[j] = new LinearBvhNode();
        }
        flattenTree(buildTreeRoot, 0);
    }
    
    // returns node, totalNodes
    private Pair<BuildNode, Integer> recursiveBuild(List<PrimitiveInfo> primitiveInfos, int start, int end,
            int numExistingNodes, List<Primitive> orderedPrimitives)
    {
        BuildNode node = new BuildNode();
        int totalNodes = numExistingNodes + 1;
        
        // compute bounds of all primitives in node
        BoundingBox3 bounds = new BoundingBox3();
        for (PrimitiveInfo primitiveInfo : primitiveInfos)
        {
            bounds = bounds.union(primitiveInfo.getBounds());
        }
        
        int numPrimitives = end - start;
        if (numPrimitives == 1)
        {
            // create leaf
            int firstPrimitiveOffset = orderedPrimitives.size();
            for(int i = start; i < end; i++)
            {
                int primitiveNumber = primitiveInfos.get(i).getPrimitiveNumber();
                orderedPrimitives.add(primitives.get(primitiveNumber));
            }
            node.initLeaf(firstPrimitiveOffset, numPrimitives, bounds);
            return new Pair<>(node, totalNodes);
        }
        else
        {
            // compute bound of centroids and choose split dimension
            BoundingBox3 centroidBounds = new BoundingBox3();
            for (int i = start; i < end; i++)
            {
                centroidBounds = centroidBounds.union(primitiveInfos.get(i).getCentroid());
            }
            Axis splitAxis = centroidBounds.maximumExtent();
            
            // partition primitives and build children
            int mid = (start + end) / 2;
            if (centroidBounds.getMaxPoint().get(splitAxis) ==
                centroidBounds.getMinPoint().get(splitAxis))
            {
                // degenerate case -- create leaf
                int firstPrimitiveOffset = orderedPrimitives.size();
                for(int i = start; i < end; i++)
                {
                    int primitiveNumber = primitiveInfos.get(i).getPrimitiveNumber();
                    orderedPrimitives.add(primitives.get(primitiveNumber));
                }
                node.initLeaf(firstPrimitiveOffset, numPrimitives, bounds);
                return new Pair<>(node, totalNodes);
            }
            else
            {
                // partition primitives based on split method
                List<PrimitiveInfo> leftPartition;
                List<PrimitiveInfo> rightPartition;
                switch(splitMethod)
                {
                case MIDDLE:
                    double pmid = (centroidBounds.getMinPoint().get(splitAxis) +
                                   centroidBounds.getMaxPoint().get(splitAxis)) /
                                  2;
                    var partitionedPrimitiveInfos = primitiveInfos.stream()
                                                                  .collect(Collectors.partitioningBy(pi -> pi.getCentroid()
                                                                                                             .get(splitAxis) < pmid));
                    leftPartition = partitionedPrimitiveInfos.get(false);
                    rightPartition = partitionedPrimitiveInfos.get(true);
                    
                    if (!leftPartition.isEmpty() && !rightPartition.isEmpty())
                    {
                        logger.fine("MIDDLE: Partitioned " + primitiveInfos.size() +
                                    " primitives into sublists of size " + leftPartition.size() +
                                    " and " + rightPartition.size() + ".");
                        break;
                    }
                    else
                    {
                        // fall through to EQUAL_COUNTS
                    }
                case EQUAL_COUNTS:
                    mid = (start + end) / 2;
                    // TODO: can be done without a full sort -- see C++ std::nth_element
                    primitiveInfos.sort(Comparator.comparingDouble(pi -> pi.getCentroid().get(splitAxis)));
                    leftPartition = primitiveInfos.subList(0, mid);
                    rightPartition = primitiveInfos.subList(mid, primitiveInfos.size());
                    logger.fine("EQUAL_COUNTS: Partitioned " + primitiveInfos.size() +
                                " primitives into sublists of size " + leftPartition.size() +
                                " and " + rightPartition.size() + ".");
                    break;
                case SURFACE_AREA_HEURISTIC:
                    if (numPrimitives <= 4)
                    {
                        // XXX do equal_counts -- can we avoid duplicating code?
                        mid = (start + end) / 2;
                        // TODO: can be done without a full sort -- see C++ std::nth_element
                        primitiveInfos.sort(Comparator.comparingDouble(pi -> pi.getCentroid().get(splitAxis)));
                        leftPartition = primitiveInfos.subList(0, mid);
                        rightPartition = primitiveInfos.subList(mid, primitiveInfos.size());
                        break;
                    }
                    else
                    {
                        // allocate BucketInfos
                        final int numBuckets = 12;
                        BucketInfo[] buckets = new BucketInfo[numBuckets];
                        
                        // initialize BucketInfos
                        for (int i = start; i < end; i++)
                        {
                            int b = (int) (numBuckets *
                                           centroidBounds.offset(primitiveInfos.get(i)
                                                                               .getCentroid())
                                                         .get(splitAxis));
                            if (b == numBuckets)
                            {
                                b = numBuckets - 1;
                            }
                            buckets[b].count++;
                            buckets[b].bounds = buckets[b].bounds.union(primitiveInfos.get(i).getBounds());
                            
                        }
                        
                        // compute costs for splitting after each bucket
                        // traversal cost relative to intersection cost is arbitrarily set to 1/8.
                        double[] costs = new double[numBuckets - 1];
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
                            for (int j = i + 1; j < numBuckets; j++)
                            {
                                b1 = b1.union(buckets[j].bounds);
                                count1 += buckets[j].count;
                            }
                            costs[i] = 0.125 +
                                       (count0 * b0.surfaceArea() + count1 * b1.surfaceArea()) /
                                               bounds.surfaceArea();
                        }
                        
                        // find bucket split that minimizes metric
                        double minCost = costs[0];
                        int minCostSplitBucket = 0;
                        for (int i = 1; i < costs.length; i++)
                        {
                            if (costs[i] < minCost)
                            {
                                minCost = costs[i];
                                minCostSplitBucket = i;
                            }
                        }
                        
                        // create leaf or split
                        double leafCost = numPrimitives;
                        if (numPrimitives > maxPrimitivesPerNode || minCost < leafCost)
                        {
                            final BoundingBox3 finalCentroidBounds = new BoundingBox3(centroidBounds);
                            final int finalSplitBucket = minCostSplitBucket;
                            var splitPrimitiveInfos = primitiveInfos.stream()
                                                                    .collect(Collectors.partitioningBy(pi ->
                                                                    {
                                                                        int b = (int) (numBuckets *
                                                                                       finalCentroidBounds.offset(pi.getCentroid())
                                                                                                          .get(splitAxis));
                                                                        if (b == numBuckets)
                                                                        {
                                                                            b = numBuckets - 1;
                                                                        }
                                                                        return b <= finalSplitBucket;
                                                                    }));
                            leftPartition = splitPrimitiveInfos.get(false);
                            rightPartition = splitPrimitiveInfos.get(true);
                            logger.fine("SURFACE_AREA_HEURISTIC: Partitioned " + primitiveInfos.size() +
                                        " primitives into sublists of size " + leftPartition.size() +
                                        " and " + rightPartition.size() + ".");
                        }
                        else
                        {
                            // create leaf
                            int firstPrimitiveOffset = orderedPrimitives.size();
                            for(int i = start; i < end; i++)
                            {
                                int primitiveNumber = primitiveInfos.get(i).getPrimitiveNumber();
                                orderedPrimitives.add(primitives.get(primitiveNumber));
                            }
                            node.initLeaf(firstPrimitiveOffset, numPrimitives, bounds);
                            return new Pair<>(node, totalNodes);
                        }
                    }
                    break;
                case HLBVH:
                    throw new RuntimeException("Programming error.");
                default:
                    throw new RuntimeException("Unknown split method " + splitMethod);
                }
                var buildLeft = recursiveBuild(leftPartition,
                                               0,
                                               leftPartition.size(),
                                               totalNodes,
                                               orderedPrimitives);
                BuildNode leftChild = buildLeft.getFirst();
                totalNodes = buildLeft.getSecond();
                
                var buildRight = recursiveBuild(rightPartition,
                                                0,
                                                rightPartition.size(),
                                                totalNodes,
                                                orderedPrimitives);
                BuildNode rightChild = buildRight.getFirst();
                totalNodes = buildRight.getSecond();
                
                node.initInterior(splitAxis, leftChild, rightChild);
            }
        }
        
        return new Pair<>(node, totalNodes);
    }
    
    
    // returns (node offset, running offset)
    private Pair<Integer, Integer> flattenTree(BuildNode node, int offset)
    {
        LinearBvhNode linearNode = nodes[offset];
        linearNode.bounds = node.bounds;
        int myOffset = offset++;
        if (node.numPrimitives > 0)
        {
            // leaf node -- linear node's offset is the starting offset into the ordered primitives list
            linearNode.offset = node.firstPrimitiveOffset;
            linearNode.numPrimitives = node.numPrimitives;
        }
        else
        {
            // interior node -- linear node's offset is the index of the second child
            linearNode.axis = node.splitAxis;
            linearNode.numPrimitives = 0;
            offset = flattenTree(node.children[0], offset).getSecond();
            var newOffsets = flattenTree(node.children[1], offset);
            linearNode.offset = newOffsets.getFirst();
            offset = newOffsets.getSecond();
        }
        return new Pair<>(myOffset, offset);
    }
    
    @Override
    public BoundingBox3 worldBound()
    {
        if (nodes != null)
        {
            return nodes[0].bounds;
        }
        return new BoundingBox3();
    }

    @Override
    public SurfaceInteraction intersect(Ray ray)
    {
        SurfaceInteraction intersection = null;
        Direction3 invDirection = new Direction3(1 / ray.getDirection().x(),
                                                 1 / ray.getDirection().y(),
                                                 1 / ray.getDirection().z());
        boolean[] directionIsNegative = new boolean[] { invDirection.x() < 0, invDirection.y() < 0,
                                                        invDirection.z() < 0 };
        // follow ray through nodes to find primitive intersection
        int toVisitOffset = 0;
        int currentNodeIndex = 0;
        int[] nodesToVisit = new int[64];
        while(true)
        {
            LinearBvhNode node = nodes[currentNodeIndex];
            // check ray against node
            if (node.bounds.intersect(ray, invDirection, directionIsNegative))
            {
                if (node.numPrimitives > 0)
                {
                    // leaf node; intersect ray with primitives
                    for (int i = 0; i < node.numPrimitives; i++)
                    {
                        // Primitive intersection updates ray's tMax, so we always get the closest
                        // intersection.
                        SurfaceInteraction isect = primitives.get(node.offset + i).intersect(ray);
                        if (isect != null)
                        {
                            intersection = isect;
                        }
                    }
                    if (toVisitOffset == 0)
                    {
                        break;
                    }
                    // pop next node to visit off stack
                    currentNodeIndex = nodesToVisit[--toVisitOffset];
                }
                else
                {
                    // put far node on nodesToVisit stack; advance to near node
                    if (directionIsNegative[node.axis.ordinal()])
                    {
                        // push first child onto stack and visit second child next
                        nodesToVisit[toVisitOffset++] = currentNodeIndex + 1;
                        currentNodeIndex = node.offset;
                    }
                    else
                    {
                        // push second child onto stack and visit first child next
                        nodesToVisit[toVisitOffset++] = node.offset;
                        currentNodeIndex = currentNodeIndex + 1;
                    }
                }
            }
            else
            {
                if (toVisitOffset == 0)
                {
                    break;
                }
                // complete miss -- pop next node to visit off stack
                currentNodeIndex = nodesToVisit[--toVisitOffset];
            }
        }
        return intersection;
    }

    @Override
    public boolean intersectP(Ray ray)
    {
        Direction3 invDirection = new Direction3(1 / ray.getDirection().x(),
                                                 1 / ray.getDirection().y(),
                                                 1 / ray.getDirection().z());
        boolean[] directionIsNegative = new boolean[] { invDirection.x() < 0, invDirection.y() < 0,
                                                        invDirection.z() < 0 };
        // follow ray through nodes to find primitive intersection
        int toVisitOffset = 0;
        int currentNodeIndex = 0;
        int[] nodesToVisit = new int[64];
        while(true)
        {
            LinearBvhNode node = nodes[currentNodeIndex];
            // check ray against node
            if (node.bounds.intersect(ray, invDirection, directionIsNegative))
            {
                if (node.numPrimitives > 0)
                {
                    // leaf node; intersect ray with primitives
                    for (int i = 0; i < node.numPrimitives; i++)
                    {
                        // Primitive intersection updates ray's tMax, so we always get the closest
                        // intersection.
                        boolean hit = primitives.get(node.offset + i).intersectP(ray);
                        if (hit)
                        {
                            return true;
                        }
                    }
                    if (toVisitOffset == 0)
                    {
                        break;
                    }
                    currentNodeIndex = nodesToVisit[--toVisitOffset];
                }
                else
                {
                    // put far node on nodesToVisit stack; advance to near node
                    if (directionIsNegative[node.axis.ordinal()])
                    {
                        nodesToVisit[toVisitOffset++] = currentNodeIndex + 1;
                        currentNodeIndex = node.offset;
                    }
                    else
                    {
                        nodesToVisit[toVisitOffset++] = node.offset;
                        currentNodeIndex = currentNodeIndex + 1;
                    }
                }
            }
            else
            {
                if (toVisitOffset == 0)
                {
                    break;
                }
                currentNodeIndex = nodesToVisit[--toVisitOffset];
            }
        }
        return false;
    }

    @Override
    public Material getMaterial()
    {
        throw new UnsupportedOperationException("No material for BoundingVolumeHierarchy.");
    }
    
    @Override
    public Shape getShape()
    {
        throw new UnsupportedOperationException("No shape for BoundingVolumeHierarchy.");
    }

    @Override
    public void computeScatteringFunctions(SurfaceInteraction surfaceInteraction,
            TransportMode mode, boolean allowMultipleLobes)
    {
        throw new UnsupportedOperationException("No scattering functions for BoundingVolumeHierarchy.");
    }
    
    private static class BucketInfo
    {
        private int count = 0;
        private BoundingBox3 bounds = new BoundingBox3();
    }
    
    private static class BuildNode
    {
        private BoundingBox3 bounds;
        private BuildNode[] children = new BuildNode[2];
        private Axis splitAxis;
        private int firstPrimitiveOffset; // needed?
        private int numPrimitives;
        
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
    
    private static class LinearBvhNode
    {
        BoundingBox3 bounds;
        int offset; // primitive offset for leaf nodes, secondChild offset for interior nodes
        int numPrimitives; // 0 -> interior node
        Axis axis;
    }
}
