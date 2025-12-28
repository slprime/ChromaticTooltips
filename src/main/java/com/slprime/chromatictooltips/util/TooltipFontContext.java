package com.slprime.chromatictooltips.util;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.slprime.chromatictooltips.api.TooltipStyle;

public class TooltipFontContext {

    protected static class Context {

        public int[] colors = new int[32];
        public boolean shadow = true;
        public int paragraph = 6;

        public Context(int[] colors, boolean shadow, int paragraph) {
            this.colors = Arrays.copyOf(TooltipFontContext.getFontRenderer().colorCode, 32);
            this.shadow = shadow;
            this.paragraph = paragraph;

            for (int i = 0; i < colors.length; i++) {
                if (colors[i] != INHERIT_COLOR) {
                    this.colors[i] = colors[i];
                }
            }

        }

        public void applyColors() {
            System.arraycopy(this.colors, 0, TooltipFontContext.getFontRenderer().colorCode, 0, 32);
        }
    }

    protected static FontRenderer fontRenderer = null;
    protected static final int INHERIT_COLOR = 0x00000000;
    protected static final String[] colorOrders = new String[] { "black", "dark_blue", "dark_green", "dark_aqua",
        "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
        "yellow", "white" };

    protected Context previousContext = null;
    protected static Context activeContext = null;

    protected int[] customColors = new int[32];
    protected Boolean shadow = null;
    protected Integer paragraph = null;

    public TooltipFontContext(TooltipStyle formatting) {
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

        Arrays.fill(this.customColors, INHERIT_COLOR);

        for (int i = 0; i < colorOrders.length; i++) {
            final String key = colorOrders[i];

            if (colorStyle.containsKey(key)) {
                final JsonElement element = colorStyle.get(key);

                if (element.isJsonArray()) {
                    final int[] colors = colorStyle.getAsColors(key, new int[] { INHERIT_COLOR });

                    this.customColors[i] = colors[0];

                    if (colors.length == 1) {
                        this.customColors[i + 16] = (colors[0] & 16579836) >> 2 | colors[0] & -16777216;
                    } else {
                        this.customColors[i + 16] = colors[1];
                    }

                } else if (element.isJsonPrimitive()) {
                    final int color = colorStyle.getAsColor(key, INHERIT_COLOR);

                    this.customColors[i] = color;
                    this.customColors[i + 16] = (color & 16579836) >> 2 | color & -16777216;
                }

            }

        }

    }

    protected static Context context() {

        if (TooltipFontContext.activeContext == null) {
            TooltipFontContext.activeContext = new Context(new int[0], true, 6);
        }

        return TooltipFontContext.activeContext;
    }

    public void pushContext() {
        this.previousContext = context();

        TooltipFontContext.activeContext = new Context(
            this.customColors,
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

    public static int getParagraphSpacing() {
        return context().paragraph;
    }

    public static FontRenderer getFontRenderer() {

        if (TooltipFontContext.fontRenderer == null) {
            TooltipFontContext.fontRenderer = ClientUtil.mc().fontRenderer;
        }

        return TooltipFontContext.fontRenderer;
    }

    public static int getStringWidth(String text) {
        return getFontRenderer().getStringWidth(text);
    }

    public static List<String> listFormattedStringToWidth(String text, int maxWidth) {
        return getFontRenderer().listFormattedStringToWidth(text, maxWidth);
    }

    public static void drawString(String text, int x, int y) {
        drawString(text, x, y, 0xffffffff);
    }

    public static void drawString(String text, int x, int y, int color) {
        getFontRenderer().drawString(text, x, y, color, context().shadow);
    }

}
