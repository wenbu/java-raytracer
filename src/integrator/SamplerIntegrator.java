package integrator;

import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import camera.Camera;
import core.RayDifferential;
import core.colors.RGBSpectrum;
import core.math.Direction2;
import core.math.Direction3;
import core.math.Normal3;
import core.math.Point2;
import core.space.BoundingBox2;
import film.FilmTile;
import camera.CameraSample;
import metrics.MetricsLogger;
import sampler.Sampler;
import scene.Scene;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.functions.AbstractBidirectionalDistributionFunction.BxDFType;
import scene.materials.functions.BidirectionalScatteringDistributionFunction;

public abstract class SamplerIntegrator implements Integrator
{
    protected static final Logger logger = Logger.getLogger(SamplerIntegrator.class.getName());
    protected static final MetricsLogger metricsLogger = MetricsLogger.getInstance();

    private final Sampler sampler;
    private final Camera camera;

    private final ExecutorService executor;
    private final int numThreads;
    private static final int TILE_SIZE = 16;
    
    public SamplerIntegrator(Sampler sampler, Camera camera)
    {
        this.sampler = sampler;
        this.camera = camera;

        numThreads = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(numThreads);
    }

    public void preprocess(Scene scene, Sampler sampler)
    {
        //
    }

    @Override
    public void render(Scene scene)
    {
        long preprocessStart = System.currentTimeMillis();
        preprocess(scene, sampler);
        long preprocessEnd = System.currentTimeMillis();

        BoundingBox2 sampleBounds = camera.getFilm().getSampleBounds();
        Direction2 sampleExtent = sampleBounds.diagonal();
        Point2 nTiles = new Point2((int) (sampleExtent.x() + TILE_SIZE - 1) / TILE_SIZE,
                                   (int) (sampleExtent.y() + TILE_SIZE - 1) / TILE_SIZE);
        metricsLogger.onRenderStart(preprocessEnd - preprocessStart, numThreads, (int) (nTiles.x() * nTiles.y()));

        long renderStart = System.currentTimeMillis();
        for (int y = 0; y < nTiles.y(); y++)
        {
            for (int x = 0; x < nTiles.x(); x++)
            {
                Point2 tile = new Point2(x, y);
                executor.execute(new TileRenderTask(tile, scene, nTiles, sampleBounds));
            }
        }

        try
        {
            executor.shutdown();
            executor.awaitTermination(1000, TimeUnit.DAYS);
        }
        catch (InterruptedException e)
        {
            logger.log(Level.WARNING, "Executor was interrupted.", e);
        }
        long renderEnd = System.currentTimeMillis();
        metricsLogger.onRenderComplete(renderEnd - renderStart);

        long imageWriteStart = System.currentTimeMillis();
        camera.getFilm().writeImage(1);
        long imageWriteEnd = System.currentTimeMillis();
        metricsLogger.onOutputComplete(imageWriteEnd - imageWriteStart);

        metricsLogger.outputMetrics();
    }

    protected RGBSpectrum getRadiance(RayDifferential ray, Scene scene, Sampler sampler)
    {
        return getRadiance(ray, scene, sampler, 0);
    }

    protected abstract RGBSpectrum getRadiance(RayDifferential ray, Scene scene, Sampler sampler,
            int depth);

