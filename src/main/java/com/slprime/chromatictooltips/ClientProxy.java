package com.slprime.chromatictooltips;

import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import com.slprime.chromatictooltips.enricher.AttributeModifierEnricher;
import com.slprime.chromatictooltips.enricher.ContextInfoEnricher;
import com.slprime.chromatictooltips.enricher.EnchantmentEnricher;
import com.slprime.chromatictooltips.enricher.HotkeyEnricher;
import com.slprime.chromatictooltips.enricher.ItemInfoEnricher;
import com.slprime.chromatictooltips.enricher.KeyboardModifierEnricher;
import com.slprime.chromatictooltips.enricher.ModInfoEnricher;
import com.slprime.chromatictooltips.enricher.OreDictionaryEnricher;
import com.slprime.chromatictooltips.enricher.StackSizeEnricher;
import com.slprime.chromatictooltips.enricher.TitleEnricher;
import com.slprime.chromatictooltips.util.ClientUtil;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientProxy extends CommonProxy implements IResourceManagerReloadListener {

    public static final KeyBinding nextPage = new KeyBinding(
        "key.chromatictooltips.next_page",
        Keyboard.KEY_Z,
        "key.categories.chromatictooltips");
    public static final KeyBinding previousPage = new KeyBinding(
        "key.chromatictooltips.previous_page",
        Keyboard.KEY_NONE,
        "key.categories.chromatictooltips");
    protected static boolean nextPageIsPressed = false;
    protected static boolean previousPageIsPressed = false;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        FMLCommonHandler.instance()
            .bus()
            .register(this);
        MinecraftForge.EVENT_BUS.register(this);

        ClientRegistry.registerKeyBinding(nextPage);
        ClientRegistry.registerKeyBinding(previousPage);

        TooltipHandler.addEnricher(new TitleEnricher());
        TooltipHandler.addEnricher(new KeyboardModifierEnricher());
        TooltipHandler.addEnricher(new AttributeModifierEnricher(true));
        TooltipHandler.addEnricher(new StackSizeEnricher());
        TooltipHandler.addEnricher(new HotkeyEnricher());
        TooltipHandler.addEnricher(new ItemInfoEnricher());
        TooltipHandler.addEnricher(new EnchantmentEnricher());
        TooltipHandler.addEnricher(new AttributeModifierEnricher(false));
        TooltipHandler.addEnricher(new OreDictionaryEnricher());
        TooltipHandler.addEnricher(new ContextInfoEnricher());
        TooltipHandler.addEnricher(new ModInfoEnricher());

        TooltipHandler.setRendererClass(TooltipRenderer.class);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        if (ClientUtil.mc()
            .getResourceManager() instanceof IReloadableResourceManager manager) {
            manager.registerReloadListener(this);
        } else {
            TooltipHandler.reload();
        }
    }

    @SubscribeEvent
    public void onScreenPostDraw(DrawScreenEvent.Post event) {
        TooltipHandler.drawLastTooltip();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        final boolean nextPressed = nextPage.getKeyCode() != 0 && Keyboard.isKeyDown(nextPage.getKeyCode());
        final boolean previousPressed = previousPage.getKeyCode() != 0 && Keyboard.isKeyDown(previousPage.getKeyCode());

        if (nextPressed && !nextPageIsPressed) {
            TooltipHandler.nextTooltipPage();
        } else if (previousPressed && !previousPageIsPressed) {
            TooltipHandler.previousTooltipPage();
        }

        nextPageIsPressed = nextPressed;
        previousPageIsPressed = previousPressed;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        TooltipHandler.reload();
    }

}
