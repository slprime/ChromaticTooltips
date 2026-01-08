package com.slprime.chromatictooltips.config;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.slprime.chromatictooltips.ChromaticTooltips;

@Config(modid = ChromaticTooltips.MODID, category = "general")
public class GeneralConfig {

    @Config.Comment("Enabled tooltip transformations.")
    @Config.DefaultBoolean(true)
    public static boolean transformEnabled;

    @Config.Comment("Hide tooltip lines matching these patterns. Regexp are case insensitive.")
    @Config.DefaultStringList({})
    public static String[] tooltipBlacklistLines;

    @Config.Comment("Scaling factor for tooltips. 0 = auto, 1 = normal size. Positive values increase the tooltip to the next scale steps, negative values decrease it.")
    @Config.RangeInt(min = -7, max = 7)
    @Config.DefaultInt(0)
    public static int scaleFactor;

    @Config.Comment("Maximum width of tooltips in scaled pixels. 0 = no limit.")
    @Config.RangeInt(min = 0, max = 10000)
    @Config.DefaultInt(0)
    public static int maxWidth;

}
