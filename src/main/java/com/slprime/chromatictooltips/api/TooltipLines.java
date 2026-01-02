package com.slprime.chromatictooltips.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.EnumChatFormatting;

import com.slprime.chromatictooltips.TooltipHandler;
import com.slprime.chromatictooltips.component.DividerComponent;
import com.slprime.chromatictooltips.component.ParagraphComponent;
import com.slprime.chromatictooltips.component.SpaceComponent;
import com.slprime.chromatictooltips.component.TextComponent;
import com.slprime.chromatictooltips.event.TextLinesConverterEvent;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class TooltipLines {

    protected static final Pattern DIVIDER_PATTERN = Pattern
        .compile("^(\\s*)(?:§[0-9a-fk-or])*§([0-9a-f])(?:§[0-9a-fk-or])*-{3,}(?:§r)?$", Pattern.CASE_INSENSITIVE);
    public static final EnumChatFormatting BASE_COLOR = EnumChatFormatting.GRAY;
    protected static final String HEADER_SUFFIX = "§h";
    protected static final int HEADER_SPACING = 4;

    private final List<Object> textLines = new ArrayList<>();

    public TooltipLines() {}

    public TooltipLines(List<?> lines) {
        lines(lines);
    }

    public TooltipLines header(String line) {
        this.textLines.add(line + HEADER_SUFFIX);
        return this;
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

    public TooltipLines lines(List<?> lines) {

        if (lines != null) {
            for (Object line : lines) {
                if (line != null) {
                    this.textLines.add(line);
                }
            }
        }

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
                results.add(new ParagraphComponent());
            } else if (line instanceof String str && !ClientUtil.isBlacklistedLine(str)) {
                final Matcher matcher = DIVIDER_PATTERN.matcher(str);

                if (matcher.matches()) {
                    final String colorCode = matcher.group(2);
                    final int colorCodeIndex = "0123456789abcdef".indexOf(colorCode.toLowerCase());
                    final int marginLeft = TooltipFontContext.getStringWidth(matcher.group(1));

                    results.add(new DividerComponent(renderer.getSpacing("divider"), marginLeft, colorCodeIndex));
                } else if (str.endsWith(HEADER_SUFFIX)) {
                    results.add(
                        new TextComponent(
                            ClientUtil.applyBaseColorIfAbsent(
                                str.substring(0, str.length() - HEADER_SUFFIX.length()),
                                BASE_COLOR),
                            HEADER_SPACING));
                } else {
                    ITooltipComponent component = TooltipHandler.getTooltipComponent(str);

                    if (component == null) {
                        component = new TextComponent(ClientUtil.applyBaseColorIfAbsent(str, BASE_COLOR));
                    }

                    results.add(component);
                }

            }

        }

        while (!results.isEmpty() && results.get(0) instanceof SpaceComponent) {
            results.remove(0);
        }

        while (!results.isEmpty() && results.get(results.size() - 1) instanceof SpaceComponent) {
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
