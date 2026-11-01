package com.slprime.chromatictooltips.util;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.util.color.ColorValue;
import com.slprime.chromatictooltips.util.color.PerimeterGradient;
import com.slprime.chromatictooltips.util.color.TooltipPalette;

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
    protected ColorValue[] colorValues = null;
    protected int[] colors = null;
    protected boolean dynamicColors = false;
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

            if (style.containsKey("color")) {
                this.colorValues = new ColorValue[] { style.getAsColorValue("color", WHITE) };
            }
        } else if (this.decoratorType == DecoratorType.BACKGROUND || this.decoratorType == DecoratorType.BORDER) {

            if (style.get("color") != null && style.get("color")
                .isJsonArray()) {
                this.colorValues = prepare4Colors(style.getAsColorValues("color", new int[] { TRANSPARENT }));
            } else {
                this.colorValues = prepare4Colors(new ColorValue[] { style.getAsColorValue("color", TRANSPARENT) });
            }

            this.thickness = style.getAsInt("thickness", 1);
            this.corner = style.getAsBoolean("corner", true);
        } else if (this.decoratorType == DecoratorType.VERTICAL || this.decoratorType == DecoratorType.HORIZONTAL) {

            if (style.get("color") != null && style.get("color")
                .isJsonArray()) {
                this.colorValues = style.getAsColorValues("color", new int[] { TRANSPARENT });
            } else {
                final ColorValue color = style.getAsColorValue("color", TRANSPARENT);
                final ColorValue color60 = ColorValue
                    .map(color, colorInt -> (colorInt & 0x00FFFFFF) | ((colorInt >> 24 & 0xFF) * 96 / 255 << 24));
                this.colorValues = new ColorValue[] { ColorValue.constant(TRANSPARENT), color60, color, color60,
                    ColorValue.constant(TRANSPARENT) };
            }

        } else {
            this.colorValues = new ColorValue[] {};
        }

        if (this.decoratorType != DecoratorType.TEXTURE && this.decoratorType != DecoratorType.ITEM
            && this.colorValues.length == 0) {
            this.decoratorType = DecoratorType.NONE;
        }

        for (ColorValue value : this.colorValues == null ? new ColorValue[0] : this.colorValues) {
            this.dynamicColors |= value.isDynamic();
        }

        if (this.decoratorType != DecoratorType.NONE && style.containsKey("transform")) {
            this.transform = new TooltipTransform(style.getAsStyle("transform"));
        }

    }

    protected ColorValue[] prepare4Colors(ColorValue[] colors) {
        if (colors.length == 1) {
            return new ColorValue[] { colors[0], colors[0], colors[0], colors[0] };
        } else if (colors.length == 2) {
            return new ColorValue[] { colors[0], colors[0], colors[1], colors[1] };
        } else if (colors.length == 3) {
            return new ColorValue[] { colors[0], colors[1], colors[2], colors[0] };
        } else {
            return colors;
        }
    }

    protected void resolveColors(TooltipContext context) {
        final TooltipPalette palette = context != null && this.dynamicColors ? context.getPalette()
            : TooltipPalette.DEFAULT;

        if (this.colors == null || this.colors.length != this.colorValues.length) {
            this.colors = new int[this.colorValues.length];
        }

        for (int i = 0; i < this.colors.length; i++) {
            this.colors[i] = this.colorValues[i].resolve(palette);
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
            int tint = mixColor;

            if (this.colorValues != null) {
                resolveColors(context);
                tint = blend(this.colors[0], mixColor);
            }

            this.texture.draw(x, y, width, height, this.alignInline, this.alignBlock, tint);
        } else if (this.decoratorType == DecoratorType.ITEM) {
            final ItemStack stack = context.getItem();
            final FluidStack fluid = context.getFluid();

            if (stack != null) {
                drawItemContext(x, y, width, height, () -> TooltipRenderUtils.drawItemStack(stack, 0, false));
            } else if (fluid != null) {
                drawItemContext(x, y, width, height, () -> TooltipRenderUtils.drawFluidStack(fluid, 0));
            }

        } else if (this.decoratorType != DecoratorType.NONE) {
            resolveColors(context);
            drawGradient(x, y, width, height, mixColor);
        }

        if (this.transform != null && this.transform.isAnimated()) {
            this.transform.popTransformMatrix();
        }
    }

    protected void drawItemContext(double x, double y, double width, double height, Runnable callback) {
        final double scale = Math.min(width / 16f, height / 16f);

        GL11.glTranslated(x, y, 400);
        GL11.glScaled(scale, scale, scale);

        callback.run();

        GL11.glScaled(1 / scale, 1 / scale, 1 / scale);
        GL11.glTranslated(-x, -y, -400);
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
            final int offset = this.corner ? 0 : this.thickness;

            if (this.colors.length > 4) {
                drawPerimeterBorder(x, y, width, height, offset, mixColor);
            } else {
                drawBorder(x, y, width, height, offset, mixColor);
            }
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

    protected void drawPerimeterBorder(double x, double y, double width, double height, int offset, int mixColor) {
        final double horizontal = width - 2 * offset;
        final double vertical = height - 2 * this.thickness;

        if (horizontal <= 0 || vertical <= 0) {
            return;
        }

        final int[] stops = new int[this.colors.length];

        for (int i = 0; i < stops.length; i++) {
            stops[i] = blend(this.colors[i], mixColor);
        }

        final PerimeterGradient gradient = new PerimeterGradient(stops);
        final double perimeter = 2 * (horizontal + vertical);
        final double spacing = perimeter / stops.length;
        double start = 0;

        for (int edge = 0; edge < 4; edge++) {
            final double end = start + (edge % 2 == 0 ? horizontal : vertical);
            double from = start;

            while (from < end - 1e-6) {
                final double to = Math.min(end, (Math.floor(from / spacing + 1e-9) + 1) * spacing);

                drawPerimeterSegment(
                    edge,
                    x,
                    y,
                    width,
                    height,
                    offset,
                    from - start,
                    to - start,
                    gradient.colorAt(from / perimeter),
                    gradient.colorAt(to / perimeter));
                from = to;
            }

            start = end;
        }
    }

    protected void drawPerimeterSegment(int edge, double x, double y, double width, double height, int offset,
        double from, double to, int fromColor, int toColor) {
        final double t = this.thickness;

        switch (edge) {
            case 0:
                drawGradientRect(x + offset + from, y, x + offset + to, y + t, fromColor, fromColor, toColor, toColor);
                break;
            case 1:
                drawGradientRect(
                    x + width - t,
                    y + t + from,
                    x + width,
                    y + t + to,
                    fromColor,
                    toColor,
                    fromColor,
                    toColor);
                break;
            case 2:
                drawGradientRect(
                    x + width - offset - to,
                    y + height - t,
                    x + width - offset - from,
                    y + height,
                    toColor,
                    toColor,
                    fromColor,
                    fromColor);
                break;
            default:
                drawGradientRect(
                    x,
                    y + height - t - to,
                    x + t,
                    y + height - t - from,
                    toColor,
                    fromColor,
                    toColor,
                    fromColor);
                break;
        }
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
