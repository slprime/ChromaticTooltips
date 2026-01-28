package com.slprime.chromatictooltips.api;

import java.util.EnumSet;
import java.util.List;

import com.slprime.chromatictooltips.component.SectionComponent;
import com.slprime.chromatictooltips.component.SpaceComponent;
import com.slprime.chromatictooltips.util.SectionBox;

public interface ITooltipRenderer {

    public static final int DEFAULT_Z_INDEX = 300;

    public TooltipStyle getStyle();

    public int getMainAxisOffset();

    public int getCrossAxisOffset();

    public SectionBox getSectionBox(String path);

    public SpaceComponent getSpacing(String path);

    public EnumSet<TooltipModifier> getEnricherModes(String enricherId, EnumSet<TooltipModifier> defaultModes);

    public EnricherPlace getEnricherPlace(String enricherId, EnricherPlace defaultPlace);

    public boolean matches(TooltipTarget target);

    public List<SectionComponent> paginateTooltip(TooltipContext context);

    public void draw(TooltipContext context, int x, int y);
}
