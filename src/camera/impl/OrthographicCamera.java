package camera.impl;

import camera.ProjectiveCamera;
import core.Ray;
import core.RayDifferential;
import core.math.Direction3;
import core.math.Point2;
import utilities.MathUtilities;
import core.math.Point3;
import core.math.Transformation;
import core.space.BoundingBox2;
import core.tuple.Pair;
import film.Film;
import camera.CameraSample;
import scene.medium.Medium;

import static utilities.SamplingUtilities.concentricSampleDisk;

public class OrthographicCamera extends ProjectiveCamera
{
    private final Direction3 dxCamera;
    private final Direction3 dyCamera;
    
    public OrthographicCamera(Transformation cameraToWorld, BoundingBox2 screenWindow,
            double shutterOpen, double shutterClose, double lensRadius, double focalDistance,
            Film film, Medium medium)
    {
        super(cameraToWorld,
              getOrthographicTransform(0, 1),
              screenWindow,
              shutterOpen,
              shutterClose,
              lensRadius,
              focalDistance,
              film,
              medium);
        
        dxCamera = rasterToCamera.transform(new Direction3(1, 0, 0));
        dyCamera = rasterToCamera.transform(new Direction3(0, 1, 0));
    }
    
    private static Transformation getOrthographicTransform(double zNear, double zFar)
    {
        return Transformation.getScale(1, 1, 1 / (zFar - zNear))
                             .compose(Transformation.getTranslation(0, 0, -zNear));
    }

    @Override
    public Pair<Ray, Double> generateRay(CameraSample sample)
    {
        Point3 pFilm = new Point3(sample.getPFilm().x(), sample.getPFilm().y(), 0);
        Point3 pCamera = rasterToCamera.transform(pFilm);
        
        Ray ray = new Ray(pCamera, new Direction3(0, 0, 1));

        // modify ray for depth of field
        if (lensRadius > 0)
        {
            applyDepthOfField(ray, sample);
        }
        
        ray.setTime(MathUtilities.lerp(sample.getTime(), shutterOpen, shutterClose));
        ray.setMedium(medium);
        
        ray = cameraToWorld.transform(ray);
        return new Pair<>(ray, 1.0);
    }

    @Override
    public Pair<RayDifferential, Double> generateRayDifferential(CameraSample sample)
    {
        Point3 pFilm = new Point3(sample.getPFilm().x(), sample.getPFilm().y(), 0);
        Point3 pCamera = rasterToCamera.transform(pFilm);
        
        RayDifferential ray = new RayDifferential(pCamera, new Direction3(0, 0, 1));

        // modify ray for depth of field
        if (lensRadius > 0)
        {
            applyDepthOfField(ray, sample);
        }

        if (lensRadius > 0)
        {
            // sample point on lens
            Point2 pLens = concentricSampleDisk(sample.getPLens()).times(lensRadius);
            double ft = focalDistance / ray.getDirection().z();

            Point3 pFocus = pCamera.plus(dxCamera).plus(new Direction3(0, 0, ft));
            ray.setRxOrigin(new Point3(pLens.x(), pLens.y(), 0));
            ray.setRxDirection(pFocus.minus(ray.getRxOrigin()).normalize());

            pFocus = pCamera.plus(dyCamera).plus(new Direction3(0, 0, ft));
            ray.setRyOrigin(new Point3(pLens.x(), pLens.y(), 0));
            ray.setRyDirection(pFocus.minus(ray.getRyOrigin()).normalize());
        }
        else
        {
            ray.setRxOrigin(ray.getOrigin().plus(dxCamera));
            ray.setRyOrigin(ray.getOrigin().plus(dyCamera));
            ray.setRxDirection(ray.getDirection());
            ray.setRyDirection(ray.getDirection());
        }
        ray.setTime(MathUtilities.lerp(sample.getTime(), shutterOpen, shutterClose));
        ray.setHasDifferentials(true);
        ray.setMedium(medium);
        
        ray = cameraToWorld.transform(ray);
        return new Pair<>(ray, 1.0);
    }
}
