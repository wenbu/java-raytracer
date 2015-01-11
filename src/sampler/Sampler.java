package sampler;

import java.util.Set;

import core.Pixel;
import core.Sample;

public interface Sampler
{
    public Set<Sample> getSamples(Pixel pixel);
}
