package com.slprime.chromatictooltips.api;

import java.awt.Rectangle;

import net.minecraft.item.ItemStack;

import com.slprime.chromatictooltips.component.SpaceTooltipComponent;
import com.slprime.chromatictooltips.util.SectionBox;

public interface ITooltipRenderer {

    public static final int DEFAULT_Z_INDEX = 300;

    public TooltipStyle getStyle();

    public SectionBox getSectionBox(String path);

    public boolean matches(ItemStack stack);

    public boolean nextTooltipPage();

    public boolean previousTooltipPage();

    public SpaceTooltipComponent getSpacing(String key);

    public Rectangle getTooltipBounds(TooltipContext context);

    public void draw(TooltipContext context);
}
