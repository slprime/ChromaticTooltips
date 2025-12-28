package com.slprime.chromatictooltips.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;

public class TooltipDecorator {

    protected enum DecoratorType {

        NONE,
        BACKGROUND,
        TEXTURE,
        ITEM,
        BORDER,
        HORIZONTAL,
        VERTICAL;

        public static DecoratorType fromString(String str) {
            switch (str.toLowerCase()) {
                case "gradient-horizontal":
                    return HORIZONTAL;
                case "gradient-vertical":
                    return VERTICAL;
                case "background":
                    return BACKGROUND;
                case "texture":
                    return TEXTURE;
                case "border":
                    return BORDER;
                case "item":
                    return ITEM;
                default:
                    return NONE;
            }
        }
    }

    public static final int TRANSPARENT = 0x00000000;
    public static final int WHITE = 0xFFFFFFFF;

    protected TooltipTexture texture = null;
    protected TooltipTransform transform = null;

    protected DecoratorType decoratorType = DecoratorType.NONE;
    protected int[] colors = null;
    protected int thickness = 1;
    protected boolean corner = true;

    protected TooltipSpacing margin;
    protected TooltipAlign alignInline = TooltipAlign.START;
    protected TooltipAlign alignBlock = TooltipAlign.END;
    protected int sizeWidth = 0;
    protected int sizeHeight = 0;

    public TooltipDecorator(TooltipStyle style) {
        this.decoratorType = DecoratorType.fromString(style.getAsString("type", "none"));
        this.margin = style.getAsTooltipSpacing("margin", new int[] { 0, 0, 0, 0 });

        this.alignInline = TooltipAlign
            .fromString(style.getAsString("alignInline", style.getAsString("align.inline", "left")));
        this.alignBlock = TooltipAlign
            .fromString(style.getAsString("alignBlock", style.getAsString("align.block", "top")));

        this.sizeWidth = style.getAsInt("width", 0);
        this.sizeHeight = style.getAsInt("height", 0);

        if (this.decoratorType == DecoratorType.TEXTURE && style.containsKey("path")) {
            this.texture = new TooltipTexture(style);
        } else if (this.decoratorType == DecoratorType.BACKGROUND || this.decoratorType == DecoratorType.BORDER) {

            if (style.get("color") != null && style.get("color")
                .isJsonArray()) {
                this.colors = prepare4Colors(style.getAsColors("color", new int[] { TRANSPARENT }));
            } else {
                this.colors = prepare4Colors(new int[] { style.getAsColor("color", TRANSPARENT) });
            }

            this.thickness = style.getAsInt("thickness", 1);
            this.corner = style.getAsBoolean("corner", true);
        } else if (this.decoratorType == DecoratorType.VERTICAL || this.decoratorType == DecoratorType.HORIZONTAL) {

            if (style.get("color") != null && style.get("color")
                .isJsonArray()) {
                this.colors = style.getAsColors("color", new int[] { TRANSPARENT });
            } else {
                final int colorInt = style.getAsColor("color", TRANSPARENT);
                final int color60Int = (colorInt & 0x00FFFFFF) | ((colorInt >> 24 & 0xFF) * 96 / 255 << 24);
                this.colors = new int[] { TRANSPARENT, color60Int, colorInt, color60Int, TRANSPARENT };
            }

        } else {
            this.colors = new int[] {};
        }

        if (this.decoratorType != DecoratorType.TEXTURE && this.decoratorType != DecoratorType.ITEM
            && this.colors.length == 0) {
            this.decoratorType = DecoratorType.NONE;
        }

        if (this.decoratorType != DecoratorType.NONE && style.containsKey("transform")) {
            this.transform = new TooltipTransform(style.getAsStyle("transform"));
        }

    }

    protected int[] prepare4Colors(int[] colors) {
        if (colors.length == 1) {
            return new int[] { colors[0], colors[0], colors[0], colors[0] };
        } else if (colors.length == 2) {
            return new int[] { colors[0], colors[0], colors[1], colors[1] };
        } else if (colors.length == 3) {
            return new int[] { colors[0], colors[1], colors[2], colors[0] };
        } else {
            return colors;
        }
    }

    public void draw(double x, double y, int w, int h, TooltipContext context, int mixColor) {
        x += this.margin.left;
        y += this.margin.top;
        w -= this.margin.left + this.margin.right;
        h -= this.margin.top + this.margin.bottom;

        double width = this.sizeWidth == 0 ? w : this.sizeWidth;
        double height = this.sizeHeight == 0 ? h : this.sizeHeight;

        if (this.texture != null) {
            width = this.sizeWidth == 0 ? this.texture.getWidth(width) : width;
            height = this.sizeHeight == 0 ? this.texture.getHeight(height) : height;
        }

        if (width == 0 || height == 0) {
            return;
        }

        if (this.alignInline == TooltipAlign.END) {
            x += w - width;
        } else if (this.alignInline == TooltipAlign.CENTER) {
            x += (w - width) / 2d;
        }

        if (this.alignBlock == TooltipAlign.END) {
            y += h - height;
        } else if (this.alignBlock == TooltipAlign.CENTER) {
            y += (h - height) / 2d;
        }

        if (this.transform != null && this.transform.isAnimated()) {
            this.transform.pushTransformMatrix(x, y, width, height, context.getAnimationStartTime());
            x = y = 0;
        }

        if (this.texture != null) {
            this.texture.draw(x, y, width, height, this.alignInline, this.alignBlock, mixColor);
        } else if (this.decoratorType == DecoratorType.ITEM) {
            final ItemStack stack = context.getStack();

            if (stack != null) {
                drawItemStack(x, y, width, height, stack);
            }
        } else if (this.decoratorType != DecoratorType.NONE) {
            drawGradient(x, y, width, height, mixColor);
        }

        if (this.transform != null && this.transform.isAnimated()) {
            this.transform.popTransformMatrix();
        }
    }

