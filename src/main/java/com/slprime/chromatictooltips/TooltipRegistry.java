package com.slprime.chromatictooltips;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;

import com.slprime.chromatictooltips.api.ITargetSanitizer;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.ITooltipLineConverter;
import com.slprime.chromatictooltips.api.ITooltipRequestResolver;
import com.slprime.chromatictooltips.api.TooltipRequest;
import com.slprime.chromatictooltips.api.TooltipTarget;

public class TooltipRegistry {

    protected static final List<ITooltipEnricher> tooltipEnrichers = new ArrayList<>();
    protected static final List<ITooltipRequestResolver> requestResolvers = new ArrayList<>();
    protected static final List<ITargetSanitizer> targetSanitizers = new ArrayList<>();
    protected static final List<Map.Entry<Pattern, ITooltipLineConverter>> lineConverters = new ArrayList<>();

    private TooltipRegistry() {}

    public static void addRequestResolver(ITooltipRequestResolver resolver) {
        TooltipRegistry.requestResolvers.add(resolver);
    }

    public static TooltipRequest resolveRequest(TooltipRequest request) {
        request = new TooltipRequest(request, sanitizeTarget(request.target));

        for (ITooltipRequestResolver resolver : TooltipRegistry.requestResolvers) {
            if (resolver.resolve(request)) {
                break;
            }
        }

        return request;
    }

    public static void addLineConverter(Pattern regexp, ITooltipLineConverter converter) {
        TooltipRegistry.lineConverters.add(new AbstractMap.SimpleEntry<>(regexp, converter));
    }

    public static Map.Entry<Matcher, ITooltipLineConverter> getLineConverter(String line) {
        for (Map.Entry<Pattern, ITooltipLineConverter> entry : TooltipRegistry.lineConverters) {
            final Matcher matcher = entry.getKey()
                .matcher(line);

            if (matcher.matches()) {
                return new AbstractMap.SimpleEntry<>(matcher, entry.getValue());
            }
        }

        return null;
    }

    public static void addTargetSanitizer(ITargetSanitizer sanitizer) {
        TooltipRegistry.targetSanitizers.add(sanitizer);
    }

    public static TooltipTarget sanitizeTarget(TooltipTarget target) {
        if (target == null) return null;

        for (ITargetSanitizer sanitizer : TooltipRegistry.targetSanitizers) {
            target = sanitizer.sanitize(target);
        }

        return target;
    }

    public static TooltipTarget createTargetFromItemStack(ItemStack stack) {
        return sanitizeTarget(TooltipTarget.ofItem(stack));
    }

    public static void addEnricher(ITooltipEnricher enricher) {
        TooltipRegistry.tooltipEnrichers.add(enricher);
    }

    public static void addEnricherAfter(String sectionId, ITooltipEnricher enricher) {
        int index = -1;

        for (int i = 0; i < TooltipRegistry.tooltipEnrichers.size(); i++) {
            if (TooltipRegistry.tooltipEnrichers.get(i)
                .sectionId()
                .equalsIgnoreCase(sectionId)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            TooltipRegistry.tooltipEnrichers.add(index + 1, enricher);
        } else {
            TooltipRegistry.tooltipEnrichers.add(enricher);
        }
    }

    public static List<ITooltipEnricher> getEnrichers() {
        return Collections.unmodifiableList(TooltipRegistry.tooltipEnrichers);
    }

}
