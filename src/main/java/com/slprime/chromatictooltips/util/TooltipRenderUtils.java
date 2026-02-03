package com.slprime.chromatictooltips.util;

import java.awt.Rectangle;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.slprime.chromatictooltips.ChromaticTooltips;

public class TooltipRenderUtils {

    private static final Rectangle ICON_RECTANGLE = new Rectangle(0, 0, 16, 16);

    public enum Alignment {

        TopLeft(-1, -1),
        TopCenter(0, -1),
        TopRight(1, -1),
        CenterLeft(-1, 0),
        Center(0, 0),
        CenterRight(1, 0),
        BottomLeft(-1, 1),
        BottomCenter(0, 1),
        BottomRight(1, 1);

        public final int x, y;

        private Alignment(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public float getX(float parentWidth, float childWidth) {
            final float x = (this.x + 1) / 2f;
            return parentWidth * x - childWidth * x;
        }

        public float getY(float parentHeight, float childHeight) {
            final float y = (this.y + 1) / 2f;
            return parentHeight * y - childHeight * y;
        }
    }

    private TooltipRenderUtils() {}

    public static void gl2DRenderContext(Runnable callback) {
        boolean isLighting = GL11.glGetBoolean(GL11.GL_LIGHTING);
        boolean isDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        boolean isAlphaTest = GL11.glGetBoolean(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        callback.run();

        if (isLighting) GL11.glEnable(GL11.GL_LIGHTING);
        if (isDepthTest) GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (!isAlphaTest) GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

    public static void drawOverlayText(String text, Rectangle rect, float scale, int color, boolean shadow,
        Alignment alignment) {
        final float screenScale = TooltipUtils.getScaledResolution()
            .getScaleFactor();
        final double smallTextScale = Math.max(
            screenScale,
            (Math.max(scale, 1f) * (TooltipFontContext.getFontRenderer()
                .getUnicodeFlag() ? 3F / 4F : 1F / 2F)));

        gl2DRenderContext(() -> {
            final int width = TooltipFontContext.getStringWidth(text);
            final float partW = rect.width / 2f;
            final float partH = rect.height / 2f;
            final double offsetX = Math
                .ceil(rect.x + partW + partW * alignment.x - (width / 2f * (alignment.x + 1)) * smallTextScale);
            final double offsetY = Math.ceil(
                rect.y + partH
                    + partH * alignment.y
                    - (TooltipFontContext.getFontRenderer().FONT_HEIGHT / 2f * (alignment.y + 1)) * smallTextScale);

            GL11.glTranslated(offsetX, offsetY, 0);
            GL11.glScaled(smallTextScale, smallTextScale, 1);

            TooltipFontContext.drawString(text, 0, 0, color, shadow);

            GL11.glScaled(1 / smallTextScale, 1 / smallTextScale, 1);
            GL11.glTranslated(-1 * offsetX, -1 * offsetY, 0);
        });
    }

    public static void drawItemStack(ItemStack stack, long stackAmount, boolean showOverlayInfo) {
        final FontRenderer font = TooltipFontContext.getFontRenderer();
        final RenderItem itemRender = TooltipUtils.getItemRenderer();
        final TextureManager renderEngine = TooltipUtils.mc()
            .getTextureManager();

        drawIromIcon(() -> {
            itemRender.zLevel += 100.0F;
            itemRender.renderItemAndEffectIntoGUI(font, renderEngine, stack, 0, 0);

            if (showOverlayInfo) {
                itemRender.renderItemOverlayIntoGUI(font, renderEngine, stack, 0, 0, "");
            }

            itemRender.zLevel -= 100.0F;
        }, stackAmount, false);
    }

    public static void drawFluidStack(FluidStack stack, long stackAmount) {
        final Fluid fluid = stack.getFluid();
        final IIcon fluidIcon = fluid.getIcon(stack);
        final int fluidColor = fluid.getColor(stack);

        if (fluidIcon == null) {
            return;
        }

        drawIromIcon(() -> {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            TooltipUtils.bindTexture(TextureMap.locationBlocksTexture);

            float a = (fluidColor >> 24 & 255) / 255F;
            float r = (fluidColor >> 16 & 255) / 255F;
            float g = (fluidColor >> 8 & 255) / 255F;
            float b = (fluidColor & 255) / 255F;
            a = a == 0f ? 1f : a;

            GL11.glColor4f(r, g, b, a);

            TooltipUtils.mc().currentScreen.drawTexturedModelRectFromIcon(0, 0, fluidIcon, 16, 16);
            GL11.glColor3f(1f, 1f, 1f);

        }, stackAmount, true);

    }

    protected static void drawIromIcon(Runnable drawIcon, long stackAmount, boolean fluid) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        RenderHelper.enableGUIStandardItemLighting();

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        try {
            drawIcon.run();
        } catch (Exception e) {
            ChromaticTooltips.LOG.error("renderStack Item/Fluid Stack Error", e);
        }

        if (stackAmount > 1) {
            drawStackSize(stackAmount, fluid);
        }

        GL11.glPopAttrib();
    }

    protected static void drawStackSize(long stackAmount, boolean isFluid) {
        String amountString = "";

        if (stackAmount < 10_000) {
            amountString = String.valueOf(stackAmount);
        } else {
            amountString = NumberFormat.SI.format(stackAmount);
        }

        if (isFluid) {
            amountString += "L";
        }

        drawOverlayText(
            amountString,
            ICON_RECTANGLE,
            1f,
            0xFFFFFF,
            true,
            isFluid ? Alignment.BottomLeft : Alignment.BottomRight);
    }

}
