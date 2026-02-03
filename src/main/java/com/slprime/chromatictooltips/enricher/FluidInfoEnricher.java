package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.api.TooltipTarget;
import com.slprime.chromatictooltips.event.FluidInfoEnricherEvent;
import com.slprime.chromatictooltips.util.TooltipUtils;

public class FluidInfoEnricher implements ITooltipEnricher {

    private static final ItemStack glassBottle = new ItemStack(Items.glass_bottle, 1);

    @Override
    public String sectionId() {
        return "fluidInfo";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.NONE, TooltipModifier.SHIFT);
    }

    @Override
    public TooltipLines build(TooltipContext context) {

        if (!context.getTarget()
            .isFluid()) {
            return null;
        }

        return new TooltipLines(getFluidInformation(context.getTarget()));
    }

    public static List<Object> getFluidInformation(TooltipTarget target) {
        final List<String> tooltip = new ArrayList<>();
        final FluidStack fluidStack = target.getFluid();
        final ItemStack potion = getPotion(fluidStack);

        if (potion != null && potion.getItem() instanceof ItemPotion) {
            potion.getItem()
                .addInformation(potion, Minecraft.getMinecraft().thePlayer, tooltip, false);
        }

        final FluidInfoEnricherEvent event = new FluidInfoEnricherEvent(target, tooltip);
        TooltipUtils.postEvent(event);

        return event.tooltip;
    }

    protected static ItemStack getPotion(FluidStack fluidStack) {
        if (fluidStack == null) return null;
        final ItemStack fillStack = fillStack(glassBottle, fluidStack);

        if (fillStack != null && fillStack.getItem() instanceof ItemPotion) {
            return fillStack;
        }

        return null;
    }

    protected static ItemStack fillStack(ItemStack itemStack, FluidStack fluid) {
        if (itemStack == null || itemStack.stackSize != 1) return null;
        Item item = itemStack.getItem();

        if (item instanceof IFluidContainerItem container) {
            container.fill(itemStack, fluid, true);
            return itemStack;
        } else if (FluidContainerRegistry.isContainer(itemStack)) {
            return FluidContainerRegistry.fillFluidContainer(fluid, itemStack);
        }

        return null;
    }

}
