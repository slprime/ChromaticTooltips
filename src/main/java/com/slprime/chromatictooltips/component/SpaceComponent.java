package com.slprime.chromatictooltips.component;

import java.util.Objects;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.util.TooltipDecoratorCollection;
import com.slprime.chromatictooltips.util.TooltipTransform;

public class SpaceComponent implements ITooltipComponent {

    protected TooltipDecoratorCollection decorators;
    protected TooltipTransform transform;
    protected int mixColor = 0xFFFFFFFF;
    protected int height;

    public SpaceComponent(TooltipStyle style) {
        this.decorators = style.getDecoratorCollection();
        this.height = style.getAsInt("height", 0);

        if (style.containsKey("transform")) {
            this.transform = new TooltipTransform(style.getAsStyle("transform"));
        }
    }

    public SpaceComponent(int height) {
        this.height = height;
    }

    public SpaceComponent(SpaceComponent space) {
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
    public int hashCode() {
        return Objects
            .hash(this.height, this.decorators == null || this.decorators.isEmpty(), this.transform, this.mixColor);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof SpaceComponent other) {
            return this.height == other.height
                && (this.decorators == null || this.decorators.isEmpty())
                    == (other.decorators == null || other.decorators.isEmpty())
                && Objects.equals(this.transform, other.transform)
                && this.mixColor == other.mixColor;
        }

        return false;
    }

    @Override
    public String toString() {
        return "SpaceTooltipComponent{height=" + this.height
            + (this.decorators != null ? ", decorators=" + this.decorators : "")
            + "}";
    }

}
