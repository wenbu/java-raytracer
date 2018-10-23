package scene.primitives.accelerator.bvh.tree;

import java.util.List;

import core.space.Axis;
import core.space.BoundingBox3;
import core.tuple.Pair;
import scene.primitives.Primitive;
import scene.primitives.accelerator.bvh.LinearNode;
import scene.primitives.accelerator.bvh.PrimitiveInfo;

public abstract class TreeBuilder
{
    protected final List<Primitive> unorderedPrimitives;
    protected final int maxPrimitivesPerNode;
    
    protected TreeBuilder(List<Primitive> unorderedPrimitives)
    {
        this(unorderedPrimitives, 0);
    }
    
    protected TreeBuilder(List<Primitive> unorderedPrimitives, int maxPrimitivesPerNode)
    {
        this.unorderedPrimitives = unorderedPrimitives;
        this.maxPrimitivesPerNode = maxPrimitivesPerNode;
    }
    
    public LinearNode[] build(List<PrimitiveInfo> primitiveInfos, List<Primitive> orderedPrimitives)
    {
        var buildTree = recursiveBuild(primitiveInfos, 0, orderedPrimitives);
        BuildNode rootNode = buildTree.getFirst();
        int numBuildNodes = buildTree.getSecond();
        
        LinearNode[] linearizedTree = new LinearNode[numBuildNodes];
        for (int i = 0; i < numBuildNodes; i++)
        {
            linearizedTree[i] = new LinearNode();
        }
        flattenTree(linearizedTree, rootNode, 0);
        
        return linearizedTree;
    }

    protected Pair<BuildNode, Integer> recursiveBuild(List<PrimitiveInfo> primitiveInfos,
            int numTotalNodes, List<Primitive> orderedPrimitives)
    {
        BuildNode node = new BuildNode();
        int totalNodes = numTotalNodes + 1;
        
        // compute bounds of all primitives in node
        BoundingBox3 bounds = new BoundingBox3();
        for (PrimitiveInfo primitiveInfo : primitiveInfos)
        {
            bounds = bounds.union(primitiveInfo.getBounds());
        }
        
        if (primitiveInfos.size() == 1)
        {
            initLeaf(node, primitiveInfos, orderedPrimitives, bounds);
            return new Pair<>(node, totalNodes);
        }
        else
        {
            // compute bound of centroids and choose split dimension
            BoundingBox3 centroidBounds = new BoundingBox3();
            for (PrimitiveInfo primitiveInfo : primitiveInfos)
            {
                centroidBounds = centroidBounds.union(primitiveInfo.getCentroid());
            }
            Axis splitAxis = centroidBounds.maximumExtent();
            
            if (centroidBounds.getMaxPoint().get(splitAxis) ==
                centroidBounds.getMinPoint().get(splitAxis))
            {
                // handle degenerate case
                initLeaf(node, primitiveInfos, orderedPrimitives, bounds);
                return new Pair<>(node, totalNodes);
            }
            else
            {
                // partition primitives based on split method
                var partition = partition(node,
                                          splitAxis,
                                          primitiveInfos,
                                          orderedPrimitives,
                                          centroidBounds,
                                          bounds);
                if (partition == null)
                {
                    return new Pair<>(node, totalNodes);
                }
                else
                {
                    List<PrimitiveInfo> leftPartition = partition.getFirst();
                    List<PrimitiveInfo> rightPartition = partition.getSecond();
                    
                    var left = recursiveBuild(leftPartition, totalNodes, orderedPrimitives);
                    BuildNode leftChild = left.getFirst();
                    totalNodes = left.getSecond();
                    
                    var right = recursiveBuild(rightPartition, totalNodes, orderedPrimitives);
                    BuildNode rightChild = right.getFirst();
                    totalNodes = right.getSecond();
                    
                    node.initInterior(splitAxis, leftChild, rightChild);
                }
            }
        }
        return new Pair<>(node, totalNodes);
    }
    
    /**
     * Partition the provided list of primitiveInfos and return the two partitioned sublists, or
     * return null if a leaf was created instead.
     */
    protected abstract Pair<List<PrimitiveInfo>, List<PrimitiveInfo>> partition(BuildNode node,
            Axis splitAxis, List<PrimitiveInfo> primitiveInfos, List<Primitive> orderedPrimitives,
            BoundingBox3 centroidBounds, BoundingBox3 primitiveBounds);
    
    private Pair<Integer, Integer> flattenTree(LinearNode[] linearizedTree, BuildNode buildNode, int offset)
    {
        LinearNode linearNode = linearizedTree[offset];
        linearNode.setBounds(buildNode.bounds);
        int myOffset = offset++;
        
        if (buildNode.numPrimitives > 0)
        {
            // leaf node -- linear node's offset is the starting offset into the ordered primitives list
            linearNode.setOffset(buildNode.firstPrimitiveOffset);
            linearNode.setNumPrimitives(buildNode.numPrimitives);
        }
        else
        {
            // interior node -- linear node's offset is the index of the second child
            linearNode.setAxis(buildNode.splitAxis);
            linearNode.setNumPrimitives(0);
            offset = flattenTree(linearizedTree, buildNode.children[0], offset).getSecond();
            var newOffsets = flattenTree(linearizedTree, buildNode.children[1], offset);
            linearNode.setOffset(newOffsets.getFirst());
            offset = newOffsets.getSecond();
        }
        return new Pair<>(myOffset, offset);
    }

    protected void initLeaf(BuildNode node, List<PrimitiveInfo> primitiveInfos,
            List<Primitive> orderedPrimitives, BoundingBox3 bounds)
    {
        int firstPrimitiveOffset = orderedPrimitives.size();
        for (PrimitiveInfo primitiveInfo : primitiveInfos)
        {
            int primitiveNumber = primitiveInfo.getPrimitiveNumber();
            orderedPrimitives.add(unorderedPrimitives.get(primitiveNumber));
        }
        node.initLeaf(firstPrimitiveOffset, primitiveInfos.size(), bounds);
    }
}
