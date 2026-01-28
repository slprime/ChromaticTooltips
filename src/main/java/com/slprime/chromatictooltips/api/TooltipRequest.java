package com.slprime.chromatictooltips.api;

import java.awt.Point;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class TooltipRequest {

    public final String context;
    public final TooltipTarget target;
    public final TooltipLines tooltip;
    public final Point mouse;

    public TooltipRequest(String context, TooltipTarget target, TooltipLines tooltip, Point mouse) {
        this.context = context;
        this.target = target;
        this.tooltip = tooltip != null ? tooltip : new TooltipLines();
        this.mouse = mouse;
    }

    public TooltipRequest(TooltipRequest request) {
        this(request.context, request.target, new TooltipLines(request.tooltip), request.mouse);
    }

    public TooltipRequest(TooltipRequest request, TooltipTarget target) {
        this(request.context, target, new TooltipLines(request.tooltip), request.mouse);
    }

    public TooltipRequest copy() {
        return new TooltipRequest(this);
    }

    public boolean sameSubjectAs(TooltipRequest other) {
        if (other == null || !sameSubjectPresence(other)) return false;

        if (this.target.isFluid() && !this.target.getFluid()
            .isFluidEqual(other.target.getFluid())) {
            return false;
        }

        if (this.target.isItem() && !areStacksSameType(this.target.getItem(), other.target.getItem())) {
            return false;
        }

        return true;
    }

    public boolean equivalentTo(TooltipRequest other) {
        if (!sameSubjectAs(other)) return false;

        if (this.target.getStackAmount() != other.target.getStackAmount()) {
            return false;
        }

        return this.target.equivalentTo(other.target) && tooltip.equals(other.tooltip);
    }

    private boolean sameSubjectPresence(TooltipRequest other) {
        return this.target.isItem() == other.target.isItem() && this.target.isFluid() == other.target.isFluid();
    }

    /**
     * Checks if two stacks represent the same logical item type.
     * Ignores stack size and allows wildcard / damageable matching.
     */
    protected static boolean areStacksSameType(ItemStack stackA, ItemStack stackB) {
        if (stackA == stackB) return true;
        if (stackA == null || stackB == null) return false;

        return stackA.getItem() == stackB.getItem() && (stackA.getItemDamage() == stackB.getItemDamage()
            || stackA.getItemDamage() == OreDictionary.WILDCARD_VALUE
            || stackB.getItemDamage() == OreDictionary.WILDCARD_VALUE
            || stackA.getItem()
                .isDamageable());
    }

}
