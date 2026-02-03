package com.slprime.chromatictooltips.event;

import java.util.Map;

import com.slprime.chromatictooltips.api.TooltipTarget;

public class HotkeyEnricherEvent extends TooltipEvent {

    public Map<String, String> hotkeys;

    public HotkeyEnricherEvent(TooltipTarget target, Map<String, String> hotkeys) {
        super(target);
        this.hotkeys = hotkeys;
    }

}
