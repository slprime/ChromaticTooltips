package com.slprime.chromatictooltips.util.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser of CSS-like color expressions:
 * {@code hsv(from var(--primary) calc(h - 20) min(0.8, s) 0.2 / 0.9)}.
 * <ul>
 * <li>color: {@code 0xAARRGGBB}, {@code var(--primary)}, {@code var(--secondary)} or {@code hsv(...)}</li>
 * <li>channel: number, {@code h}, {@code s}, {@code v} (channels of the {@code from} color), {@code var(--neutral)},
 * {@code var(--time)} (seconds), {@code calc()}, {@code min()}, {@code max()}, {@code sin()} (radians)</li>
 * </ul>
 * Hue is in degrees, saturation, value and alpha are in 0..1. Alpha defaults to the alpha of the {@code from} color.
 */
public final class ColorExpression {

    private interface Channel {

        float eval(float[] from, TooltipPalette palette);
    }

    private interface ColorNode {

        void eval(TooltipPalette palette, float[] hsva);
    }

    private final String src;
    private int pos = 0;

    private ColorExpression(String src) {
        this.src = src;
    }

    public static ColorValue parse(String text) {
        final ColorExpression parser = new ColorExpression(text);
        final ColorNode node = parser.parseColor();

        parser.skipSpaces();

        if (parser.pos != text.length()) {
            throw parser.error("Unexpected input");
        }

        return palette -> {
            final float[] hsva = new float[4];
            node.eval(palette, hsva);
            return (Math.round(hsva[3] * 255f) << 24) | (Color.HSBtoRGB(hsva[0] / 360f, hsva[1], hsva[2]) & 0xFFFFFF);
        };
    }

    private ColorNode parseColor() {
        skipSpaces();

        if (accept("0x")) {
            return parseHex();
        } else if (accept("var(")) {
            return parseColorVariable();
        } else if (accept("hsv(")) {
            return parseHsv();
        }

        throw error("Expected a color");
    }

    private ColorNode parseHex() {
        final int start = pos;

        while (pos < src.length() && Character.digit(src.charAt(pos), 16) >= 0) {
            pos++;
        }

        if (start == pos) {
            throw error("Expected hex digits");
        }

        if (pos - start > 8) {
            throw error("Hex color has more than 8 digits");
        }

        final int argb = (int) Long.parseUnsignedLong(src.substring(start, pos), 16);
        final float[] hsb = Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
        final float alpha = ((argb >>> 24) & 0xFF) / 255f;

        return (palette, hsva) -> {
            hsva[0] = hsb[0] * 360f;
            hsva[1] = hsb[1];
            hsva[2] = hsb[2];
            hsva[3] = alpha;
        };
    }

    private ColorNode parseColorVariable() {
        final String name = parseVariableName();

        if ("primary".equals(name)) {
            return (palette, hsva) -> {
                hsva[0] = palette.primaryH;
                hsva[1] = palette.primaryS;
                hsva[2] = palette.primaryV;
                hsva[3] = 1f;
            };
        } else if ("secondary".equals(name)) {
            return (palette, hsva) -> {
                hsva[0] = palette.secondaryH;
                hsva[1] = palette.secondaryS;
                hsva[2] = palette.secondaryV;
                hsva[3] = 1f;
            };
        }

        throw error("Unknown color variable --" + name);
    }

    private String parseVariableName() {
        skipSpaces();
        expect("--");

        final int start = pos;

        while (pos < src.length() && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '-')) {
            pos++;
        }

