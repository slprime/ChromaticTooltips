package com.slprime.chromatictooltips.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.enchantment.Enchantment;

public class EnchantmentData {

    public final Enchantment enchantment;
    public final List<String> hint;
    public final int level;

    public EnchantmentData(Enchantment enchantment, int level, List<String> hint) {
        this.enchantment = enchantment;
        this.level = level;
        this.hint = hint;
    }

    public EnchantmentData(Enchantment enchantment, int level) {
        this(enchantment, level, new ArrayList<>());
    }

    public EnchantmentData(int effectId, int level) {
        this(Enchantment.enchantmentsList[effectId], level);
    }

}
