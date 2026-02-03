package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.event.ContextInfoEnricherEvent;
import com.slprime.chromatictooltips.util.TooltipUtils;

public class ContextInfoEnricher implements ITooltipEnricher {

    @Override
    public String sectionId() {
        return "contextInfo";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.NONE);
    }

    @Override
    public TooltipLines build(TooltipContext context) {
        final List<ITooltipComponent> lines = new ArrayList<>(context.getContextTooltip());

        if (context.getItem() == null && context.getFluid() == null && !lines.isEmpty()) {
            lines.remove(0);
        }

        final ContextInfoEnricherEvent event = new ContextInfoEnricherEvent(context.getTarget(), lines);
        TooltipUtils.postEvent(event);

        return new TooltipLines(event.tooltip);
    }

}
