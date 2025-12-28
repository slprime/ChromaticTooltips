package com.slprime.chromatictooltips.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.api.TooltipStyle;

public class Parser {

    protected static final JsonParser parser = new JsonParser();
    protected JsonObject root;

    public List<TooltipStyle> parse(String json) {
        final List<TooltipStyle> scopes = new ArrayList<>();

        try {
            this.root = parser.parse(json)
                .getAsJsonObject();

            if (!this.root.has("styles")) {
                final JsonArray styles = new JsonArray();
                styles.add(
                    this.root.has("style") ? this.root.get("style")
                        .getAsJsonObject() : new JsonObject());
                this.root.add("styles", styles);
            }

            for (JsonElement styleElement : prepareJsonObject(this.root.get("styles"), new JsonObject())
                .getAsJsonArray()) {
                if (styleElement.isJsonObject()) {
                    scopes.add(new TooltipStyle(styleElement.getAsJsonObject()));
                }
            }

        } catch (JsonParseException e) {
            ChromaticTooltips.LOG.error("JSON parsing error: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalStateException e) {
            ChromaticTooltips.LOG.error("Invalid JSON structure: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            ChromaticTooltips.LOG.error("Unexpected error during JSON parsing: " + e.getMessage());
            e.printStackTrace();
        }

        return scopes;
    }

    protected JsonElement getPath(String path, JsonElement obj) {

        for (String key : path.split("\\.")) {
            if (obj == null || !obj.isJsonObject()) return JsonNull.INSTANCE;
            obj = ((JsonObject) obj).get(key);
        }

        return obj == null ? JsonNull.INSTANCE : obj;
    }

    protected JsonElement prepareJsonObject(JsonElement element, JsonObject params) {

        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            JsonObject result = new JsonObject();

            if (object.has("$ref") && object.get("$ref")
                .isJsonPrimitive()
                && object.get("$ref")
                    .getAsJsonPrimitive()
                    .isString()) {
                final JsonElement ref = getPath(
                    object.get("$ref")
                        .getAsString(),
                    this.root);

                if (!ref.isJsonNull()) {
                    final JsonObject prm = object.has("$params") ? prepareJsonObject(
                        object.get("$params")
                            .getAsJsonObject(),
                        params).getAsJsonObject() : new JsonObject();
                    result = clone(prepareJsonObject(ref, prm)).getAsJsonObject();
                }
            }

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                final String key = entry.getKey();
                final JsonElement value = entry.getValue();

                if (key.startsWith("$ref:")) {
                    final String refKey = key.substring(5);
                    JsonElement refResult = null;

                    for (String ref : getRefs(value)) {
                        refResult = join(refResult, clone(prepareJsonObject(getPath(ref, this.root), params)));
                    }

                    result.add(refKey, refResult);
                } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive()
                    .isString()
                    && value.getAsString()
                        .startsWith("$param:")) {
                            final String ref = value.getAsString()
                                .substring(7);
                            result.add(key, prepareJsonObject(getPath(ref, params), params));
                        } else
                    if (!key.startsWith("$")) {
                        result.add(key, prepareJsonObject(value, params));
                    }
            }

            return result;
        } else if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final JsonArray newArray = new JsonArray();

            for (int i = 0; i < array.size(); i++) {
                newArray.add(prepareJsonObject(array.get(i), params));
            }

            return newArray;
        }

        return element;
    }

    protected String[] getRefs(JsonElement element) {

        if (element != null && element.isJsonPrimitive()
            && element.getAsJsonPrimitive()
                .isString()
            && element.getAsString()
                .startsWith("$")) {
            return new String[] { element.getAsString() };
        } else if (element != null && element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final String[] refs = new String[array.size()];
            int index = 0;

            for (int i = 0; i < array.size(); i++) {
                final JsonElement e = array.get(i);

                if (e != null && e.isJsonPrimitive()
                    && e.getAsJsonPrimitive()
                        .isString()
                    && e.getAsString()
                        .startsWith("$")) {
                    refs[index++] = e.getAsString();
                }
            }

            return Arrays.copyOf(refs, index);
        }

        return new String[0];
    }

    protected static JsonElement join(JsonElement a, JsonElement b) {
        if (a == null || a.isJsonNull()) return b;
        if (b == null || b.isJsonNull()) return a;

        if (a.isJsonObject() && b.isJsonObject()) {
            final JsonObject result = a.getAsJsonObject();

            b.getAsJsonObject()
                .entrySet()
                .forEach(entry -> { result.add(entry.getKey(), entry.getValue()); });

            return result;
        }

        if (a.isJsonArray() && b.isJsonArray()) {
            JsonArray result = a.getAsJsonArray();
            result.addAll(b.getAsJsonArray());
            return result;
        }

        return b;
    }

    protected static JsonElement clone(JsonElement element) {
        return element == null ? JsonNull.INSTANCE : parser.parse(element.toString());
    }

}
