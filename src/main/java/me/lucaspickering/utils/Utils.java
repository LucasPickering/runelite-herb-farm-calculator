package me.lucaspickering.utils;

public class Utils {
    /**
     * Map a value from one known range to another. E.g. mapping 5 from [0,10]
     * to [10,20] yields 15, and 60 from [50,100] to [0,10] yields 2.
     *
     * @param value  The value to map
     * @param inMin  The minimum value of the input range
     * @param inMax  The maximum value of the input range
     * @param outMin The minimum value of the output range
     * @param outMax The maximum value of the output range
     * @return Mapped value
     */
    public static double mapToRange(double value, double inMin, double inMax, double outMin, double outMax) {
        if (inMin >= inMax) {
            throw new IllegalArgumentException(
                    String.format("Input min must be less than max, but got: [%f, %f]", inMin, inMax));
        }
        if (outMin >= outMax) {
            throw new IllegalArgumentException(
                    String.format("Output min must be less than max, but got: [%f, %f]", outMin, outMax));
        }

        // Shift down to [0,inSpan], then scale down to [0,1]
        double normalized = (value - inMin) / (inMax - inMin);
        // Scale up to [0,outSpan], then shift up to [outMin,outMax]
        return (normalized * (outMax - outMin)) + outMin;
    }
}
