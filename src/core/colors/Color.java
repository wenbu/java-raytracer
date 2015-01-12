package core.colors;

public class Color
{
    private double r;
    private double g;
    private double b;

    public Color(double r, double g, double b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Color()
    {
        this(0, 0, 0);
    }

    public double r()
    {
        return r;
    }

    public double g()
    {
        return g;
    }

    public double b()
    {
        return b;
    }

    /*
     * Arithmetic operations that return a new Color.
     */

    public Color times(double scalar)
    {
        return new Color(scalar * r, scalar * g, scalar * b);
    }

    public Color times(Color other)
    {
        return new Color(r * other.r(), g * other.g(), b * other.b());
    }

    public Color plus(Color other)
    {
        return new Color(r + other.r(), g + other.g(), b + other.b());
    }

    public Color divide(double scalar)
    {
        return times(1.0 / scalar);
    }

    /*
     * Arithmetic operations that mutate this Color.
     */

    public synchronized Color add(Color other)
    {
        r += other.r;
        g += other.g;
        b += other.b;
        return this;
    }

    public synchronized Color subtract(Color other)
    {
        r -= other.r;
        g -= other.g;
        b -= other.b;
        return this;
    }

    public synchronized Color multiplyBy(Color other)
    {
        r *= other.r;
        g *= other.g;
        b *= other.b;
        return this;
    }

    public synchronized Color multiplyBy(double scalar)
    {
        r *= scalar;
        g *= scalar;
        b *= scalar;
        return this;
    }

    public synchronized Color divideBy(double scalar)
    {
        return multiplyBy(1.0 / scalar);
    }

    @Override
    public boolean equals(Object other)
    {
        if (!( other instanceof Color ))
            return false;

        Color otherColor = (Color) other;
        return ( r == otherColor.r ) && ( g == otherColor.g ) &&
               ( b == otherColor.b );
    }
    
    @Override
    public String toString()
    {
        return String.format("Color [%.3f, %.3f, %.3f]", r, g, b);
    }
}