        final String name = src.substring(start, pos);
        expect(")");
        return name;
    }

    private ColorNode parseHsv() {
        skipSpaces();

        final ColorNode base = acceptWord("from") ? parseColor() : null;
        final Channel hue = parseTerm();
        final Channel saturation = parseTerm();
        final Channel value = parseTerm();
        Channel alpha = null;

        skipSpaces();

        if (accept("/")) {
            alpha = parseTerm();
        }

        expect(")");

        final Channel alphaChannel = alpha;

        return (palette, hsva) -> {
            final float[] from = new float[4];

            if (base != null) {
                base.eval(palette, from);
            } else {
                from[3] = 1f;
            }

            hsva[0] = wrapHue(hue.eval(from, palette));
            hsva[1] = clamp01(saturation.eval(from, palette));
            hsva[2] = clamp01(value.eval(from, palette));
            hsva[3] = alphaChannel == null ? from[3] : clamp01(alphaChannel.eval(from, palette));
        };
    }

    private Channel parseSum() {
        Channel left = parseProduct();

        while (true) {
            skipSpaces();

            if (accept("+")) {
                final Channel a = left;
                final Channel b = parseProduct();
                left = (from, palette) -> a.eval(from, palette) + b.eval(from, palette);
            } else if (accept("-")) {
                final Channel a = left;
                final Channel b = parseProduct();
                left = (from, palette) -> a.eval(from, palette) - b.eval(from, palette);
            } else {
                return left;
            }
        }
    }

    private Channel parseProduct() {
        Channel left = parseTerm();

        while (true) {
            skipSpaces();

            if (accept("*")) {
                final Channel a = left;
                final Channel b = parseTerm();
                left = (from, palette) -> a.eval(from, palette) * b.eval(from, palette);
            } else if (accept("/")) {
                final Channel a = left;
                final Channel b = parseTerm();
                left = (from, palette) -> a.eval(from, palette) / b.eval(from, palette);
            } else {
                return left;
            }
        }
    }

    private Channel parseTerm() {
        skipSpaces();

        if (pos >= src.length()) {
            throw error("Unexpected end");
        }

        final char c = src.charAt(pos);

        if (c == '-') {
            pos++;
            final Channel inner = parseTerm();
            return (from, palette) -> -inner.eval(from, palette);
        } else if (c == '(') {
            pos++;
            final Channel inner = parseSum();
            expect(")");
            return inner;
        } else if (Character.isDigit(c) || c == '.') {
            return parseNumber();
        } else if (accept("var(")) {
            final String name = parseVariableName();

            if ("neutral".equals(name)) {
                return (from, palette) -> palette.neutral;
            } else if ("time".equals(name)) {
                return (from, palette) -> palette.time;
            }

            throw error("Unknown number variable --" + name);
        } else if (accept("calc(")) {
            final Channel inner = parseSum();
            expect(")");
            return inner;
        } else if (accept("min(")) {
            return parseFunction(true);
        } else if (accept("max(")) {
            return parseFunction(false);
        } else if (accept("sin(")) {
            final Channel inner = parseSum();
            expect(")");
            return (from, palette) -> (float) Math.sin(inner.eval(from, palette));
        } else if (isChannelName(c)) {
            pos++;
            final int index = c == 'h' ? 0 : (c == 's' ? 1 : 2);
            return (from, palette) -> from[index];
        }

        throw error("Unexpected token");
    }

    private Channel parseNumber() {
        final int start = pos;

        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
            pos++;
        }

        final float number;

        try {
            number = Float.parseFloat(src.substring(start, pos));
        } catch (NumberFormatException e) {
            throw error("Invalid number");
        }

        return (from, palette) -> number;
    }

    private Channel parseFunction(boolean min) {
        final List<Channel> args = new ArrayList<>();

        do {
            args.add(parseSum());
            skipSpaces();
        } while (accept(","));

        expect(")");

        return (from, palette) -> {
            float result = args.get(0)
                .eval(from, palette);

            for (int i = 1; i < args.size(); i++) {
                final float next = args.get(i)
                    .eval(from, palette);
                result = min ? Math.min(result, next) : Math.max(result, next);
            }

            return result;
        };
    }

    private boolean isChannelName(char c) {
        return (c == 'h' || c == 's' || c == 'v')
            && (pos + 1 >= src.length() || !Character.isLetter(src.charAt(pos + 1)));
    }

    private boolean acceptWord(String word) {
        if (src.startsWith(word, pos)
            && (pos + word.length() >= src.length() || !Character.isLetterOrDigit(src.charAt(pos + word.length())))) {
            pos += word.length();
            return true;
        }

        return false;
    }

    private boolean accept(String token) {
        if (src.startsWith(token, pos)) {
            pos += token.length();
            return true;
        }

        return false;
    }

    private void expect(String token) {
        skipSpaces();

        if (!accept(token)) {
            throw error("Expected '" + token + "'");
        }
    }

    private void skipSpaces() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at position " + pos + " in '" + src + "'");
    }

    private static float clamp01(float value) {
        return value > 0f ? Math.min(value, 1f) : 0f;
    }

    private static float wrapHue(float hue) {
        if (Float.isNaN(hue) || Float.isInfinite(hue)) return 0f;
        final float wrapped = hue % 360f;
        return wrapped < 0f ? wrapped + 360f : wrapped;
    }

}
