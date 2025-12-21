package com.slprime.chromatictooltips.component;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.util.TooltipDecoratorCollection;

public class SpaceTooltipComponent implements ITooltipComponent {

    protected TooltipDecoratorCollection decorators;
    protected int height;

    public SpaceTooltipComponent(int height) {
        this(height, null);
    }

    public SpaceTooltipComponent(int height, TooltipDecoratorCollection decorators) {
        this.height = height;
        this.decorators = decorators;
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
            this.decorators.draw(x, y, availableWidth, this.height, context, 0xffffffff);
        }
    }

    @Override
    public String toString() {
        return "SpaceTooltipComponent{height=" + this.height
            + (this.decorators != null ? ", decorators=" + this.decorators : "")
            + "}";
    }

}
