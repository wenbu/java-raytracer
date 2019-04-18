package metrics;

import java.util.function.Supplier;

import static metrics.MetricsCategory.*;
import static metrics.Unit.*;

public enum Metrics
{
    ACCELERATOR_BUILD_TIME(ACCELERATOR, "Time to build accelerator", MS, MetricsLogger.getInstance()::getAcceleratorBuildTime),
    NUM_PRIMITIVES(ACCELERATOR, "Number of primitives", NONE, MetricsLogger.getInstance()::getNumPrimitives),

    TEXTURE_CACHE_HITS(TEXTURE, "Texture cache hits", NONE, MetricsLogger.getInstance()::getNumTextureCacheHits),
    TEXTURE_CACHE_MISSES(TEXTURE, "Texture cache misses", NONE, MetricsLogger.getInstance()::getNumTextureCacheMisses),
    TEXTURE_LOAD_TIME(TEXTURE, "Total time spent loading textures", MS, MetricsLogger.getInstance()::getTextureLoadTimes),
    TEXTURE_RESAMPLE_TIME(TEXTURE, "Total time spent resampling textures", MS, MetricsLogger.getInstance()::getTextureResampleTimes),
    MIPMAP_PROCESS_TIME(TEXTURE, "Total time spent processing mipmaps", MS, MetricsLogger.getInstance()::getMipmapProcessTimes),

    SCENE_PREPROCESS_TIME(INTEGRATOR, "Scene preprocessing time", MS, MetricsLogger.getInstance()::getScenePreprocessTime),
    RENDER_THREAD_COUNT(INTEGRATOR, "Number of rendering threads", NONE, MetricsLogger.getInstance()::getNumRenderingThreads),
    NUM_TILES(INTEGRATOR, "Number of tiles", NONE, MetricsLogger.getInstance()::getNumTiles),
    TOTAL_RENDER_TIME(INTEGRATOR, "Total render time", MS, MetricsLogger.getInstance()::getTotalRenderTime),
    OUTPUT_WRITE_TIME(INTEGRATOR, "Output write time", MS, MetricsLogger.getInstance()::getOutputWriteTime);

    private final MetricsCategory category;
    private final String metricName;
    private final Unit unit;
    private final Supplier<Long> valueSupplier;

    private static final String INT_FORMAT_STRING = "\t%-42s%8d%s";

    Metrics(MetricsCategory category, String metricName, Unit unit, Supplier<Long> valueSupplier)
    {
        this.category = category;
        this.metricName = metricName;
        this.unit = unit;
        this.valueSupplier = valueSupplier;
    }

    public MetricsCategory getCategory()
    {
        return category;
    }

    public String getStringForLogging()
    {
        Long value = valueSupplier.get();
        if (value == null)
        {
            return null;
        }
        return String.format(INT_FORMAT_STRING, metricName, value, unit.toString());
    }
}
