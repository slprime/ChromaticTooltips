package com.slprime.chromatictooltips.enricher;

import java.util.EnumSet;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.event.StackSizeEnricherEvent;
import com.slprime.chromatictooltips.util.ClientUtil;

public class StackSizeEnricher implements ITooltipEnricher {

    @Override
    public String sectionId() {
        return "stacksize";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.SHIFT);
    }

    @Override
    public TooltipLines build(TooltipContext context) {
        final ItemStack stack = context.getStack();

        if (stack == null || !EnricherConfig.stackSizeEnabled) {
            return null;
        }

        final long stackSize = EnricherConfig.includePlayerInventoryEnabled ? getStackSize(context) : stack.stackSize;
        final StackSizeEnricherEvent event = new StackSizeEnricherEvent(context, getFluid(stack), stackSize);
        ClientUtil.postEvent(event);

        if (event.stackSize <= 0 || event.fluid != null && event.fluid.amount <= 0) {
            return null;
        }

        if (event.fluid != null) {
            return new TooltipLines(formatFluidAmount(event.fluid.amount * event.stackSize));
        }

        return new TooltipLines(formatStackSize(event.stackSize, stack.getMaxStackSize()));
    }

    protected long getStackSize(TooltipContext context) {
        final GuiContainer guiContainer = ClientUtil.getGuiContainer();
        final ItemStack stack = context.getStack();

        if (guiContainer != null) {
            final Slot slot = guiContainer.getSlotAtPosition(context.getMouseX(), context.getMouseY());
            final InventoryPlayer playerInventory = ClientUtil.getPlayerInventory();
            if (slot != null && slot.getHasStack() && slot.inventory == playerInventory) {
                long stackSize = 0;

                for (ItemStack invStack : playerInventory.mainInventory) {
                    if (invStack != null && stack.isItemEqual(invStack)
                        && ItemStack.areItemStackTagsEqual(stack, invStack)) {
                        stackSize += invStack.stackSize;
                    }
                }

                return stackSize;
            }
        }

        return stack.stackSize;
    }

    protected FluidStack getFluid(ItemStack stack) {
        FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stack);

        if (fluidStack == null && stack.getItem() instanceof IFluidContainerItem fluidItem) {
            fluidStack = fluidItem.getFluid(stack);
        }

        return fluidStack;
    }

    protected String formatStackSize(long stackSize, int maxStackSize) {
        return format(
            stackSize,
            maxStackSize,
            ClientUtil.translate("enricher.stacksize.item", "%s = %s * %s + %s"),
            ClientUtil.translate("enricher.stacksize.item", "%s = %s * %s"),
            ClientUtil.translate("enricher.stacksize.item", "%s"));
    }

    protected String formatFluidAmount(long amount) {
        return format(
            amount,
            144,
            ClientUtil.translate("enricher.stacksize.fluid", "%s L = %s * %s L + %s L"),
            ClientUtil.translate("enricher.stacksize.fluid", "%s L = %s * %s L"),
            ClientUtil.translate("enricher.stacksize.fluid", "%s L"));
    }

    protected String format(long stackSize, int maxStackSize, String fullPattern, String shortPattern,
        String stackPattern) {

        if (stackSize <= maxStackSize || maxStackSize == 1) {
            return String.format(stackPattern, ClientUtil.formatNumbers(stackSize));
        }

        final int remainder = (int) (stackSize % maxStackSize);

        if (remainder > 0) {
            return String.format(
                fullPattern,
                ClientUtil.formatNumbers(stackSize),
                ClientUtil.formatNumbers(stackSize / maxStackSize),
                ClientUtil.formatNumbers(maxStackSize),
                ClientUtil.formatNumbers(remainder));
        } else {
            return String.format(
                shortPattern,
                ClientUtil.formatNumbers(stackSize),
                ClientUtil.formatNumbers(stackSize / maxStackSize),
                ClientUtil.formatNumbers(maxStackSize));
        }
    }

}
