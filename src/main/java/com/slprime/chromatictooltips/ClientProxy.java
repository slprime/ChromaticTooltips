package com.slprime.chromatictooltips;

import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import com.slprime.chromatictooltips.converter.DividerConverter;
import com.slprime.chromatictooltips.converter.ModifierConverter;
import com.slprime.chromatictooltips.enricher.ContextInfoEnricher;
import com.slprime.chromatictooltips.enricher.EnchantmentEnricher;
import com.slprime.chromatictooltips.enricher.FluidInfoEnricher;
import com.slprime.chromatictooltips.enricher.HotkeyEnricher;
import com.slprime.chromatictooltips.enricher.ItemInfoEnricher;
import com.slprime.chromatictooltips.enricher.ItemStatsEnricher;
import com.slprime.chromatictooltips.enricher.KeyboardModifierEnricher;
import com.slprime.chromatictooltips.enricher.ModInfoEnricher;
import com.slprime.chromatictooltips.enricher.OreDictionaryEnricher;
import com.slprime.chromatictooltips.enricher.StackSizeEnricher;
import com.slprime.chromatictooltips.enricher.TitleEnricher;
import com.slprime.chromatictooltips.util.TooltipUtils;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
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

        TooltipRegistry.addEnricher(new TitleEnricher());
        TooltipRegistry.addEnricher(new KeyboardModifierEnricher());
        TooltipRegistry.addEnricher(new ItemStatsEnricher(true));
        TooltipRegistry.addEnricher(new StackSizeEnricher());
        TooltipRegistry.addEnricher(new HotkeyEnricher());
        TooltipRegistry.addEnricher(new ItemInfoEnricher());
        TooltipRegistry.addEnricher(new FluidInfoEnricher());
        TooltipRegistry.addEnricher(new EnchantmentEnricher());
        TooltipRegistry.addEnricher(new ItemStatsEnricher(false));
        TooltipRegistry.addEnricher(new OreDictionaryEnricher());
        TooltipRegistry.addEnricher(new ContextInfoEnricher());
        TooltipRegistry.addEnricher(new ModInfoEnricher());

        TooltipHandler.setRendererClass(TooltipRenderer.class);

        TooltipRegistry.addLineConverter(ModifierConverter.PATTERN, new ModifierConverter());
        TooltipRegistry.addLineConverter(DividerConverter.PATTERN, new DividerConverter());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        if (TooltipUtils.mc()
            .getResourceManager() instanceof IReloadableResourceManager manager) {
            manager.registerReloadListener(this);
        } else {
            TooltipHandler.reload();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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