    protected RGBSpectrum specularReflect(RayDifferential ray,
            SurfaceInteraction surfaceInteraction, Scene scene, Sampler sampler, int depth)
    {
        Direction3 wo = surfaceInteraction.getWo();
        EnumSet<BxDFType> type = EnumSet.of(BxDFType.SPECULAR, BxDFType.REFLECTION);
        var sample = surfaceInteraction.getBsdf().sampleF(wo, sampler.get2D(), type);
        RGBSpectrum f = sample.getFirst();
        Direction3 wi = sample.getSecond();
        double pdf = sample.getThird();

        Normal3 ns = surfaceInteraction.getShadingGeometry().getN();

        // compute ray differential for specular reflection
        if (pdf > 0 && !f.isBlack() && wi.absDot(ns) != 0)
        {
            RayDifferential rd = new RayDifferential(surfaceInteraction.spawnRay(wi));
            if (ray.hasDifferentials())
            {
                rd.setHasDifferentials(true);
                rd.setRxOrigin(surfaceInteraction.getP().plus(surfaceInteraction.getDpdx()));
                rd.setRyOrigin(surfaceInteraction.getP().plus(surfaceInteraction.getDpdy()));

                // compute differential reflected directions
                Normal3 dndx = surfaceInteraction.getDndu()
                                                 .times(surfaceInteraction.getDudx())
                                                 .plus(surfaceInteraction.getDndv()
                                                                         .times(surfaceInteraction.getDvdx()));
                Normal3 dndy = surfaceInteraction.getDndu()
                                                 .times(surfaceInteraction.getDudy())
                                                 .plus(surfaceInteraction.getDndv()
                                                                         .times(surfaceInteraction.getDvdy()));
                Direction3 dwodx = ray.getRxDirection().times(-1).minus(wo);
                Direction3 dwody = ray.getRyDirection().times(-1).minus(wo);

                double dDNdx = dwodx.dot(ns) + wo.dot(dndx);
                double dDNdy = dwody.dot(ns) + wo.dot(dndy);

                rd.setRxDirection(wi.minus(dwodx)
                                    .plus(new Direction3(dndx.times(wo.dot(ns))
                                                             .plus(ns.times(dDNdx))).times(2)));
                rd.setRyDirection(wi.minus(dwody)
                                    .plus(new Direction3(dndy.times(wo.dot(ns))
                                                             .plus(ns.times(dDNdy))).times(2)));
            }
            return f.times(getRadiance(rd, scene, sampler, depth + 1))
                    .times(wi.absDot(ns))
                    .divideBy(pdf);
        }
        else
        {
            return new RGBSpectrum(0);
        }
    }
    
    protected RGBSpectrum specularTransmit(RayDifferential ray,
            SurfaceInteraction surfaceInteraction, Scene scene, Sampler sampler, int depth)
    {
        Direction3 wo = surfaceInteraction.getWo();
        EnumSet<BxDFType> type = EnumSet.of(BxDFType.SPECULAR, BxDFType.TRANSMISSION);
        var sample = surfaceInteraction.getBsdf().sampleF(wo, sampler.get2D(), type);
        RGBSpectrum f = sample.getFirst();
        Direction3 wi = sample.getSecond();
        double pdf = sample.getThird();
        
        Normal3 ns = surfaceInteraction.getShadingGeometry().getN();
        BidirectionalScatteringDistributionFunction bsdf = surfaceInteraction.getBsdf();
        
        if (pdf > 0 && !f.isBlack() && wi.absDot(ns) != 0)
        {
            RayDifferential rd = new RayDifferential(surfaceInteraction.spawnRay(wi));
            if (ray.hasDifferentials())
            {
                rd.setHasDifferentials(true);
                rd.setRxOrigin(surfaceInteraction.getP().plus(surfaceInteraction.getDpdx()));
                rd.setRyOrigin(surfaceInteraction.getP().plus(surfaceInteraction.getDpdy()));
                
                double eta = bsdf.getEta();
                Direction3 w = wo.times(-1);
                if (wo.dot(ns) < 0)
                {
                    eta = 1 / eta;
                }
                
                Normal3 dndx = surfaceInteraction.getDndu()
                        .times(surfaceInteraction.getDudx())
                        .plus(surfaceInteraction.getDndv()
                                                .times(surfaceInteraction.getDvdx()));
                Normal3 dndy = surfaceInteraction.getDndu()
                        .times(surfaceInteraction.getDudy())
                        .plus(surfaceInteraction.getDndv()
                                                .times(surfaceInteraction.getDvdy()));
                Direction3 dwodx = ray.getRxDirection().times(-1).minus(wo);
                Direction3 dwody = ray.getRyDirection().times(-1).minus(wo);

                double dDNdx = dwodx.dot(ns) + wo.dot(dndx);
                double dDNdy = dwody.dot(ns) + wo.dot(dndy);
                
                double mu = eta * w.dot(ns) - wi.dot(ns);
                
                double dmudx = (eta - (eta * eta * w.dot(ns)) / wi.dot(ns)) * dDNdx;
                double dmudy = (eta - (eta * eta * w.dot(ns)) / wi.dot(ns)) * dDNdy;

                rd.setRxDirection(wi.plus(dwodx.times(eta))
                                    .minus(new Direction3(dndx.times(mu).plus(ns.times(dmudx)))));
                rd.setRyDirection(wi.plus(dwody.times(eta))
                                    .minus(new Direction3(dndy.times(mu).plus(ns.times(dmudy)))));
            }
            
            return f.times(getRadiance(rd, scene, sampler, depth + 1)).times(wi.absDot(ns)).divideBy(pdf);
        }
        else
        {
            return new RGBSpectrum(0);
        }
    }

