package com.slprime.chromatictooltips.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipLineConverter;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.component.DividerTextComponent;
import com.slprime.chromatictooltips.component.SpaceComponent;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class DividerTextConverter implements ITooltipLineConverter {

    public static final Pattern PATTERN = Pattern.compile(
        "^(\\s*)(?:§[0-9a-fk-or])*§([0-9a-f])(?:§[0-9a-fk-or])*-{2,}(?:§r)?\\s+(.+?)\\s+(?:§[0-9a-fk-or])*-{2,}(?:§r)?$",
        Pattern.CASE_INSENSITIVE);

    @Override
    public ITooltipComponent convert(Matcher matcher, TooltipContext context) {

        if (matcher.matches()) {
            final String colorCode = matcher.group(2);
            final int colorCodeIndex = "0123456789abcdef".indexOf(colorCode.toLowerCase());
            final int marginLeft = TooltipFontContext.getStringWidth(matcher.group(1));
            final String text = matcher.group(3);

            final TooltipStyle rendererStyle = context.getRenderer()
                .getStyle();
            final TooltipStyle style = rendererStyle.containsKey("dividerText")
                ? rendererStyle.getAsStyle("dividerText")
                : new TooltipStyle(createDefaultStyle());

            return new DividerTextComponent(
                new SpaceComponent(style.getAsStyle("left")),
                new SpaceComponent(style.getAsStyle("right")),
                style.getAsInt("height", 10),
                marginLeft,
                colorCodeIndex,
                text);
        }

        return null;
    }

    protected JsonObject createDefaultStyle() {
        final JsonObject style = new JsonObject();

        style.add("left", createSideStyle("marginRight"));
        style.add("right", createSideStyle("marginLeft"));
        style.addProperty("height", 10);

        return style;
    }

    protected JsonObject createSideStyle(String marginProperty) {
        final JsonObject decorator = new JsonObject();

        decorator.addProperty("type", "background");
        decorator.addProperty("color", "0xFFFFFFFF");
        decorator.addProperty("alignBlock", "center");
        decorator.addProperty(marginProperty, 4);
        decorator.addProperty("height", 1);

        final JsonObject side = new JsonObject();
        side.add("decorator", decorator);

        return side;
    }

}
