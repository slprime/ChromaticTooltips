package com.slprime.chromatictooltips.event;

import java.util.List;

import com.slprime.chromatictooltips.api.AttributeModifierData;
import com.slprime.chromatictooltips.api.TooltipContext;

public class AttributeEnricherEvent extends TooltipEvent {

    public final List<AttributeModifierData> attributeModifiers;

    public AttributeEnricherEvent(TooltipContext context, List<AttributeModifierData> attributeModifiers) {
        super(context);
        this.attributeModifiers = attributeModifiers;
    }

}
