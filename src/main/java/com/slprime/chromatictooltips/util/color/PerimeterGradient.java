package com.slprime.chromatictooltips.util.color;

/** Evenly spaced color stops on a closed loop, the last stop blends back into the first. */
public final class PerimeterGradient {

    private final int[] stops;

    public PerimeterGradient(int[] stops) {
        this.stops = stops;
    }

    /** @param fraction position on the loop, 0..1 */
    public int colorAt(double fraction) {
        final double position = fraction * this.stops.length;
        final int index = (int) Math.floor(position);
        final double blend = position - index;

        return lerp(this.stops[index % this.stops.length], this.stops[(index + 1) % this.stops.length], blend);
    }

    public static int lerp(int from, int to, double t) {
        int result = 0;

        for (int shift = 24; shift >= 0; shift -= 8) {
            final int a = (from >>> shift) & 0xFF;
            final int b = (to >>> shift) & 0xFF;
            result |= (int) Math.round(a + (b - a) * t) << shift;
        }

        return result;
    }

}
