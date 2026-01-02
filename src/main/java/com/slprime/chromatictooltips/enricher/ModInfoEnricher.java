package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.component.TextComponent;
import com.slprime.chromatictooltips.util.ClientUtil;

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
    public EnumSet<EnricherMode> mode() {
        return EnumSet.of(EnricherMode.ALWAYS);
    }

    @Override
    public List<ITooltipComponent> build(TooltipContext context) {
        final ItemStack stack = context.getStack();

        if (stack == null) {
            return null;
        }

        final List<ITooltipComponent> components = new ArrayList<>();
        final UniqueIdentifier identifier = getIdentifier(stack);
        final String modname = nameFromStack(identifier);

        if (ClientUtil.isCtrlKeyDown() && ClientUtil.mc().gameSettings.advancedItemTooltips) {
            final boolean modnameEqualsModId = modname.replaceAll("\\s+", "")
                .equalsIgnoreCase(identifier.modId.replaceAll("\\s+", ""));

            components.add(
                new TextComponent(
                    ClientUtil.translate(
                        "enricher.modinfo.identifier",
                        modnameEqualsModId ? modname : identifier.modId,
                        identifier.name)));
        } else {
            components.add(new TextComponent(ClientUtil.translate("enricher.modinfo.modname", modname)));
        }

        return components;
    }

    protected static UniqueIdentifier getIdentifier(ItemStack stack) {
        final UniqueIdentifier identifier = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        return identifier != null ? identifier : UNKNOWN_IDENTIFIER;
    }

    protected static String nameFromStack(UniqueIdentifier identifier) {

        if (namedMods == null) {
            namedMods = Loader.instance()
                .getIndexedModList();
        }

        final ModContainer modContainer = namedMods.get(identifier.modId);
        return modContainer != null ? modContainer.getName() : DEFAULT_MOD_NAME;
    }

}
