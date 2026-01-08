package com.slprime.chromatictooltips.api;

import java.util.EnumSet;

public interface ITooltipEnricher {

    public String sectionId();

    public EnricherPlace place();

    public EnumSet<TooltipModifier> mode();

    public TooltipLines build(TooltipContext context);

}
