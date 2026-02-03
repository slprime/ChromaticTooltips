package com.slprime.chromatictooltips.event;

import com.slprime.chromatictooltips.api.TooltipTarget;

public class ModInfoEnricherEvent extends TooltipEvent {

    public String modName;
    public String modId;
    public String itemId;

    public ModInfoEnricherEvent(TooltipTarget target, String modName, String modId, String itemId) {
        super(target);
        this.modName = modName;
        this.modId = modId;
        this.itemId = itemId;
    }

}
