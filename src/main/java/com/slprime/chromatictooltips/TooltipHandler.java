package com.slprime.chromatictooltips;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.client.resources.IResource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.ITooltipRenderer;
import com.slprime.chromatictooltips.api.TooltipBuilder;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.api.TooltipRequest;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.api.TooltipTarget;
import com.slprime.chromatictooltips.component.SectionComponent;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.config.GeneralConfig;
import com.slprime.chromatictooltips.event.TooltipEnricherEvent;
import com.slprime.chromatictooltips.util.ComponentRegistry;
import com.slprime.chromatictooltips.util.Parser;
import com.slprime.chromatictooltips.util.TooltipUtils;

public class TooltipHandler {

    private static class TooltipCache {

        public long lastFrameTime = -1;
        public long lastUpdateTime = -1;
        public TooltipContext context = null;
        public TooltipRequest request = null;
        public int hashCode = -1;

        public void reset() {
            this.lastFrameTime = -1;
            this.lastUpdateTime = -1;
            this.context = null;
            this.request = null;
            this.hashCode = -1;
        }
    }

    private static class ShowDelayTracker {

        public long hoverStartTime = -1;
        public boolean track = false;

        public void reset() {
            this.hoverStartTime = -1;
            this.track = false;
        }
    }

    protected static final int MIN_FPS = 8;
    protected static final int MAX_FPS = 60;

    protected static final ComponentRegistry componentRegistry = new ComponentRegistry();

    protected static final String CONFIG_FILE = "tooltip.json";
    protected static final String COMPONENT_PREFIX = "\u00A7z";

    protected static ITooltipRenderer defaultTooltipRenderer = null;
    protected static Map<String, List<ITooltipRenderer>> otherTooltipRenderers = new HashMap<>();

    protected static final Parser parser = new Parser();
    protected static Class<? extends ITooltipRenderer> rendererClass = null;

