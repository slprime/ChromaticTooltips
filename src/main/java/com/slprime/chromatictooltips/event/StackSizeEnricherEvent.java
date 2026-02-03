package com.slprime.chromatictooltips.event;

import com.slprime.chromatictooltips.api.TooltipTarget;

public class StackSizeEnricherEvent extends TooltipEvent {

    public long stackAmount;

    public StackSizeEnricherEvent(TooltipTarget target, long stackAmount) {
        super(target);
        this.stackAmount = stackAmount;
    }

}
