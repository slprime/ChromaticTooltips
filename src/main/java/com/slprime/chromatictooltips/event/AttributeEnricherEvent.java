package com.slprime.chromatictooltips.event;

import java.util.List;

import com.slprime.chromatictooltips.api.ItemStats;
import com.slprime.chromatictooltips.api.TooltipTarget;

public class AttributeEnricherEvent extends TooltipEvent {

    public final List<ItemStats> stats;

    public AttributeEnricherEvent(TooltipTarget target, List<ItemStats> stats) {
        super(target);
        this.stats = stats;
    }

}
