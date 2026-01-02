package com.slprime.chromatictooltips;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import net.minecraft.client.resources.IResource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.ITooltipEnricher.EnricherMode;
import com.slprime.chromatictooltips.api.ITooltipEnricher.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipRenderer;
import com.slprime.chromatictooltips.api.TooltipBuilder;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipRequest;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.component.SectionComponent;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.Parser;

public class TooltipHandler {

    protected static final WeakHashMap<ITooltipComponent, String> tipLineComponents = new WeakHashMap<>();
    protected static int nextComponentId = 0;

    protected static final String CONFIG_FILE = "tooltip.json";
    protected static final String COMPONENT_PREFIX = "\u00A7z";

    protected static ITooltipRenderer defaultTooltipRenderer = null;
    protected static Map<String, List<ITooltipRenderer>> otherTooltipRenderers = new HashMap<>();

    protected static final Parser parser = new Parser();
    protected static Class<? extends ITooltipRenderer> rendererClass = null;
    protected static final List<ITooltipEnricher> tooltipEnrichers = new ArrayList<>();

    // cache
    protected static TooltipContext lastContext = null;
    protected static TooltipLines lastTextLines = null;
    protected static boolean ignoreLastTooltip = true;
    protected static int lastHashCode = -1;

    public static void reload() {
        TooltipHandler.otherTooltipRenderers.clear();
        TooltipHandler.defaultTooltipRenderer = null;

        loadTooltipResource();

        if (TooltipHandler.defaultTooltipRenderer == null) {
            TooltipHandler.defaultTooltipRenderer = createRenderer(new TooltipStyle());
        }
    }

    protected static void parseStyle(String json) {
        final List<TooltipStyle> scopes = TooltipHandler.parser.parse(json);

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
            io.printStackTrace();
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

    public static void addEnricher(ITooltipEnricher enricher) {
        TooltipHandler.tooltipEnrichers.add(enricher);
    }

    public static void addEnricherAfter(String sectionId, ITooltipEnricher enricher) {
        int index = -1;

        for (int i = 0; i < TooltipHandler.tooltipEnrichers.size(); i++) {
            if (TooltipHandler.tooltipEnrichers.get(i)
                .sectionId()
                .equalsIgnoreCase(sectionId)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            TooltipHandler.tooltipEnrichers.add(index + 1, enricher);
        } else {
            TooltipHandler.tooltipEnrichers.add(enricher);
        }
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
        if (request == null) {
            ChromaticTooltips.LOG.error("drawHoveringText called with null request");
            return;
        }

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
            TooltipHandler.lastContext = new TooltipContext(request, getRendererFor(request.context, request.stack));
            updateContent = true;
        }

        if (updateContent || TooltipHandler.lastHashCode != currentHash) {
            TooltipHandler.lastContext.clear();
            TooltipHandler.lastContext.setStack(request.stack);
            TooltipHandler.lastContext.setPosition(mouse);

            TooltipHandler.lastHashCode = currentHash;
            TooltipHandler.lastTextLines = request.tooltip;
            enrichTooltip(TooltipHandler.lastContext);
        } else {
            TooltipHandler.lastContext.setPosition(mouse);
        }

        TooltipHandler.ignoreLastTooltip = false;
    }

    protected static boolean areItemStackEqual(ItemStack stackA, ItemStack stackB) {
        if (stackA == null && stackB == null) return true;
        if (stackA == null || stackB == null) return false;
        if (!stackA.isItemEqual(stackB)) return false;

        if (stackA.hasTagCompound() && stackB.hasTagCompound()) {
            return stackA.stackTagCompound.equals(stackB.stackTagCompound);
        }

        return (stackA.stackTagCompound == null || stackA.stackTagCompound.hasNoTags())
            && (stackB.stackTagCompound == null || stackB.stackTagCompound.hasNoTags());
    }

    protected static void clearCache() {
        TooltipHandler.lastContext = null;
        TooltipHandler.ignoreLastTooltip = true;
        TooltipHandler.lastTextLines = null;
        TooltipHandler.lastHashCode = 0;
    }

    protected static void enrichTooltip(TooltipContext context) {
        final int enricherCount = TooltipHandler.tooltipEnrichers.size();
        final EnricherMode activeModifier = ClientUtil.getActiveModifier();
        final ITooltipRenderer renderer = context.getRenderer();

        final List<ITooltipComponent> headerSections = new ArrayList<>(enricherCount);
        final List<ITooltipComponent> bodySections = new ArrayList<>(enricherCount);
        final List<ITooltipComponent> footerSections = new ArrayList<>(enricherCount);

        context.setEnricherMode(activeModifier);

        for (ITooltipEnricher enricher : TooltipHandler.tooltipEnrichers) {
            final EnumSet<EnricherMode> modes = renderer.getEnricherModes(enricher.sectionId(), enricher.mode());

            if (modes.contains(EnricherMode.ALWAYS) || modes.contains(activeModifier)) {
                final List<ITooltipComponent> result = enricher.build(context);

                if (result != null && !result.isEmpty()) {
                    final EnricherPlace place = renderer.getEnricherPlace(enricher.sectionId(), enricher.place());
                    final String sectionId = enricher.sectionId();
                    final SectionComponent section = new SectionComponent(
                        sectionId,
                        renderer.getSectionBox("sections." + sectionId),
                        result);

                    if (place == ITooltipEnricher.EnricherPlace.HEADER) {
                        headerSections.add(section);
                    } else if (place == ITooltipEnricher.EnricherPlace.BODY) {
                        bodySections.add(section);
                    } else if (place == ITooltipEnricher.EnricherPlace.FOOTER) {
                        footerSections.add(section);
                    }

                }
            }
        }

        if (bodySections.isEmpty() && activeModifier != EnricherMode.DEFAULT) {
            context.setEnricherMode(EnricherMode.DEFAULT);

            for (ITooltipEnricher enricher : TooltipHandler.tooltipEnrichers) {
                final EnumSet<EnricherMode> modes = renderer.getEnricherModes(enricher.sectionId(), enricher.mode());
                final EnricherPlace place = renderer.getEnricherPlace(enricher.sectionId(), enricher.place());

                if (place == ITooltipEnricher.EnricherPlace.BODY && modes.contains(EnricherMode.DEFAULT)) {
                    final List<ITooltipComponent> result = enricher.build(context);

                    if (result != null && !result.isEmpty()) {
                        final String sectionId = enricher.sectionId();
                        final SectionComponent section = new SectionComponent(
                            sectionId,
                            renderer.getSectionBox("sections." + sectionId),
                            result);
                        bodySections.add(section);
                    }
                }
            }
        }

        context.addSection("header", headerSections);
        context.addSection("body", bodySections);
        context.addSection("footer", footerSections);
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

        if (stack != null && !"item".equals(context) && !"default".equals(context)) {
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
