package scene.lights.impl;

import static utilities.GeometryUtilities.sphericalPhi;
import static utilities.GeometryUtilities.sphericalTheta;
import static utilities.MathUtilities.INV_2PI;
import static utilities.MathUtilities.INV_PI;

import java.util.EnumSet;
import java.util.logging.Logger;

import core.RayDifferential;
import core.colors.RGBSpectrum;
import core.distribution.Distribution2D;
import core.math.Direction3;
import core.math.Point2;
import core.math.Point3;
import core.math.Transformation;
import core.space.BoundingSphere;
import core.tuple.Quadruple;
import scene.Scene;
import scene.interactions.Interaction;
import scene.lights.Light;
import scene.medium.Medium;
import texture.mipmap.MipMap;
import texture.mipmap.MipMap.ImageWrap;
import utilities.ImageUtilities;
import utilities.MathUtilities;

public class InfiniteAreaLight extends Light
{
    private static final Logger logger = Logger.getLogger(InfiniteAreaLight.class.getName());
    
    private final MipMap<RGBSpectrum> radianceMap;
    private final Distribution2D distribution;
    private Point3 worldCenter;
    private double worldRadius;
    
    public InfiniteAreaLight(Transformation lightToWorld, RGBSpectrum radiance, int numSamples,
            String fileName)
    {
        super(EnumSet.of(LightType.INFINITE), lightToWorld, numSamples, new Medium.MediumInterface());
        RGBSpectrum[] texels;
        Point2 resolution;
        if (fileName != null && !fileName.isEmpty())
        {
            var image = ImageUtilities.getImageArray(fileName, false); // pass in gamma flag?
            texels = image.getFirst();
            resolution = image.getSecond();
            for (int i = 0; i < resolution.x() * resolution.y(); i++)
            {
                texels[i] = texels[i].times(radiance);
            }
        }
        else
        {
            resolution = new Point2(1, 1);
            texels = new RGBSpectrum[] { new RGBSpectrum(1, 1, 1).times(radiance) };
        }
        radianceMap = new MipMap<>(RGBSpectrum.class, resolution, texels, true, 1, ImageWrap.REPEAT);
        
        // compute scalar-valued image from environment map
        int width = (int) resolution.x();
        int height = (int) resolution.y();
        double filter = 1.0 / Math.max(width, height);
        double[] img = new double[width * height];
        for (int v = 0; v < height; v++)
        {
            double vp = (double) v / (double) height;
            double sinTheta = Math.sin(Math.PI * (v + 0.5) / height);
            for (int u = 0; u < width; u++)
            {
                double up = (double) u / (double) width;
                img[u + v * width] = radianceMap.lookup(new Point2(up, vp), filter).g();
                img[u + v * width] *= sinTheta;
            }
        }
        
        // compute sampling distributions for rows and columns of image
        distribution = new Distribution2D(img, width, height);
    }
    
    @Override
    public void preprocess(Scene scene)
    {
        BoundingSphere worldBoundingSphere = scene.worldBound().boundingSphere();
        worldCenter = worldBoundingSphere.getCenter();
        worldRadius = worldBoundingSphere.getRadius();
    }
    
    @Override
    public Quadruple<RGBSpectrum, Direction3, Double, VisibilityTester> sampleRadiance(
            Interaction ref, Point2 u)
    {
        // find (u, v) sample coordinates in infinite light texture
        var map = distribution.sampleContinuous(u);
        Point2 uv = map.getFirst();
        double mapPdf = map.getSecond();
        if (mapPdf == 0)
        {
            return new Quadruple<>(new RGBSpectrum(0), null, 0.0, null);
        }
        
        // convert infinite light sample point to direction
        double theta = uv.get(1) * Math.PI;
        double phi = uv.get(0) * 2 * Math.PI;
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double sinPhi = Math.sin(phi);
        double cosPhi = Math.cos(phi);
        Direction3 wi = lightToWorld.transform(new Direction3(sinTheta * cosPhi,
                                                              sinTheta * sinPhi,
                                                              cosTheta));

        // compute pdf for sampled infinite light direction
        double pdf = mapPdf / (2 * Math.PI * Math.PI * sinTheta);
        if (sinTheta == 0)
        {
            pdf = 0;
        }
        
        // return radiance value for infinite light direction
        VisibilityTester vis = new VisibilityTester(ref,
                                                    new Interaction(ref.getP()
                                                                       .plus(wi.times(2 *
                                                                                      worldRadius)),
                                                                    ref.getT(),
                                                                    mediumInterface));
        return new Quadruple<>(radianceMap.lookup(uv), wi, pdf, vis);
    }

    @Override
    public RGBSpectrum power()
    {
        return radianceMap.lookup(new Point2(0.5, 0.5)).times(Math.PI * worldRadius * worldRadius);
    }

    @Override
    public RGBSpectrum emittedRadiance(RayDifferential ray)
    {
        Direction3 w = worldToLight.transform(ray.getDirection()).normalize();
        Point2 st = new Point2(sphericalPhi(w) * MathUtilities.INV_2PI,
                               sphericalTheta(w) * MathUtilities.INV_PI);
        return radianceMap.lookup(st, 0);
    }

    @Override
    public double pdfRadiance(Interaction ref, Direction3 w)
    {
        Direction3 wi = worldToLight.transform(w);
        double theta = sphericalTheta(wi);
        double phi = sphericalPhi(wi);
        double sinTheta = Math.sin(theta);
        if (sinTheta == 0)
        {
            return 0;
        }
        return distribution.pdf(new Point2(phi * INV_2PI, theta * INV_PI)) /
               (2 * Math.PI * Math.PI * sinTheta);
    }
}
