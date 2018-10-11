package scene.materials.functions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import core.colors.RGBSpectrum;
import core.math.Direction3;
import utilities.MathUtilities;
import core.math.Normal3;
import core.math.Point2;
import core.math.Vector3;
import core.tuple.Quadruple;
import scene.interactions.impl.SurfaceInteraction;
import scene.materials.functions.AbstractBidirectionalDistributionFunction.BxDFType;

public class BidirectionalScatteringDistributionFunction
{
    private final double eta;
    private final Normal3 ns, ng;
    private final Direction3 ss, ts;
    private final List<AbstractBidirectionalDistributionFunction> bxdfs;

    public BidirectionalScatteringDistributionFunction(SurfaceInteraction si)
    {
        this(si, 1);
    }

    public BidirectionalScatteringDistributionFunction(SurfaceInteraction si, double eta)
    {
        this.eta = eta;
        this.ns = si.getShadingGeometry().getN();
        this.ng = si.getN();
        this.ss = si.getShadingGeometry().getDpdu().normalized();
        this.ts = new Direction3(ns).cross(ss);
        bxdfs = new ArrayList<>(8);
    }
    
    public double getEta()
    {
        return eta;
    }

    public RGBSpectrum f(Direction3 woW, Direction3 wiW)
    {
        return f(woW, wiW, EnumSet.allOf(BxDFType.class));
    }

    public RGBSpectrum f(Direction3 woW, Direction3 wiW, EnumSet<BxDFType> flags)
    {
        Direction3 wi = worldToLocal(wiW);
        Direction3 wo = worldToLocal(woW);
        boolean reflect = wiW.dot(ng) * woW.dot(ng) > 0;
        RGBSpectrum f = new RGBSpectrum(0);
        for (AbstractBidirectionalDistributionFunction bxdf : bxdfs)
        {
            if (bxdf.matchesTypes(flags) && (reflect && bxdf.hasType(BxDFType.REFLECTION) ||
                                         (!reflect && bxdf.hasType(BxDFType.TRANSMISSION))))
            {
                RGBSpectrum bxdfContribution = bxdf.f(wo, wi);
                f = f.plus(bxdfContribution);
            }
        }
        return f;
    }

    // return: sampled spectrum, wiWorld, pdf, sampled bxdfType
    public Quadruple<RGBSpectrum, Direction3, Double, EnumSet<BxDFType>> sampleF(Direction3 woWorld,
            Point2 u, EnumSet<BxDFType> flags)
    {
        // choose which bxdf to sample
        int numMatchingComponents = numComponents(flags);
        if (numMatchingComponents == 0)
        {
            return new Quadruple<>(new RGBSpectrum(0), null, 0.0, EnumSet.noneOf(BxDFType.class));
        }
        int comp = Math.min((int) Math.floor(u.get(0) * numMatchingComponents),
                            numMatchingComponents - 1);

        // get chosen bxdf
        int count = comp;
        AbstractBidirectionalDistributionFunction bxdfToSample = null;
        for (AbstractBidirectionalDistributionFunction bxdf_i : bxdfs)
        {
            if (bxdf_i.matchesTypes(flags) && count-- == 0)
            {
                bxdfToSample = bxdf_i;
                break;
            }
        }

        // remap sample to [0,1)^2
        Point2 uRemapped = new Point2(u.get(0) * numMatchingComponents - comp, u.get(1));

        // sample chosen bxdf
        Direction3 wo = worldToLocal(woWorld);
        EnumSet<BxDFType> sampledType = bxdfToSample.getType();
        var bxdfSample = bxdfToSample.sample_f(wo, uRemapped);
        RGBSpectrum f = bxdfSample.getFirst();
        double pdf = bxdfSample.getThird();
        if (pdf == 0)
        {
            return null;
        }
        Direction3 wi = bxdfSample.getSecond();
        Direction3 wiWorld = localToWorld(wi);

        // compute overall pdf
        if (!bxdfToSample.hasType(BxDFType.SPECULAR) && numMatchingComponents > 1)
        {
            for (AbstractBidirectionalDistributionFunction bxdf : bxdfs)
            {
                if (bxdf != bxdfToSample && bxdf.matchesTypes(flags))
                {
                    pdf += bxdf.pdf(wo, wi);
                }
            }
        }
        if (numMatchingComponents > 1)
        {
            pdf /= numMatchingComponents;
        }

        // compute BSDF value for sampled direction
        if (!bxdfToSample.hasType(BxDFType.SPECULAR) && numMatchingComponents > 1)
        {
            f = new RGBSpectrum();
            boolean reflect = wiWorld.dot(ng) * woWorld.dot(ng) > 0;
            for (AbstractBidirectionalDistributionFunction bxdf : bxdfs)
            {
                if (bxdf.matchesTypes(flags) && ((reflect && bxdf.hasType(BxDFType.REFLECTION)) ||
                                             (!reflect && bxdf.hasType(BxDFType.TRANSMISSION))))
                {
                    f = f.plus(bxdf.f(wo, wi));
                }
            }
        }
        return new Quadruple<>(f, wiWorld, pdf, sampledType);
    }
    
