package com.slprime.chromatictooltips.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.util.TooltipDecorator;
import com.slprime.chromatictooltips.util.TooltipDecoratorCollection;
import com.slprime.chromatictooltips.util.TooltipSpacing;
import com.slprime.chromatictooltips.util.color.ColorExpression;
import com.slprime.chromatictooltips.util.color.ColorValue;

public class TooltipStyle {

    protected static final String[][] SPACE_PROPERTY = new String[][] { new String[] { "block", "top" },
        new String[] { "inline", "right" }, new String[] { "block", "bottom" }, new String[] { "inline", "left" } };

    protected JsonObject map = new JsonObject();

    public TooltipStyle() {
        this(new JsonObject());
    }

    public TooltipStyle(JsonObject map) {
        this.map = map;
    }

    public JsonElement get(String path) {
        JsonElement obj = this.map;

        for (String key : path.split("\\.")) {
            if (obj == null || !obj.isJsonObject()) return null;
            obj = ((JsonObject) obj).get(key);
        }

        return obj;
    }

    public boolean containsKey(String path) {
        return get(path) != null;
    }

    public TooltipStyle getAsStyle(String path) {
        return new TooltipStyle(getAsJsonObject(path, new JsonObject()));
    }

    public <T> T getAs(JsonElement element, T defaultValue, Function<JsonElement, T> parser) {

        if (element != null) {
            try {
                return parser.apply(element);
            } catch (Exception ignored) {}
        }

        return defaultValue;
    }

    public <T> T getAs(String path, T defaultValue, Function<JsonElement, T> parser) {
        return getAs(get(path), defaultValue, parser);
    }

    public int getAsColor(String path, int defaultValue) {
        return getAs(path, defaultValue, TooltipStyle::parseInt);
    }

    public int getAsInt(String path, int defaultValue) {
        return getAs(path, defaultValue, el -> el.getAsInt());
    }

    public boolean getAsBoolean(String path, boolean defaultValue) {
        return getAs(path, defaultValue, el -> el.getAsBoolean());
    }

    public float getAsFloat(String path, float defaultValue) {
        return getAs(path, defaultValue, el -> el.getAsFloat());
    }

    public long getAsLong(String path, long defaultValue) {
        return getAs(path, defaultValue, el -> el.getAsLong());
    }

    public String getAsString(String path, String defaultValue) {
        return getAs(path, defaultValue, el -> el.getAsString());
    }

    public String[] getAsStringArray(String path, String[] defaultValue) {
        final JsonElement element = get(path);

        if (element != null && element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final String[] values = new String[array.size()];

            for (int i = 0; i < array.size(); i++) {
                values[i] = getAs(array.get(i), "", el -> el.getAsString());
            }

            return values;
        }

        return defaultValue;
    }

    public double getAsDouble(String path, double defaultValue) {
        return getAs(path, defaultValue, el -> el.getAsDouble());
    }

    public JsonObject getAsJsonObject(String path, JsonObject defaultValue) {
        return getAs(path, defaultValue, el -> el.getAsJsonObject());
    }

    public JsonArray getAsJsonArray(String path, JsonArray defaultValue) {
        return getAs(path, defaultValue, el -> el.getAsJsonArray());
    }

    public int[] getAsColors(String path, int[] defaultValue) {
        final JsonElement element = get(path);

        if (element != null && element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final int[] values = new int[array.size()];

            for (int i = 0; i < array.size(); i++) {
                values[i] = getAs(array.get(i), 0, TooltipStyle::parseInt);
            }

            return values;
        }

        return defaultValue;
    }

    public ColorValue getAsColorValue(String path, int defaultValue) {
        return getAsColorValue(path, ColorValue.constant(defaultValue));
    }

    public ColorValue getAsColorValue(String path, ColorValue fallback) {
        return parseColorValue(path, get(path), fallback);
    }

    public ColorValue[] getAsColorValues(String path, int[] defaultValue) {
        return getAsColorValues(path, ColorValue.constant(0), ColorValue.constants(defaultValue));
    }

    public ColorValue[] getAsColorValues(String path, ColorValue elementFallback, ColorValue[] defaultValue) {
        final JsonElement element = get(path);

        if (element != null && element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final ColorValue[] values = new ColorValue[array.size()];

            for (int i = 0; i < array.size(); i++) {
                values[i] = parseColorValue(path + "[" + i + "]", array.get(i), elementFallback);
            }

            return values;
        }

        return defaultValue;
    }

    public TooltipDecoratorCollection getDecoratorCollection() {

        if (containsKey("decorators")) {
            final JsonArray decoratorsStyle = getAsJsonArray("decorators", new JsonArray());
            final List<TooltipDecorator> decorators = new ArrayList<>();

            for (JsonElement element : decoratorsStyle) {

                if (element == null || !element.isJsonObject()) {
                    continue;
                }

                decorators.add(new TooltipDecorator(new TooltipStyle(element.getAsJsonObject())));
            }

            return new TooltipDecoratorCollection(decorators.toArray(new TooltipDecorator[0]));
        } else if (containsKey("decorator")) {
            return new TooltipDecoratorCollection(
                new TooltipDecorator[] { new TooltipDecorator(getAsStyle("decorator")) });
        }

        return new TooltipDecoratorCollection(new TooltipDecorator[0]);
    }

    public TooltipSpacing getAsTooltipSpacing(String path, int[] defaultValue) {
        return new TooltipSpacing(getAsProperty(path, SPACE_PROPERTY, defaultValue));
    }

    public int[] getAsProperty(String path, String[][] keys, int[] result) {
        final JsonElement element = get(path);

        if (element == null) {
            for (int i = 0; i < keys.length; i++) {
                for (String key : keys[i]) {
                    key = path + key.substring(0, 1)
                        .toUpperCase() + key.substring(1);
                    if (containsKey(key)) {
                        result[i] = getAsInt(key, result[i]);
                        break;
                    }
                }
            }
        } else if (element.isJsonPrimitive()) {
            final int value = getAs(element, result[0], el -> el.getAsInt());

            for (int i = 0; i < result.length; i++) {
                result[i] = value;
            }

            return result;
        } else if (element.isJsonObject()) {
            final JsonObject obj = element.getAsJsonObject();

            for (int i = 0; i < keys.length; i++) {
                for (String key : keys[i]) {
                    if (obj.has(key)) {
                        result[i] = getAs(obj.get(key), result[i], el -> el.getAsInt());
                        break;
                    }
                }
            }

        } else if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();

            for (int i = 0; i < keys.length && i < array.size(); i++) {
                result[i] = getAs(array.get(i), result[i], el -> el.getAsInt());
            }

        }

        return result;
    }

    private static ColorValue parseColorValue(String path, JsonElement element, ColorValue fallback) {

        if (element == null) {
            return fallback;
        }

        try {
            final String str = element.getAsString()
                .trim();

            if (str.startsWith("hsv(") || str.startsWith("var(")) {
                return ColorExpression.parse(str);
            }

            return ColorValue.constant(parseInt(element));
        } catch (Exception e) {
            ChromaticTooltips.LOG.warn("Invalid color at '{}': {}", path, e.getMessage());
            return fallback;
        }
    }

    private static int parseInt(JsonElement element) {
        final String str = element.getAsString();

        if (str.startsWith("0x")) {
            return (int) Long.parseUnsignedLong(str.substring(2), 16);
        } else {
            return Integer.parseInt(str);
        }
    }

    @Override
    public String toString() {
        return this.map.toString();
    }

}
