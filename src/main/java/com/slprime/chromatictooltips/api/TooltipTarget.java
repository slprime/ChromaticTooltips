package com.slprime.chromatictooltips.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class TooltipTarget {

    public static final TooltipTarget EMPTY = new TooltipTarget(null, null, 0, null, 0);

    private ItemStack item;
    private FluidStack fluid;

    private long stackAmount;

    private Fluid containedFluid;
    private long containedFluidAmount;

    private TooltipTarget(ItemStack item, FluidStack fluid, long stackAmount, Fluid containedFluid,
        long containedFluidAmount) {
        this.item = item != null ? item.copy() : null;
        this.fluid = fluid != null ? fluid.copy() : null;
        this.stackAmount = stackAmount;
        this.containedFluid = containedFluid;
        this.containedFluidAmount = containedFluidAmount;
    }

    public static TooltipTarget ofItem(ItemStack item, long stackAmount) {

        if (item == null) {
            return TooltipTarget.EMPTY;
        }

        final FluidStack fluid = getFluid(item);

        if (fluid != null) {
            return new TooltipTarget(item, null, stackAmount, fluid.getFluid(), fluid.amount);
        } else {
            return new TooltipTarget(item, null, stackAmount, null, 0);
        }

    }

    public static TooltipTarget ofItem(ItemStack item) {

        if (item == null) {
            return TooltipTarget.EMPTY;
        }

        return ofItem(item, item.stackSize);
    }

    public static TooltipTarget ofFluid(FluidStack fluid, long stackAmount) {

        if (fluid == null) {
            return TooltipTarget.EMPTY;
        }

        return new TooltipTarget(null, fluid, stackAmount, null, 0);
    }

    public static TooltipTarget ofFluid(FluidStack fluid) {

        if (fluid == null) {
            return TooltipTarget.EMPTY;
        }

        return ofFluid(fluid, fluid.amount);
    }

    public TooltipTarget withContainedFluid(Fluid fluid, long amount) {
        return new TooltipTarget(this.item, this.fluid, this.stackAmount, fluid, amount);
    }

    public TooltipTarget withContainedFluid(FluidStack fluid) {
        return new TooltipTarget(this.item, this.fluid, this.stackAmount, fluid.getFluid(), fluid.amount);
    }

    public TooltipTarget withStackAmount(long amount) {
        if (this.stackAmount == amount) return this;
        return new TooltipTarget(this.item, this.fluid, amount, this.containedFluid, this.containedFluidAmount);
    }

    public long getStackAmount() {
        return this.stackAmount;
    }

    public Fluid getContainedFluid() {
        return this.containedFluid;
    }

    public long getContainedFluidAmount() {
        return this.containedFluidAmount;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public FluidStack getFluid() {
        return this.fluid;
    }

    public boolean isItem() {
        return this.item != null;
    }

    public boolean isFluidContainer() {
        return this.item != null && this.containedFluid != null;
    }

    public boolean isFluid() {
        return this.item == null && this.fluid != null;
    }

    protected static FluidStack getFluid(ItemStack stack) {
        if (stack == null) return null;
        FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stack);

        if (fluidStack == null && stack.getItem() instanceof IFluidContainerItem fluidItem) {
            fluidStack = fluidItem.getFluid(stack);
        }

        return fluidStack;
    }

    public boolean equivalentTo(TooltipTarget other) {
        if (this == other) return true;
        if (other == null) return false;

        if (this.item != null || other.item != null) {
            if (this.item == null || other.item == null) return false;
            return isItemEqualWithNBT(this.item, other.item);
        }

        if (this.fluid != null || other.fluid != null) {
            if (this.fluid == null || other.fluid == null) return false;
            return this.fluid.isFluidEqual(other.fluid);
        }

        return true;
    }

    protected static boolean isItemEqualWithNBT(ItemStack stackA, ItemStack stackB) {
        if (stackA == stackB) return true;
        if (stackA == null || stackB == null || !stackA.isItemEqual(stackB)) return false;

        if (stackA.hasTagCompound() && stackB.hasTagCompound()) {
            return stackA.stackTagCompound.equals(stackB.stackTagCompound);
        }

        return (stackA.stackTagCompound == null || stackA.stackTagCompound.hasNoTags())
            && (stackB.stackTagCompound == null || stackB.stackTagCompound.hasNoTags());
    }

}
