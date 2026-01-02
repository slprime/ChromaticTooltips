package com.slprime.chromatictooltips;

import java.awt.Point;
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
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher.EnricherMode;
import com.slprime.chromatictooltips.api.ITooltipEnricher.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipRenderer;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.component.SectionComponent;
import com.slprime.chromatictooltips.component.SpaceComponent;
import com.slprime.chromatictooltips.component.TextComponent;
import com.slprime.chromatictooltips.config.GeneralConfig;
import com.slprime.chromatictooltips.event.RenderTooltipEvent;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.ItemStackFilterParser;
import com.slprime.chromatictooltips.util.SectionBox;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class TooltipRenderer implements ITooltipRenderer {

    protected static class TooltipSectionBox extends SectionBox {

        protected int maxWidth = Integer.MAX_VALUE;
        protected SectionBox navigationBox = null;

        public TooltipSectionBox(TooltipStyle style) {
            super(style);

            this.maxWidth = style.getAsInt("maxWidth", this.maxWidth);
            this.navigationBox = new SectionBox(style.getAsStyle("navigation"));
        }

        public TooltipSectionBox(TooltipSectionBox box) {
            super(box);

            this.maxWidth = box.maxWidth;
            this.navigationBox = box.navigationBox;
        }

        public int getMaxWidth() {
            return this.maxWidth;
        }

        public int getNavigationHeight() {
            return this.navigationBox.getBlock() + TooltipFontContext.getFontHeight()
                - TooltipFontContext.DEFAULT_SPACING;
        }
    }

    protected Map<String, SectionBox> sectionBoxCache = new HashMap<>();
    protected Map<String, SpaceComponent> spacingCache = new HashMap<>();
    protected Map<String, EnumSet<EnricherMode>> enricherModesCache = new HashMap<>();
    protected Map<String, EnricherPlace> enricherPlaceCache = new HashMap<>();

    protected int mainAxisOffset = 6;
    protected int crossAxisOffset = -18;
    protected TooltipSectionBox tooltipSectionBox;

    protected TooltipContext lastContext = null;
    protected int lastRevision = 0;

    protected SectionComponent pagedTooltipComponent = null;
    protected int currentPage = 0;
    protected int totalPages = 0;
    protected Point lastPosition = null;
    protected float guiScaleFactor = 0;

    protected Predicate<ItemStack> filter;
    protected TooltipStyle tooltipStyle;

    public TooltipRenderer(TooltipStyle style) {
        this.filter = ItemStackFilterParser.parse(style.getAsString("filter", ""));
        this.tooltipSectionBox = new TooltipSectionBox(style);
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
                final EnumSet<EnricherMode> values = EnumSet.noneOf(EnricherMode.class);

                for (JsonElement modeElement : section.getAsJsonArray("modes")) {
                    final String mode = modeElement.getAsString();
                    if (mode != null && !mode.isEmpty()) {
                        values.add(EnricherMode.fromString(mode));
                    }
                }

                this.enricherModesCache.put(sectionId, values);
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
    public boolean matches(ItemStack stack) {
        return this.filter == null || this.filter.test(stack);
    }

    @Override
    public TooltipStyle getStyle() {
        return this.tooltipStyle;
    }

    public EnumSet<EnricherMode> getEnricherModes(String enricherId, EnumSet<EnricherMode> defaultModes) {
        return this.enricherModesCache.getOrDefault(enricherId, defaultModes);
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

    @Override
    public boolean nextTooltipPage() {

        if (this.lastContext != null
            && this.currentPage != (this.currentPage = (this.currentPage + 1) % this.totalPages)) {
            this.lastPosition = null;
            return true;
        }

        return false;
    }

    @Override
    public boolean previousTooltipPage() {

        if (this.lastContext != null
            && this.currentPage != (this.currentPage = (this.currentPage - 1 + this.totalPages) % this.totalPages)) {
            this.lastPosition = null;
            return true;
        }

        return false;
    }

    protected List<ITooltipComponent> prepareComponents(List<SectionComponent> components) {
        final List<ITooltipComponent> result = new ArrayList<>(components);

        if (result.size() > 1 && getSpacing("hr").getHeight() > 0) {
            result.add(1, getSpacing("hr"));
        }

        return result;
    }

    protected List<SectionComponent> paginateComponents(SectionComponent component, int maxWidth, int maxHeight) {
        maxWidth = Math
            .max(Math.min(this.tooltipSectionBox.getMaxWidth(), maxWidth), this.tooltipSectionBox.getMinWidth());
        maxHeight = Math.max(maxHeight, this.tooltipSectionBox.getMinHeight());

        final List<SectionComponent> pages = new ArrayList<>();
        ITooltipComponent[] split = component.paginate(this.lastContext, maxWidth, maxHeight);
        final SectionComponent firstPage = (SectionComponent) split[0];
        final int paginationHeight = this.tooltipSectionBox.getNavigationHeight();

        if (split.length == 1) {
            return Collections.singletonList(firstPage);
        }

        while (component != null) {
            split = component.paginate(this.lastContext, maxWidth, maxHeight - paginationHeight);
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
            text = ClientUtil.translate("navigation.page", currentPage, totalPages, nextKey, previousKey);
        } else if (!nextKey.isEmpty()) {
            text = ClientUtil.translate("navigation.page.next_only", currentPage, totalPages, nextKey);
        } else if (!previousKey.isEmpty()) {
            text = ClientUtil.translate("navigation.page.previous_only", currentPage, totalPages, previousKey);
        }

        if (!text.isEmpty()) {
            component.clearPendingComponent();
            component.addComponent(
                new SectionComponent(
                    "navigation",
                    this.tooltipSectionBox.navigationBox,
                    Collections.singletonList(new TextComponent(text))));
        }
    }

    @Override
    public Rectangle getTooltipBounds(TooltipContext context) {

        if (this.pagedTooltipComponent == null || this.lastPosition == null || context != this.lastContext) {
            return null;
        }

        return new Rectangle(
            this.lastPosition.x,
            this.lastPosition.y,
            this.pagedTooltipComponent.getWidth(),
            this.pagedTooltipComponent.getHeight());
    }

    @Override
    public void draw(TooltipContext context) {

        if (context != this.lastContext) {
            this.lastContext = context;
            this.lastPosition = null;
            this.currentPage = 0;
        }

        if (this.lastContext == null || this.lastContext.isEmpty()) {
            return;
        }

        if (this.lastRevision != this.lastContext.getRevision()) {
            this.lastRevision = this.lastContext.getRevision();
            this.lastPosition = null;
        }

        if (this.lastPosition == null) {
            final Minecraft mc = ClientUtil.mc();
            final float tooltipScale = GeneralConfig.scaleFactor == 0 ? ClientUtil.getScaledResolution()
                .getScaleFactor() : GeneralConfig.scaleFactor;
            this.guiScaleFactor = tooltipScale / ClientUtil.getScaledResolution()
                .getScaleFactor();
            final int scaledWidth = (int) Math.ceil(mc.displayWidth / tooltipScale);
            final int scaledHeight = (int) Math.ceil(mc.displayHeight / tooltipScale);

            final List<ITooltipComponent> components = prepareComponents(this.lastContext.getSections());
            final List<SectionComponent> pages = paginateComponents(
                new SectionComponent("page", this.tooltipSectionBox, components),
                scaledWidth - this.tooltipSectionBox.getInline(),
                scaledHeight - this.tooltipSectionBox.getBlock());

            this.totalPages = pages.size();
            this.currentPage = Math.max(0, Math.min(this.currentPage, this.totalPages - 1));
            this.pagedTooltipComponent = pages.get(this.currentPage);

            if (this.totalPages > 1) {
                addNavigation(this.pagedTooltipComponent, this.currentPage + 1, this.totalPages);
            }

            this.lastPosition = prepareTooltipPosition(
                this.pagedTooltipComponent.getWidth(),
                this.pagedTooltipComponent.getHeight(),
                scaleAnchorBounds(this.lastContext.getAnchorBounds(), this.guiScaleFactor),
                scaledWidth,
                scaledHeight);
        }

        if (!this.pagedTooltipComponent.isEmpty()) {
            drawContent();
            ClientUtil.postEvent(new RenderTooltipEvent(context, this.pagedTooltipComponent, this.lastPosition));
        }
    }

    protected Rectangle scaleAnchorBounds(Rectangle anchorBounds, float scaleFactor) {
        return new Rectangle(
            (int) Math.ceil(anchorBounds.x / scaleFactor),
            (int) Math.ceil(anchorBounds.y / scaleFactor),
            (int) Math.ceil(anchorBounds.width / scaleFactor),
            (int) Math.ceil(anchorBounds.height / scaleFactor));
    }

    protected void drawContent() {
        GL11.glPushMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        ClientUtil.incZLevel(DEFAULT_Z_INDEX);

        GL11.glTranslatef(this.lastPosition.x * this.guiScaleFactor, this.lastPosition.y * this.guiScaleFactor, 0);
        GL11.glScalef(this.guiScaleFactor, this.guiScaleFactor, 1);

        this.pagedTooltipComponent.draw(0, 0, this.pagedTooltipComponent.getWidth(), this.lastContext);

        GL11.glScalef(1 / this.guiScaleFactor, 1 / this.guiScaleFactor, 1);

        ClientUtil.incZLevel(-DEFAULT_Z_INDEX);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
    }

    protected Point prepareTooltipPosition(int width, int height, Rectangle anchor, int scaledWidth, int scaledHeight) {
        final int offsetMain = anchor.width == 0 && anchor.height == 0 ? this.mainAxisOffset : 0;
        final int offsetCross = anchor.width == 0 && anchor.height == 0 ? this.crossAxisOffset : 0;
        final Point rightPoint = new Point(anchor.x + anchor.width + offsetMain, anchor.y + offsetCross);
        final Point leftPoint = new Point(anchor.x - width - offsetMain, anchor.y + offsetCross);
        final Point topPoint = new Point(anchor.x + offsetCross, anchor.y - height - offsetMain);
        final Point bottomPoint = new Point(anchor.x + offsetCross, anchor.y + anchor.height + offsetMain);

        if (rightPoint.x + width <= scaledWidth) {
            rightPoint.y = clamp(rightPoint.y, 0, scaledHeight - height);
            return rightPoint;
        }

        if (leftPoint.x >= 0) {
            leftPoint.y = clamp(leftPoint.y, 0, scaledHeight - height);
            return leftPoint;
        }

        if (bottomPoint.y + height <= scaledHeight) {
            bottomPoint.x = clamp(bottomPoint.x, 0, scaledWidth - width);
            return bottomPoint;
        }

        if (topPoint.y >= 0) {
            topPoint.x = clamp(topPoint.x, 0, scaledWidth - width);
            return topPoint;
        }

        // more space on right side
        if (anchor.x < scaledWidth - anchor.x - anchor.width) {
            rightPoint.y = clamp(rightPoint.y, 0, scaledHeight - height);
            rightPoint.x = clamp(rightPoint.x, 0, scaledWidth - width);
            return rightPoint;
        } else {
            leftPoint.y = clamp(leftPoint.y, 0, scaledHeight - height);
            leftPoint.x = clamp(leftPoint.x, 0, scaledWidth - width);
            return leftPoint;
        }

    }

    protected int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

}
