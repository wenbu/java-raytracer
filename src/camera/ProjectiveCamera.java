package camera;

import static core.math.Transformation.*;
import core.Ray;
import core.math.Transformation;
import core.space.BoundingBox2;
import core.tuple.Pair;
import film.Film;
import sample.CameraSample;
import scene.medium.Medium;

public abstract class ProjectiveCamera extends Camera
{
    protected Transformation cameraToScreen;
    protected Transformation rasterToCamera;
    protected double lensRadius;
    
    public ProjectiveCamera(Transformation cameraToWorld, Transformation cameraToScreen,
            BoundingBox2 screenWindow, double shutterOpen, double shutterClose, double lensr,
            double focald, Film film, Medium medium)
    {
        super(cameraToWorld, shutterOpen, shutterClose, film, medium);
        this.cameraToScreen = cameraToScreen;
        this.lensRadius = lensr;

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
}
