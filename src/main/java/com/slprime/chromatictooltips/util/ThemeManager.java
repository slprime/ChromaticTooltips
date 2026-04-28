package com.slprime.chromatictooltips.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.TooltipRenderer;
import com.slprime.chromatictooltips.api.ITooltipRenderer;
import com.slprime.chromatictooltips.api.TooltipRequest;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.api.TooltipTarget;
import com.slprime.chromatictooltips.config.GeneralConfig;

import cpw.mods.fml.client.FMLFileResourcePack;

public class ThemeManager {

    protected static final ResourceLocation DEFAULT_TOOLTIP_LOCATION = new ResourceLocation(
        ChromaticTooltips.MODID,
        "tooltip.json");

    protected final Parser parser = new Parser();
    protected Class<? extends ITooltipRenderer> rendererClass = null;

    protected ITooltipRenderer defaultTooltipRenderer = null;
    protected Map<String, List<ITooltipRenderer>> otherTooltipRenderers = new HashMap<>();
    protected IResourcePack[] resourcePacks = new IResourcePack[0];
    protected IMetadataSerializer frmMetadataSerializer;

    public void reloadThemes() {
        this.defaultTooltipRenderer = null;
        this.otherTooltipRenderers.clear();

        try {
            final SimpleReloadableResourceManager resourceManager = (SimpleReloadableResourceManager) TooltipUtils.mc()
                .getResourceManager();
            final FallbackResourceManager fallback = resourceManager.domainResourceManagers
                .get(ChromaticTooltips.MODID);

            if (fallback != null) {
                this.resourcePacks = fallback.resourcePacks.toArray(new IResourcePack[0]);
                this.frmMetadataSerializer = fallback.frmMetadataSerializer;
            } else {
                this.resourcePacks = new IResourcePack[0];
                this.frmMetadataSerializer = null;
            }

        } catch (Exception e) {
            ChromaticTooltips.LOG.error("Failed to get resource pack list", e);
            this.resourcePacks = new IResourcePack[0];
            this.frmMetadataSerializer = null;
        }

        loadTooltipResource(null);
        loadTooltipResource("default");

        if (this.defaultTooltipRenderer == null) {
            this.defaultTooltipRenderer = createRenderer(new TooltipStyle());
        }

    }

    public void setRendererClass(Class<? extends ITooltipRenderer> rendererClass) {
        this.rendererClass = rendererClass;
        reloadThemes();
    }

    public boolean hasTooltipRendererFor(TooltipRequest request) {
        return getRendererFor(request) != this.defaultTooltipRenderer;
    }

    public ITooltipRenderer getRendererFor(TooltipRequest request) {
        final String fallbackContext = request.target.isItem() ? "item"
            : (request.target.isFluid() ? "fluid" : "default");
        final String context = request.context != null ? request.context : fallbackContext;

        if ("default".equals(context)) {
            return this.defaultTooltipRenderer;
        }

        ITooltipRenderer renderer = findRenderer(context, request.target);

        if (renderer == null && (request.target.isItem() || request.target.isFluid())
            && !fallbackContext.equals(context)) {
            renderer = findRenderer(fallbackContext, request.target);
        }

        if (renderer == null && request.target.isFluid()) {
            renderer = findRenderer("item", null);
        }

        return renderer != null ? renderer : this.defaultTooltipRenderer;
    }

    protected ITooltipRenderer findRenderer(String context, TooltipTarget target) {

        if (!"default".equals(context) && !this.otherTooltipRenderers.containsKey(context)) {
            this.otherTooltipRenderers.put(context, new ArrayList<>());
            loadTooltipResource(context);
        }

        for (ITooltipRenderer renderer : this.otherTooltipRenderers.getOrDefault(context, Collections.emptyList())) {
            if (renderer.matches(target)) {
                return renderer;
            }
        }

        return null;
    }

    protected void parseStyle(String json, String context) {
        final List<TooltipStyle> scopes = this.parser.parse(json);

        for (TooltipStyle style : scopes) {
            String localContext = context != null ? context : style.getAsString("context", null);

            if (localContext == null && style.containsKey("filter")) {
                localContext = "item";
            }

            if (localContext == null || "default".equals(localContext)) {
                this.defaultTooltipRenderer = createRenderer(style);
            } else {
                this.otherTooltipRenderers.computeIfAbsent(localContext, k -> new ArrayList<>())
                    .add(createRenderer(style));
            }

        }

    }

    protected IResource getResource(ResourceLocation location) {

        try {
            for (int i = this.resourcePacks.length - 1; i >= 0; --i) {
                final IResourcePack resourcepack = this.resourcePacks[i];

                if (GeneralConfig.enabledResourcePackThemes || resourcepack instanceof FMLFileResourcePack) {

                    if (resourcepack.resourceExists(location)) {
                        final ResourceLocation resourcelocation = getLocationMcmeta(location);
                        InputStream inputstream = null;

                        if (resourcepack.resourceExists(resourcelocation)) {
                            inputstream = resourcepack.getInputStream(resourcelocation);
                        }

                        return new SimpleResource(
                            location,
                            resourcepack.getInputStream(location),
                            inputstream,
                            this.frmMetadataSerializer);
                    }

                    if (!DEFAULT_TOOLTIP_LOCATION.equals(location)
                        && resourcepack.resourceExists(DEFAULT_TOOLTIP_LOCATION)) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            // skip
        }

        return null;
    }

    protected void loadTooltipResource(String context) {
        final String file = context != null ? "tooltip." + context + ".json" : "tooltip.json";
        final ResourceLocation location = new ResourceLocation(ChromaticTooltips.MODID, file);
        final IResource resource = getResource(location);

        if (resource != null) {
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                ChromaticTooltips.LOG.info("Loading '{}'", location);
                final String json = reader.lines()
                    .collect(Collectors.joining("\n"));
                parseStyle(json, context);
            } catch (Exception e) {
                ChromaticTooltips.LOG.error("Failed to load '{}'", location, e);
                e.printStackTrace();
            }
        }

    }

    protected ITooltipRenderer createRenderer(TooltipStyle style) {
        try {
            return this.rendererClass.getConstructor(TooltipStyle.class)
                .newInstance(style);
        } catch (Exception e1) {
            return new TooltipRenderer(style);
        }
    }

    protected ResourceLocation getLocationMcmeta(ResourceLocation location) {
        return new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".mcmeta");
    }

}
