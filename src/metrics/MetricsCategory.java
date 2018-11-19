package metrics;

public enum MetricsCategory
{
    ACCELERATOR("Accelerator"),
    TEXTURE("Texture"),
    INTEGRATOR("Integrator");

    private final String categoryName;

    MetricsCategory(String categoryName)
    {
        this.categoryName = categoryName + " Metrics";
    }

    public String getName()
    {
        return categoryName;
    }
}
