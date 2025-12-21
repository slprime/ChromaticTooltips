package com.slprime.chromatictooltips.api;

import java.awt.Point;

import net.minecraft.item.ItemStack;

public class TooltipRequest {

    public final String context;
    public final ItemStack stack;
    public final TooltipLines tooltip;
    public final Point mouse;

    public TooltipRequest(String context, ItemStack stack, TooltipLines tooltip, Point mouse) {
        this.context = context;
        this.stack = stack;
        this.tooltip = tooltip != null ? tooltip : new TooltipLines();
        this.mouse = mouse;
    }
}
