package com.slprime.chromatictooltips.event;

import com.slprime.chromatictooltips.api.TooltipTarget;

import cpw.mods.fml.common.eventhandler.Event;

public class TooltipEvent extends Event {

    public final TooltipTarget target;

    protected TooltipEvent(TooltipTarget target) {
        this.target = target;
    }

}
