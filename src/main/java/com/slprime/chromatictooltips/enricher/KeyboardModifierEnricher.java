package com.slprime.chromatictooltips.enricher;

import java.util.EnumSet;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.component.KeyboardModifierComponent;
import com.slprime.chromatictooltips.config.EnricherConfig;

public class KeyboardModifierEnricher implements ITooltipEnricher {

    @Override
    public String sectionId() {
        return "keyboard-modifier";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.HEADER;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.NONE);
    }

    @Override
    public TooltipLines build(TooltipContext context) {

        if (!EnricherConfig.keyboardModifiersEnabled || context.getSupportedModifiers()
            .isEmpty()) {
            return null;
        }

        return new TooltipLines(
            new KeyboardModifierComponent(context.getSupportedModifiers(), context.getActiveModifier()));
    }

}
