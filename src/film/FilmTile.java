package film;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import core.colors.RGBSpectrum;
import core.math.Direction2;
import core.math.Point2;
import core.space.BoundingBox2;

public class FilmTile
{
    private final BoundingBox2 pixelBounds;
    private final Direction2 filterRadius;
    private final Direction2 invFilterRadius;
    private final double[] filter;
    // Note that filter is a linearized 2D array and so filter.length != filterTableWidth
    private final int filterTableWidth;
    private final List<FilmTilePixel> pixels;
    
    private final int myTileNum;
    private static AtomicInteger tileNum = new AtomicInteger();
    
    public FilmTile(BoundingBox2 pixelBounds, Direction2 filterRadius, double[] filter, int filterTableWidth)
    {
        this.pixelBounds = pixelBounds;
        this.filterRadius = filterRadius;
        this.invFilterRadius = new Direction2(1 / filterRadius.x(), 1 / filterRadius.y());
        this.filter = filter;
        this.filterTableWidth = filterTableWidth;
        
        myTileNum = tileNum.getAndIncrement();
        
        pixels = new ArrayList<>(pixelBounds.integerArea());
        for (int i = 0; i < pixelBounds.integerArea(); i++)
        {
            pixels.add(new FilmTilePixel());
        }
        
    }
    
    public int getTileNum()
    {
        return myTileNum;
    }
    
    public void addSample(Point2 pFilm, RGBSpectrum radiance, double sampleWeight)
    {
        Point2 pFilmDiscrete = pFilm.minus(new Direction2(0.5, 0.5));
        Point2 p0 = pFilmDiscrete.minus(filterRadius).ceil();
        Point2 p1 = pFilmDiscrete.plus(filterRadius).floor().plus(new Direction2(1, 1));
        p0 = Point2.max(p0, pixelBounds.get(0));
        p1 = Point2.min(p1, pixelBounds.get(1));
        
        // precompute x and y table offsets
        int ifxSize = (int) p1.x() - (int) p0.x();
        if (ifxSize < 0)
        {
            System.out.println("lmao");
            return;
        }
        int[] ifx = new int[(int) p1.x() - (int) p0.x()];
        for (int x = (int) p0.x(); x < (int) p1.x(); x++)
        {
            double fx = Math.abs((x - pFilmDiscrete.x()) * invFilterRadius.x() * filterTableWidth);
            ifx[x - (int)p0.x()] = Math.min((int)Math.floor(fx), filterTableWidth - 1);
        }
        
        int ifySize = (int) p1.y() - (int) p0.y();
        if (ifySize < 0)
        {
            System.out.println("lmao");
            return;
        }
        int[] ify = new int[(int) p1.y() - (int) p0.y()];
        for (int y = (int) p0.y(); y < (int) p1.y(); y++)
        {
            double fy = Math.abs((y - pFilmDiscrete.y()) * invFilterRadius.y() * filterTableWidth);
            ify[y - (int)p0.y()] = Math.min((int)Math.floor(fy), filterTableWidth - 1);
        }
        
        for (int y = (int) p0.y(); y < (int) p1.y(); y++)
        {
            for (int x = (int) p0.x(); x < (int) p1.x(); x++)
            {
                // evaluate filter value at pixel x, y
                int offset = ify[y - (int) p0.y()] * filterTableWidth + ifx[x - (int)p0.x()];
                double filterWeight = filter[offset];
                
                // update pixel values with filtered sample contribution
                FilmTilePixel pixel = getPixel(new Point2(x, y));
                pixel.addContribution(radiance.times(sampleWeight * filterWeight));
                pixel.filterWeightSum += filterWeight;
            }
        }
    }
    
    public FilmTilePixel getPixel(Point2 p)
    {
        int width = (int) pixelBounds.get(1).x() - (int) pixelBounds.get(0).x();
        int offset = ((int) p.x() - (int) pixelBounds.get(0).x()) +
                     ((int) p.y() - (int) pixelBounds.get(0).y()) * width;
        return pixels.get(offset);
    }
    
    public BoundingBox2 getPixelBounds()
    {
        return pixelBounds;
    }
    
    public static class FilmTilePixel
    {
        private RGBSpectrum contributionSum = new RGBSpectrum(0, 0, 0);
        private double filterWeightSum = 0;
        
        public void addContribution(RGBSpectrum color)
        {
            contributionSum = contributionSum.plus(color);
        }
        
        public RGBSpectrum getContribution()
        {
            return contributionSum;
        }
        
        public double getFilterWeight()
        {
            return filterWeightSum;
        }
    }
}
