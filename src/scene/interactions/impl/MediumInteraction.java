package scene.interactions.impl;

import core.math.Direction3;
import core.math.Point3;
import scene.interactions.Interaction;
import scene.medium.Medium;
import scene.medium.Medium.MediumInterface;
import scene.medium.PhaseFunction;

public class MediumInteraction extends Interaction
{
    private final PhaseFunction phase;
    
    public MediumInteraction(Point3 p, Direction3 wo, double time, Medium medium, PhaseFunction phase)
    {
        // XXX ????????????
        super(p, wo, time, new MediumInterface(medium));
        this.phase = phase;
    }
}
