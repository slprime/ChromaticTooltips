package com.slprime.chromatictooltips.api;

public interface ITooltipComponent {

    public int getWidth();

    public int getHeight();

    public int getSpacing();

    default ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        return new ITooltipComponent[] { this };
    }

    public void draw(int x, int y, int availableWidth, TooltipContext context);

}
