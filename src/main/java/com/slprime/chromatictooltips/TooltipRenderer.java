package com.slprime.chromatictooltips;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipRenderer;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.api.TooltipTarget;
import com.slprime.chromatictooltips.component.SectionComponent;
import com.slprime.chromatictooltips.component.SpaceComponent;
import com.slprime.chromatictooltips.component.TextComponent;
import com.slprime.chromatictooltips.config.GeneralConfig;
import com.slprime.chromatictooltips.event.RenderTooltipEvent;
import com.slprime.chromatictooltips.util.ItemStackFilterParser;
import com.slprime.chromatictooltips.util.SectionBox;
import com.slprime.chromatictooltips.util.TooltipFontContext;
import com.slprime.chromatictooltips.util.TooltipSpacing;
import com.slprime.chromatictooltips.util.TooltipUtils;

public class TooltipRenderer implements ITooltipRenderer {

    protected Map<String, SectionBox> sectionBoxCache = new HashMap<>();
    protected Map<String, SpaceComponent> spacingCache = new HashMap<>();
    protected Map<String, EnumSet<TooltipModifier>> tooltipModifierCache = new HashMap<>();
    protected Map<String, EnricherPlace> enricherPlaceCache = new HashMap<>();

    protected int mainAxisOffset = 6;
    protected int crossAxisOffset = -18;
    protected int maxWidth = Integer.MAX_VALUE;
    protected SectionBox navigationBox = null;
    protected SectionBox tooltipBox;

    protected Predicate<TooltipTarget> filter;
    protected TooltipStyle tooltipStyle;

    public TooltipRenderer(TooltipStyle style) {
        this.filter = ItemStackFilterParser.parse(style.getAsString("filter", ""));
        this.maxWidth = style.getAsInt("maxWidth", this.maxWidth);
        this.navigationBox = new SectionBox(style.getAsStyle("navigation"));
        this.tooltipBox = new SectionBox(style);
        this.tooltipStyle = style;

        if (!style.containsKey("divider")) {
            this.spacingCache.put("divider", new SpaceComponent(new TooltipStyle(createDefaultDivider())));
        }

        final int[] offset = style.getAsProperty(
            "offset",
            new String[][] { new String[] { "main" }, new String[] { "cross" } },
            new int[] { this.mainAxisOffset, this.crossAxisOffset });

        this.mainAxisOffset = offset[0];
        this.crossAxisOffset = offset[1];

        generateEnricherCaches();
    }

    @Override
    public int getMainAxisOffset() {
        return this.mainAxisOffset;
    }

    @Override
    public int getCrossAxisOffset() {
        return this.crossAxisOffset;
    }

    protected JsonObject createDefaultDivider() {
        final JsonObject dividerStyle = new JsonObject();
        final JsonObject decorator = new JsonObject();

        decorator.addProperty("type", "background");
        decorator.addProperty("color", "0xFFFFFFFF");
        decorator.addProperty("alignBlock", "center");
        decorator.addProperty("height", 1);
        dividerStyle.add("decorator", decorator);
        dividerStyle.addProperty("height", 10);

        return dividerStyle;
    }

    protected void generateEnricherCaches() {
        final JsonObject sections = this.tooltipStyle.getAsJsonObject("sections", new JsonObject());

        for (Map.Entry<String, JsonElement> entry : sections.entrySet()) {
            if (entry.getValue() == null || !entry.getValue()
                .isJsonObject()) {
                continue;
            }

            final String sectionId = entry.getKey();
            final JsonObject section = entry.getValue()
                .getAsJsonObject();

            if (section.has("modes")) {
                final EnumSet<TooltipModifier> values = EnumSet.noneOf(TooltipModifier.class);

                for (JsonElement modeElement : section.getAsJsonArray("modes")) {
                    final String mode = modeElement.getAsString();
                    if (mode != null && !mode.isEmpty()) {
                        values.add(TooltipModifier.fromString(mode));
                    }
                }

                this.tooltipModifierCache.put(sectionId, values);
            }

            if (section.has("place")) {
                this.enricherPlaceCache.put(
                    sectionId,
                    EnricherPlace.fromString(
                        section.get("place")
                            .getAsString()));
            }
        }

    }

    @Override
    public boolean matches(TooltipTarget target) {
        return this.filter == null || this.filter.test(target);
    }

    @Override
    public TooltipStyle getStyle() {
        return this.tooltipStyle;
    }

    public EnumSet<TooltipModifier> getEnricherModes(String enricherId, EnumSet<TooltipModifier> defaultModes) {
        return this.tooltipModifierCache.getOrDefault(enricherId, defaultModes);
    }

    public EnricherPlace getEnricherPlace(String enricherId, EnricherPlace defaultPlace) {
        return this.enricherPlaceCache.getOrDefault(enricherId, defaultPlace);
    }

    @Override
    public SectionBox getSectionBox(String path) {
        return this.sectionBoxCache.computeIfAbsent(path, p -> new SectionBox(this.tooltipStyle.getAsStyle(p)));
    }

    @Override
    public SpaceComponent getSpacing(String path) {
        return this.spacingCache.computeIfAbsent(path, p -> new SpaceComponent(this.tooltipStyle.getAsStyle(p)));
    }

