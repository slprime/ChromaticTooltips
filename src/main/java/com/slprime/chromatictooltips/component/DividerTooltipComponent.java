package com.slprime.chromatictooltips.component;

import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class DividerTooltipComponent extends SpaceTooltipComponent {

    protected int colorCodeIndex = -1;

    public DividerTooltipComponent(SpaceTooltipComponent spaceComponent, int colorCodeIndex) {
        super(spaceComponent.height, spaceComponent.decorators);
        this.colorCodeIndex = colorCodeIndex;
    }

    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        if (this.decorators != null) {
            final int color = (0xFF << 24) | (TooltipFontContext.getColor(this.colorCodeIndex) & 0xFFFFFF);
            this.decorators.draw(x, y, availableWidth, this.height, context, color);
        }
    }

    @Override
    public String toString() {
        return "DividerTooltipComponent{height=" + this.height
            + (this.decorators != null ? ", decorators=" + this.decorators : "")
            + ", colorCodeIndex="
            + this.colorCodeIndex
            + "}";
    }

}
