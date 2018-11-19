package film.writer;

import film.ImageWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class PngImageWriter implements ImageWriter
{
    private static final Logger logger = Logger.getLogger(PngImageWriter.class.getName());
    private final String fileName;

    public PngImageWriter(String fileName)
    {
        this.fileName = fileName;
    }

    @Override
    public void writeImage(int resolutionX, int resolutionY, double[] pixels) throws IOException
    {
        File imageFile = new File(fileName);
        logger.info("Writing image to " + imageFile.getAbsolutePath());

        if (!imageFile.getParentFile().exists())
        {
            if (!imageFile.getParentFile().mkdirs())
            {
                logger.severe("Failed to create output directory " + imageFile.getParentFile().getAbsolutePath());
            }
        }

        BufferedImage img = new BufferedImage(resolutionX, resolutionY, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = (WritableRaster) img.getData();
        raster.setPixels(0, 0, resolutionX, resolutionY, pixels);
        img.setData(raster);
        ImageIO.write(img, "PNG", imageFile);
    }
}
