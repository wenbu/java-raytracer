package utilities;

import core.colors.RGBSpectrum;
import scene.materials.impl.GlassMaterial;
import scene.materials.impl.MatteMaterial;
import scene.materials.impl.MirrorMaterial;
import scene.materials.impl.PlasticMaterial;
import texture.impl.ConstantTexture;

public class MaterialUtilities
{
    public static MatteMaterial getMatteMaterial(RGBSpectrum constantColor,
            double constantRoughness)
    {
        return new MatteMaterial(new ConstantTexture<>(constantColor),
                                 new ConstantTexture<>(constantRoughness),
                                 null);
    }

    public static PlasticMaterial getPlasticMaterial(RGBSpectrum constantDiffuseColor,
            RGBSpectrum constantSpecularColor, double constantRoughness, boolean remapRoughness)
    {
        return new PlasticMaterial(new ConstantTexture<>(constantDiffuseColor),
                                   new ConstantTexture<>(constantSpecularColor),
                                   new ConstantTexture<>(constantRoughness),
                                   null,
                                   remapRoughness);
    }

    public static MirrorMaterial getMirrorMaterial(RGBSpectrum constantReflectionColor)
    {
        return new MirrorMaterial(new ConstantTexture<>(constantReflectionColor), null);
    }
    
    public static GlassMaterial getGlassMaterial(RGBSpectrum constantReflectionColor,
            RGBSpectrum constantTransmissionColor, double constantRoughness,
            double constantRefractionIndex)
    {
        return new GlassMaterial(new ConstantTexture<>(constantReflectionColor),
                                 new ConstantTexture<>(constantTransmissionColor),
                                 new ConstantTexture<>(constantRoughness),
                                 new ConstantTexture<>(constantRoughness),
                                 new ConstantTexture<>(constantRefractionIndex),
                                 null,
                                 false);
    }
}
