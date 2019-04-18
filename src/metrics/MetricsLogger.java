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
    private Long numPrimitives = null;
    private Long scenePreprocessTime = null;
    private Long numRenderingThreads = null;
    private Long numTiles = null;
    private Long totalRenderTime = null;
    private Long outputWriteTime = null;
    private final AtomicLong textureCacheHits = new AtomicLong();
    private final AtomicLong textureCacheMisses = new AtomicLong();
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

    Long getNumTextureCacheHits()
    {
        return textureCacheHits.get();
    }

    public void onTextureCacheMiss()
    {
        textureCacheMisses.incrementAndGet();
    }

    Long getNumTextureCacheMisses()
    {
        return textureCacheMisses.get();
    }

    public void onTextureLoaded(long timeToLoad)
    {
        textureLoadTimes.addAndGet(timeToLoad);
    }

    Long getTextureLoadTimes()
    {
        return textureLoadTimes.get();
    }

    public void onTextureResampled(long timeToResample)
    {
        textureResampleTimes.addAndGet(timeToResample);
    }

    Long getTextureResampleTimes()
    {
        return textureResampleTimes.get();
    }

    public void onMipmapProcessed(long timeToProcess)
    {
        mipmapProcessTimes.addAndGet(timeToProcess);
    }

    Long getMipmapProcessTimes()
    {
        return mipmapProcessTimes.get();
    }

    public void onAcceleratorStructureBuilt(long timeToBuild, long numPrimitives)
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

    Long getAcceleratorBuildTime()
    {
        return acceleratorBuildTime;
    }

    Long getNumPrimitives()
    {
        return numPrimitives;
    }

    public void onRenderStart(long scenePreprocessTime, long numRenderingThreads, long numTiles)
    {
        if (this.scenePreprocessTime == null && this.numRenderingThreads == null && this.numTiles == null)
        {
            this.scenePreprocessTime = scenePreprocessTime;
            this.numRenderingThreads = numRenderingThreads;
            this.numTiles = numTiles;
        }
        else
        {
            throw new RuntimeException("Programming error. Rendering should only start once.");
        }
    }
    public void onRenderComplete(long totalRenderTime)
    {
        if (this.totalRenderTime == null)
        {
            this.totalRenderTime = totalRenderTime;
        }
        else
        {
            throw new RuntimeException("Programming error. Rendering should only complete once.");
        }
    }

    Long getScenePreprocessTime()
    {
        return scenePreprocessTime;
    }

    Long getNumRenderingThreads()
    {
        return numRenderingThreads;
    }

    Long getNumTiles()
    {
        return numTiles;
    }

    Long getTotalRenderTime()
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

    Long getOutputWriteTime()
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
                String loggingString = metric.getStringForLogging();
                if (loggingString == null)
                {
                    continue;
                }
                sb.append(loggingString).append("\n");
            }
        }

        logger.info(sb.toString());
    }
}
