package com.slprime.chromatictooltips.event;

import java.awt.Rectangle;

import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.component.SectionComponent;

public class RenderTooltipEvent extends TooltipEvent {

    public final SectionComponent page;
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public RenderTooltipEvent(TooltipContext context, SectionComponent page, Rectangle position) {
        super(context);
        this.page = page;

        this.x = position.x;
        this.y = position.y;
        this.width = position.width;
        this.height = position.height;
    }

}
