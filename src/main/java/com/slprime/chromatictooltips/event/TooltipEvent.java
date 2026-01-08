package com.slprime.chromatictooltips.event;

import com.slprime.chromatictooltips.api.TooltipContext;

import cpw.mods.fml.common.eventhandler.Event;

public class TooltipEvent extends Event {

    public final TooltipContext context;

    protected TooltipEvent(TooltipContext context) {
        this.context = context;
    }

}
