package film.filter.impl;

import core.math.Direction2;
import core.math.Point2;
import film.filter.Filter;

public class MitchellFilter extends Filter
{
    private final double b;
    private final double c;

    /**
     * It's recommended that b and c lie on the line b + 2c = 1. 
     */
    public MitchellFilter(Direction2 radius, double b, double c)
    {
        super(radius);
        this.b = b;
        this.c = c;
    }

    @Override
    public double evaluate(Point2 p)
    {
        return mitchell1D(p.x() * invRadius.x()) * mitchell1D(p.y() * invRadius.y());
    }

    private double mitchell1D(double x)
    {
        x = Math.abs(2 * x);
        if (x > 1)
        {
            return ((-b - 6 * c) * x * x * x + (6 * b + 30 * c) * x * x + (-12 * b - 48 * c) * x +
                    (8 * b + 24 * c)) *
                   (1.0 / 6.0);
        } else
        {
            return ((12 - 9 * b - 6 * c) * x * x * x + (-18 + 12 * b + 6 * c) * x * x +
                    (6 - 2 * b)) *
                   (1.0 / 6.0);
        }
    }
}
