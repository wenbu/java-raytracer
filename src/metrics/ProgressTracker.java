package metrics;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that keeps track of rendering progress based on tiles completed.
 */
public class ProgressTracker
{
    private final int numTiles;
    private final AtomicInteger numCompletedTiles = new AtomicInteger(0);
    private final long startTime = System.currentTimeMillis();

    public ProgressTracker(int numTiles)
    {
        this.numTiles = numTiles;
    }

    public void onTileCompleted()
    {
        numCompletedTiles.incrementAndGet();
        updateProgressBar();
    }

    // should the progress bar functionality be separated from the tracking functionality?

    // number of characters in progress bar, minus leading and trailing brackets
    private static final int PROGRESS_BAR_WIDTH = 50;

    public void updateProgressBar()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\r[");

        int completedTiles = numCompletedTiles.get();
        float fractionCompletedTiles = ((float) completedTiles) / numTiles;
        int completedLength = (int) (fractionCompletedTiles * PROGRESS_BAR_WIDTH);
        int remainderLength = PROGRESS_BAR_WIDTH - completedLength;

        for (int i = 0; i < completedLength; i++)
        {
            sb.append('=');
        }
        for (int i = 0; i < remainderLength; i++)
        {
            sb.append(' ');
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        long estimatedRemainingTime;
        if (fractionCompletedTiles == 0.0f)
        {
            estimatedRemainingTime = -1;
        }
        else
        {
            estimatedRemainingTime = ((long) (((double) elapsedTime) / fractionCompletedTiles)) - elapsedTime;
        }
        sb.append(String.format("] %02d%% (%d/%d) %s elapsed, %s remaining", (int) (fractionCompletedTiles * 100),
                                completedTiles, numTiles, convertMilliseconds(elapsedTime),
                                convertMilliseconds(estimatedRemainingTime)));
        if (completedTiles == numTiles)
        {
            sb.append("\n");
        }
        System.out.print(sb.toString());
    }

    private String convertMilliseconds(long millis)
    {
        if (millis == -1)
        {
            return "???";
        }

        long hours = millis / (3_600_000);
        long remaining = millis - (hours * 3_600_000);
        long minutes = remaining / (60_000);
        remaining -= (minutes * 60_000);
        float seconds = ((float) remaining) / 1000;

        if (hours > 0)
        {
            return String.format("%02dh%02dm%.2fs", hours, minutes, seconds);
        }
        else if (minutes > 0)
        {
            return String.format("%02dm%.2fs", minutes, seconds);
        }
        else
        {
            return String.format("%.2fs", seconds);
        }
    }
}
