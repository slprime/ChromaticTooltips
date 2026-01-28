package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.slprime.chromatictooltips.api.EnchantmentData;
import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.component.EnchantmentComponent;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.event.EnchantmentEnricherEvent;
import com.slprime.chromatictooltips.util.TooltipUtils;

public class EnchantmentEnricher implements ITooltipEnricher {

    protected static final Map<EnumEnchantmentType, EnumChatFormatting> enchantmentColors = new HashMap<>();
    protected static final Map<EnumEnchantmentType, String> enchantmentIcons = new HashMap<>();

    static {
        enchantmentColors.put(EnumEnchantmentType.armor, EnumChatFormatting.GOLD);
        enchantmentColors.put(EnumEnchantmentType.armor_feet, EnumChatFormatting.DARK_GREEN);
        enchantmentColors.put(EnumEnchantmentType.armor_legs, EnumChatFormatting.DARK_PURPLE);
        enchantmentColors.put(EnumEnchantmentType.armor_torso, EnumChatFormatting.LIGHT_PURPLE);
        enchantmentColors.put(EnumEnchantmentType.armor_head, EnumChatFormatting.AQUA);
        enchantmentColors.put(EnumEnchantmentType.bow, EnumChatFormatting.BLUE);
        enchantmentColors.put(EnumEnchantmentType.weapon, EnumChatFormatting.DARK_RED);
        enchantmentColors.put(EnumEnchantmentType.fishing_rod, EnumChatFormatting.DARK_AQUA);
        enchantmentColors.put(EnumEnchantmentType.digger, EnumChatFormatting.GREEN);

        enchantmentIcons.put(EnumEnchantmentType.armor, "chestplate");
        enchantmentIcons.put(EnumEnchantmentType.armor_feet, "boots");
        enchantmentIcons.put(EnumEnchantmentType.armor_legs, "leggings");
        enchantmentIcons.put(EnumEnchantmentType.armor_torso, "chestplate");
        enchantmentIcons.put(EnumEnchantmentType.armor_head, "helmet");
        enchantmentIcons.put(EnumEnchantmentType.bow, "bow");
        enchantmentIcons.put(EnumEnchantmentType.weapon, "sword");
        enchantmentIcons.put(EnumEnchantmentType.fishing_rod, "fishing");
        enchantmentIcons.put(EnumEnchantmentType.digger, "pickaxe");
    }

    @Override
    public String sectionId() {
        return "enchantments";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.NONE);
    }

    @Override
    public TooltipLines build(TooltipContext context) {
        final ItemStack stack = context.getItem();

        if (stack == null) {
            return null;
        }

        final List<EnchantmentData> enchantments = getEnchantments(context, stack);
        final TooltipLines enchantmentsList = new TooltipLines();

        for (final EnchantmentData enchantmentData : enchantments) {
            enchantmentsList.line(createEnchantmentComponent(enchantmentData));
        }

        return enchantmentsList;
    }

    protected static List<EnchantmentData> getEnchantments(TooltipContext context, ItemStack stack) {
        final List<EnchantmentData> enchantmentList = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : EnchantmentHelper.getEnchantments(stack)
            .entrySet()) {
            if (Enchantment.enchantmentsList[entry.getKey()] != null) {
                enchantmentList.add(new EnchantmentData(entry.getKey(), entry.getValue()));
            }
        }

        final EnchantmentEnricherEvent event = new EnchantmentEnricherEvent(context, enchantmentList);
        TooltipUtils.postEvent(event);

        Collections.sort(
            event.enchantments,
            (EnchantmentData a, EnchantmentData b) -> a.enchantment.type.compareTo(b.enchantment.type));

        return event.enchantments;
    }

    protected EnchantmentComponent createEnchantmentComponent(EnchantmentData enchantmentData) {
        final String icon = enchantmentIcons.getOrDefault(enchantmentData.enchantment.type, "star");
        final EnumChatFormatting color = enchantmentColors
            .getOrDefault(enchantmentData.enchantment.type, EnumChatFormatting.YELLOW);
        List<String> hint = EnricherConfig.enchantmentHintEnabled ? enchantmentData.hint : Collections.emptyList();

        if (EnricherConfig.enchantmentHintEnabled && hint.isEmpty()) {
            final String hintKey = enchantmentData.enchantment.getName() + ".hint";

            if (!hintKey.equals(StatCollector.translateToLocal(hintKey))) {
                hint.add(StatCollector.translateToLocal(hintKey));
            }
        }

        return new EnchantmentComponent(
            "enchantments/" + icon + ".png",
            TooltipUtils
                .applyBaseColorIfAbsent(enchantmentData.enchantment.getTranslatedName(enchantmentData.level), color),
            hint);
    }

}
