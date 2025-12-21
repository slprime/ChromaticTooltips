package com.slprime.chromatictooltips.util;

import com.slprime.chromatictooltips.api.TooltipContext;

public class TooltipDecoratorCollection {

    protected TooltipDecorator[] decorators;

    public TooltipDecoratorCollection(TooltipDecorator[] decorators) {
        this.decorators = decorators;
    }

    public boolean isEmpty() {
        return this.decorators.length == 0;
    }

    public void draw(int x, int y, int width, int height, TooltipContext context, int mixColor) {
        for (TooltipDecorator decorator : this.decorators) {
            decorator.draw(x, y, width, height, context, mixColor);
        }
    }
}
