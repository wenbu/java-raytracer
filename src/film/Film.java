package film;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import core.colors.RGBSpectrum;
import core.concurrency.AtomicDouble;
import core.math.Direction2;
import core.math.Point2;
import core.space.BoundingBox2;
import film.FilmTile.FilmTilePixel;
import film.filter.Filter;
import film.writer.PngImageWriter;
import utilities.MathUtilities;

public class Film
{
    private static final Logger logger = Logger.getLogger(Film.class.getName());

    private final Point2 resolution;
    private final double diagonal;
    private Filter filter;
    private final double scale;
    private BoundingBox2 croppedPixelBounds;
    private List<Pixel> pixels;
    
    private static final int FILTER_TABLE_WIDTH = 16;
    private double[] filterTable;

    private final ImageWriter imageWriter;
    
    public Film(Point2 resolution, BoundingBox2 cropWindow, Filter filter, double diagonal, String fileName,
            double scale)
    {
        this.resolution = resolution;
        this.filter = filter;
        this.diagonal = diagonal * 0.001;
        this.scale = scale;
        this.imageWriter = new PngImageWriter(fileName);
        
        croppedPixelBounds = new BoundingBox2(new Point2(Math.ceil(resolution.x() * cropWindow.get(0).x()),
                                                         Math.ceil(resolution.y() * cropWindow.get(0).y())),
                                              new Point2(Math.ceil(resolution.x() * cropWindow.get(1).x()),
                                                         Math.ceil(resolution.y() * cropWindow.get(1).y())));
        pixels = new ArrayList<>(croppedPixelBounds.integerArea());
        for (int i = 0; i < croppedPixelBounds.integerArea(); i++)
        {
            pixels.add(new Pixel());
        }
        
        filterTable = new double[FILTER_TABLE_WIDTH * FILTER_TABLE_WIDTH];
        int offset = 0;
        for (int y = 0; y < FILTER_TABLE_WIDTH; y++)
        {
            for (int x = 0; x < FILTER_TABLE_WIDTH; x++, offset++)
            {
                Point2 p = new Point2((x + 0.5) * filter.getRadius().x() / FILTER_TABLE_WIDTH,
                                      (y + 0.5) * filter.getRadius().y() / FILTER_TABLE_WIDTH);
                filterTable[offset] = filter.evaluate(p);
            }
        }
    }
    
    public BoundingBox2 getSampleBounds()
    {
        return new BoundingBox2(new Point2(croppedPixelBounds.get(0)).plus(new Direction2(0.5, 0.5)).minus(filter.getRadius()).floor(),
                                new Point2(croppedPixelBounds.get(1)).minus(new Direction2(0.5, 0.5)).plus(filter.getRadius()).ceil());
    }
    
    public BoundingBox2 getPhysicalExtent()
    {
        double aspect = resolution.y() / resolution.x();
        double x = Math.sqrt(diagonal * diagonal / (1 + aspect * aspect));
        double y = aspect * x;
        return new BoundingBox2(new Point2(-x / 2, -y / 2), new Point2(x / 2, y / 2));
    }
    
    public FilmTile getFilmTile(BoundingBox2 sampleBounds)
    {
        Direction2 halfPixel = new Direction2(0.5, 0.5);
        BoundingBox2 bounds = new BoundingBox2(sampleBounds);
        Point2 p0 = bounds.get(0).minus(halfPixel).minus(filter.getRadius()).ceil();
        Point2 p1 = bounds.get(1).minus(halfPixel).plus(filter.getRadius()).floor();
        BoundingBox2 tilePixelBounds = new BoundingBox2(p0, p1).intersect(croppedPixelBounds);
        return new FilmTile(tilePixelBounds, filter.getRadius(), filterTable, FILTER_TABLE_WIDTH);
    }
    
    public synchronized void mergeFilmTile(FilmTile filmTile)
    {
        for (Point2 pixel : filmTile.getPixelBounds())
        {
            FilmTilePixel tilePixel = filmTile.getPixel(pixel);
            Pixel mergePixel = getPixel(pixel);
            double[] xyz = tilePixel.getContribution().toXYZ();
            for (int i = 0 ; i < 3; i++)
            {
                mergePixel.xyz[i] += xyz[i];
            }
            mergePixel.filterWeightSum += tilePixel.getFilterWeight();
        }
    }
    
    public void setImage(RGBSpectrum[] img)
    {
        for (int i = 0; i < croppedPixelBounds.integerArea(); i++)
        {
            Pixel p = pixels.get(i);
            double[] xyz = img[i].toXYZ();
            p.filterWeightSum = 1;
            p.splatXYZ[0] = new AtomicDouble(0);
            p.splatXYZ[1] = new AtomicDouble(0);
            p.splatXYZ[2] = new AtomicDouble(0);
        }
    }
    
    public void addSplat(Point2 p, RGBSpectrum v)
    {
        if (!croppedPixelBounds.containsExclusive(p))
        {
            return;
        }
        double[] xyz = v.toXYZ();
        Pixel pixel = getPixel(p);
        for (int i = 0; i < 3; i++)
        {
            pixel.splatXYZ[i].add(xyz[i]);
        }
    }
    
    public void writeImage(double splatScale)
    {
        double[] imageRgb = new double[3 * croppedPixelBounds.integerArea()];
        int offset = 0;
        for (Point2 p : croppedPixelBounds)
        {
            Pixel pixel = getPixel(p);
            // convert XYZ color to RGB
            // TODO this is already RGB; do conversion when we are using Spectrum instead of Color
            double[] rgb = Arrays.copyOf(pixel.xyz, 3);
            
            // normalize pixel with weight sum
            double filterWeightSum = pixel.filterWeightSum;
            if (filterWeightSum != 0)
            {
                double invWeight = 1.0 / filterWeightSum;
                for (int i = 0; i < 3; i++)
                {
                    rgb[i] = Math.max(0, rgb[i] * invWeight);
                }
            }

            // TODO XYZ conversion to RGB
            for (int i = 0; i < 3; i++)
            {
                rgb[i] += splatScale * pixel.splatXYZ[i].getValue();
                imageRgb[3 * offset + i] = rgb[i] * scale;
            }
            offset += 1;
        }
        
        // TODO: move this functionality elsewhere
        // Scale samples from [0,1) to [0,256)
        for (int i = 0; i < imageRgb.length; i++)
        {
            double scaledSample = 255 * MathUtilities.gammaCorrect(imageRgb[i]);
            imageRgb[i] = MathUtilities.clamp(scaledSample, 0, 255);
        }

        try
        {
            imageWriter.writeImage((int) resolution.x(), (int) resolution.y(), imageRgb);
        }
        catch (IOException e)
        {
            logger.severe("IOException while writing image: " + e);
            throw new RuntimeException(e);
        }
    }
    
    public Point2 getResolution()
    {
        return resolution;
    }
    
    private Pixel getPixel(Point2 p)
    {
        int width = (int) croppedPixelBounds.get(1).x() - (int) croppedPixelBounds.get(0).x();
        int offset = ((int) p.x() - (int) croppedPixelBounds.get(0).x()) +
                     ((int) p.y() - (int) croppedPixelBounds.get(0).y()) * width;
        return pixels.get(offset);   
    }
    
    private static class Pixel
    {
        double[] xyz = new double[] {0, 0, 0};
        double filterWeightSum = 0;
        AtomicDouble[] splatXYZ = new AtomicDouble[] { new AtomicDouble(), new AtomicDouble(), new AtomicDouble() };
    }
}
