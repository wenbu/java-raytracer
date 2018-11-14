package film;

import java.io.IOException;

public interface ImageWriter
{
    void writeImage(int resolutionX, int resolutionY, double[] pixels) throws IOException;
}
