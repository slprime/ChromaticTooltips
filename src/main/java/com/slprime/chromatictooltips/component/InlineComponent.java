package com.slprime.chromatictooltips.component;

import java.util.ArrayList;
import java.util.List;

import org.joml.Math;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class InlineComponent implements ITooltipComponent {

    protected List<List<ITooltipComponent>> lines;
    protected int inlineGap = 0;
    protected int blockGap = 0;

    public InlineComponent(List<List<ITooltipComponent>> lines, int inlineGap, int blockGap) {
        this.lines = lines;
        this.inlineGap = inlineGap;
        this.blockGap = blockGap;
    }

    protected List<List<ITooltipComponent>> generateLines(List<List<ITooltipComponent>> rawLines, int maxWidth) {
        final List<List<ITooltipComponent>> lines = new ArrayList<>();
        List<ITooltipComponent> currentLine = new ArrayList<>();
        int lineWidth = 0;

        for (List<ITooltipComponent> rawLine : rawLines) {
            for (ITooltipComponent component : rawLine) {

                if (lineWidth > 0 && lineWidth + component.getWidth() > maxWidth) {
                    lines.add(currentLine);
                    currentLine = new ArrayList<>();
                    lineWidth = 0;
                }

                lineWidth += component.getWidth() + this.inlineGap;
                currentLine.add(component);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }

        return lines;
    }

    @Override
    public int getWidth() {
        int width = 0;

        for (List<ITooltipComponent> line : this.lines) {
            int lineWidth = -this.inlineGap;

            for (ITooltipComponent component : line) {
                lineWidth += component.getWidth() + this.inlineGap;
            }

            width = Math.max(width, lineWidth);
        }

        return width;
    }

    @Override
    public int getHeight() {
        int height = 0;
        int lastSpacing = 0;

        for (List<ITooltipComponent> line : this.lines) {
            int lineSpacing = 0;
            int lineHeight = 0;

            for (ITooltipComponent component : line) {
                lineHeight = Math.max(lineHeight, component.getHeight());
                lineSpacing = Math.max(lineSpacing, component.getSpacing());
            }

            height += lineHeight + lastSpacing;
            lastSpacing = this.blockGap + lineSpacing;
        }

        return Math.max(0, height);
    }

    @Override
    public int getSpacing() {
        return TooltipFontContext.DEFAULT_SPACING;
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        final List<List<ITooltipComponent>> firstComponent = new ArrayList<>();
        final List<List<ITooltipComponent>> secondComponent = new ArrayList<>();

        int height = 0;
        int lastSpacing = 0;

        for (List<ITooltipComponent> line : generateLines(this.lines, maxWidth)) {
            int lineSpacing = 0;
            int lineHeight = 0;

            for (ITooltipComponent component : line) {
                lineHeight = Math.max(lineHeight, component.getHeight());
                lineSpacing = Math.max(lineSpacing, component.getSpacing());
            }

            if (height + lineHeight + lastSpacing > maxHeight && !firstComponent.isEmpty()) {
                secondComponent.add(line);
            } else {
                firstComponent.add(line);
            }

            height += lineHeight + lastSpacing;
            lastSpacing = this.blockGap + lineSpacing;
        }

        if (secondComponent.isEmpty()) {
            return new ITooltipComponent[] { new InlineComponent(firstComponent, this.inlineGap, this.blockGap) };
        }

        return new ITooltipComponent[] { new InlineComponent(firstComponent, this.inlineGap, this.blockGap),
            new InlineComponent(secondComponent, this.inlineGap, this.blockGap) };
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        int height = 0;

        for (List<ITooltipComponent> line : this.lines) {
            int lineWidth = 0;
            int lineSpacing = 0;
            int lineHeight = 0;

            for (ITooltipComponent component : line) {
                component.draw(x + lineWidth, y + height, availableWidth - lineWidth, context);
                lineWidth += component.getWidth() + this.inlineGap;
                lineHeight = Math.max(lineHeight, component.getHeight());
                lineSpacing = Math.max(lineSpacing, component.getSpacing());
            }

            height += lineHeight + this.blockGap + lineSpacing;
        }

    }

    @Override
    public int hashCode() {
        return this.lines.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof InlineComponent other) {
            return this.lines.equals(other.lines);
        }

        return false;
    }
}
