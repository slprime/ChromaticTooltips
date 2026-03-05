package com.slprime.chromatictooltips.event;

import java.util.List;

import net.minecraft.potion.PotionEffect;

import com.slprime.chromatictooltips.api.TooltipTarget;

public class FoodStatsEvent extends TooltipEvent {

    public int hunger;
    public float saturationModifier;
    public List<PotionEffect> effects;

    public FoodStatsEvent(TooltipTarget target, int hunger, float saturationModifier, List<PotionEffect> effects) {
        super(target);
        this.hunger = hunger;
        this.saturationModifier = saturationModifier;
        this.effects = effects;
    }

}
