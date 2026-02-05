package com.slprime.chromatictooltips.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.util.NumberFormat;

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

    @Config.Comment("Configuration for item stack amounts.")
    @Config.LangKey("chromatictooltips.config.stackamount.itemAmount")
    public static final FormatConfig itemConfig = new FormatConfig();

    @Config.Comment("Configuration for fluid amounts.")
    @Config.LangKey("chromatictooltips.config.stackamount.fluidAmount")
    public static final FormatConfig fluidConfig = new FormatConfig();

    @Config.LangKey("GT5U.gui.config.client.nei")
    public static class FormatConfig {

        @Config.Comment("Number format used for item stack amounts (SI, POWER, E).")
        @Config.LangKey("chromatictooltips.config.stackamount.numberFormat")
        @Config.DefaultEnum("SI")
        public NumberFormat numberFormat;

        @Config.Comment("Disable detailed item formats (short/full) starting from 10^N.")
        @Config.LangKey("chromatictooltips.config.stackamount.detailCutoffPower")
        @Config.DefaultInt(6)
        @Config.RangeInt(min = 0, max = 18)
        public int detailCutoffPower;

    }

}
