package metrics;

public enum Unit
{
    NONE(""),
    MS(" ms");

    private final String str;

    Unit(String str)
    {
        this.str = str;
    }

    @Override
    public String toString()
    {
        return str;
    }
}
