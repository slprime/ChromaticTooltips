package com.slprime.chromatictooltips;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.config.GeneralConfig;
import com.slprime.chromatictooltips.config.StackAmountConfig;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        try {
            ConfigurationManager.registerConfig(GeneralConfig.class);
            ConfigurationManager.registerConfig(EnricherConfig.class);
            ConfigurationManager.registerConfig(StackAmountConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}

}
