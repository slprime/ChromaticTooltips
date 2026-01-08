package com.slprime.chromatictooltips.api;

public enum TooltipModifier {

    NONE,
    SHIFT,
    CTRL,
    ALT;

    public static TooltipModifier fromString(String str) {

        for (TooltipModifier mode : TooltipModifier.values()) {
            if (mode.name()
                .equalsIgnoreCase(str)) {
                return mode;
            }
        }

        return NONE;
    }
}
