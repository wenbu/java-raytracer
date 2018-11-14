package film.writer;

import film.ImageWriter;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class JFrameImageWriter implements ImageWriter
{
    private final boolean exitOnClose;

    public JFrameImageWriter()
    {
        this(true);
    }

    public JFrameImageWriter(boolean exitOnClose)
    {
        this.exitOnClose = exitOnClose;
    }

    @Override
    public void writeImage(int resolutionX, int resolutionY, double[] pixels)
    {
        JFrame frame = new JFrame();
        JLabel     label = new JLabel();
        BufferedImage img = new BufferedImage(resolutionX,
                                              resolutionY,
                                              BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = (WritableRaster) img.getData();
        raster.setPixels(0, 0, resolutionX, resolutionY, pixels);
        img.setData(raster);
        ImageIcon icon = new ImageIcon(img);
        label.setIcon(icon);
        frame.add(label);
        frame.pack();
        frame.setVisible(true);
        if (exitOnClose)
        {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
    }
}