    protected void drawItemStack(double x, double y, double w, double h, ItemStack stack) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        RenderHelper.enableGUIStandardItemLighting();

        double xTranslation = x;
        double yTranslation = y;
        double zTranslation = 400;
        double scale = Math.min(w / 16f, h / 16f);

        GL11.glTranslated(xTranslation, yTranslation, zTranslation);
        GL11.glScaled(scale, scale, 1);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        final FontRenderer font = TooltipFontContext.getFontRenderer();
        final RenderItem itemRender = ClientUtil.getItemRenderer();
        final TextureManager renderEngine = ClientUtil.mc()
            .getTextureManager();

        itemRender.zLevel += 100.0F;
        itemRender.renderItemAndEffectIntoGUI(font, renderEngine, stack, 0, 0);
        itemRender.renderItemOverlayIntoGUI(font, renderEngine, stack, 0, 0, "");
        itemRender.zLevel -= 100.0F;

        GL11.glScaled(1f / scale, 1f / scale, 1);
        GL11.glTranslated(-xTranslation, -yTranslation, -zTranslation);
        GL11.glPopAttrib();
    }

    protected void drawGradient(double x, double y, double width, double height, int mixColor) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        if (this.decoratorType == DecoratorType.HORIZONTAL) {
            final double segmentWidth = width / (this.colors.length - 1);

            for (int i = 0; i < this.colors.length - 1; i++) {
                drawGradientRect(
                    x,
                    y,
                    x + segmentWidth,
                    y + height,
                    blend(this.colors[i], mixColor),
                    blend(this.colors[i], mixColor),
                    blend(this.colors[i + 1], mixColor),
                    blend(this.colors[i + 1], mixColor));
                x += segmentWidth;
            }

        } else if (this.decoratorType == DecoratorType.VERTICAL) {
            final double segmentHeight = height / (this.colors.length - 1);

            for (int i = 0; i < this.colors.length - 1; i++) {
                drawGradientRect(
                    x,
                    y,
                    x + width,
                    y + segmentHeight,
                    blend(this.colors[i], mixColor),
                    blend(this.colors[i + 1], mixColor),
                    blend(this.colors[i], mixColor),
                    blend(this.colors[i + 1], mixColor));
                y += segmentHeight;
            }
        } else if (this.decoratorType == DecoratorType.BACKGROUND) {
            final int offset = this.corner ? 0 : this.thickness;

            drawGradientRect(
                x + offset,
                y + offset,
                x + width - offset,
                y + height - offset,
                blend(this.colors[0], mixColor),
                blend(this.colors[1], mixColor),
                blend(this.colors[3], mixColor),
                blend(this.colors[2], mixColor));

            if (!this.corner) {
                drawBorder(x, y, width, height, this.thickness, mixColor);
            }

        } else if (this.decoratorType == DecoratorType.BORDER) {
            drawBorder(x, y, width, height, this.corner ? 0 : this.thickness, mixColor);
        }

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    protected void drawBorder(double x, double y, double width, double height, int offset, int mixColor) {
        final int[] colors = new int[] { blend(this.colors[0], mixColor), blend(this.colors[1], mixColor),
            blend(this.colors[2], mixColor), blend(this.colors[3], mixColor) };

        // Top
        drawGradientRect(
            x + offset,
            y,
            x + width - offset,
            y + this.thickness,
            colors[0],
            colors[0],
            colors[1],
            colors[1]);
        // Bottom
        drawGradientRect(
            x + offset,
            y + height - this.thickness,
            x + width - offset,
            y + height,
            colors[3],
            colors[3],
            colors[2],
            colors[2]);
        // Left
        drawGradientRect(
            x,
            y + this.thickness,
            x + this.thickness,
            y + height - this.thickness,
            colors[0],
            colors[3],
            colors[0],
            colors[3]);
        // Right
        drawGradientRect(
            x + width - this.thickness,
            y + this.thickness,
            x + width,
            y + height - this.thickness,
            colors[1],
            colors[2],
            colors[1],
            colors[2]);
    }

    protected void drawGradientRect(double left, double top, double right, double bottom, int lt, int lb, int rt,
        int rb) {
        final Tessellator tess = Tessellator.instance;

        tess.startDrawingQuads();
        tess.setColorRGBA_I(lt, lt >> 24 & 255);
        tess.addVertex(left, top, 0);
        tess.setColorRGBA_I(lb, lb >> 24 & 255);
        tess.addVertex(left, bottom, 0);
        tess.setColorRGBA_I(rb, rb >> 24 & 255);
        tess.addVertex(right, bottom, 0);
        tess.setColorRGBA_I(rt, rt >> 24 & 255);
        tess.addVertex(right, top, 0);
        tess.draw();
    }

    public static int blend(int dst, int src) {

        int tr = (dst >>> 16) & 0xFF;
        int tg = (dst >>> 8) & 0xFF;
        int tb = dst & 0xFF;
        int ta = (dst >>> 24) & 0xFF;

        int cr = (src >>> 16) & 0xFF;
        int cg = (src >>> 8) & 0xFF;
        int cb = src & 0xFF;

        int r = tr * cr / 255;
        int g = tg * cg / 255;
        int b = tb * cb / 255;

        return (ta << 24) | (r << 16) | (g << 8) | b;
    }

}
