package camera;

import static core.math.Transformation.*;
import static utilities.SamplingUtilities.concentricSampleDisk;

import core.Ray;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.space.BoundingBox2;
import core.tuple.Pair;
import film.Film;
import scene.medium.Medium;

public abstract class ProjectiveCamera extends Camera
{
    protected Transformation cameraToScreen;
    protected Transformation rasterToCamera;
    protected double lensRadius;
    protected double focalDistance;
    
    public ProjectiveCamera(Transformation cameraToWorld, Transformation cameraToScreen,
            BoundingBox2 screenWindow, double shutterOpen, double shutterClose, double lensr,
            double focald, Film film, Medium medium)
    {
        super(cameraToWorld, shutterOpen, shutterClose, film, medium);
        this.cameraToScreen = cameraToScreen;
        this.lensRadius = lensr;
        this.focalDistance = focald;

        Transformation screenToRaster =
                getScale(film.getResolution().x(),
                         film.getResolution().y(),
                         1)
                .compose(getScale(1 / (screenWindow.get(1).x() - screenWindow.get(0).x()),
                                  1 / (screenWindow.get(0).y() - screenWindow.get(1).y()),
                                  1))
                .compose(getTranslation(-screenWindow.get(0).x(),
                                        -screenWindow.get(1).y(),
                                        0));
        
        Transformation rasterToScreen = screenToRaster.inverse();
        rasterToCamera = cameraToScreen.inverse().compose(rasterToScreen);
    }

    @Override
    public abstract Pair<Ray, Double> generateRay(CameraSample sample);

    /**
     * Modify input ray for depth of field.
     */
    protected void applyDepthOfField(Ray ray, CameraSample sample)
    {
        // sample point on lens
        Point2 pLens = concentricSampleDisk(sample.getPLens()).times(lensRadius);

        // compute point on plane of focus
        double ft     = focalDistance / ray.getDirection().z();
        Point3 pFocus = ray.pointAt(ft);

        // update ray for effect of lens
        ray.setOrigin(new Point3(pLens.x(), pLens.y(), 0));
        ray.setDirection(pFocus.minus(ray.getOrigin()).normalize());
    }
}
