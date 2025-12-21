package com.slprime.chromatictooltips;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import net.minecraft.client.resources.IResource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.ITooltipRenderer;
import com.slprime.chromatictooltips.api.TooltipBuilder;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipRequest;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.component.TextTooltipComponent;
import com.slprime.chromatictooltips.event.TooltipEnricherEvent;
import com.slprime.chromatictooltips.util.BlacklistLines;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.Parser;

public class TooltipHandler {

    protected static final WeakHashMap<ITooltipComponent, String> tipLineComponents = new WeakHashMap<>();
    protected static int nextComponentId = 0;

    protected static final String CONFIG_FILE = "tooltip.json";
    protected static final String COMPONENT_PREFIX = "\u00A7z";

    protected static ITooltipRenderer defaultTooltipRenderer = null;
    protected static Map<String, List<ITooltipRenderer>> otherTooltipRenderers = new HashMap<>();

    protected static Class<? extends ITooltipRenderer> rendererClass = null;
    protected static final Map<String, ITooltipEnricher> tooltipEnrichers = new LinkedHashMap<>();

    // cache
    protected static TooltipContext lastContext = null;
    protected static TooltipLines lastTextLines = null;
    protected static boolean ignoreLastTooltip = true;
    protected static int lastHashCode = -1;

    public static void reload() {
        TooltipHandler.otherTooltipRenderers.clear();
        TooltipHandler.defaultTooltipRenderer = null;

        loadTooltipResource();

        BlacklistLines.loadBlacklist();

        if (TooltipHandler.defaultTooltipRenderer == null) {
            TooltipHandler.defaultTooltipRenderer = createRenderer(new TooltipStyle());
        }
    }

    protected static void parseStyle(String json) {
        final List<TooltipStyle> scopes = (new Parser()).parse(json);

        for (TooltipStyle style : scopes) {
            String context = style.getAsString("context", null);

            if (context == null && style.containsKey("filter")) {
                context = "item";
            }

            if (context == null || "default".equals(context)) {
                TooltipHandler.defaultTooltipRenderer = createRenderer(style);
            } else {
                TooltipHandler.otherTooltipRenderers.computeIfAbsent(context, k -> new ArrayList<>())
                    .add(createRenderer(style));
            }

        }

    }

