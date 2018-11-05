package utilities;

import java.util.function.Predicate;

public class CollectionUtilities
{
    /**
     * Return the highest index in the sorted array that satisfies the predicate.
     * Assumes the array is sorted in a manner such that all elements that satisfy
     * the predicate are before all those that do not.
     */
    public static int findInterval(double[] list, Predicate<Double> predicate)
    {
        int first = 0;
        int len = list.length;
        while (len > 0)
        {
            int half = len >> 1;
            int middle = first + half;
            if (predicate.test(list[middle]))
            {
                first = middle + 1;
                len -= half + 1;
            }
            else
            {
                len = half;
            }
        }
        return MathUtilities.clamp(first - 1, 0, list.length - 2);
    }
}
