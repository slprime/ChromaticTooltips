package com.slprime.chromatictooltips.event;

import java.util.List;

import com.slprime.chromatictooltips.api.EnchantmentData;
import com.slprime.chromatictooltips.api.TooltipContext;

public class EnchantmentEnricherEvent extends TooltipEvent {

    public final List<EnchantmentData> enchantments;

    public EnchantmentEnricherEvent(TooltipContext context, List<EnchantmentData> enchantments) {
        super(context);
        this.enchantments = enchantments;
    }

}
