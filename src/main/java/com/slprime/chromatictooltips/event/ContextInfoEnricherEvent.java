package com.slprime.chromatictooltips.event;

import java.util.ArrayList;
import java.util.List;

import com.slprime.chromatictooltips.api.TooltipContext;

public class ContextInfoEnricherEvent extends TooltipEvent {

    public List<Object> tooltip;

    public ContextInfoEnricherEvent(TooltipContext context, List<?> tooltip) {
        super(context);
        this.tooltip = new ArrayList<>(tooltip);
    }

}
