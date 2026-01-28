package com.slprime.chromatictooltips.api;

import java.util.regex.Matcher;

public interface ITooltipLineConverter {

    public ITooltipComponent convert(Matcher matcher, TooltipContext context);

}
