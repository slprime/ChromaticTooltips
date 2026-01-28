package com.slprime.chromatictooltips.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipLineConverter;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipModifier;

public class ModifierConverter implements ITooltipLineConverter {

    public static final Pattern PATTERN = Pattern.compile("^§!(alt|ctrl|shift)$", Pattern.CASE_INSENSITIVE);

    @Override
    public ITooltipComponent convert(Matcher matcher, TooltipContext context) {
        final String mod = matcher.group(1)
            .toLowerCase();

        if ("shift".equals(mod)) {
            context.supportModifiers(TooltipModifier.SHIFT);
        } else if ("ctrl".equals(mod)) {
            context.supportModifiers(TooltipModifier.CTRL);
        } else if ("alt".equals(mod)) {
            context.supportModifiers(TooltipModifier.ALT);
        }

        return null;
    }

}