    protected static void loadTooltipResource() {
        final ResourceLocation location = new ResourceLocation(ChromaticTooltips.MODID, CONFIG_FILE);

        try {
            final IResource res = ClientUtil.mc()
                .getResourceManager()
                .getResource(location);

            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
                ChromaticTooltips.LOG.info("Loading '{}' from resourcepack {}", CONFIG_FILE, location);
                parseStyle(
                    reader.lines()
                        .collect(Collectors.joining("\n")));
            }

        } catch (Exception io) {
            ChromaticTooltips.LOG.error("Failed to load '{}' resourcepack {}", CONFIG_FILE, location);
        }

    }

    protected static ITooltipRenderer createRenderer(TooltipStyle style) {
        try {
            return TooltipHandler.rendererClass.getConstructor(TooltipStyle.class)
                .newInstance(style);
        } catch (Exception e1) {
            return new TooltipRenderer(style);
        }
    }

    public static String getComponentId(ITooltipComponent component) {
        TooltipHandler.tipLineComponents
            .put(component, TooltipHandler.COMPONENT_PREFIX + TooltipHandler.nextComponentId++);
        return TooltipHandler.tipLineComponents.get(component);
    }

    public static ITooltipComponent getTooltipComponent(String line) {
        if (!line.startsWith(TooltipHandler.COMPONENT_PREFIX)) return null;
        return TooltipHandler.tipLineComponents.entrySet()
            .stream()
            .filter(
                entry -> entry.getValue()
                    .equals(line))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    public static void addEnricher(String id, ITooltipEnricher enricher) {
        TooltipHandler.tooltipEnrichers.put(id, enricher);
    }

    public static void setRendererClass(Class<? extends ITooltipRenderer> rendererClass) {
        TooltipHandler.rendererClass = rendererClass;
        reload();
    }

    public static TooltipBuilder builder() {
        return new TooltipBuilder();
    }

    public static void drawHoveringText(ItemStack stack, List<?> textLines) {
        drawHoveringText(new TooltipRequest(null, stack, new TooltipLines(textLines), null));
    }

    public static void drawHoveringText(List<?> textLines) {
        drawHoveringText(new TooltipRequest(null, null, new TooltipLines(textLines), null));
    }

    public static void drawHoveringText(TooltipRequest request) {
        final int currentHash = ClientUtil.getMetaHash();
        boolean updateContent = false;

        if (request.stack == null && request.tooltip.isEmpty()) {
            return;
        }

        final Point mouse = request.mouse != null ? request.mouse : ClientUtil.getMousePosition();

        // change in item stack invalidates cache
        if (TooltipHandler.lastContext != null
            && !areItemStackEqual(request.stack, TooltipHandler.lastContext.getStack())) {
            clearCache();
        }

        // change in stack size may update content
        if (TooltipHandler.lastContext != null && request.stack != null
            && TooltipHandler.lastContext.getStack() != null
            && TooltipHandler.lastContext.getStack().stackSize != request.stack.stackSize) {
            updateContent = true;
        }

        // change in text lines may update context
        if (!updateContent && !request.tooltip.equals(TooltipHandler.lastTextLines)) {
            if (request.stack != null
                || TooltipHandler.lastContext != null && Math.abs(TooltipHandler.lastContext.getMouseX() - mouse.x) < 3
                    && Math.abs(TooltipHandler.lastContext.getMouseY() - mouse.y) < 3) {
                updateContent = true;
            } else {
                clearCache();
            }
        }

        if (TooltipHandler.lastContext == null) {
            TooltipHandler.lastContext = new TooltipContext(
                request.context,
                getRendererFor(request.context, request.stack),
                request.stack);
            updateContent = true;
        }

        if (updateContent || TooltipHandler.lastHashCode != currentHash) {
            TooltipHandler.lastContext.clearComponents();
            TooltipHandler.lastContext.setStack(request.stack);
            TooltipHandler.lastContext.setPosition(mouse);

            TooltipHandler.lastHashCode = currentHash;
            TooltipHandler.lastTextLines = request.tooltip;
            enrichTooltip(TooltipHandler.lastContext, request.tooltip.buildComponents(TooltipHandler.lastContext));
        } else {
            TooltipHandler.lastContext.setPosition(mouse);
        }

        TooltipHandler.ignoreLastTooltip = false;
    }

    protected static boolean areItemStackEqual(ItemStack stackA, ItemStack stackB) {
        if (stackA == null && stackB == null) return true;
        if (stackA == null || stackB == null) return false;
        return stackA.isItemEqual(stackB) && ItemStack.areItemStackTagsEqual(stackA, stackB);
    }

    protected static void clearCache() {
        TooltipHandler.lastContext = null;
        TooltipHandler.ignoreLastTooltip = true;
        TooltipHandler.lastTextLines = null;
        TooltipHandler.lastHashCode = 0;
    }

    protected static void enrichTooltip(TooltipContext context, List<ITooltipComponent> lines) {

        if (context.getStack() == null && !lines.isEmpty() && lines.get(0) instanceof TextTooltipComponent) {
            context.addSectionComponent("title", Arrays.asList(lines.remove(0)));
        }

        for (Map.Entry<String, ITooltipEnricher> entry : TooltipHandler.tooltipEnrichers.entrySet()) {
            context.addSectionComponent(
                entry.getKey(),
                entry.getValue()
                    .enrich(context));
        }

        if (!lines.isEmpty()) {
            context.addSectionComponent(
                Math.max(
                    1,
                    context.getComponents()
                        .size() - 1),
                "tooltip",
                lines);
        }

        ClientUtil.postEvent(new TooltipEnricherEvent(context));
    }

    protected static ITooltipRenderer getRendererFor(String context, ItemStack stack) {

        if (context == null) {
            context = stack != null ? "item" : "default";
        }

        for (ITooltipRenderer renderer : TooltipHandler.otherTooltipRenderers
            .getOrDefault(context, Collections.emptyList())) {
            if (renderer.matches(stack)) {
                return renderer;
            }
        }

        if (stack != null && !"item".equals(context)) {

            for (ITooltipRenderer renderer : TooltipHandler.otherTooltipRenderers
                .getOrDefault("item", Collections.emptyList())) {
                if (renderer.matches(stack)) {
                    return renderer;
                }
            }

        }

        return TooltipHandler.defaultTooltipRenderer;
    }

    public static Rectangle getLastTooltipBounds() {

        if (TooltipHandler.lastContext != null) {
            return TooltipHandler.lastContext.getRenderer()
                .getTooltipBounds(TooltipHandler.lastContext);
        }

        return null;
    }

    public static void drawLastTooltip() {

        if (TooltipHandler.lastContext != null && !TooltipHandler.lastContext.isEmpty()
            && !TooltipHandler.ignoreLastTooltip) {
            TooltipHandler.lastContext.getRenderer()
                .draw(TooltipHandler.lastContext);
            TooltipHandler.ignoreLastTooltip = true;
        } else if (TooltipHandler.lastContext != null && TooltipHandler.ignoreLastTooltip) {
            TooltipHandler.clearCache();
        }

    }

    public static boolean nextTooltipPage() {

        if (TooltipHandler.lastContext != null) {
            return TooltipHandler.lastContext.getRenderer()
                .nextTooltipPage();
        }

        return false;
    }

    public static boolean previousTooltipPage() {

        if (TooltipHandler.lastContext != null) {
            return TooltipHandler.lastContext.getRenderer()
                .previousTooltipPage();
        }

        return false;
    }

}