    protected static final TooltipCache tooltipCache = new TooltipCache();
    protected static final ShowDelayTracker showDelayTracker = new ShowDelayTracker();
    protected static Point lastMousePosition = null;
    protected static boolean renderLastTooltip = false;

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
            final IResource res = TooltipUtils.mc()
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
        return TooltipHandler.COMPONENT_PREFIX + TooltipHandler.componentRegistry.add(component);
    }

    public static ITooltipComponent getTooltipComponent(String line) {
        if (!line.startsWith(TooltipHandler.COMPONENT_PREFIX)) return null;
        try {
            final int token = Integer.parseInt(line.substring(TooltipHandler.COMPONENT_PREFIX.length()));
            return TooltipHandler.componentRegistry.get(token);
        } catch (NumberFormatException e) {
            return null;
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
        drawHoveringText(new TooltipRequest(null, TooltipTarget.ofItem(stack), new TooltipLines(textLines), null));
    }

    public static void drawHoveringText(List<?> textLines) {
        drawHoveringText(new TooltipRequest(null, TooltipTarget.ofItem(null), new TooltipLines(textLines), null));
    }

    public static void drawHoveringText(TooltipRequest request) {

        if (request == null || !request.target.isItem() && !request.target.isFluid() && request.tooltip.isEmpty()) {
            return;
        }

        final long currentTime = System.currentTimeMillis();

        if (GeneralConfig.tooltipShowUpDelay > 0 && !handleTooltipShowDelay(request, currentTime)) {
            return;
        }

        // FPS throttling
        if ((currentTime - TooltipHandler.tooltipCache.lastFrameTime) > 1_000 / MAX_FPS) {
            final int currentHash = TooltipUtils.getMetaHash();

            if (TooltipHandler.tooltipCache.context == null
                || !request.sameSubjectAs(TooltipHandler.tooltipCache.request)) {
                TooltipHandler.tooltipCache.lastUpdateTime = currentTime;
                TooltipHandler.tooltipCache.request = request.copy();

                TooltipHandler.tooltipCache.context = createTooltipContext(request, null);
            } else if (TooltipHandler.tooltipCache.hashCode != currentHash
                || (currentTime - TooltipHandler.tooltipCache.lastUpdateTime) > 1_000 / MIN_FPS
                || !request.equivalentTo(TooltipHandler.tooltipCache.request)) {
                    TooltipHandler.tooltipCache.request = request.copy();
                    TooltipHandler.tooltipCache.lastUpdateTime = currentTime;

                    TooltipHandler.tooltipCache.context = createTooltipContext(
                        request,
                        TooltipHandler.tooltipCache.context);
                }

            TooltipHandler.tooltipCache.hashCode = currentHash;
            TooltipHandler.tooltipCache.lastFrameTime = currentTime;
        }

        TooltipHandler.lastMousePosition = request.mouse != null ? request.mouse : TooltipUtils.getMousePosition();
        TooltipHandler.renderLastTooltip = true;
    }

    protected static boolean handleTooltipShowDelay(TooltipRequest request, long currentTime) {

        if (TooltipHandler.showDelayTracker.hoverStartTime == -1) {
            TooltipHandler.showDelayTracker.hoverStartTime = currentTime;
        }

        if ((currentTime - TooltipHandler.showDelayTracker.hoverStartTime) < GeneralConfig.tooltipShowUpDelay) {
            final Point mouse = request.mouse != null ? request.mouse : TooltipUtils.getMousePosition();

            if (TooltipHandler.lastMousePosition != null
                && (mouse.x != TooltipHandler.lastMousePosition.x || mouse.y != TooltipHandler.lastMousePosition.y)) {
                TooltipHandler.showDelayTracker.hoverStartTime = -1;
            }

            TooltipHandler.lastMousePosition = mouse;
            TooltipHandler.showDelayTracker.track = true;
            return false;
        }

        return true;
    }

    public static TooltipContext createTooltipContext(TooltipRequest request, TooltipContext previousContext) {
        request = TooltipRegistry.resolveRequest(request);
        TooltipContext context;

        if (previousContext != null) {
            context = new TooltipContext(request, previousContext);
        } else {
            context = new TooltipContext(request, getRendererFor(request));
        }

        if (EnricherConfig.keyboardModifiersEnabled) {
            updateSupportedModifiers(context);
        }

        enrichTooltip(context);
        return context;
    }

    protected static void enrichTooltip(TooltipContext context) {
        final TooltipModifier activeModifier = TooltipUtils.getActiveModifier();
        final TooltipStyle style = context.getRenderer()
            .getStyle();
        final ITooltipRenderer renderer = context.getRenderer();
        final List<SectionComponent> headerSections = new ArrayList<>();
        final List<SectionComponent> bodySections = new ArrayList<>();
        final List<SectionComponent> footerSections = new ArrayList<>();
        final Comparator<SectionComponent> byOrder = Comparator
            .comparingInt(s -> style.getAsInt("sections." + s.getSectionId() + ".order", 0));

        context.setActiveModifier(activeModifier);

        for (ITooltipEnricher enricher : TooltipRegistry.getEnrichers()) {
            final EnumSet<TooltipModifier> modes = renderer.getEnricherModes(enricher.sectionId(), enricher.mode());
            final EnricherPlace place = renderer.getEnricherPlace(enricher.sectionId(), enricher.place());

            if (modes.contains(TooltipModifier.NONE) && (place == EnricherPlace.HEADER || place == EnricherPlace.FOOTER)
                || modes.contains(activeModifier)) {
                final TooltipLines result = enricher.build(context);

                if (result != null && !result.isEmpty()) {
                    final String sectionId = enricher.sectionId();
                    final SectionComponent section = new SectionComponent(
                        sectionId,
                        renderer.getSectionBox("sections." + sectionId),
                        result.build(context));

                    if (place == EnricherPlace.HEADER) {
                        headerSections.add(section);
                    } else if (place == EnricherPlace.BODY) {
                        bodySections.add(section);
                    } else if (place == EnricherPlace.FOOTER) {
                        footerSections.add(section);
                    }

                }
            }
        }

        if (bodySections.isEmpty() && activeModifier != TooltipModifier.NONE) {
            bodySections.addAll(fallbackBuildBodyList(context));
        }

        headerSections.sort(byOrder);
        bodySections.sort(byOrder);
        footerSections.sort(byOrder);

        context.addSection("header", new ArrayList<>(headerSections));
        context.addSection("body", new ArrayList<>(bodySections));
        context.addSection("footer", new ArrayList<>(footerSections));

        TooltipUtils.postEvent(new TooltipEnricherEvent(context));
    }

    public static void updateSupportedModifiers(TooltipContext context) {
        final ITooltipRenderer renderer = context.getRenderer();

        for (ITooltipEnricher enricher : TooltipRegistry.getEnrichers()) {
            final EnricherPlace place = renderer.getEnricherPlace(enricher.sectionId(), enricher.place());

            if ("itemInfo".equals(enricher.sectionId()) || place != EnricherPlace.BODY) {
                continue;
            }

            final EnumSet<TooltipModifier> modes = renderer.getEnricherModes(enricher.sectionId(), enricher.mode());
            TooltipLines noneComponents = null;

            if (modes.contains(TooltipModifier.NONE)) {
                context.setActiveModifier(TooltipModifier.NONE);
                noneComponents = enricher.build(context);
            }

            for (TooltipModifier modifier : modes) {

                if (modifier == TooltipModifier.NONE || context.getSupportedModifiers()
                    .contains(modifier)) {
                    continue;
                }

                context.setActiveModifier(modifier);
                final TooltipLines result = enricher.build(context);

                if (result != null && !result.isEmpty() && (noneComponents == null || !result.equals(noneComponents))) {
                    context.supportModifiers(modifier);
                }
            }

        }
    }

    protected static List<SectionComponent> fallbackBuildBodyList(TooltipContext context) {
        final List<SectionComponent> bodySections = new ArrayList<>();
        final ITooltipRenderer renderer = context.getRenderer();
        context.setActiveModifier(TooltipModifier.NONE);

        for (ITooltipEnricher enricher : TooltipRegistry.getEnrichers()) {
            final EnumSet<TooltipModifier> modes = renderer.getEnricherModes(enricher.sectionId(), enricher.mode());
            final EnricherPlace place = renderer.getEnricherPlace(enricher.sectionId(), enricher.place());

            if (place == EnricherPlace.BODY && modes.contains(TooltipModifier.NONE)) {
                final TooltipLines result = enricher.build(context);

                if (result != null && !result.isEmpty()) {
                    final String sectionId = enricher.sectionId();
                    final SectionComponent section = new SectionComponent(
                        sectionId,
                        renderer.getSectionBox("sections." + sectionId),
                        result.build(context));
                    bodySections.add(section);
                }
            }
        }

        return bodySections;
    }

    protected static ITooltipRenderer getRendererFor(TooltipRequest request) {
        final String fallbackContext = request.target.isItem() ? "item"
            : (request.target.isFluid() ? "fluid" : "default");
        final String context = request.context != null ? request.context : fallbackContext;

        if ("default".equals(context)) {
            return TooltipHandler.defaultTooltipRenderer;
        }

        ITooltipRenderer renderer = findRenderer(context, request.target);

        if (renderer == null && (request.target.isItem() || request.target.isFluid())
            && !fallbackContext.equals(context)) {
            renderer = findRenderer(fallbackContext, request.target);
        }

        if (renderer == null && request.target.isFluid()) {
            renderer = findRenderer("item", null);
        }

        return renderer != null ? renderer : TooltipHandler.defaultTooltipRenderer;
    }

    private static ITooltipRenderer findRenderer(String context, TooltipTarget target) {
        for (ITooltipRenderer renderer : TooltipHandler.otherTooltipRenderers
            .getOrDefault(context, Collections.emptyList())) {
            if (renderer.matches(target)) {
                return renderer;
            }
        }
        return null;
    }

    public static TooltipContext getLastTooltipContext() {
        return TooltipHandler.tooltipCache.context;
    }

    public static void drawLastTooltip() {

        if (TooltipHandler.tooltipCache.context == null) {

            if (TooltipHandler.showDelayTracker.track) {
                TooltipHandler.showDelayTracker.track = false;
            } else {
                TooltipHandler.showDelayTracker.hoverStartTime = -1;
            }

        } else if (TooltipHandler.renderLastTooltip) {
            TooltipHandler.tooltipCache.context
                .drawAtMousePosition(TooltipHandler.lastMousePosition.x, TooltipHandler.lastMousePosition.y);
            TooltipHandler.renderLastTooltip = false;
        } else {
            TooltipHandler.tooltipCache.reset();
            TooltipHandler.showDelayTracker.reset();
        }

    }

    public static boolean nextTooltipPage() {

        if (TooltipHandler.tooltipCache.context != null) {
            return TooltipHandler.tooltipCache.context.nextTooltipPage();
        }

        return false;
    }

    public static boolean previousTooltipPage() {

        if (TooltipHandler.tooltipCache.context != null) {
            return TooltipHandler.tooltipCache.context.previousTooltipPage();
        }

        return false;
    }

}
