package sample;

import core.math.Point2;

public class CameraSample
{
    private final Point2 pFilm;
    private final Point2 pLens;
    private final double time;
    
    public CameraSample(Point2 pFilm, Point2 pLens, double time)
    {
        this.pFilm = pFilm;
        this.pLens = pLens;
        this.time = time;
    }

    public Point2 getPFilm()
    {
        return pFilm;
    }

    public Point2 getPLens()
    {
        return pLens;
    }

    public double getTime()
    {
        return time;
    }
    
}
