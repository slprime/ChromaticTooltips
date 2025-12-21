package com.slprime.chromatictooltips.api;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class TooltipBuilder {

    protected String context;
    protected ItemStack stack;
    protected TooltipLines textLines = new TooltipLines();
    protected Rectangle anchorBounds = new Rectangle(0, 0, 0, 0);
    protected Point mouse;

    public TooltipBuilder() {}

    public TooltipBuilder context(String context) {
        this.context = context;
        return this;
    }

    public TooltipBuilder stack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public TooltipBuilder position(int x, int y) {
        this.mouse = new Point(x, y);
        return anchorBounds(this.mouse.x, this.mouse.y, 0, 0);
    }

    public TooltipBuilder position(Point point) {
        this.mouse = point;
        return anchorBounds(this.mouse.x, this.mouse.y, 0, 0);
    }

    public TooltipBuilder anchorBounds(int x, int y, int width, int height) {
        this.anchorBounds.setBounds(x, y, width, height);
        return this;
    }

    public TooltipBuilder header(String line) {
        this.textLines.header(line);
        return this;
    }

    public TooltipBuilder line(ITooltipComponent line) {
        this.textLines.line(line);
        return this;
    }

    public TooltipBuilder line(String line) {
        this.textLines.line(line);
        return this;
    }

    public TooltipBuilder lines(String... lines) {
        this.textLines.lines(lines);
        return this;
    }

    public TooltipBuilder lines(List<String> lines) {
        this.textLines.lines(lines);
        return this;
    }

    public TooltipBuilder paragraph() {
        this.textLines.paragraph();
        return this;
    }

    public TooltipBuilder divider() {
        this.textLines.divider();
        return this;
    }

    public TooltipBuilder divider(EnumChatFormatting color) {
        this.textLines.divider(color);
        return this;
    }

    public TooltipRequest build() {
        return new TooltipRequest(this.context, this.stack, this.textLines, this.mouse);
    }
}
