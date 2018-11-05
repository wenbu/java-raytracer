package scene.primitives.accelerator.bvh;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import core.Ray;
import core.math.Direction3;
import core.space.BoundingBox3;
import scene.interactions.impl.SurfaceInteraction;
import scene.primitives.Aggregate;
import scene.primitives.Primitive;
import scene.primitives.accelerator.bvh.tree.EqualCountsTreeBuilder;
import scene.primitives.accelerator.bvh.tree.MiddleTreeBuilder;
import scene.primitives.accelerator.bvh.tree.SurfaceAreaHeuristicTreeBuilder;
import scene.primitives.accelerator.bvh.tree.TreeBuilder;

public class BoundingVolumeHierarchy implements Aggregate
{
    public static enum SplitMethod
    {
        SURFACE_AREA_HEURISTIC, HLBVH, MIDDLE, EQUAL_COUNTS;
    }

    private static final Logger logger = Logger.getLogger(BoundingVolumeHierarchy.class.getName());
    
    private List<Primitive> primitives;
    private final TreeBuilder treeBuilder;
    
    private LinearNode[] nodes;
    
    public BoundingVolumeHierarchy(List<Primitive> primitives, int maxPrimitivesPerNode, SplitMethod splitMethod)
    {
        this.primitives = Collections.unmodifiableList(primitives);
        maxPrimitivesPerNode = Math.min(255, maxPrimitivesPerNode);
        treeBuilder = getTreeBuilder(splitMethod, maxPrimitivesPerNode);
        
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
        
        long treeBuildStart = System.currentTimeMillis();
        nodes = treeBuilder.build(primitiveInfos, orderedPrimitives);
        long treeBuildEnd = System.currentTimeMillis();

        logger.info("Spent " + (treeBuildEnd - treeBuildStart) +
                    "ms building BVH tree with split method " + splitMethod +
                    ". Number of primitives = " + primitives.size());
        
        this.primitives = Collections.unmodifiableList(orderedPrimitives);
    }
    
    private TreeBuilder getTreeBuilder(SplitMethod splitMethod, int maxPrimitivesPerNode)
    {
        switch(splitMethod)
        {
            case MIDDLE:
                return new MiddleTreeBuilder(this.primitives);
            case EQUAL_COUNTS:
                return new EqualCountsTreeBuilder(this.primitives);
            case SURFACE_AREA_HEURISTIC:
                return new SurfaceAreaHeuristicTreeBuilder(this.primitives, maxPrimitivesPerNode);
            default:
                throw new UnsupportedOperationException("Unsupported split method: " + splitMethod);
        }
    }
    
    @Override
    public BoundingBox3 worldBound()
    {
        if (nodes != null)
        {
            return nodes[0].getBounds();
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
        int currentNodeIndex = 0;
        Deque<Integer> nodesToVisit = new ArrayDeque<>();
        
        while(true)
        {
            LinearNode node = nodes[currentNodeIndex];
            // check ray against node
            if (node.getBounds().intersect(ray, invDirection, directionIsNegative))
            {
                if (node.getNumPrimitives() > 0)
                {
                    // leaf node; intersect ray with primitives
                    for (int i = 0; i < node.getNumPrimitives(); i++)
                    {
                        // Primitive intersection updates ray's tMax, so we always get the closest
                        // intersection.
                        SurfaceInteraction isect = primitives.get(node.getOffset() + i).intersect(ray);
                        if (isect != null)
                        {
                            intersection = isect;
                        }
                    }
                    if (nodesToVisit.isEmpty())
                    {
                        break;
                    }
                    currentNodeIndex = nodesToVisit.pop();
                }
                else
                {
                    // put far node on nodesToVisit stack; advance to near node
                    if (directionIsNegative[node.getAxis().ordinal()])
                    {
                        nodesToVisit.push(currentNodeIndex + 1);
                        currentNodeIndex = node.getOffset();
                    }
                    else
                    {
                        nodesToVisit.push(node.getOffset());
                        currentNodeIndex = currentNodeIndex + 1;
                    }
                }
            }
            else
            {
                if (nodesToVisit.isEmpty())
                {
                    break;
                }
                // complete miss -- pop next node to visit off stack
                currentNodeIndex = nodesToVisit.pop();
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
        int currentNodeIndex = 0;
        Deque<Integer> nodesToVisit = new ArrayDeque<>();
        
        while(true)
        {
            LinearNode node = nodes[currentNodeIndex];
            // check ray against node
            if (node.getBounds().intersect(ray, invDirection, directionIsNegative))
            {
                if (node.getNumPrimitives() > 0)
                {
                    // leaf node; intersect ray with primitives
                    for (int i = 0; i < node.getNumPrimitives(); i++)
                    {
                        // Primitive intersection updates ray's tMax, so we always get the closest
                        // intersection.
                        boolean hit = primitives.get(node.getOffset() + i).intersectP(ray);
                        if (hit)
                        {
                            return true;
                        }
                    }
                    if (nodesToVisit.isEmpty())
                    {
                        break;
                    }
                    currentNodeIndex = nodesToVisit.pop();
                }
                else
                {
                    // put far node on nodesToVisit stack; advance to near node
                    if (directionIsNegative[node.getAxis().ordinal()])
                    {
                        nodesToVisit.push(currentNodeIndex + 1);
                        currentNodeIndex = node.getOffset();
                    }
                    else
                    {
                        nodesToVisit.push(node.getOffset());
                        currentNodeIndex = currentNodeIndex + 1;
                    }
                }
            }
            else
            {
                if (nodesToVisit.isEmpty())
                {
                    break;
                }
                currentNodeIndex = nodesToVisit.pop();
            }
        }
        return false;
    }
}
