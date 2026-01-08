package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.component.TextComponent;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.util.ClientUtil;

public class OreDictionaryEnricher implements ITooltipEnricher {

    protected String titleComponent;

    public OreDictionaryEnricher() {
        this.titleComponent = ClientUtil.translate("enricher.oreDictionary.title");
    }

    @Override
    public String sectionId() {
        return "oreDictionary";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.CTRL);
    }

    @Override
    public TooltipLines build(TooltipContext context) {
        final ItemStack stack = context.getStack();

        if (stack == null || !EnricherConfig.oreDictionaryEnabled) {
            return null;
        }

        final List<ITooltipComponent> components = new ArrayList<>();

        for (int oredict : OreDictionary.getOreIDs(stack)) {
            components.add(
                new TextComponent(
                    ClientUtil.translate("enricher.oreDictionary.entry", OreDictionary.getOreName(oredict))));
        }

        if (!components.isEmpty()) {
            components.add(0, new TextComponent(this.titleComponent));
        }

        return new TooltipLines(components);
    }

}
