package camera;

import core.Ray;
import core.RayDifferential;
import core.Sample;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.tuple.Pair;
import film.Film;
import sample.CameraSample;
import scene.medium.Medium;

public abstract class Camera
{
    protected final Transformation cameraToWorld;
    protected final double shutterOpen;
    protected final double shutterClose;
    protected final Film film;
    protected final Medium medium;

    public Camera(Transformation cameraToWorld, double shutterOpen, double shutterClose, Film film, Medium medium)
    {
        this.cameraToWorld = cameraToWorld;
        this.shutterOpen = shutterOpen;
        this.shutterClose = shutterClose;
        this.film = film;
        this.medium = medium;
    }

    // returns <camera ray, ray weight>
    public abstract Pair<Ray, Double> generateRay(CameraSample sample);

    // returns <camera RayDifferential, ray weight>
    public Pair<RayDifferential, Double> generateRayDifferential(CameraSample sample)
    {
        var r = generateRay(sample);
        RayDifferential rayDifferential = new RayDifferential(r.getFirst());
        Point2 pFilm = sample.getPFilm();

        CameraSample shiftedSampleX = new CameraSample(new Point2(pFilm.x() + 1, pFilm.y()),
                                                       sample.getPLens(),
                                                       sample.getTime());
        var x = generateRay(shiftedSampleX);
        if (x.getSecond() == 0)
        {
            return new Pair<>(null, 0.0);
        }
        Ray rx = x.getFirst();
        rayDifferential.setRxOrigin(rx.getOrigin());
        rayDifferential.setRxDirection(rx.getDirection());

        CameraSample shiftedSampleY = new CameraSample(new Point2(pFilm.x(), pFilm.y() + 1),
                                                       sample.getPLens(),
                                                       sample.getTime());
        var y = generateRay(shiftedSampleY);
        if (y.getSecond() == 0)
        {
            return new Pair<>(null, 0.0);
        }
        Ray ry = y.getFirst();
        rayDifferential.setRyOrigin(ry.getOrigin());
        rayDifferential.setRyDirection(ry.getDirection());
        
        rayDifferential.setHasDifferentials(true);
        return new Pair<>(rayDifferential, r.getSecond());
    }

    public Film getFilm()
    {
        return film;
    }
}
