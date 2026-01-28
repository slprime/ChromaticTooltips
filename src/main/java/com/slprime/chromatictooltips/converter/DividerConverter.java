package com.slprime.chromatictooltips.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipLineConverter;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.component.DividerComponent;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class DividerConverter implements ITooltipLineConverter {

    public static final Pattern PATTERN = Pattern
        .compile("^(\\s*)(?:§[0-9a-fk-or])*§([0-9a-f])(?:§[0-9a-fk-or])*-{3,}(?:§r)?$", Pattern.CASE_INSENSITIVE);

    @Override
    public ITooltipComponent convert(Matcher matcher, TooltipContext context) {

        if (matcher.matches()) {
            final String colorCode = matcher.group(2);
            final int colorCodeIndex = "0123456789abcdef".indexOf(colorCode.toLowerCase());
            final int marginLeft = TooltipFontContext.getStringWidth(matcher.group(1));

            return new DividerComponent(
                context.getRenderer()
                    .getSpacing("divider"),
                marginLeft,
                colorCodeIndex);
        }

        return null;
    }

}
