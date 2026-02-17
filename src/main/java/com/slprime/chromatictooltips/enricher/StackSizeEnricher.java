package com.slprime.chromatictooltips.enricher;

import java.awt.Point;
import java.util.EnumSet;
import java.util.function.LongFunction;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil;
import com.gtnewhorizon.gtnhlib.util.numberformatting.options.CompactOptions;
import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.api.TooltipTarget;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.config.StackAmountConfig;
import com.slprime.chromatictooltips.event.StackSizeEnricherEvent;
import com.slprime.chromatictooltips.util.TooltipUtils;

public class StackSizeEnricher implements ITooltipEnricher {

    private static CompactOptions fluidCompactOptions = new CompactOptions()
        .setCompactThreshold(pow10(StackAmountConfig.fluidCompactThreshold));
    private static CompactOptions itemCompactOptions = new CompactOptions()
        .setCompactThreshold(pow10(StackAmountConfig.itemCompactThreshold));
    private static int itemCompactThreshold = StackAmountConfig.itemCompactThreshold;
    private static int fluidCompactThreshold = StackAmountConfig.fluidCompactThreshold;

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

        final TooltipLines components = new TooltipLines();
        final StackSizeEnricherEvent event = new StackSizeEnricherEvent(target, stackAmount);
        TooltipUtils.postEvent(event);

        if (target.isItem()) {
            final int maxStackSize = target.getItem()
                .getMaxStackSize();

            if (event.stackAmount > 1
                && (!StackAmountConfig.hideWhenBelowMaxStackSize || event.stackAmount > maxStackSize
                    || event.stackAmount != target.getStackAmount())) {
                components.line(format(event.stackAmount, maxStackSize, "enricher.stacksize.item", this::formatNumber));
            }

            if (target.isFluidContainer() && target.getContainedFluidAmount() > 0) {
                final long fluidAmount = target.getContainedFluidAmount();

                if (event.stackAmount > 1) {
                    components.line(
                        format(event.stackAmount * fluidAmount, 144, "enricher.stacksize.fluid", this::formatFluid));
                }

                components.line(format(fluidAmount, 144, "enricher.stacksize.container", this::formatFluid));
            }
        }

        if (target.isFluid() && event.stackAmount > 0
            && (!StackAmountConfig.hideWhenBelowMaxStackSize || event.stackAmount > 144)) {
            components.line(format(event.stackAmount, 144, "enricher.stacksize.fluid", this::formatFluid));
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

    protected String format(long stackAmount, long maxStackSize, String pattern, LongFunction<String> valueFormatter) {

        if (stackAmount <= maxStackSize || maxStackSize == 1) {
            return TooltipUtils.translate(pattern, valueFormatter.apply(stackAmount));
        }

        final long remainder = stackAmount % maxStackSize;
        final long units = stackAmount / maxStackSize;

        if (remainder > 0) {
            return TooltipUtils.translate(
                pattern + ".full",
                valueFormatter.apply(stackAmount),
                formatNumber(units),
                valueFormatter.apply(maxStackSize),
                valueFormatter.apply(remainder));
        } else {
            return TooltipUtils.translate(
                pattern + ".short",
                valueFormatter.apply(stackAmount),
                formatNumber(units),
                valueFormatter.apply(maxStackSize));
        }
    }

    public String formatFluid(long stackAmount) {

        if (StackAmountConfig.fluidCompactThreshold != StackSizeEnricher.fluidCompactThreshold) {
            StackSizeEnricher.fluidCompactThreshold = StackAmountConfig.fluidCompactThreshold;
            StackSizeEnricher.fluidCompactOptions.setCompactThreshold(pow10(StackSizeEnricher.fluidCompactThreshold));
        }

        return NumberFormatUtil.formatFluidCompact(stackAmount, StackSizeEnricher.fluidCompactOptions);
    }

    public String formatNumber(long stackAmount) {

        if (StackAmountConfig.itemCompactThreshold != StackSizeEnricher.itemCompactThreshold) {
            StackSizeEnricher.itemCompactThreshold = StackAmountConfig.itemCompactThreshold;
            StackSizeEnricher.itemCompactOptions.setCompactThreshold(pow10(StackSizeEnricher.itemCompactThreshold));
        }

        return NumberFormatUtil.formatNumberCompact(stackAmount, StackSizeEnricher.itemCompactOptions);
    }

    private static long pow10(int exp) {
        long r = 1;

        for (int i = 0; i < exp; i++) {
            r *= 10;
        }

        return r;
    }

}
