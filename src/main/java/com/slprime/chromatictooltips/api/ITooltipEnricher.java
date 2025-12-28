package com.slprime.chromatictooltips.api;

import java.util.EnumSet;
import java.util.List;

public interface ITooltipEnricher {

    enum EnricherPlace {

        HEADER,
        BODY,
        FOOTER;

        public static EnricherPlace fromString(String str) {

            for (EnricherPlace place : EnricherPlace.values()) {
                if (place.name()
                    .equalsIgnoreCase(str)) {
                    return place;
                }
            }

            return BODY;
        }
    }

    enum EnricherMode {

        NONE,
        ALWAYS,
        DEFAULT,
        SHIFT,
        CTRL,
        ALT;

        public static EnricherMode fromString(String str) {

            for (EnricherMode mode : EnricherMode.values()) {
                if (mode.name()
                    .equalsIgnoreCase(str)) {
                    return mode;
                }
            }

            return DEFAULT;
        }
    }

    public String sectionId();

    public EnricherPlace place();

    public EnumSet<EnricherMode> mode();

    public List<ITooltipComponent> build(TooltipContext context);

}
