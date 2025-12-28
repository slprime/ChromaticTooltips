package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.component.TextTooltipComponent;
import com.slprime.chromatictooltips.util.ClientUtil;

public class ContextInfoEnricher implements ITooltipEnricher {

    @Override
    public String sectionId() {
        return "contextInfo";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<EnricherMode> mode() {
        return EnumSet.of(EnricherMode.DEFAULT);
    }

    @Override
    public List<ITooltipComponent> build(TooltipContext context) {
        final List<ITooltipComponent> lines = new ArrayList<>(context.getContextTooltip());

        if (context.getStack() == null && !lines.isEmpty() && lines.get(0) instanceof TextTooltipComponent) {
            lines.remove(0);
        }

        return lines;
    }

    protected List<String> itemInformation(ItemStack stack) {
        final Minecraft mc = ClientUtil.mc();

        try {
            final List<String> namelist = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
            namelist.remove(0);
            return namelist;
        } catch (Throwable ignored) {}

        return Collections.emptyList();
    }

}
