package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.StatCollector;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.ItemStats;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.component.InlineComponent;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.event.AttributeEnricherEvent;
import com.slprime.chromatictooltips.util.TooltipUtils;

public class ItemStatsEnricher implements ITooltipEnricher {

    protected static final int PADDING = 4;
    protected static final String SECTION_ID = "stats";
    protected boolean showOnlyIcons = false;

    public ItemStatsEnricher(boolean showOnlyIcons) {
        this.showOnlyIcons = showOnlyIcons;
    }

    @Override
    public String sectionId() {
        return SECTION_ID + (this.showOnlyIcons ? ":icons" : "");
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return this.showOnlyIcons ? EnumSet.of(TooltipModifier.NONE)
            : EnumSet.of(TooltipModifier.NONE, TooltipModifier.SHIFT);
    }

    protected boolean shownIcons(TooltipContext context) {
        return EnricherConfig.attributeModifierIconsEnabled && context.getRenderer()
            .getEnricherModes(SECTION_ID + ":icons", EnumSet.of(TooltipModifier.NONE))
            .contains(context.getActiveModifier());
    }

    @Override
    public TooltipLines build(TooltipContext context) {
        final ItemStack stack = context.getItem();

        if (stack == null || this.showOnlyIcons && !EnricherConfig.attributeModifierIconsEnabled) {
            return null;
        }

        final boolean shownIcons = !this.showOnlyIcons && shownIcons(context);
        final List<ItemStats> attributeModifiers = getAttributeModifiers(context);
        final List<ITooltipComponent> attributeModifiersList = new ArrayList<>();

        for (final ItemStats attributeData : attributeModifiers) {
            if (this.showOnlyIcons && attributeData.hasIcon()) {
                attributeModifiersList.add(attributeData.getIconComponent());
            } else if (!this.showOnlyIcons && (!shownIcons || !attributeData.hasIcon())) {
                attributeModifiersList.add(attributeData.getTextComponent());
            }
        }

        if (this.showOnlyIcons && !attributeModifiersList.isEmpty()) {
            return new TooltipLines(new InlineComponent(Collections.singletonList(attributeModifiersList), PADDING, 0));
        } else {
            return new TooltipLines(attributeModifiersList);
        }

    }

    public static List<ItemStats> getAttributeModifiers(TooltipContext context) {
        final ItemStack stack = context.getItem();
        final List<ItemStats> stats = new ArrayList<>();

        for (Map.Entry<String, AttributeModifier> entry : stack.getAttributeModifiers()
            .entries()) {

            if ("generic.attackDamage".equals(entry.getKey())) {
                stats.add(new ItemStats.AttackDamageStats(ItemStats.getModifiedAmount(entry.getValue(), stack)));
            } else if ("generic.maxHealth".equals(entry.getKey())) {
                stats.add(new ItemStats.MaxHealthStats(ItemStats.getModifiedAmount(entry.getValue(), stack)));
            } else if ("generic.knockbackResistance".equals(entry.getKey())) {
                stats.add(new ItemStats.KnockbackResistanceStats(ItemStats.getModifiedAmount(entry.getValue(), stack)));
            } else if ("generic.movementSpeed".equals(entry.getKey())) {
                stats.add(new ItemStats.MovementSpeedStats(ItemStats.getModifiedAmount(entry.getValue(), stack)));
            } else {
                final String attributeName = StatCollector.translateToLocal("attribute.name." + entry.getKey());
                final double modifiedAmount = ItemStats.getModifiedAmount(entry.getValue(), stack);

                stats.add(
                    new ItemStats(
                        attributeName,
                        modifiedAmount,
                        ItemStats.StatsOperator.fromOperation(
                            entry.getValue()
                                .getOperation()),
                        null));
            }
        }

        if (stack.getItem() instanceof ItemArmor armor) {
            stats.add(new ItemStats.ArmorStats(armor.damageReduceAmount));
        }

        addUnbreakableAttribute(stack, stats);

        if (EnricherConfig.durabilityEnabled) {
            addDurabilityAttribute(stack, stats);
        }

        if (EnricherConfig.burnTimeEnabled) {
            addBurnTimeAttribute(stack, stats);
        }

        final AttributeEnricherEvent event = new AttributeEnricherEvent(context, stats);
        TooltipUtils.postEvent(event);

        Collections.sort(event.stats, (ItemStats a, ItemStats b) -> Double.compare(b.getOrder(), a.getOrder()));

        return event.stats;
    }

    private static void addUnbreakableAttribute(ItemStack stack, List<ItemStats> stats) {
        if (stack.hasTagCompound() && stack.getTagCompound()
            .getBoolean("Unbreakable")) {
            stats.add(new ItemStats.UnbreakableStats());
        }
    }

    private static void addDurabilityAttribute(ItemStack stack, List<ItemStats> stats) {
        if (stack.isItemStackDamageable() && !stack.getHasSubtypes()) {
            final int maxDurability = stack.getMaxDamage();
            final int durability = maxDurability - stack.getItemDamage();
            stats.add(new ItemStats.DurabilityStats(durability, maxDurability));
        }
    }

    private static void addBurnTimeAttribute(ItemStack stack, List<ItemStats> stats) {
        final int burnTime = TileEntityFurnace.getItemBurnTime(stack);
        if (burnTime > 0) {
            stats.add(new ItemStats.BurnTimeStats(burnTime));
        }
    }

}
