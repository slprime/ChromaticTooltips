package com.slprime.chromatictooltips.component;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class DividerTooltipComponent extends SpaceTooltipComponent {

    protected int colorCodeIndex = -1;
    protected int marginLeft = 0;

    public DividerTooltipComponent(SpaceTooltipComponent space, int marginLeft, int colorCodeIndex) {
        super(space);
        this.colorCodeIndex = colorCodeIndex;
        this.marginLeft = marginLeft;
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        this.mixColor = (0xFF << 24) | TooltipFontContext.getColor(this.colorCodeIndex);
        return new ITooltipComponent[] { this };
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        super.draw(x + this.marginLeft, y, availableWidth - this.marginLeft, context);
    }

    @Override
    public String toString() {
        return "DividerTooltipComponent{height=" + this.height + ", colorCodeIndex=" + this.colorCodeIndex + "}";
    }

}
