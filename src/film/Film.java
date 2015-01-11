package film;

import core.colors.Color;

public interface Film
{
    public void registerSample(int x, int y, Color sample);
    public void imageComplete();
}
