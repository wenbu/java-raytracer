package scene.medium;

import core.math.Direction3;

public interface PhaseFunction
{
    double p(Direction3 wo, Direction3 wi);
}
