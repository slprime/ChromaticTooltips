package com.slprime.chromatictooltips.util.color;

/**
 * Base colors of a tooltip target as seen by color expressions: {@code --primary}, {@code --secondary} (hue in degrees,
 * saturation and value in 0..1), {@code --neutral} (0..1) and {@code --time} (seconds since the tooltip appeared).
 */
public final class TooltipPalette {

    public static final TooltipPalette DEFAULT = fromTones(0.60f, 0.06f, 0.78f, 0.685f, 0.06f, 0.663f);

    public final float primaryH, primaryS, primaryV;
    public final float secondaryH, secondaryS, secondaryV;
    public final float neutral;
    public final float time;

    private TooltipPalette(float primaryH, float primaryS, float primaryV, float secondaryH, float secondaryS,
        float secondaryV, float neutral, float time) {
        this.primaryH = primaryH;
        this.primaryS = primaryS;
        this.primaryV = primaryV;
        this.secondaryH = secondaryH;
        this.secondaryS = secondaryS;
        this.secondaryV = secondaryV;
        this.neutral = neutral;
        this.time = time;
    }

    static boolean isNeutral(float s1, float v1, float s2, float v2) {
        return s1 * v1 < 0.055f && s2 * v2 < 0.055f && s1 < 0.12f;
    }

    static TooltipPalette fromTones(float h1, float s1, float v1, float h2, float s2, float v2) {
        if (isNeutral(s1, v1, s2, v2)) {
            final float lum = Math.max(0.22f, Math.min(0.95f, v1));
            final float secondaryV = lum < 0.35f ? 0.42f : Math.max(0.55f, lum * 0.78f);
            return new TooltipPalette(216f, 0.12f, lum, 234f, 0.15f, secondaryV, 1f, 0f);
        }

        final float primaryS = Math.min(0.90f, Math.max(0.50f, (float) Math.pow(s1, 0.65)));
        final float primaryV = Math.max(0.84f, Math.min(1.0f, v1 * 1.18f));

        final float secondaryS;
        final float secondaryH;

        // A gray second tone must not be pushed to a saturated hue
        if (s2 * v2 < 0.055f || s2 < 0.10f) {
            secondaryS = Math.min(0.22f, Math.max(0.10f, s2));
            secondaryH = wrapHue(h1 + 0.04f);
        } else {
            secondaryS = Math.min(0.92f, Math.max(0.55f, (float) Math.pow(s2, 0.65)));
            float hueDiff = Math.abs(h1 - h2);
            if (hueDiff > 0.5f) hueDiff = 1.0f - hueDiff;
            secondaryH = hueDiff < 0.04f ? wrapHue(h1 + 0.085f) : h2;
        }

        return new TooltipPalette(
            h1 * 360f,
            primaryS,
            primaryV,
            secondaryH * 360f,
            secondaryS,
            Math.max(0.76f, primaryV * 0.85f),
            0f,
            0f);
    }

    public static TooltipPalette lerp(TooltipPalette a, TooltipPalette b, float t) {
        return new TooltipPalette(
            lerpHue(a.primaryH, b.primaryH, t),
            a.primaryS + (b.primaryS - a.primaryS) * t,
            a.primaryV + (b.primaryV - a.primaryV) * t,
            lerpHue(a.secondaryH, b.secondaryH, t),
            a.secondaryS + (b.secondaryS - a.secondaryS) * t,
            a.secondaryV + (b.secondaryV - a.secondaryV) * t,
            a.neutral + (b.neutral - a.neutral) * t,
            b.time);
    }

    public TooltipPalette withTime(float seconds) {
        return new TooltipPalette(primaryH, primaryS, primaryV, secondaryH, secondaryS, secondaryV, neutral, seconds);
    }

    private static float lerpHue(float from, float to, float t) {
        float diff = to - from;
        if (diff > 180f) diff -= 360f;
        if (diff < -180f) diff += 360f;
        final float hue = (from + diff * t) % 360f;
        return hue < 0f ? hue + 360f : hue;
    }

    private static float wrapHue(float h) {
        return h - (float) Math.floor(h);
    }

}
