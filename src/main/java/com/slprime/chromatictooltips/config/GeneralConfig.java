package com.slprime.chromatictooltips.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.slprime.chromatictooltips.ChromaticTooltips;

@Config(modid = ChromaticTooltips.MODID, category = "general")
public class GeneralConfig {

    @Config.Comment("Disabled tooltip animations.")
    @Config.LangKey("chromatictooltips.config.general.transformDisabled")
    @Config.DefaultBoolean(false)
    public static boolean transformDisabled;

    @Config.Comment("Hide tooltip lines matching these patterns. Regexp are case insensitive.")
    @Config.LangKey("chromatictooltips.config.general.tooltipBlacklistLines")
    @Config.DefaultStringList({})
    public static String[] tooltipBlacklistLines;

    @Config.Comment("Scaling factor for tooltips. 0 = gui scale. Positive values increase the tooltip to the next scale steps, negative values decrease it.")
    @Config.LangKey("chromatictooltips.config.general.scaleFactor")
    @Config.RangeInt(min = -7, max = 7)
    @Config.DefaultInt(0)
    public static int scaleFactor;

    @Config.Comment("Maximum width of tooltips in scaled pixels. 0 = no limit.")
    @Config.LangKey("chromatictooltips.config.general.maxWidth")
    @Config.RangeInt(min = 0, max = 10000)
    @Config.DefaultInt(0)
    public static int maxWidth;

    @Config.Comment("Delay in milliseconds before showing tooltips.")
    @Config.LangKey("chromatictooltips.config.general.tooltipShowUpDelay")
    @Config.RangeInt(min = 0, max = 10000)
    @Config.DefaultInt(0)
    public static int tooltipShowUpDelay;

}
