package core.colors;

import java.util.Collection;

public class Colors
{
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(1, 1, 1);

    public static final Color GRAY90 = new Color(0.9, 0.9, 0.9);
    public static final Color GRAY80 = new Color(0.8, 0.8, 0.8);
    public static final Color GRAY70 = new Color(0.7, 0.7, 0.7);
    public static final Color GRAY60 = new Color(0.6, 0.6, 0.6);
    public static final Color GRAY50 = new Color(0.5, 0.5, 0.5);
    public static final Color GRAY40 = new Color(0.4, 0.4, 0.4);
    public static final Color GRAY30 = new Color(0.3, 0.3, 0.3);
    public static final Color GRAY20 = new Color(0.2, 0.2, 0.2);
    public static final Color GRAY10 = new Color(0.1, 0.1, 0.1);

    public static final Color RED = new Color(1, 0, 0);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);

    public static final Color YELLOW = new Color(1, 1, 0);
    public static final Color CYAN = new Color(0, 1, 1);
    public static final Color MAGENTA = new Color(1, 0, 1);

    public static Color sum(Collection<? extends Color> colors)
    {
        return Colors.sum(colors.toArray(new Color[0]));
    }

    public static Color sum(Color... colors)
    {
        double sumR = 0;
        double sumG = 0;
        double sumB = 0;

        for (Color color : colors)
        {
            sumR += color.r();
            sumG += color.g();
            sumB += color.b();
        }

        return new Color(sumR, sumG, sumB);
    }
}
