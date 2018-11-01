package utilities;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import core.colors.RGBSpectrum;
import core.math.Point2;
import core.tuple.Pair;

public class ImageUtilities
{
    public static Pair<RGBSpectrum[], Point2> getImageArray(String filePath, boolean gamma)
    {
        File img = new File(filePath);
        BufferedImage bufferedImage;
        try
        {
            bufferedImage = ImageIO.read(img);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read image " + filePath);
        }
        
        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        RGBSpectrum[] ret;
        int bytesPerPixel;
        if (bufferedImage.getColorModel().hasAlpha())
        {
            bytesPerPixel = 4;
        }
        else
        {
            bytesPerPixel = 3;
        }
        ret = new RGBSpectrum[pixels.length / bytesPerPixel];
        for (int i = 0; i < ret.length; i++)
        {
            double r = remapToDouble(pixels[bytesPerPixel * i]);
            double g = remapToDouble(pixels[bytesPerPixel * i + 1]);
            double b = remapToDouble(pixels[bytesPerPixel * i + 2]);
            if (gamma)
            {
                r = inverseGammaCorrect(r);
                g = inverseGammaCorrect(g);
                b = inverseGammaCorrect(b);
            }
            ret[i] = new RGBSpectrum(r, g, b);
        }
        
        return new Pair<>(ret, new Point2(bufferedImage.getWidth(), bufferedImage.getHeight()));
    }
    
    public static Pair<Double[], Point2> getGrayscaleImageArray(String filePath, boolean gamma)
    {
        File img = new File(filePath);
        BufferedImage bufferedImage;
        try
        {
            bufferedImage = ImageIO.read(img);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to read image " + filePath);
        }
        
        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        Double[] ret;
        int bytesPerPixel;
        if (bufferedImage.getColorModel().hasAlpha())
        {
            bytesPerPixel = 4;
        }
        else
        {
            bytesPerPixel = 3;
        }
        ret = new Double[pixels.length / bytesPerPixel];
        for (int i = 0; i < ret.length; i++)
        {
            double r = remapToDouble(pixels[bytesPerPixel * i]);
            double g = remapToDouble(pixels[bytesPerPixel * i + 1]);
            double b = remapToDouble(pixels[bytesPerPixel * i + 2]);
            if (gamma)
            {
                r = inverseGammaCorrect(r);
                g = inverseGammaCorrect(g);
                b = inverseGammaCorrect(b);
            }
            // XXX pbrt just uses the g channel instead of taking the average
            ret[i] = (r + g + b) / 3;
        }
        
        return new Pair<>(ret, new Point2(bufferedImage.getWidth(), bufferedImage.getHeight()));
    }
    
    public static double gammaCorrect(double value)
    {
        if (value <= 0.0031308)
        {
            return 12.92 * value;
        }
        else
        {
            return 1.055 * Math.pow(value, (1.0 / 2.4)) - 0.055;
        }
    }
    
    public static double inverseGammaCorrect(double value)
    {
        if (value <= 0.04045)
        {
            return value * (1.0 / 12.92);
        }
        else
        {
            return Math.pow((value + 0.055) * 1.0 / 1.055, 2.4);
        }
    }
    
    // Map an unsigned byte (0-255) to a double (0.0-1.0)
    private static double remapToDouble(byte b)
    {
        return ((double) (b & 0xff)) / 255;
    }
}
