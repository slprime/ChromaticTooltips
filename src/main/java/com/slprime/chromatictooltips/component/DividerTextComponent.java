package com.slprime.chromatictooltips.component;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class DividerTextComponent extends SpaceComponent {

    protected int colorCodeIndex = -1;
    protected int marginLeft = 0;
    protected String text;
    protected SpaceComponent right;

    public DividerTextComponent(SpaceComponent left, SpaceComponent right, int height, int marginLeft,
        int colorCodeIndex, String text) {
        super(left);
        this.height = height;
        this.right = right;
        this.colorCodeIndex = colorCodeIndex;
        this.marginLeft = marginLeft;
        this.text = text;
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        this.mixColor = (0xFF << 24) | TooltipFontContext.getColor(this.colorCodeIndex);
        this.right.mixColor = this.mixColor;
        return new ITooltipComponent[] { this };
    }

    @Override
    public int getWidth() {
        return this.marginLeft + TooltipFontContext.getStringWidth(this.text);
    }

    @Override
    public int getHeight() {
        return Math.max(this.height, TooltipFontContext.getFontHeight());
    }

    protected void drawSegment(SpaceComponent space, int x, int y, int width, int height, TooltipContext context) {

        if (space.decorators == null || width <= 0) {
            return;
        }

        if (space.transform != null && space.transform.isAnimated()) {
            space.transform.pushTransformMatrix(x, y, width, height, context.getAnimationStartTime());
            x = y = 0;
        }

        space.decorators.draw(x, y, width, height, context, space.mixColor);

        if (space.transform != null && space.transform.isAnimated()) {
            space.transform.popTransformMatrix();
        }
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        x += this.marginLeft;
        availableWidth -= this.marginLeft;

        final int blockHeight = getHeight();
        final int textWidth = TooltipFontContext.getStringWidth(this.text);
        final int lineWidth = Math.max(0, (availableWidth - textWidth) / 2);

        drawSegment(this, x, y, lineWidth, blockHeight, context);
        drawSegment(this.right, x + availableWidth - lineWidth, y, lineWidth, blockHeight, context);

        final int textY = y + (blockHeight - TooltipFontContext.getFontHeight()) / 2;
        TooltipFontContext.drawString(this.text, x + lineWidth, textY, this.mixColor);
    }

    @Override
    public String toString() {
        return "DividerTextTooltipComponent{height=" + this.height
            + ", colorCodeIndex="
            + this.colorCodeIndex
            + ", text="
            + this.text
            + "}";
    }

}
