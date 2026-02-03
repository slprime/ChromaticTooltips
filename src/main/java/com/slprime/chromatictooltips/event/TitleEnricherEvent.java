package com.slprime.chromatictooltips.event;

import com.slprime.chromatictooltips.api.TooltipTarget;

public class TitleEnricherEvent extends TooltipEvent {

    public String displayName;

    public TitleEnricherEvent(TooltipTarget target, String displayName) {
        super(target);
        this.displayName = displayName;
    }

}
