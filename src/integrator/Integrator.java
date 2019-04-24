package integrator;

import scene.Scene;

public interface Integrator
{
    void render(Scene scene);
    void shutdownNow();
}