    public double pdf(Direction3 woWorld, Direction3 wiWorld, EnumSet<BxDFType> flags)
    {
        if (bxdfs.isEmpty())
        {
            return 0;
        }
        
        Direction3 wo = worldToLocal(woWorld);
        Direction3 wi = worldToLocal(wiWorld);
        if (wo.z() == 0)
        {
            return 0;
        }
        
        double pdf = 0;
        int numMatchingComponents = 0;
        for (AbstractBidirectionalDistributionFunction bxdf : bxdfs)
        {
            if (bxdf.matchesTypes(flags))
            {
                numMatchingComponents += 1;
                pdf += bxdf.pdf(wo, wi);
            }
        }
        
        double v = numMatchingComponents > 0 ? pdf / numMatchingComponents : 0;
        return v;
    }

    public RGBSpectrum rho(Point2[] samples1, Point2[] samples2, EnumSet<BxDFType> flags)
    {
        RGBSpectrum ret = new RGBSpectrum(0);
        for (AbstractBidirectionalDistributionFunction bxdf : bxdfs)
        {
            if (bxdf.matchesTypes(flags))
            {
                ret = ret.plus(bxdf.rho(samples1, samples2));
            }
        }
        return ret;
    }

    public RGBSpectrum rho(Direction3 wo, Point2[] samples, EnumSet<BxDFType> flags)
    {
        RGBSpectrum ret = new RGBSpectrum(0);
        for (AbstractBidirectionalDistributionFunction bxdf : bxdfs)
        {
            if (bxdf.matchesTypes(flags))
            {
                ret = ret.plus(bxdf.rho(wo, samples));
            }
        }
        return ret;
    }

    public void addBxdf(AbstractBidirectionalDistributionFunction bxdf)
    {
        bxdfs.add(bxdf);
    }

    // Return the number of bxdfs with the matching type.
    public int numComponents(EnumSet<BxDFType> types)
    {
        return (int) bxdfs.stream().filter(bxdf -> bxdf.matchesTypes(types)).count();
    }

    Direction3 worldToLocal(Direction3 v)
    {
        return new Direction3(v.dot(ss), v.dot(ts), v.dot(ns));
    }

    Direction3 localToWorld(Direction3 v)
    {
        return new Direction3(ss.x() * v.x() + ts.x() * v.y() + ns.x() * v.z(),
                              ss.y() * v.x() + ts.y() * v.y() + ns.y() * v.z(),
                              ss.z() * v.x() + ts.z() * v.y() + ns.z() * v.z());
    }

    /*
     * Conventions: - Incident light w_i and outgoing view w_o are both normalized
     * and outward facing. - Normal n always points towards outside of object. -
     * Local coordinate system used for shading may not be the one returned by
     * Shape.intersect() (e.g. bumpmapping)
     */
    public static double cosTheta(Vector3 w)
    {
        return w.z();
    }

    public static double cos2Theta(Vector3 w)
    {
        return w.z() * w.z();
    }

    public static double absCosTheta(Vector3 w)
    {
        return Math.abs(w.z());
    }

    public static double sin2Theta(Vector3 w)
    {
        return Math.max(0, 1 - cos2Theta(w));
    }

    public static double sinTheta(Vector3 w)
    {
        return Math.sqrt(sin2Theta(w));
    }

    public static double tanTheta(Vector3 w)
    {
        return sinTheta(w) / cosTheta(w);
    }

    public static double tan2Theta(Vector3 w)
    {
        return sin2Theta(w) / cos2Theta(w);
    }

    public static double cosPhi(Vector3 w)
    {
        double sinTheta = sinTheta(w);
        return (sinTheta == 0) ? 1 : MathUtilities.clamp(w.x() / sinTheta, -1, 1);
    }

    public static double sinPhi(Vector3 w)
    {
        double sinTheta = sinTheta(w);
        return (sinTheta == 0) ? 0 : MathUtilities.clamp(w.y() / sinTheta, -1, 1);
    }

    public static double cos2Phi(Vector3 w)
    {
        double cosPhi = cosPhi(w);
        return cosPhi * cosPhi;
    }

    public static double sin2Phi(Vector3 w)
    {
        double sinPhi = sinPhi(w);
        return sinPhi * sinPhi;
    }

    public static double cosDeltaPhi(Vector3 wa, Vector3 wb)
    {
        return MathUtilities.clamp((wa.x() * wb.x() + wa.y() * wb.y()) /
                                   Math.sqrt((wa.x() * wa.x() + wa.y() * wa.y()) *
                                             (wb.x() * wb.x() + wb.y() * wb.y())),
                                   -1,
                                   1);
    }

    public static Direction3 reflect(Direction3 wo, Normal3 n)
    {
        return wo.times(-1).plus(n.times(2 * wo.dot(n)));
    }

    public static Direction3 refract(Direction3 wi, Normal3 n, double eta)
    {
        double cosThetaI = n.dot(wi);
        double sin2ThetaI = Math.max(0, 1 - cosThetaI * cosThetaI);
        double sin2ThetaT = eta * eta * sin2ThetaI;
        if (sin2ThetaT >= 1)
        {
            return null;
        }
        double cosThetaT = Math.sqrt(1 - sin2ThetaT);
        Direction3 wt = wi.times(-eta).plus(new Direction3(n).times(eta * cosThetaI - cosThetaT));
        return wt;
    }

    public static boolean sameHemisphere(Vector3 w, Vector3 wp)
    {
        return w.z() * wp.z() > 0;
    }
}
