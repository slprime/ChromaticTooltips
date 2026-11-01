package com.slprime.chromatictooltips.util.color;

public final class PaletteSmoother {

    private static final long RESET_AFTER_NS = 150_000_000L;
    private static final long SAME_FRAME_NS = 1_500_000L;
    private static final float TIME_CONSTANT = 0.22f;

    private long lastKey = 0L;
    private long lastTimeNs = 0L;
    private TooltipPalette last = null;

    public TooltipPalette smooth(long key, TooltipPalette target) {
        final long now = System.nanoTime();
        final long elapsed = now - lastTimeNs;

        if (last == null || key != lastKey || elapsed > RESET_AFTER_NS) {
            lastKey = key;
            lastTimeNs = now;
            last = target;
            return last;
        }

        if (elapsed < SAME_FRAME_NS) {
            return last;
        }

        lastTimeNs = now;

        final float alpha = 1.0f - (float) Math.exp(-(elapsed / 1_000_000_000.0f) / TIME_CONSTANT);
        last = TooltipPalette.lerp(last, target, Math.min(1.0f, Math.max(0.01f, alpha)));
        return last;
    }

}
