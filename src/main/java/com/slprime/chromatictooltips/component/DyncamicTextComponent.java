package com.slprime.chromatictooltips.component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.util.EnumChatFormatting;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class DyncamicTextComponent implements ITooltipComponent {

    protected final Supplier<String> handler;
    protected int maxWidth = Integer.MAX_VALUE;
    protected int lineIndex = 0;
    protected int spacing = 2;

    protected DyncamicTextComponent(Supplier<String> handler, int lineIndex, int maxWidth, int spacing) {
        this.handler = handler;
        this.maxWidth = maxWidth;
        this.lineIndex = lineIndex;
        this.spacing = spacing;
    }

    public DyncamicTextComponent(Supplier<String> handler, int spacing) {
        this(handler, 0, Integer.MAX_VALUE, spacing);
    }

    public DyncamicTextComponent(Supplier<String> handler) {
        this(handler, TooltipFontContext.DEFAULT_SPACING);
    }

    @Override
    public int getWidth() {
        int width = 0;

        for (String line : generateLines(this.maxWidth)) {
            width = Math.max(width, TooltipFontContext.getStringWidth(line));
        }

        return width;
    }

    @Override
    public int getHeight() {
        return generateLines(this.maxWidth).size() * TooltipFontContext.getFontHeight() - this.spacing;
    }

    @Override
    public int getSpacing() {
        return this.spacing;
    }

    public Supplier<String> getHandler() {
        return this.handler;
    }

    protected List<String> generateLines(int maxWidth) {
        final List<String> lines = new ArrayList<>();

        for (String line : this.handler.get()
            .split("\n")) {
            lines.addAll(TooltipFontContext.listFormattedStringToWidth(line, maxWidth));
        }

        return lines;
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        final int linesPerPage = Math.max(1, maxHeight / TooltipFontContext.getFontHeight());
        final List<String> lines = generateLines(maxWidth);

        if (linesPerPage >= lines.size()) {
            return new ITooltipComponent[] { new DyncamicTextComponent(this.handler, 0, maxWidth, this.spacing) };
        }

        return new ITooltipComponent[] { new DyncamicTextComponent(this.handler, 0, maxWidth, this.spacing),
            new DyncamicTextComponent(this.handler, linesPerPage, maxWidth, this.spacing) };
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        final int lineHeight = TooltipFontContext.getFontHeight();
        final List<String> lines = generateLines(this.maxWidth);

        for (int i = this.lineIndex; i < lines.size(); i++) {
            context.drawString(ClientUtil.applyBaseColorIfAbsent(lines.get(i), TooltipLines.BASE_COLOR), x, y);
            y += lineHeight;
        }

    }

    @Override
    public int hashCode() {
        return EnumChatFormatting.getTextWithoutFormattingCodes(this.handler.get())
            .hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof DyncamicTextComponent other) {
            return EnumChatFormatting.getTextWithoutFormattingCodes(this.handler.get())
                .equals(EnumChatFormatting.getTextWithoutFormattingCodes(other.handler.get()));
        }

        return false;
    }

}
