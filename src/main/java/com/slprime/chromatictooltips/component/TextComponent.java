package com.slprime.chromatictooltips.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class TextComponent implements ITooltipComponent {

    protected List<String> textLines = new ArrayList<>();
    protected int spacing = 2;

    public TextComponent(List<String> textLines, int spacing) {
        this.spacing = spacing;

        for (String line : textLines) {
            this.textLines.addAll(Arrays.asList(line.split("\n")));
        }

    }

    public TextComponent(List<String> textLines) {
        this(textLines, TooltipFontContext.DEFAULT_SPACING);
    }

    public TextComponent(String text, int spacing) {
        this(Arrays.asList(text), spacing);
    }

    public TextComponent(String text) {
        this(Arrays.asList(text));
    }

    public List<String> getLines() {
        return new ArrayList<>(this.textLines);
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        final int linesPerPage = Math.max(1, maxHeight / TooltipFontContext.getFontHeight());
        final List<String> lines = new ArrayList<>();

        for (String line : this.textLines) {
            lines.addAll(TooltipFontContext.listFormattedStringToWidth(line, maxWidth));
        }

        if (lines.size() == this.textLines.size()) {
            return new ITooltipComponent[] { this };
        }

        if (linesPerPage >= lines.size()) {
            return new ITooltipComponent[] { createInstance(lines) };
        }

        return new ITooltipComponent[] { createInstance(lines.subList(0, linesPerPage)),
            createInstance(lines.subList(linesPerPage, lines.size())) };
    }

    protected ITooltipComponent createInstance(List<String> lines) {
        return new TextComponent(lines, spacing);
    }

    @Override
    public int getWidth() {
        int width = 0;

        for (String line : this.textLines) {
            width = Math.max(width, TooltipFontContext.getStringWidth(line));
        }

        return width;
    }

    @Override
    public int getHeight() {
        return this.textLines.size() * TooltipFontContext.getFontHeight() - this.spacing;
    }

    @Override
    public int getSpacing() {
        return this.spacing;
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        final int lineHeight = TooltipFontContext.getFontHeight();

        for (String line : this.textLines) {
            context.drawString(line, x, y);
            y += lineHeight;
        }

    }

    @Override
    public int hashCode() {
        return this.textLines.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof TextComponent other) {
            return this.textLines.equals(other.textLines);
        }

        return false;
    }

}
