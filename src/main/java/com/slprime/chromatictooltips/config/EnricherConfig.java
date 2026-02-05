package com.slprime.chromatictooltips.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.slprime.chromatictooltips.ChromaticTooltips;

@Config(modid = ChromaticTooltips.MODID, category = "enricher")
public class EnricherConfig {

    @Config.Comment("Show hotkey tooltip information.")
    @Config.LangKey("chromatictooltips.config.enricher.hotkeysEnabled")
    @Config.DefaultBoolean(true)
    public static boolean hotkeysEnabled;

    @Config.Comment("Show attribute modifier information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.attributeModifierIconsEnabled")
    @Config.DefaultBoolean(true)
    public static boolean attributeModifierIconsEnabled = true;

    @Config.Comment("Show stack amount information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.stackAmountEnabled")
    @Config.DefaultBoolean(true)
    public static boolean stackAmountEnabled = true;

    @Config.Comment("Show burn time information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.burnTimeEnabled")
    @Config.DefaultBoolean(true)
    public static boolean burnTimeEnabled;

    @Config.Comment("Show durability information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.durabilityEnabled")
    @Config.DefaultBoolean(true)
    public static boolean durabilityEnabled = true;

    @Config.Comment("Show ore dictionary information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.oreDictionaryEnabled")
    @Config.DefaultBoolean(true)
    public static boolean oreDictionaryEnabled;

    @Config.Comment("Show enchantment icons in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.enchantmentIconsEnabled")
    @Config.DefaultBoolean(true)
    public static boolean enchantmentIconsEnabled;

    @Config.Comment("Show enchantment hint text in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.enchantmentHintEnabled")
    @Config.DefaultBoolean(true)
    public static boolean enchantmentHintEnabled;

    @Config.Comment("Show keyboard modifier hints in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.keyboardModifiersEnabled")
    @Config.DefaultBoolean(true)
    public static boolean keyboardModifiersEnabled = true;

    @Config.Comment("Show mod information in tooltips.")
    @Config.LangKey("chromatictooltips.config.enricher.modInfoEnabled")
    @Config.DefaultBoolean(true)
    public static boolean modInfoEnabled = true;

}
