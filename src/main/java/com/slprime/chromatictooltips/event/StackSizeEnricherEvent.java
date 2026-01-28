package com.slprime.chromatictooltips.event;

import com.slprime.chromatictooltips.api.TooltipContext;

public class StackSizeEnricherEvent extends TooltipEvent {

    public long stackAmount;

    public StackSizeEnricherEvent(TooltipContext context, long stackAmount) {
        super(context);
        this.stackAmount = stackAmount;
    }

}
