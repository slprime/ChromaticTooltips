package com.slprime.chromatictooltips.enricher;

import java.awt.Point;
import java.util.EnumSet;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.api.TooltipTarget;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.config.StackAmountConfig;
import com.slprime.chromatictooltips.config.StackAmountConfig.FormatConfig;
import com.slprime.chromatictooltips.event.StackSizeEnricherEvent;
import com.slprime.chromatictooltips.util.TooltipUtils;

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

        if (!EnricherConfig.stackAmountEnabled) {
            return null;
        }

        final TooltipTarget target = context.getTarget();

        if (!target.isItem() && !target.isFluid()) {
            return null;
        }

        long stackAmount = target.getStackAmount();

        if (StackAmountConfig.includeContainerInventory && target.isItem()) {
            stackAmount = getStackSize(target.getItem());
        }

        final StackSizeEnricherEvent event = new StackSizeEnricherEvent(target, stackAmount);
        TooltipUtils.postEvent(event);

        if (event.stackAmount <= 0) {
            return null;
        }

        final TooltipLines components = new TooltipLines();

        if (target.isItem() && event.stackAmount > 1
            && (!StackAmountConfig.hideWhenBelowMaxStackSize || event.stackAmount > target.getItem()
                .getMaxStackSize() || event.stackAmount != target.getStackAmount())) {
            components.line(
                formatItemAmount(
                    event.stackAmount,
                    target.getItem()
                        .getMaxStackSize()));
        }

        if (target.isFluidContainer() && target.getContainedFluidAmount() > 0) {
            components.line(formatFluidAmount(event.stackAmount * target.getContainedFluidAmount()));
        }

        if (target.isFluid() && (!StackAmountConfig.hideWhenBelowMaxStackSize || event.stackAmount > 144)) {
            components.line(formatFluidAmount(event.stackAmount));
        }

        return components;
    }

    protected long getStackSize(ItemStack stack) {
        final GuiContainer guiContainer = TooltipUtils.getGuiContainer();

        if (guiContainer != null) {
            final Point mouse = TooltipUtils.getMousePosition();
            final Slot slot = guiContainer.getSlotAtPosition(mouse.x, mouse.y);

            if (slot != null && slot.getHasStack()) {
                long stackSize = 0;

                for (Slot currentSlot : guiContainer.inventorySlots.inventorySlots) {
                    if (slot.inventory == currentSlot.inventory && currentSlot.getHasStack()
                        && stack.isItemEqual(currentSlot.getStack())
                        && ItemStack.areItemStackTagsEqual(stack, currentSlot.getStack())) {
                        stackSize += currentSlot.getStack().stackSize;
                    }
                }

                return stackSize == 0 ? stack.stackSize : stackSize;
            }
        }

        return stack.stackSize;
    }

    protected String formatItemAmount(long stackAmount, int maxStackSize) {
        return format(stackAmount, maxStackSize, "enricher.stacksize.item", StackAmountConfig.itemConfig);
    }

    protected String formatFluidAmount(long stackAmount) {
        return format(stackAmount, 144, "enricher.stacksize.fluid", StackAmountConfig.fluidConfig);
    }

    protected String format(long stackAmount, long maxStackSize, String pattern, FormatConfig formatter) {

        if (stackAmount <= maxStackSize || maxStackSize == 1) {
            return TooltipUtils
                .translate(pattern, formatter.numberFormat.format(stackAmount, formatter.detailCutoffPower));
        }

        final long remainder = stackAmount % maxStackSize;

        if (remainder > 0) {
            return TooltipUtils.translate(
                pattern + ".full",
                formatter.numberFormat.format(stackAmount, formatter.detailCutoffPower),
                formatter.numberFormat.format(stackAmount / maxStackSize, formatter.detailCutoffPower),
                formatter.numberFormat.format(maxStackSize, formatter.detailCutoffPower),
                formatter.numberFormat.format(remainder, formatter.detailCutoffPower));
        } else {
            return TooltipUtils.translate(
                pattern + ".short",
                formatter.numberFormat.format(stackAmount, formatter.detailCutoffPower),
                formatter.numberFormat.format(stackAmount / maxStackSize, formatter.detailCutoffPower),
                formatter.numberFormat.format(maxStackSize, formatter.detailCutoffPower));
        }
    }

}
