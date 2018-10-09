package film.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import metrics.MetricsAware;
import core.colors.RGBSpectrum;
/*
public class ToneMappingFilm extends JFrameFilm implements MetricsAware
{
    private static final Logger logger = Logger.getLogger(ToneMappingFilm.class.getName());
    
    private final PixelSampleData[][] colorSamples;

    private final List<Double> brightnessSamples;

    private final double BRIGHTNESS_CUTOFF = 0.75;
    
    private long timeSpentProcessing = 0L;

    public ToneMappingFilm(int imageX, int imageY)
    {
        super(imageX, imageY);
        colorSamples = new PixelSampleData[imageX][imageY];
        brightnessSamples = Collections.synchronizedList(new ArrayList<>(imageX * imageY));
    }

    @Override
    public void registerSample(int x, int y, Color sample)
    {
        double brightestColorChannelBrightness = Math.max(sample.r(),
                                                          Math.max(sample.g(),
                                                                   sample.b()));
        if (brightestColorChannelBrightness > 0)
        {
            brightnessSamples.add(brightestColorChannelBrightness);
        }

        // save sample for later
        if (colorSamples[x][y] == null)
            colorSamples[x][y] = new PixelSampleData(sample);
        else
            colorSamples[x][y].addColorSample(sample);
    }

    @Override
    public void imageComplete()
    {
        long start = System.currentTimeMillis();
        
        // scale colors
        double scaleFactor = 1;
        if (brightnessSamples.size() > 0)
        {
            Collections.sort(brightnessSamples);
            int sampleIndex = Math.min(brightnessSamples.size() - 1,
                                       (int) ( brightnessSamples.size() * BRIGHTNESS_CUTOFF ));
            double brightnessCutoff = brightnessSamples.get(sampleIndex);
            scaleFactor = 1.0 / brightnessCutoff;
        }
        for (int x = 0; x < imageX; x++)
        {
            for (int y = 0; y < imageY; y++)
            {
                Color color = colorSamples[x][y].getColor();

                int scaledR = (int) ( ( color.r() * scaleFactor ) * 255 );
                int scaledG = (int) ( ( color.g() * scaleFactor ) * 255 );
                int scaledB = (int) ( ( color.b() * scaleFactor ) * 255 );

                addSample(x, y, scaledR, scaledG, scaledB);
            }
        }

        super.imageComplete();
        timeSpentProcessing = System.currentTimeMillis() - start;
    }

    @Override
    public void logMetrics()
    {
        logger.log(Level.INFO, "Spent " + timeSpentProcessing + " ms processing final image.");
    }
}
*/