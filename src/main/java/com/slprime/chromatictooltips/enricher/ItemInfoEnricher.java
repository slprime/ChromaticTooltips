package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.event.ItemInfoEnricherEvent;
import com.slprime.chromatictooltips.util.ClientUtil;

public class ItemInfoEnricher implements ITooltipEnricher {

    @Override
    public List<ITooltipComponent> enrich(TooltipContext context) {
        final ItemStack stack = context.getStack();

        if (stack == null) {
            return null;
        }

        final List<String> namelist = itemInformation(stack);
        final ItemInfoEnricherEvent event = new ItemInfoEnricherEvent(context, namelist);
        ClientUtil.postEvent(event);

        return event.tooltip.buildComponents(context);
    }

    protected List<String> itemInformation(ItemStack stack) {
        final Minecraft mc = ClientUtil.mc();
        List<String> namelist = new ArrayList<>();

        try {
            namelist = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
            namelist.remove(0);
        } catch (Throwable ignored) {}

        return namelist;
    }

}
