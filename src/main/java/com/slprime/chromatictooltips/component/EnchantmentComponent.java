package com.slprime.chromatictooltips.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class EnchantmentComponent implements ITooltipComponent {

    private final static int ICON_SIZE = 8;
    private final static int SPACE = 3;

    private final ResourceLocation resourceLocation;
    private final int marginLeft;
    private final String title;
    private final List<String> hint;
    private int colorCodeIndex = 15;
    private int width = 0;
    private int height = 0;

    public EnchantmentComponent(String path, String title, List<String> hint) {
        this(new ResourceLocation(path.contains(":") ? path : ChromaticTooltips.MODID + ":" + path), title, hint);
    }

    protected EnchantmentComponent(ResourceLocation resourceLocation, String title, List<String> hint) {
        this.hint = EnricherConfig.enchantmentHintEnabled ? hint : Collections.emptyList();
        this.colorCodeIndex = ClientUtil.getColorCodeIndex(title);
        this.resourceLocation = resourceLocation;
        this.title = title;

        this.marginLeft = EnricherConfig.enchantmentIconsEnabled ? ICON_SIZE + SPACE : 0;
        this.width = Math.max(this.width, TooltipFontContext.getStringWidth(title));

        for (String line : hint) {
            this.width = Math.max(this.width, TooltipFontContext.getStringWidth(line));
        }

        this.width += this.marginLeft;
        this.height = (hint.size() + 1) * TooltipFontContext.getFontHeight() - getSpacing();
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
        return TooltipFontContext.DEFAULT_SPACING;
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {

        if (this.hint.isEmpty()) {
            return new ITooltipComponent[] { this };
        }

        final List<String> lines = new ArrayList<>();

        maxWidth -= this.marginLeft + TooltipFontContext.getStringWidth("  ");

        for (String line : this.hint) {
            boolean firstLine = true;
            for (String wrappedLine : TooltipFontContext.listFormattedStringToWidth(line, maxWidth)) {
                lines.add(
                    EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC.toString()
                        + (firstLine ? "- " : "  ")
                        + wrappedLine);
                firstLine = false;
            }
        }

        return new ITooltipComponent[] { new EnchantmentComponent(resourceLocation, this.title, lines) };
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        final int lineHeight = TooltipFontContext.getFontHeight();

        if (EnricherConfig.enchantmentIconsEnabled) {
            drawIcon(x, y - 16f / 20f);
            x += this.marginLeft;
        }

        context.drawString(this.title, x, y);
        y += lineHeight;

        for (String line : this.hint) {
            context.drawString(line, x, y);
            y += lineHeight;
        }
    }

    private void drawIcon(double x, double y) {
        final int color = TooltipFontContext.getColor(this.colorCodeIndex);
        final int shadow = TooltipFontContext.getColorShadow(this.colorCodeIndex);
        final boolean hasBlend = GL11.glGetBoolean(GL11.GL_BLEND);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ClientUtil.bindTexture(this.resourceLocation);

        GL11.glColor4f((shadow >> 16) / 255.0F, (shadow >> 8 & 255) / 255.0F, (shadow & 255) / 255.0F, 1);
        drawQuad(x + 1, y + 1, ICON_SIZE, ICON_SIZE);

        GL11.glColor4f((color >> 16) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1);
        drawQuad(x, y, ICON_SIZE, ICON_SIZE);

        if (!hasBlend) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    protected void drawQuad(double x, double y, double width, double height) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y, 0, 0, 0);
        tessellator.addVertexWithUV(x, y + height, 0, 0, 1);
        tessellator.addVertexWithUV(x + width, y + height, 0, 1, 1);
        tessellator.addVertexWithUV(x + width, y, 0, 1, 0);
        tessellator.draw();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.hint, this.resourceLocation.getResourcePath());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof EnchantmentComponent other) {
            return this.title.equals(other.title) && this.hint.equals(other.hint)
                && this.resourceLocation.getResourcePath()
                    .equals(other.resourceLocation.getResourcePath());
        }

        return false;
    }

}
