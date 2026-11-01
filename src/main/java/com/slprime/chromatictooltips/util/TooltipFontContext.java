package com.slprime.chromatictooltips.util;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.util.color.ColorValue;
import com.slprime.chromatictooltips.util.color.TooltipPalette;

public class TooltipFontContext {

    protected static class Context {

        public FontRenderer fontRenderer;
        public int[] colors = new int[32];
        public boolean shadow = true;
        public int paragraph = 6;

        public Context(FontRenderer fontRenderer, int[] colors, boolean[] overridden, boolean shadow, int paragraph) {
            this.fontRenderer = fontRenderer;
            this.colors = Arrays.copyOf(fontRenderer.colorCode, 32);
            this.shadow = shadow;
            this.paragraph = paragraph;

            for (int i = 0; i < colors.length; i++) {
                if (overridden[i]) {
                    this.colors[i] = colors[i];
                }
            }

        }

        public void applyColors() {
            System.arraycopy(this.colors, 0, TooltipFontContext.getFontRenderer().colorCode, 0, 32);
        }
    }

    public static final int LINE_SPACE = 1;
    public static final int DEFAULT_SPACING = 2;

    protected static final String[] colorOrders = new String[] { "black", "dark_blue", "dark_green", "dark_aqua",
        "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
        "yellow", "white" };

    protected Context previousContext = null;
    protected static Context activeContext = null;

    protected FontRenderer fontRenderer = null;
    protected int[] customColors = new int[32];
    protected boolean[] overridden = new boolean[32];
    protected ColorValue[] colorValues = new ColorValue[32];
    protected boolean dynamicColors = false;
    protected Boolean shadow = null;
    protected Integer paragraph = null;

    public TooltipFontContext(TooltipStyle formatting) {
        this(formatting, null);
    }

    public TooltipFontContext(TooltipStyle formatting, FontRenderer fontRenderer) {
        final TooltipStyle colorStyle = new TooltipStyle(
            formatting.getAsJsonObject("font.colors", formatting.getAsJsonObject("fontColors", new JsonObject())));

        if (formatting.containsKey("font.shadow")) {
            this.shadow = formatting.getAsBoolean("font.shadow", true);
        } else if (formatting.containsKey("fontShadow")) {
            this.shadow = formatting.getAsBoolean("fontShadow", true);
        }

        if (formatting.containsKey("font.paragraph")) {
            this.paragraph = formatting.getAsInt("font.paragraph", 6);
        } else if (formatting.containsKey("fontParagraph")) {
            this.paragraph = formatting.getAsInt("fontParagraph", 6);
        }

        this.fontRenderer = fontRenderer;

        for (int i = 0; i < colorOrders.length; i++) {
            final String key = colorOrders[i];

            if (colorStyle.containsKey(key)) {
                final JsonElement element = colorStyle.get(key);

                if (element.isJsonArray()) {
                    final ColorValue[] colors = colorStyle.getAsColorValues(key, null, new ColorValue[0]);

                    if (colors.length > 0) {
                        this.colorValues[i] = colors[0];
                        this.colorValues[i + 16] = colors.length == 1 ? shadowOf(colors[0]) : colors[1];
                    }
                } else if (element.isJsonPrimitive()) {
                    final ColorValue color = colorStyle.getAsColorValue(key, (ColorValue) null);

                    this.colorValues[i] = color;
                    this.colorValues[i + 16] = shadowOf(color);
                }

            }

        }

        for (int i = 0; i < this.colorValues.length; i++) {
            this.overridden[i] = this.colorValues[i] != null;
            this.dynamicColors |= this.overridden[i] && this.colorValues[i].isDynamic();
        }

    }

    protected static ColorValue shadowOf(ColorValue color) {
        return ColorValue.map(color, argb -> ((argb & 0x00FFFFFF) & 16579836) >> 2);
    }

    protected static Context context() {

        if (TooltipFontContext.activeContext == null) {
            TooltipFontContext.activeContext = new Context(
                TooltipUtils.mc().fontRenderer,
                new int[0],
                new boolean[0],
                true,
                6);
        }

        return TooltipFontContext.activeContext;
    }

    public void pushContext() {
        pushContext(null);
    }

    public void pushContext(TooltipContext tooltipContext) {
        final TooltipPalette palette = tooltipContext != null && this.dynamicColors ? tooltipContext.getPalette()
            : TooltipPalette.DEFAULT;

        for (int i = 0; i < this.colorValues.length; i++) {
            if (this.overridden[i]) {
                this.customColors[i] = this.colorValues[i].resolve(palette) & 0x00FFFFFF;
            }
        }

        this.previousContext = context();

        TooltipFontContext.activeContext = new Context(
            this.fontRenderer == null ? TooltipFontContext.activeContext.fontRenderer : this.fontRenderer,
            this.customColors,
            this.overridden,
            this.shadow == null ? TooltipFontContext.activeContext.shadow : this.shadow,
            this.paragraph == null ? TooltipFontContext.activeContext.paragraph : this.paragraph);

        TooltipFontContext.activeContext.applyColors();
    }

    public void popContext() {
        TooltipFontContext.activeContext = this.previousContext;
        TooltipFontContext.activeContext.applyColors();
    }

    public static int getColor(int index) {
        return context().colors[index];
    }

    public static int getColorShadow(int index) {
        return context().colors[index + 16];
    }

    public static int getParagraphSpacing() {
        return context().paragraph;
    }

    public static FontRenderer getFontRenderer() {
        return context().fontRenderer;
    }

    public static int getFontHeight() {
        return getFontRenderer().FONT_HEIGHT + LINE_SPACE;
    }

    public static int getStringWidth(String text) {
        return getFontRenderer().getStringWidth(text);
    }

    public static List<String> listFormattedStringToWidth(String text, int maxWidth) {
        return getFontRenderer().listFormattedStringToWidth(text, maxWidth);
    }

    public static void drawString(String text, int x, int y) {
        drawString(text, x, y, 0xffffffff, context().shadow);
    }

    public static void drawString(String text, int x, int y, int color) {
        drawString(text, x, y, color, context().shadow);
    }

    public static void drawString(String text, int x, int y, int color, boolean shadow) {
        getFontRenderer().drawString(text, x, y, color, shadow);
    }

}
