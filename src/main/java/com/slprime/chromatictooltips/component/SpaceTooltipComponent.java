package com.slprime.chromatictooltips.component;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.util.TooltipDecoratorCollection;
import com.slprime.chromatictooltips.util.TooltipTransform;

public class SpaceTooltipComponent implements ITooltipComponent {

    protected TooltipDecoratorCollection decorators;
    protected TooltipTransform transform;
    protected int mixColor = 0xFFFFFFFF;
    protected int height;

    public SpaceTooltipComponent(TooltipStyle style) {
        this.decorators = style.getDecoratorCollection();
        this.height = style.getAsInt("height", 0);

        if (style.containsKey("transform")) {
            this.transform = new TooltipTransform(style.getAsStyle("transform"));
        }
    }

    public SpaceTooltipComponent(int height) {
        this.height = height;
    }

    public SpaceTooltipComponent(SpaceTooltipComponent space) {
        this.height = space.height;
        this.decorators = space.decorators;
        this.transform = space.transform;
        this.mixColor = space.mixColor;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getSpacing() {
        return 0;
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        if (this.decorators != null) {

            if (this.transform != null && this.transform.isAnimated()) {
                this.transform.pushTransformMatrix(x, y, availableWidth, this.height, context.getAnimationStartTime());
                x = y = 0;
            }

            this.decorators.draw(x, y, availableWidth, this.height, context, this.mixColor);

            if (this.transform != null && this.transform.isAnimated()) {
                this.transform.popTransformMatrix();
            }

        }
    }

    @Override
    public String toString() {
        return "SpaceTooltipComponent{height=" + this.height
            + (this.decorators != null ? ", decorators=" + this.decorators : "")
            + "}";
    }

}
