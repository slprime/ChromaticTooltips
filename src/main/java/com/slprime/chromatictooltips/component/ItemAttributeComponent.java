package com.slprime.chromatictooltips.component;

import java.util.Objects;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class ItemAttributeComponent implements ITooltipComponent {

    private final static int ICON_SIZE = 8;
    private final static int SPACE = 3;

    private final ResourceLocation resourceLocation;
    private final int marginLeft = ICON_SIZE + SPACE;
    private final String title;

    public ItemAttributeComponent(String path, String title) {
        this(new ResourceLocation(path.contains(":") ? path : ChromaticTooltips.MODID + ":" + path), title);
    }

    protected ItemAttributeComponent(ResourceLocation resourceLocation, String title) {
        this.resourceLocation = resourceLocation;
        this.title = title;
    }

    @Override
    public int getWidth() {
        return TooltipFontContext.getStringWidth(this.title) + this.marginLeft;
    }

    @Override
    public int getHeight() {
        return TooltipFontContext.getFontHeight() - getSpacing();
    }

    @Override
    public int getSpacing() {
        return TooltipFontContext.DEFAULT_SPACING;
    }

    @Override
    public void draw(int x, int y, int availableWidth, TooltipContext context) {
        drawIcon(x, y - 16f / 20f);
        context.drawString(this.title, x + this.marginLeft, y);
    }

    private void drawIcon(double x, double y) {
        final boolean hasBlend = GL11.glGetBoolean(GL11.GL_BLEND);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 1);
        ClientUtil.bindTexture(this.resourceLocation);

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
        return Objects.hash(this.title, this.resourceLocation.getResourcePath());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ItemAttributeComponent other) {
            return this.title.equals(other.title) && this.resourceLocation.getResourcePath()
                .equals(other.resourceLocation.getResourcePath());
        }

        return false;
    }

}
