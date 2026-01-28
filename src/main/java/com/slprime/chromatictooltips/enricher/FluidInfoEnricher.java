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
        final FluidStack fluid = context.getFluid();

        if (fluid == null) {
            return null;
        }

        final FluidInfoEnricherEvent event = new FluidInfoEnricherEvent(context, fluidInformation(fluid));
        TooltipUtils.postEvent(event);

        return new TooltipLines(event.tooltip);
    }

    protected List<String> fluidInformation(FluidStack fluidStack) {
        final List<String> tooltip = new ArrayList<>();
        final ItemStack potion = getPotion(fluidStack);

        if (potion != null && potion.getItem() instanceof ItemPotion) {
            potion.getItem()
                .addInformation(potion, Minecraft.getMinecraft().thePlayer, tooltip, false);
        }

        return tooltip;
    }

    public static ItemStack getPotion(FluidStack fluidStack) {
        if (fluidStack == null) return null;
        final ItemStack fillStack = fillStack(glassBottle, fluidStack);

        if (fillStack != null && fillStack.getItem() instanceof ItemPotion) {
            return fillStack;
        }

        return null;
    }

    public static ItemStack fillStack(ItemStack itemStack, FluidStack fluid) {
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
