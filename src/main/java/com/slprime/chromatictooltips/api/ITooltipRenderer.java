package com.slprime.chromatictooltips.api;

import java.awt.Rectangle;
import java.util.EnumSet;

import net.minecraft.item.ItemStack;

import com.slprime.chromatictooltips.component.SpaceComponent;
import com.slprime.chromatictooltips.util.SectionBox;

public interface ITooltipRenderer {

    public static final int DEFAULT_Z_INDEX = 300;

    public TooltipStyle getStyle();

    public SectionBox getSectionBox(String path);

    public SpaceComponent getSpacing(String path);

    public EnumSet<TooltipModifier> getEnricherModes(String enricherId, EnumSet<TooltipModifier> defaultModes);

    public EnricherPlace getEnricherPlace(String enricherId, EnricherPlace defaultPlace);

    public boolean matches(ItemStack stack);

    public boolean nextTooltipPage();

    public boolean previousTooltipPage();

    public Rectangle getTooltipBounds(TooltipContext context);

    public void draw(TooltipContext context);
}
