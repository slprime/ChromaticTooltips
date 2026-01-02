package com.slprime.chromatictooltips.event;

import java.awt.Point;

import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.component.SectionComponent;
import com.slprime.chromatictooltips.util.TooltipSpacing;

public class RenderTooltipEvent extends TooltipEvent {

    public final SectionComponent page;
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public RenderTooltipEvent(TooltipContext context, SectionComponent page, Point position) {
        super(context);
        this.page = page;

        final TooltipSpacing margin = page.getMargin();

        this.x = position.x + margin.getLeft();
        this.y = position.y + margin.getTop();
        this.width = page.getWidth() - margin.getInline();
        this.height = page.getHeight() - margin.getBlock();
    }

}
