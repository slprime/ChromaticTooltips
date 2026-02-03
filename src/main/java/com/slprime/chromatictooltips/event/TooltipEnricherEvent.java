package com.slprime.chromatictooltips.event;

import com.slprime.chromatictooltips.api.TooltipContext;

import cpw.mods.fml.common.eventhandler.Event;

public class TooltipEnricherEvent extends Event {

    public final TooltipContext context;

    public TooltipEnricherEvent(TooltipContext context) {
        this.context = context;
    }

}