    protected List<ITooltipComponent> prepareComponents(List<SectionComponent> components) {
        final List<ITooltipComponent> result = new ArrayList<>(components);

        if (result.size() > 1 && getSpacing("hr").getHeight() > 0) {
            result.add(1, getSpacing("hr"));
        }

        return result;
    }

    protected List<SectionComponent> paginateComponents(TooltipContext context, SectionComponent component,
        int maxWidth, int maxHeight) {
        maxWidth = Math.max(
            Math.min(GeneralConfig.maxWidth == 0 ? this.maxWidth : Math.max(50, GeneralConfig.maxWidth), maxWidth),
            this.tooltipBox.getMinWidth());
        maxHeight = Math.max(maxHeight, this.tooltipBox.getMinHeight());

        final List<SectionComponent> pages = new ArrayList<>();
        final int paginationHeight = this.navigationBox.getBlock() + TooltipFontContext.getFontHeight()
            - TooltipFontContext.DEFAULT_SPACING;
        ITooltipComponent[] split = component.paginate(context, maxWidth, maxHeight);
        final SectionComponent firstPage = (SectionComponent) split[0];

        if (split.length == 1) {
            return Collections.singletonList(firstPage);
        }

        maxHeight = Math.max(0, maxHeight - paginationHeight);

        while (component != null) {
            split = component.paginate(context, maxWidth, maxHeight);
            pages.add((SectionComponent) split[0]);

            if (split.length > 1) {
                component = (SectionComponent) split[1];
            } else {
                component = null;
            }

        }

        return pages;
    }

    protected void addNavigation(SectionComponent component, int currentPage, int totalPages) {
        final int nextKeyCode = ClientProxy.nextPage.getKeyCode();
        final int previousKeyCode = ClientProxy.previousPage.getKeyCode();
        final String nextKey = nextKeyCode != 0 ? Keyboard.getKeyName(nextKeyCode) : "";
        final String previousKey = previousKeyCode != 0 ? Keyboard.getKeyName(previousKeyCode) : "";
        String text = "";

        if (!nextKey.isEmpty() && !previousKey.isEmpty()) {
            text = TooltipUtils.translate("navigation.page", currentPage, totalPages, nextKey, previousKey);
        } else if (!nextKey.isEmpty()) {
            text = TooltipUtils.translate("navigation.page.next_only", currentPage, totalPages, nextKey);
        } else if (!previousKey.isEmpty()) {
            text = TooltipUtils.translate("navigation.page.previous_only", currentPage, totalPages, previousKey);
        }

        if (!text.isEmpty()) {
            component.clearPendingComponent();
            component.addComponent(
                new SectionComponent(
                    "navigation",
                    this.navigationBox,
                    Collections.singletonList(new TextComponent(text))));
        }
    }

    @Override
    public List<SectionComponent> paginateTooltip(TooltipContext context) {
        final SectionComponent section = new SectionComponent(
            "page",
            this.tooltipBox,
            prepareComponents(context.getSections()));

        final int scaleFactor = context.getScaleFactor();
        final Dimension freeSpace = getFreeSpace(scaleFactor);
        final List<SectionComponent> pagedComponents = paginateComponents(
            context,
            section,
            freeSpace.width,
            freeSpace.height);

        if (pagedComponents.size() > 1) {
            for (int i = 0; i < pagedComponents.size(); i++) {
                addNavigation(pagedComponents.get(i), i + 1, pagedComponents.size());
            }
        }

        return pagedComponents;
    }

    protected Dimension getFreeSpace(int scaleFactor) {
        final Minecraft mc = TooltipUtils.mc();
        return new Dimension(
            (int) Math.ceil(mc.displayWidth / (float) scaleFactor),
            (int) Math.ceil(mc.displayHeight / (float) scaleFactor));
    }

    @Override
    public void draw(TooltipContext context, int x, int y) {
        final SectionComponent section = context.getActivePageComponent();

        if (section != null && !section.isEmpty()) {
            final TooltipSpacing margin = section.getMargin();
            final int width = section.getWidth() - margin.getInline();
            final int height = section.getHeight() - margin.getBlock();
            final float scaleShift = (float) context.getScaleFactor() / TooltipUtils.getScaledResolution()
                .getScaleFactor();
            final Rectangle tooltipRectangle = new Rectangle(
                x + (int) (margin.getLeft() * scaleShift),
                y + (int) (margin.getTop() * scaleShift),
                (int) (width * scaleShift),
                (int) (height * scaleShift));

            drawContent(context, section, scaleShift, x, y);
            TooltipUtils.postEvent(new RenderTooltipEvent(context, section, tooltipRectangle));
        }

    }

    protected void drawContent(TooltipContext context, SectionComponent section, float scaleShift, int x, int y) {
        GL11.glPushMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        TooltipUtils.incZLevel(DEFAULT_Z_INDEX);

        if (scaleShift != 1.0f) {
            GL11.glTranslatef(x, y, 0);
            GL11.glScalef(scaleShift, scaleShift, 1);
            section.draw(0, 0, section.getWidth(), context);
            GL11.glScalef(1 / scaleShift, 1 / scaleShift, 1);
        } else {
            section.draw(x, y, section.getWidth(), context);
        }

        TooltipUtils.incZLevel(-DEFAULT_Z_INDEX);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
    }

}
