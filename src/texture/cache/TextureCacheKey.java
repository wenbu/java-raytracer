package texture.cache;

import texture.impl.MipMap;

import java.util.Objects;

public class TextureCacheKey
{
    private final String    fileName;
    private final boolean   doTrilinear;
    private final double    maxAnisotropy;
    private final MipMap.ImageWrap wrapMode;
    private final double    scale;
    private final boolean   gamma;

    public TextureCacheKey(String fileName, boolean doTrilinear, double maxAnisotropy, MipMap.ImageWrap wrapMode, double scale, boolean gamma)
    {
        this.fileName = fileName;
        this.doTrilinear = doTrilinear;
        this.maxAnisotropy = maxAnisotropy;
        this.wrapMode = wrapMode;
        this.scale = scale;
        this.gamma = gamma;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextureCacheKey that = (TextureCacheKey) o;
        return doTrilinear == that.doTrilinear &&
                Double.compare(that.maxAnisotropy, maxAnisotropy) == 0 &&
                Double.compare(that.scale, scale) == 0 &&
                gamma == that.gamma &&
                Objects.equals(fileName, that.fileName) &&
                wrapMode == that.wrapMode;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(fileName, doTrilinear, maxAnisotropy, wrapMode, scale, gamma);
    }
}
