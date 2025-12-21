package com.slprime.chromatictooltips.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.EnumChatFormatting;

import com.slprime.chromatictooltips.TooltipHandler;
import com.slprime.chromatictooltips.component.DividerTooltipComponent;
import com.slprime.chromatictooltips.component.SpaceTooltipComponent;
import com.slprime.chromatictooltips.component.TextTooltipComponent;
import com.slprime.chromatictooltips.event.TextLinesConverterEvent;
import com.slprime.chromatictooltips.util.BlacklistLines;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class TooltipLines {

    protected static final Pattern DIVIDER_PATTERN = Pattern
        .compile("^(?:§[0-9a-fk-or])*§([0-9a-f])(?:§[0-9a-fk-or])*§m-+$", Pattern.CASE_INSENSITIVE);

    private final List<Object> textLines = new ArrayList<>();

    public TooltipLines() {}

    public TooltipLines(List<?> lines) {
        this.textLines.addAll(lines);
    }

    public TooltipLines header(String line) {
        return line(
            new TextTooltipComponent(line, TooltipFontContext.INHERIT_COLOR, TextTooltipComponent.DEFAULT_SPACING));
    }

    public TooltipLines line(ITooltipComponent line) {
        this.textLines.add(line);
        return this;
    }

    public TooltipLines line(String line) {
        this.textLines.add(line);
        return this;
    }

    public TooltipLines lines(String... lines) {
        return lines(Arrays.asList(lines));
    }

    public TooltipLines lines(List<String> lines) {
        this.textLines.addAll(lines);
        return this;
    }

    public TooltipLines paragraph() {
        this.textLines.add("");
        return this;
    }

    public TooltipLines divider() {
        return divider(EnumChatFormatting.GRAY);
    }

    public TooltipLines divider(EnumChatFormatting color) {
        this.textLines.add(color + "---" + EnumChatFormatting.RESET);
        return this;
    }

    public List<ITooltipComponent> buildComponents(TooltipContext context) {
        final List<ITooltipComponent> results = new ArrayList<>();
        final TextLinesConverterEvent event = new TextLinesConverterEvent(context, this.textLines);
        final ITooltipRenderer renderer = context.getRenderer();
        ClientUtil.postEvent(event);

        for (Object line : event.list) {

            if (line instanceof ITooltipComponent component) {
                results.add(component);
            } else if ("".equals(line)) {
                results.add(renderer.getSpacing("paragraph"));
            } else if (line instanceof String str
                && !BlacklistLines.test(EnumChatFormatting.getTextWithoutFormattingCodes(str))) {
                    str = str.startsWith("§7") ? str.substring(2)
                        .replaceAll("§7", "§r") : str; // Remove default gray color code
                    final Matcher matcher = DIVIDER_PATTERN.matcher(str);

                    if (matcher.matches()) {
                        final String colorCode = matcher.group(1);
                        final int colorCodeIndex = "0123456789abcdef".indexOf(colorCode.toLowerCase());

                        results.add(new DividerTooltipComponent(renderer.getSpacing("divider"), colorCodeIndex));
                    } else if (str.endsWith("§h")) {
                        results.add(
                            new TextTooltipComponent(
                                str.substring(0, str.length() - 2),
                                TooltipFontContext.INHERIT_COLOR,
                                TextTooltipComponent.DEFAULT_SPACING));
                    } else {
                        ITooltipComponent component = TooltipHandler.getTooltipComponent(str);

                        if (component == null) {
                            component = new TextTooltipComponent(str);
                        }

                        results.add(component);
                    }
                }

        }

        while (!results.isEmpty() && results.get(0) instanceof SpaceTooltipComponent) {
            results.remove(0);
        }

        while (!results.isEmpty() && results.get(results.size() - 1) instanceof SpaceTooltipComponent) {
            results.remove(results.size() - 1);
        }

        return results;
    }

    public boolean isEmpty() {
        return this.textLines.isEmpty();
    }

    public void clear() {
        this.textLines.clear();
    }

    public int size() {
        return this.textLines.size();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj instanceof TooltipLines other) {
            return equalsLines(this.textLines, other.textLines);
        }

        return false;
    }

    protected static boolean equalsLines(List<?> a, List<?> b) {
        if (a.size() != b.size()) return false;

        for (int i = 0; i < a.size(); i++) {
            final Object aObject = a.get(i);
            final Object bObject = b.get(i);

            if (!Objects.equals(aObject, bObject) && !String.valueOf(aObject)
                .equals(String.valueOf(bObject))) {
                return false;
            }
        }

        return true;
    }
}
