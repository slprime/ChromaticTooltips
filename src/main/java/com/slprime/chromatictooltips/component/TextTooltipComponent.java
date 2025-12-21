package com.slprime.chromatictooltips.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class TextTooltipComponent implements ITooltipComponent {

    public static final int LINE_SPACE = 1;
    public static final int DEFAULT_SPACING = 2;

    protected List<String> textLines = null;
    protected int color = TooltipFontContext.INHERIT_COLOR;
    protected int spacing = 2;

    protected int width = 0;
    protected int height = 0;

    public TextTooltipComponent(List<String> textLines, int color, int spacing) {
        this.textLines = textLines;
        this.spacing = spacing;
        this.color = color;

        for (String line : textLines) {
            this.width = Math.max(this.width, TooltipFontContext.getStringWidth(line));
        }

        this.height = textLines.size() * (TooltipFontContext.getFontRenderer().FONT_HEIGHT + LINE_SPACE) - this.spacing;
    }

    public TextTooltipComponent(List<String> textLines) {
        this(textLines, TooltipFontContext.INHERIT_COLOR, DEFAULT_SPACING);
    }

    public TextTooltipComponent(String text, int color) {
        this(Arrays.asList(text), color, DEFAULT_SPACING);
    }

    public TextTooltipComponent(String text, int color, int spacing) {
        this(Arrays.asList(text), color, spacing);
    }

    public TextTooltipComponent(String text) {
        this(Arrays.asList(text), TooltipFontContext.INHERIT_COLOR, DEFAULT_SPACING);
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        final int linesPerPage = Math
            .max(1, maxHeight / (TooltipFontContext.getFontRenderer().FONT_HEIGHT + LINE_SPACE));
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
        return new TextTooltipComponent(lines, color, spacing);
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getSpacing() {
        return this.spacing;
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        final int lineHeight = TooltipFontContext.getFontRenderer().FONT_HEIGHT + LINE_SPACE;

        for (String line : this.textLines) {
            context.drawString(line, x, y, this.color);
            y += lineHeight;
        }

    }

    @Override
    public int hashCode() {
        return this.textLines.hashCode();
    }

}
