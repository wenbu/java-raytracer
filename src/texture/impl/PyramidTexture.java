package texture.impl;

import core.math.Point2;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;
import texture.mapping.TextureMapping2D;
import utilities.MathUtilities;

public class PyramidTexture implements Texture<Double>
{
    private final TextureMapping2D mapping;
    private final Point2 cellSize;
    private final double baseValue;
    private final double peakValue;

    public PyramidTexture(TextureMapping2D mapping, Point2 cellSize, double baseValue, double peakValue)
    {
        this.mapping = mapping;
        this.cellSize = cellSize;
        this.baseValue = baseValue;
        this.peakValue = peakValue;
    }

    @Override
    public Double evaluate(SurfaceInteraction surfaceInteraction)
    {
        var    m = mapping.map(surfaceInteraction);
        Point2 st = m.getFirst();

        double s = st.get(0);
        double t = st.get(1);

        if (s < 0)
        {
            s = -s + 1;
        }
        if (t < 0)
        {
            t = -t + 1;
        }

        // remap s, t into cell
        s = s % cellSize.get(0);
        t = t % cellSize.get(1);

        double sDistance = Math.min(s, cellSize.get(0) - s);
        double tDistance = Math.min(t, cellSize.get(1) - t);

        if (sDistance < tDistance)
        {
            return MathUtilities.lerp(2 * sDistance / cellSize.get(0), baseValue, peakValue);
        }
        else
        {
            return MathUtilities.lerp(2 * tDistance / cellSize.get(1), baseValue, peakValue);
        }
    }
}
