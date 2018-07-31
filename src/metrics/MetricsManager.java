package metrics;

import java.util.HashSet;
import java.util.Set;

public class MetricsManager
{
    private final Set<MetricsAware> metricsAwareEntities = new HashSet<>();
    
    public void registerMetricsAwareEntities(MetricsAware... metricsAwareEntities)
    {
        for (MetricsAware metricsAwareEntity : metricsAwareEntities)
        {
            this.metricsAwareEntities.add(metricsAwareEntity);
        }
    }
    
    public void logMetrics()
    {
        for (MetricsAware metricsAwareEntity : metricsAwareEntities)
        {
            metricsAwareEntity.logMetrics();
        }
    }
}
