package com.slprime.chromatictooltips.event;

import java.util.List;

import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;

public class ItemInfoEnricherEvent extends TooltipEvent {

    public final TooltipLines tooltip;

    public ItemInfoEnricherEvent(TooltipContext context, List<?> lines) {
        super(context);
        this.tooltip = new TooltipLines(lines);
    }

}
