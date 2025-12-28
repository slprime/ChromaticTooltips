package com.slprime.chromatictooltips.component;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class ParagraphTooltipComponent extends SpaceTooltipComponent {

    public ParagraphTooltipComponent() {
        super(6);
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        this.height = TooltipFontContext.getParagraphSpacing();
        return new ITooltipComponent[] { this };
    }

    @Override
    public String toString() {
        return "ParagraphTooltipComponent";
    }

}
