package com.slprime.chromatictooltips.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.slprime.chromatictooltips.ChromaticTooltips;

@Config(modid = ChromaticTooltips.MODID, category = "enricher")
public class EnricherConfig {

    @Config.Comment("Show hotkey tooltip information.")
    @Config.DefaultBoolean(true)
    public static boolean hotkeysEnabled;

    @Config.Comment("Show hotkey help text in item tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean hotkeysHelpTextEnabled;

    @Config.Comment("Show attribute modifier information in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean attributeModifierIconsEnabled = true;

    @Config.Comment("Show ore dictionary information in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean oreDictionaryEnabled;

    @Config.Comment("Show stack size information in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean amountEnabled;

    @Config.Comment("Include the player's inventory when calculating stack sizes.")
    @Config.DefaultBoolean(true)
    public static boolean playerInventoryAmountEnabled;

    @Config.Comment("Show enchantment icons in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean showEnchantmentIcons;

    @Config.Comment("Show enchantment hint text in tooltips.")
    @Config.DefaultBoolean(true)
    public static boolean showEnchantmentHint;

}
