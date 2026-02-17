package com.slprime.chromatictooltips.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.slprime.chromatictooltips.ChromaticTooltips;

@Config(modid = ChromaticTooltips.MODID, category = "stackamount")
public class StackAmountConfig {

    @Config.Comment("Include container inventory when calculating stack amounts.")
    @Config.LangKey("chromatictooltips.config.stackamount.includeContainerInventory")
    @Config.DefaultBoolean(true)
    public static boolean includeContainerInventory;

    @Config.Comment("Hide stack amount when the stack size is below the item's max stack size.")
    @Config.LangKey("chromatictooltips.config.stackamount.hideWhenBelowMaxStackSize")
    @Config.DefaultBoolean(true)
    public static boolean hideWhenBelowMaxStackSize;

    @Config.Comment("Threshold for compact formatting of item stack amounts (10^N).")
    @Config.LangKey("chromatictooltips.config.stackamount.itemCompactThreshold")
    @Config.DefaultInt(6)
    public static int itemCompactThreshold;

    @Config.Comment("Threshold for compact formatting of fluid amounts (10^N).")
    @Config.LangKey("chromatictooltips.config.stackamount.fluidCompactThreshold")
    @Config.DefaultInt(9)
    public static int fluidCompactThreshold;

}
