package com.slprime.chromatictooltips.event;

import com.slprime.chromatictooltips.api.TooltipContext;

public class TooltipEnricherEvent extends TooltipEvent {

    public TooltipEnricherEvent(TooltipContext context) {
        super(context);
    }

}
