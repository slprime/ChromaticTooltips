package com.slprime.chromatictooltips.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.slprime.chromatictooltips.ChromaticTooltips;

@Config(modid = ChromaticTooltips.MODID, category = "enricher")
public class EnricherConfig {

    @Config.Comment("Show hotkey tooltip information.")
    @Config.DefaultBoolean(true)
    public static boolean hotkeysEnabled;

    @Config.Comment("Show attribute modifier information in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean attributeModifierIconsEnabled = true;

    @Config.Comment("Show burn time information in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean burnTimeEnabled;

    @Config.Comment("Show durability information in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean durabilityEnabled = true;

    @Config.Comment("Show ore dictionary information in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean oreDictionaryEnabled;

    @Config.Comment("Show stack size information in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean stackSizeEnabled;

    @Config.Comment("Include the container's inventory when calculating stack sizes.")
    @Config.DefaultBoolean(true)
    public static boolean includeContainerInventoryEnabled;

    @Config.Comment("Only show stack size information when the stack size exceeds the item's max stack size.")
    @Config.DefaultBoolean(false)
    public static boolean showOnlyWhenOverStackSizeEnabled;

    @Config.Comment("Show enchantment icons in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean enchantmentIconsEnabled;

    @Config.Comment("Show enchantment hint text in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean enchantmentHintEnabled;

    @Config.Comment("Show keyboard modifier hints in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean keyboardModifiersEnabled = true;

    @Config.Comment("Show mod information in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean modInfoEnabled = true;

}
