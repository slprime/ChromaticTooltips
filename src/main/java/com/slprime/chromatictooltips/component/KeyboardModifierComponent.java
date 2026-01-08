package com.slprime.chromatictooltips.component;

import java.util.Arrays;
import java.util.EnumSet;

import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class KeyboardModifierComponent implements ITooltipComponent {

    protected EnumSet<TooltipModifier> supportedModifiers;
    protected TooltipModifier activeModifier;
    protected double scaleFactor;
    protected String text = "";
    protected int width = 0;
    protected int height = 0;

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

        final int tooltipScale = ClientUtil.getTooltipScale();

        this.text = this.text.trim();
        this.scaleFactor = Math.ceil(tooltipScale / 2f) / tooltipScale;
        this.height = (int) ((TooltipFontContext.getFontRenderer().FONT_HEIGHT - 1) * this.scaleFactor);
        this.width = (int) (TooltipFontContext.getStringWidth(this.text) * this.scaleFactor);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
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
        GL11.glTranslatef(x, y, 0);
        GL11.glScaled(this.scaleFactor, this.scaleFactor, 1);

        TooltipFontContext.drawString(this.text, 0, 0);

        GL11.glScaled(1d / this.scaleFactor, 1d / this.scaleFactor, 1);
        GL11.glTranslatef(-x, -y, 0);
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
