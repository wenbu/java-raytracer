package metrics;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class MetricsLogger
{
    private static final MetricsLogger INSTANCE = new MetricsLogger();
    private static final Logger logger = Logger.getLogger(MetricsLogger.class.getName());

    private Long acceleratorBuildTime = null;
    private Integer numPrimitives = null;
    private Long scenePreprocessTime = null;
    private Integer numRenderingThreads = null;
    private Long totalRenderTime = null;
    private Long outputWriteTime = null;
    private final AtomicInteger textureCacheHits = new AtomicInteger();
    private final AtomicInteger textureCacheMisses = new AtomicInteger();
    private final AtomicLong textureLoadTimes = new AtomicLong();
    private final AtomicLong textureResampleTimes = new AtomicLong();
    private final AtomicLong mipmapProcessTimes = new AtomicLong();

    public static MetricsLogger getInstance()
    {
        return INSTANCE;
    }

    public void onTextureCacheHit()
    {
        textureCacheHits.incrementAndGet();
    }

    long getNumTextureCacheHits()
    {
        return textureCacheHits.get();
    }

    public void onTextureCacheMiss()
    {
        textureCacheMisses.incrementAndGet();
    }

    long getNumTextureCacheMisses()
    {
        return textureCacheMisses.get();
    }

    public void onTextureLoaded(long timeToLoad)
    {
        textureLoadTimes.addAndGet(timeToLoad);
    }

    long getTextureLoadTimes()
    {
        return textureLoadTimes.get();
    }

    public void onTextureResampled(long timeToResample)
    {
        textureResampleTimes.addAndGet(timeToResample);
    }

    long getTextureResampleTimes()
    {
        return textureResampleTimes.get();
    }

    public void onMipmapProcessed(long timeToProcess)
    {
        mipmapProcessTimes.addAndGet(timeToProcess);
    }

    long getMipmapProcessTimes()
    {
        return mipmapProcessTimes.get();
    }

    public void onAcceleratorStructureBuilt(long timeToBuild, int numPrimitives)
    {
        if (acceleratorBuildTime == null && this.numPrimitives == null)
        {
            acceleratorBuildTime = timeToBuild;
            this.numPrimitives = numPrimitives;
        }
        else
        {
            throw new RuntimeException("Programming error. Only one accelerator structure should be built.");
        }
    }

    long getAcceleratorBuildTime()
    {
        return acceleratorBuildTime;
    }

    long getNumPrimitives()
    {
        return numPrimitives;
    }

    public void onRenderComplete(long scenePreprocessTime, int numRenderingThreads, long totalRenderTime)
    {
        if (this.scenePreprocessTime == null && this.numRenderingThreads == null && this.totalRenderTime == null)
        {
            this.scenePreprocessTime = scenePreprocessTime;
            this.numRenderingThreads = numRenderingThreads;
            this.totalRenderTime = totalRenderTime;
        }
        else
        {
            throw new RuntimeException("Programming error. Rendering should only complete once.");
        }
    }

    long getScenePreprocessTime()
    {
        return scenePreprocessTime;
    }

    long getNumRenderingThreads()
    {
        return numRenderingThreads;
    }

    long getTotalRenderTime()
    {
        return totalRenderTime;
    }

    public void onOutputComplete(long outputWriteTime)
    {
        if (this.outputWriteTime == null)
        {
            this.outputWriteTime = outputWriteTime;
        }
        else
        {
            throw new RuntimeException("Programming error. Only one output should be written.");
        }
    }

    long getOutputWriteTime()
    {
        return outputWriteTime;
    }

    private static final String METRIC_TYPE_SEPARATOR = "--------------------------------------------------------------------------------\n";

    public void outputMetrics()
    {
        Map<MetricsCategory, Set<Metrics>> categoryMap = new TreeMap<>();
        for (Metrics metric : Metrics.values())
        {
            MetricsCategory category = metric.getCategory();
            if (!categoryMap.containsKey(category))
            {
                categoryMap.put(category, new TreeSet<>());
            }

            categoryMap.get(category).add(metric);
        }

        StringBuilder sb = new StringBuilder();
        for (MetricsCategory category : MetricsCategory.values())
        {
            sb.append("\n").append(category.getName()).append("\n");
            sb.append(METRIC_TYPE_SEPARATOR);
            for (Metrics metric : categoryMap.get(category))
            {
                sb.append(metric.getStringForLogging()).append("\n");
            }
        }

        logger.info(sb.toString());
    }
}
