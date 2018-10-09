package camera.impl;

import camera.ProjectiveCamera;
import core.Ray;
import core.RayDifferential;
import core.math.Direction3;
import utilities.MathUtilities;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.space.BoundingBox2;
import core.tuple.Pair;
import film.Film;
import camera.CameraSample;
import scene.medium.Medium;

public class PerspectiveCamera extends ProjectiveCamera
{
    private Direction3 dxCamera;
    private Direction3 dyCamera;
    private double lensRadius;
    private double area;
    
    public PerspectiveCamera(Transformation cameraToWorld, BoundingBox2 screenWindow,
            double shutterOpen, double shutterClose, double lensRadius, double focalDistance,
            double fov, Film film, Medium medium)
    {
        super(cameraToWorld,
              getPerspectiveTransform(fov, 1e-2, 1000),
              screenWindow,
              shutterOpen,
              shutterClose,
              lensRadius,
              focalDistance,
              film,
              medium);
        
        this.lensRadius = lensRadius;
        
        // compute differential changes in origin for perspective camera rays
        dxCamera = rasterToCamera.transform(new Point3(1, 0, 0))
                                 .minus(rasterToCamera.transform(new Point3(0, 0, 0)));
        dyCamera = rasterToCamera.transform(new Point3(0, 1, 0))
                                 .minus(rasterToCamera.transform(new Point3(0, 0, 0)));
        
        // compute image plane bounds at z=1
        Point2 resolution = film.getResolution();
        Point3 pMin = rasterToCamera.transform(new Point3(0, 0, 0));
        Point3 pMax = rasterToCamera.transform(new Point3(resolution.x(), resolution.y(), 0));
        pMin = pMin.divideBy(pMin.z());
        pMax = pMax.divideBy(pMax.z());
        area = Math.abs((pMax.x() - pMin.x()) * (pMax.y() - pMin.y()));
    }

    private static Transformation getPerspectiveTransform(double fov, double n, double f)
    {
        // perform projective divide for perspective projection
        Transformation perspectiveTransform =
                new Transformation(new double[][] { { 1, 0,           0,                0 },
                                                    { 0, 1,           0,                0 },
                                                    { 0, 0, f / (f - n), -f * n / (f - n) },
                                                    { 0, 0,           1,                0 } });
        
        
        // scale canonical perspective view to specified field of view
        double invTanAng = 1 / Math.tan(Math.toRadians(fov) / 2);
        
        perspectiveTransform = Transformation.getScale(invTanAng, invTanAng, 1).compose(perspectiveTransform);
        return perspectiveTransform;
    }

    @Override
    public Pair<Ray, Double> generateRay(CameraSample sample)
    {
        // compute raster and camera sample positions
        Point3 pFilm = new Point3(sample.getPFilm().x(), sample.getPFilm().y(), 0);
        Point3 pCamera = rasterToCamera.transform(pFilm);
        
        Ray ray = new Ray(new Point3(0, 0, 0),
                          Direction3.getNormalizedDirection(new Direction3(pCamera)));
        
        // TODO modify ray for depth of field
        
        ray.setTime(MathUtilities.lerp(sample.getTime(), shutterOpen, shutterClose));
        ray.setMedium(medium);
        ray = cameraToWorld.transform(ray);
        
        return new Pair<>(ray, 1.0);
    }

    @Override
    public Pair<RayDifferential, Double> generateRayDifferential(CameraSample sample)
    {
        // compute raster and camera sample positions
        Point3 pFilm = new Point3(sample.getPFilm().x(), sample.getPFilm().y(), 0);
        Point3 pCamera = rasterToCamera.transform(pFilm);
        
        RayDifferential ray = new RayDifferential(new Point3(0, 0, 0),
                                                  Direction3.getNormalizedDirection(new Direction3(pCamera)));
        
        // TODO handle depth of field
        
        // compute offset rays for ray differentials
        // TODO handle lenses
        if (lensRadius > 0)
        {
            // TODO
        }
        else
        {
            ray.setRxOrigin(ray.getOrigin());
            ray.setRyOrigin(ray.getOrigin());
            ray.setRxDirection(Direction3.getNormalizedDirection(new Direction3(pCamera).plus(dxCamera)));
            ray.setRyDirection(Direction3.getNormalizedDirection(new Direction3(pCamera).plus(dyCamera)));
        }
        ray.setTime(sample.getTime());
        ray.setMedium(medium);
        ray.setHasDifferentials(true);
        ray = cameraToWorld.transform(ray);
        
        return new Pair<>(ray, 1.0);
    }
}
