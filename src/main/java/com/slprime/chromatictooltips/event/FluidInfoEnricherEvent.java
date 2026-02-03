package com.slprime.chromatictooltips.event;

import java.util.ArrayList;
import java.util.List;

import com.slprime.chromatictooltips.api.TooltipTarget;

public class FluidInfoEnricherEvent extends TooltipEvent {

    public List<Object> tooltip;

    public FluidInfoEnricherEvent(TooltipTarget target, List<?> tooltip) {
        super(target);
        this.tooltip = new ArrayList<>(tooltip);
    }

}
