package com.slprime.chromatictooltips.util;

import java.awt.Dimension;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;

public class SectionBox implements ITooltipComponent {

    protected TooltipFontContext fontContext = null;
    protected TooltipDecoratorCollection decorators = null;
    protected TooltipSpacing padding = TooltipSpacing.ZERO;
    protected TooltipSpacing margin = TooltipSpacing.ZERO;
    protected TooltipTransform transform = null;
    protected TooltipAlign alignInline = TooltipAlign.START;
    protected TooltipAlign alignBlock = TooltipAlign.END;
    protected Dimension contentSize = new Dimension(0, 0);
    protected int spacing = 0;
    protected int minWidth;
    protected int minHeight;

    public SectionBox(TooltipStyle style) {
        this.margin = style.getAsTooltipSpacing("margin", new int[] { 0, 0, 0, 0 });
        this.padding = style.getAsTooltipSpacing("padding", new int[] { 0, 0, 0, 0 });
        this.fontContext = new TooltipFontContext(style);
        this.decorators = style.getDecoratorCollection();

        if (style.containsKey("transform")) {
            this.transform = new TooltipTransform(style.getAsStyle("transform"));
        }

        this.alignInline = TooltipAlign
            .fromString(style.getAsString("alignInline", style.getAsString("align.inline", "left")));
        this.alignBlock = TooltipAlign
            .fromString(style.getAsString("alignBlock", style.getAsString("align.block", "top")));

        this.minWidth = style.getAsInt("minWidth", 0);
        this.minHeight = style.getAsInt("minHeight", 0);
        this.spacing = style.getAsInt("spacing", 0);
    }

    public SectionBox(SectionBox copy) {
        this.transform = copy.transform;
        this.fontContext = copy.fontContext;
        this.decorators = copy.decorators;
        this.padding = copy.padding;
        this.margin = copy.margin;
        this.alignInline = copy.alignInline;
        this.alignBlock = copy.alignBlock;
        this.minWidth = copy.minWidth;
        this.minHeight = copy.minHeight;
        this.spacing = copy.spacing;
    }

    public void setContentSize(int width, int height) {
        this.contentSize.setSize(width, height);
    }

    public Dimension getContentSize() {
        return this.contentSize;
    }

    public TooltipSpacing getPadding() {
        return this.padding;
    }

    public TooltipSpacing getMargin() {
        return this.margin;
    }

    public int getMinWidth() {
        return this.minWidth;
    }

    public int getMinHeight() {
        return this.minHeight;
    }

    public int getInline() {
        return this.padding.getInline() + this.margin.getInline();
    }

    public int getBlock() {
        return this.padding.getBlock() + this.margin.getBlock();
    }

    @Override
    public int getWidth() {
        return Math.max(this.minWidth, this.contentSize.width + getInline());
    }

    @Override
    public int getHeight() {
        return Math.max(this.minHeight, this.contentSize.height + getBlock());
    }

    @Override
    public int getSpacing() {
        return this.spacing;
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        int width = Math.max(getWidth(), availableWidth);
        int height = getHeight();

        if (this.transform != null && this.transform.isAnimated()) {
            this.transform.pushTransformMatrix(x, y, width, height, context.getAnimationStartTime());
            x = y = 0;
        }

        x += this.margin.getLeft();
        y += this.margin.getTop();
        width -= this.margin.getInline();
        height -= this.margin.getBlock();

        if (this.decorators != null) {
            this.decorators.draw(x, y, width, height, context, 0xFFFFFFFF);
        }

        x += this.padding.getLeft();
        y += this.padding.getTop();
        width -= this.padding.getInline();
        height -= this.padding.getBlock();

        if (this.alignInline == TooltipAlign.END) {
            x += width - this.contentSize.width;
            width = this.contentSize.width;
        } else if (this.alignInline == TooltipAlign.CENTER) {
            x += (width - this.contentSize.width) / 2d;
            width = this.contentSize.width;
        }

        if (this.alignBlock == TooltipAlign.END) {
            y += height - this.contentSize.height;
            height = this.contentSize.height;
        } else if (this.alignBlock == TooltipAlign.CENTER) {
            y += (height - this.contentSize.height) / 2d;
            height = this.contentSize.height;
        }

        this.fontContext.pushContext();
        drawContent(x, y, width, height, context);
        this.fontContext.popContext();

        if (this.transform != null && this.transform.isAnimated()) {
            this.transform.popTransformMatrix();
        }
    }

    protected void drawContent(int x, int y, int width, int height, TooltipContext context) {
        // Override in subclasses
    }

}
