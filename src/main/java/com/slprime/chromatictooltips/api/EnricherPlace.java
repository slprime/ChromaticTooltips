package com.slprime.chromatictooltips.api;

public enum EnricherPlace {

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