    private class TileRenderTask implements Runnable
    {
        private final Point2 tile;
        private final Scene scene;
        private final Point2 nTiles;
        private final BoundingBox2 sampleBounds;
        private final Sampler tileSampler;

        public TileRenderTask(Point2 tile, Scene scene, Point2 nTiles, BoundingBox2 sampleBounds)
        {
            this.tile = tile;
            this.scene = scene;
            this.nTiles = nTiles;
            this.sampleBounds = sampleBounds;
            
            int seed = (int) tile.y() * (int) nTiles.x() + (int) tile.x();
            tileSampler = sampler.getCopy(seed);
        }

        @Override
        public void run()
        {

            int x0 = (int) sampleBounds.get(0).x() + (int) tile.x() * TILE_SIZE;
            int x1 = Math.min(x0 + TILE_SIZE, (int) sampleBounds.get(1).x());
            int y0 = (int) sampleBounds.get(0).y() + (int) tile.y() * TILE_SIZE;
            int y1 = Math.min(y0 + TILE_SIZE, (int) sampleBounds.get(1).y());
            BoundingBox2 tileBounds = new BoundingBox2(new Point2(x0, y0), new Point2(x1, y1));

            FilmTile filmTile = camera.getFilm().getFilmTile(tileBounds);
            for (Point2 pixel : tileBounds)
            {
                tileSampler.startPixel(pixel);
                do
                {
                    CameraSample cameraSample = tileSampler.getCameraSample(pixel);
                    var r = camera.generateRayDifferential(cameraSample);
                    RayDifferential ray = r.getFirst();
                    double rayWeight = r.getSecond();
                    ray.scaleDifferentials(1 / Math.sqrt(tileSampler.getSamplesPerPixel()));

                    RGBSpectrum radiance = new RGBSpectrum(0, 0, 0);
                    if (rayWeight > 0)
                    {
                        radiance = getRadiance(ray, scene, tileSampler);
                    }

                    if (Double.isNaN(radiance.getSample(0)) ||
                        Double.isNaN(radiance.getSample(1)) || Double.isNaN(radiance.getSample(2)))
                    {
                        logger.warning(String.format("NaN radiance returned for pixel (%f, %f), sample %d. Setting to black.",
                                                     pixel.x(),
                                                     pixel.y(),
                                                     tileSampler.getCurrentSampleNumber()));
                        radiance = new RGBSpectrum(0);
                    } else if (radiance.getSample(1) < -1e-5)
                    {
                        logger.warning(String.format("Negative luminance value (%f) returned for pixel (%f, %f), sample %d. Setting to black.",
                                                     radiance.getSample(1),
                                                     pixel.x(),
                                                     pixel.y(),
                                                     tileSampler.getCurrentSampleNumber()));
                        radiance = new RGBSpectrum(0);
                    } else if (Double.isInfinite(radiance.getSample(1)))
                    {
                        logger.warning(String.format("Infinite luminance returned for pixel (%f, %f), sample %d. Setting to black.",
                                                     pixel.x(),
                                                     pixel.y(),
                                                     tileSampler.getCurrentSampleNumber()));
                        radiance = new RGBSpectrum(0);
                    }
                    filmTile.addSample(cameraSample.getPFilm(), radiance, rayWeight);
                } while (tileSampler.startNextSample());
            }
            camera.getFilm().mergeFilmTile(filmTile);
        }

    }

}
