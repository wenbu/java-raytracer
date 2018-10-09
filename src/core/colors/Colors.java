package core.colors;

import java.util.Collection;

public class Colors
{
    public static final RGBSpectrum BLACK = new RGBSpectrum(0, 0, 0);
    public static final RGBSpectrum WHITE = new RGBSpectrum(1, 1, 1);

    public static final RGBSpectrum GRAY90 = new RGBSpectrum(0.9, 0.9, 0.9);
    public static final RGBSpectrum GRAY80 = new RGBSpectrum(0.8, 0.8, 0.8);
    public static final RGBSpectrum GRAY70 = new RGBSpectrum(0.7, 0.7, 0.7);
    public static final RGBSpectrum GRAY60 = new RGBSpectrum(0.6, 0.6, 0.6);
    public static final RGBSpectrum GRAY50 = new RGBSpectrum(0.5, 0.5, 0.5);
    public static final RGBSpectrum GRAY40 = new RGBSpectrum(0.4, 0.4, 0.4);
    public static final RGBSpectrum GRAY30 = new RGBSpectrum(0.3, 0.3, 0.3);
    public static final RGBSpectrum GRAY20 = new RGBSpectrum(0.2, 0.2, 0.2);
    public static final RGBSpectrum GRAY10 = new RGBSpectrum(0.1, 0.1, 0.1);

    public static final RGBSpectrum RED = new RGBSpectrum(1, 0.05, 0.05);
    public static final RGBSpectrum GREEN = new RGBSpectrum(0.05, 1, 0.05);
    public static final RGBSpectrum BLUE = new RGBSpectrum(0.05, 0.05, 1);

    public static final RGBSpectrum YELLOW = new RGBSpectrum(1, 1, 0.05);
    public static final RGBSpectrum CYAN = new RGBSpectrum(0.05, 1, 1);
    public static final RGBSpectrum MAGENTA = new RGBSpectrum(1, 0.05, 1);

    public static RGBSpectrum sum(Collection<? extends RGBSpectrum> colors)
    {
        return Colors.sum(colors.toArray(new RGBSpectrum[0]));
    }

    public static RGBSpectrum sum(RGBSpectrum... colors)
    {
        double sumR = 0;
        double sumG = 0;
        double sumB = 0;

        for (RGBSpectrum color : colors)
        {
            sumR += color.r();
            sumG += color.g();
            sumB += color.b();
        }

        return new RGBSpectrum(sumR, sumG, sumB);
    }
}
