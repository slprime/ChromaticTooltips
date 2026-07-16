package com.slprime.chromatictooltips.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.util.TooltipUtils;

@Config(modid = ChromaticTooltips.MODID, category = "enricher")
public class EnricherConfig {

    public enum EnricherVisibility {

        OFF,
        ALWAYS,
        EXTENDED;

        public boolean isEnabled() {
            return switch (this) {
                case OFF -> false;
                case ALWAYS -> true;
                case EXTENDED -> TooltipUtils.mc().gameSettings.advancedItemTooltips;
            };
        }
    }

    @Config.Comment("Show hotkey tooltip information.")
    @Config.LangKey("chromatictooltips.config.enricher.hotkeysEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility hotkeys;

    @Config.Comment("Show attribute modifier information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.attributeModifierIconsEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility attributeModifierIcons;

    @Config.Comment("Show stack amount information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.stackAmountEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility stackAmount;

    @Config.Comment("Show burn time information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.burnTimeEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility burnTime;

    @Config.Comment("Show food information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.foodStatsEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility foodStats;

    @Config.Comment("Show food effect information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.foodEffectsEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility foodEffects;

    @Config.Comment("Show durability information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.durabilityEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility durability;

    @Config.Comment("Show ore dictionary information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.oreDictionaryEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility oreDictionary;

    @Config.Comment("Show enchantment icons in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.enchantmentIconsEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility enchantmentIcons;

    @Config.Comment("Show enchantment hint text in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.enchantmentHintEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility enchantmentHint;

    @Config.Comment("Show keyboard modifier hints in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.keyboardModifiersEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility keyboardModifiers;

    @Config.Comment("Show mod information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.modInfoEnabled")
    @Config.DefaultEnum("ALWAYS")
    public static EnricherVisibility modInfo;

    @Config.Comment("Show item/fluid id information in tooltip titles.")
    @Config.LangKey("chromatictooltips.config.enricher.itemIdEnabled")
    @Config.DefaultEnum("EXTENDED")
    public static EnricherVisibility itemId;

}
