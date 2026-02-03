package com.slprime.chromatictooltips.event;

import java.util.List;

import com.slprime.chromatictooltips.api.EnchantmentData;
import com.slprime.chromatictooltips.api.TooltipTarget;

public class EnchantmentEnricherEvent extends TooltipEvent {

    public final List<EnchantmentData> enchantments;

    public EnchantmentEnricherEvent(TooltipTarget target, List<EnchantmentData> enchantments) {
        super(target);
        this.enchantments = enchantments;
    }

}
