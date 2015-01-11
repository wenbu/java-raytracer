package scene.lights;

import java.util.Set;

import core.Intersection;
import core.Ray;
import core.colors.Color;
import core.colors.Colors;

public abstract class Light
{
    protected final Color ambientColor;
    protected final Color diffuseColor;
    protected final Color specularColor;

    protected Light(Color color)
    {
        ambientColor = Colors.BLACK;
        diffuseColor = color;
        specularColor = color;
    }

    protected Light(Color ambientColor, Color diffuseColor, Color specularColor)
    {
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
    }

    /**
     * @param intersection
     * @return a Ray from the intersection point towards the light source. This
     *         Ray is in worldspace.
     */
    public abstract Set<Ray> getLightRay(Intersection intersection);

    public Color getAmbientColor()
    {
        return ambientColor;
    }

    public Color getDiffuseColor()
    {
        return diffuseColor;
    }

    public Color getSpecularColor()
    {
        return specularColor;
    }

}
