package scene.materials;

import core.math.Direction2;
import core.math.Direction3;
import core.math.Normal3;
import scene.interactions.impl.SurfaceInteraction;
import texture.Texture;

public interface Material
{
    void computeScatteringFunctions(SurfaceInteraction si, TransportMode mode, boolean allowMultipleLobes);

    default void bump(Texture<Double> d, SurfaceInteraction si)
    {
        SurfaceInteraction siEval = new SurfaceInteraction(si);
        double             du     = 0.5 * (Math.abs(si.getDudx()) + Math.abs(si.getDudy()));

        /*
         * The most common reason for du to be zero is for a ray that starts from
         * a light source, where no differentials are available. In this case,
         * try to choose a small enough du that we still get a decently accurate
         * bump value.
         */
        if (du == 0)
        {
            du = 0.0005;
        }

        Direction3 dpdu = si.getShadingGeometry().getDpdu();
        Direction3 dpdv = si.getShadingGeometry().getDpdv();
        Normal3 n = si.getShadingGeometry().getN();
        Normal3 dndu = si.getDndu();
        Normal3 dndv = si.getDndv();

        siEval.setP(si.getP().plus(dpdu.times(du)));
        siEval.setUv(si.getUv().plus(new Direction2(du, 0)));
        siEval.setN(new Normal3(dpdu.cross(dpdv))
                            .plus(dndu.times(du))
                            .normalize());
        double uDisplace = d.evaluate(siEval);

        double dv = 0.5 * (Math.abs(si.getDvdx() + Math.abs(si.getDvdy())));
        if (dv == 0)
        {
            dv = 0.0005;
        }

        siEval.setP(si.getP().plus(dpdv.times(dv)));
        siEval.setUv(si.getUv().plus(new Direction2(0, dv)));
        siEval.setN(new Normal3(dpdu.cross(dpdv))
                            .plus(dndv.times(dv))
                            .normalize());
        double vDisplace = d.evaluate(siEval);
        double displace  = d.evaluate(si);

        dndu = si.getShadingGeometry().getDndu();
        dndv = si.getShadingGeometry().getDndv();

        dpdu = dpdu.plus(new Direction3(n).times((uDisplace - displace) / du))
                   .plus(new Direction3(dndu).times(displace));

        dpdv = dpdv.plus(new Direction3(n).times((vDisplace - displace) / dv))
                   .plus(new Direction3(dndv).times(displace));
        si.setShadingGeometry(dpdu, dpdv, dndu, dndv, false);
    }
}
