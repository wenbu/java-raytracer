package film.impl;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import core.colors.Color;
import core.colors.Colors;
import film.Film;

public abstract class JFrameFilm implements Film
{
    protected final int imageX;
    protected final int imageY;
    
    protected final static int NUM_BANDS = 3;
    protected final int[] image;
    
    protected JFrameFilm(int imageX, int imageY)
    {
        this.imageX = imageX;
        this.imageY = imageY;

        image = new int[imageX * imageY * NUM_BANDS];
    }
    
    protected void addSample(int x, int y, int r, int g, int b)
    {
        int rowLength = imageX * NUM_BANDS;
        int rowOffset = (imageY - y - 1) * rowLength;
        int colOffset = x * NUM_BANDS;
        int pixelIndex = rowOffset + colOffset;
        try
        {
            image[pixelIndex + 0] = Math.min(255, r);
            image[pixelIndex + 1] = Math.min(255, g);
            image[pixelIndex + 2] = Math.min(255, b);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println(String.format("x=%d, y=%d\n", x, y));
            System.out.println(String.format("rowOffset=%d\n", rowOffset));
            System.out.println(String.format("colOffset=%d\n", colOffset));
            System.out.println(String.format("pixelIndex=%d\n", pixelIndex));
            System.out.println(String.format("image.length=%d\n", image.length));
            throw e;
        }
    }
    
    @Override
    public void imageComplete()
    {
        JFrame frame = new JFrame();
        JLabel label = new JLabel();

        ImageIcon imageIcon = new ImageIcon(getImageFromArray());
        label.setIcon(imageIcon);
        frame.add(label);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private Image getImageFromArray()
    {
        BufferedImage img = new BufferedImage(imageX,
                                              imageY,
                                              BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = (WritableRaster) img.getData();
        raster.setPixels(0, 0, imageX, imageY, image);
        img.setData(raster);
        return img;
    }
    
    protected class PixelSampleData
    {
        private final Set<Color> colorSamples;
        
        public PixelSampleData()
        {
            colorSamples = new HashSet<>();
        }
        
        public PixelSampleData(Color color)
        {
            this();
            addColorSample(color);
        }
        
        public void addColorSample(Color color)
        {
            colorSamples.add(color);
        }
        
        public Color getColor()
        {
            if (colorSamples.size() > 0)
            return Colors.sum(colorSamples).divide(colorSamples.size());
            else return Colors.BLACK;
        }
    }
}
