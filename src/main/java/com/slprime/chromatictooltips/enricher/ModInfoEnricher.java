package com.slprime.chromatictooltips.enricher;

import java.util.EnumSet;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.api.TooltipTarget;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.event.ModInfoEnricherEvent;
import com.slprime.chromatictooltips.util.TooltipUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class ModInfoEnricher implements ITooltipEnricher {

    protected static final String DEFAULT_MOD_NAME = "Minecraft";
    protected static final UniqueIdentifier UNKNOWN_IDENTIFIER = new UniqueIdentifier("Unknown:unknown");
    protected static volatile Map<String, ModContainer> namedMods = null;

    @Override
    public String sectionId() {
        return "modInfo";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.FOOTER;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.NONE);
    }

    @Override
    public TooltipLines build(TooltipContext context) {

        if (!EnricherConfig.modInfoEnabled) {
            return null;
        }

        final TooltipLines components = new TooltipLines();
        final TooltipTarget target = context.getTarget();
        UniqueIdentifier identifier = UNKNOWN_IDENTIFIER;

        if (target.isItem()) {
            identifier = getIdentifier(target.getItem());
        } else if (target.isFluid()) {
            identifier = new UniqueIdentifier(
                FluidRegistry.getDefaultFluidName(
                    target.getFluid()
                        .getFluid()));
        } else {
            return null;
        }

        final ModInfoEnricherEvent event = new ModInfoEnricherEvent(
            target,
            nameFromIdentifier(identifier),
            identifier.modId,
            identifier.name);
        TooltipUtils.postEvent(event);

        if (TooltipUtils.isCtrlKeyDown() && TooltipUtils.mc().gameSettings.advancedItemTooltips) {
            components.line(TooltipUtils.translate("enricher.modinfo.identifier", event.modId, event.itemId));
        } else {
            components.line(TooltipUtils.translate("enricher.modinfo.modname", event.modName));
        }

        return components;
    }

    protected static UniqueIdentifier getIdentifier(ItemStack stack) {
        final UniqueIdentifier identifier = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        return identifier != null ? identifier : UNKNOWN_IDENTIFIER;
    }

    protected static String nameFromIdentifier(UniqueIdentifier identifier) {

        if (namedMods == null) {
            namedMods = Loader.instance()
                .getIndexedModList();
        }

        final ModContainer modContainer = namedMods.get(identifier.modId);
        return modContainer != null ? modContainer.getName() : DEFAULT_MOD_NAME;
    }

}
