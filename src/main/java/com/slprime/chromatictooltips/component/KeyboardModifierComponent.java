package com.slprime.chromatictooltips.component;

import java.util.Arrays;
import java.util.EnumSet;

import net.minecraft.util.EnumChatFormatting;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class KeyboardModifierComponent implements ITooltipComponent {

    protected EnumSet<TooltipModifier> supportedModifiers;
    protected TooltipModifier activeModifier;
    protected String text = "";

    public KeyboardModifierComponent(EnumSet<TooltipModifier> supportedModifiers, TooltipModifier activeModifier) {
        this.supportedModifiers = supportedModifiers;
        this.activeModifier = activeModifier;
        refreshText();
    }

    protected void refreshText() {
        this.text = EnumChatFormatting.DARK_GRAY.toString();

        for (TooltipModifier modifier : Arrays
            .asList(TooltipModifier.SHIFT, TooltipModifier.CTRL, TooltipModifier.ALT)) {
            if (this.supportedModifiers.contains(modifier)) {
                final String state = this.activeModifier == modifier ? "active" : "inactive";
                this.text += ClientUtil.translate(
                    "enricher.keyboard-modifier." + modifier.name()
                        .toLowerCase() + "." + state)
                    + " ";
            }
        }

        this.text = this.text.trim();
    }

    @Override
    public int getWidth() {
        return TooltipFontContext.getStringWidth(this.text);
    }

    @Override
    public int getHeight() {
        return this.supportedModifiers.isEmpty() ? 0 : TooltipFontContext.getFontRenderer().FONT_HEIGHT;
    }

    @Override
    public int getSpacing() {
        return 0;
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        refreshText();
        return new ITooltipComponent[] { this };
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        TooltipFontContext.drawString(this.text, x, y);
    }

    @Override
    public int hashCode() {
        return this.text.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof KeyboardModifierComponent other) {
            return this.text.equals(other.text);
        }

        return false;
    }

}
