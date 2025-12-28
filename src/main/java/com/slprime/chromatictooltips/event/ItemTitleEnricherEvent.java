package com.slprime.chromatictooltips.event;

import com.slprime.chromatictooltips.api.TooltipContext;

public class ItemTitleEnricherEvent extends TooltipEvent {

    public String displayName;

    public ItemTitleEnricherEvent(TooltipContext context, String displayName) {
        super(context);
        this.displayName = displayName;
    }

}
