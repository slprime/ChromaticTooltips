package com.slprime.chromatictooltips.config;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import com.slprime.chromatictooltips.ChromaticTooltips;

public class ChromaticGuiConfig extends SimpleGuiConfig {

    public ChromaticGuiConfig(GuiScreen parentScreen) throws ConfigException {
        super(
            parentScreen,
            ChromaticTooltips.MODID,
            ChromaticTooltips.NAME,
            true,
            GeneralConfig.class,
            EnricherConfig.class,
            StackAmountConfig.class);
    }
}
