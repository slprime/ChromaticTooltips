package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.oredict.OreDictionary;

import com.slprime.chromatictooltips.Config;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.component.TextTooltipComponent;
import com.slprime.chromatictooltips.util.ClientUtil;

public class OreDictionaryEnricher implements ITooltipEnricher {

    protected String titleComponent;

    public OreDictionaryEnricher() {
        this.titleComponent = EnumChatFormatting.GRAY + ClientUtil.translate("enricher.oreDictionary.message");
    }

    @Override
    public List<ITooltipComponent> enrich(TooltipContext context) {
        final ItemStack stack = context.getStack();

        if (stack == null || !Config.oreDictionaryEnricherEnabled || !ClientUtil.shiftKey()) {
            return null;
        }

        final List<ITooltipComponent> components = new ArrayList<>();

        for (int oredict : OreDictionary.getOreIDs(stack)) {
            components.add(
                new TextTooltipComponent(
                    EnumChatFormatting.DARK_GRAY + "  - "
                        + EnumChatFormatting.YELLOW
                        + OreDictionary.getOreName(oredict)));
        }

        if (!components.isEmpty()) {
            components.add(0, new TextTooltipComponent(this.titleComponent));
        }

        return components;
    }

}
