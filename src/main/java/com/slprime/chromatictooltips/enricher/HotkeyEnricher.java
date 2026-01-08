package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.component.TextComponent;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.event.HotkeyEnricherEvent;
import com.slprime.chromatictooltips.util.ClientUtil;

public class HotkeyEnricher implements ITooltipEnricher {

    @Override
    public String sectionId() {
        return "hotkeys";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.ALT);
    }

    @Override
    public TooltipLines build(TooltipContext context) {

        if (!EnricherConfig.hotkeysEnabled) {
            return null;
        }

        return new TooltipLines(hotkeysListComponent(context));
    }

    protected TextComponent hotkeysListComponent(TooltipContext context) {
        final HotkeyEnricherEvent event = new HotkeyEnricherEvent(context, new HashMap<>());
        ClientUtil.postEvent(event);

        event.hotkeys.remove(null);
        event.hotkeys.remove("");

        if (!event.hotkeys.isEmpty()) {
            return new TextComponent(getHotkeyList(event.hotkeys));
        }

        return null;
    }

    protected List<String> getHotkeyList(Map<String, String> hotkeys) {
        final Map<String, List<String>> messages = new HashMap<>();

        for (Map.Entry<String, String> entry : hotkeys.entrySet()) {
            messages.computeIfAbsent(entry.getValue(), m -> new ArrayList<>())
                .add(entry.getKey());
        }

        for (List<String> keys : messages.values()) {
            Collections.sort(keys, (a, b) -> {
                if (a.length() != b.length()) {
                    return Integer.compare(a.length(), b.length());
                }
                return a.compareTo(b);
            });
        }

        return messages.entrySet()
            .stream()
            .sorted((a, b) -> {
                final String sa = String.join("/", a.getValue());
                final String sb = String.join("/", b.getValue());

                if (sa.length() != sb.length()) {
                    return Integer.compare(sa.length(), sb.length());
                }

                return sa.compareTo(sb);
            })
            .map(entry -> getHotkeyTip(entry.getValue(), entry.getKey()))
            .collect(Collectors.toList());
    }

    protected String getHotkeyTip(List<String> keys, String message) {
        return ClientUtil.translate(
            "enricher.hotkeys.keybind.entry",
            String.join(ClientUtil.translate("enricher.hotkeys.keybind.keys"), keys),
            message);
    }

}
