package utilities;

import static core.math.MathUtilities.*;

import java.util.List;
import java.util.Random;

import core.math.Direction2;
import core.math.Direction3;
import core.math.Point2;

public class SamplingUtilities
{
    public static Direction3 cosineSampleHemisphere(Point2 u)
    {
        Point2 d = concentricSampleDisk(u);
        double z = Math.sqrt(Math.max(0, 1 - d.x() * d.x() - d.y() * d.y()));

        return new Direction3(d.x(), d.y(), z);
    }
    
    public static Direction3 uniformSampleHemisphere(Point2 u)
    {
        double z = u.get(0);
        double r = Math.sqrt(Math.max(0, 1 - z * z));
        double phi = 2 * Math.PI * u.get(1);
        return new Direction3(r * Math.cos(phi), r * Math.sin(phi), z);
    }
    
    public static double uniformHemispherePdf()
    {
        return INV_2PI;
    }

    public static Point2 concentricSampleDisk(Point2 u)
    {
        // map (0, 1] to [-1, 1]
        Point2 uOffset = u.times(2).minus(new Direction2(1, 1));

        // handle degeneracy at origin
        if (uOffset.x() == 0 && uOffset.y() == 0)
        {
            return new Point2(0, 0);
        }

        // apply concentric mapping to point
        double theta;
        double r;
        if (Math.abs(uOffset.x()) > Math.abs(uOffset.y()))
        {
            r = uOffset.x();
            theta = PI_OVER_4 * (uOffset.y() / uOffset.x());
        } else
        {
            r = uOffset.y();
            theta = PI_OVER_2 - PI_OVER_4 * (uOffset.x() / uOffset.y());
        }

        return new Point2(Math.cos(theta), Math.sin(theta)).times(r);
    }
    
    public static void stratifiedSample1D(List<Double> samples, Random random, boolean jitter)
    {
        double invNSamples = 1.0 / samples.size();
        for (int i = 0; i < samples.size(); i++)
        {
            double delta = jitter ? random.nextDouble() : 0.5;
            samples.set(i, (i + delta) * invNSamples);
        }
    }

    public static void stratifiedSample2D(List<Point2> samples, int nx, int ny, Random random,
            boolean jitter)
    {
        double dx = 1.0 / nx;
        double dy = 1.0 / ny;
        int i = 0;
        for (int y = 0; y < ny; y++)
        {
            for (int x = 0; x < nx; x++, i++)
            {
                double jx = jitter ? random.nextDouble() : 0.5;
                double jy = jitter ? random.nextDouble() : 0.5;
                samples.set(i, new Point2((x + jx) * dx, (y + jy) * dy));
            }
        }
    }

    public static void latinHypercube(List<Point2> samples, Random random)
    {
        // generate Latin hypercube sampling samples along diagonal
        double invNSamples = 1.0 / samples.size();
        for (int i = 0; i < samples.size(); i++)
        {
            double x = (i + random.nextDouble()) * invNSamples;
            double y = (i + random.nextDouble()) * invNSamples;
            samples.set(i, new Point2(x, y));
        }

        // permute Latin hypercube sampling samples in each dimension
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < samples.size(); j++)
            {
                int swap = j + random.nextInt(samples.size() - j);

                Point2 pj = samples.get(j);
                Point2 pswap = samples.get(swap);

                Point2 p1 = new Point2(pj.x(), pswap.y());
                Point2 p2 = new Point2(pswap.x(), pj.y());

                pj = (i % 2 == 0) ? p2 : p1;
                pswap = (i % 2 == 0) ? p1 : p2;

                samples.set(j, pj);
                samples.set(swap, pswap);
            }
        }
    }
}
