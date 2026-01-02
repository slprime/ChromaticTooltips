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

    @Config.Comment("Scaling factor for tooltips. 0 = auto, 1 = normal size, 2 = double size, etc.")
    @Config.RangeInt(min = 0, max = 5)
    @Config.DefaultInt(0)
    public static int scaleFactor;

}
