package com.slprime.chromatictooltips;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.gson.JsonObject;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipRenderer;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.component.SectionTooltipComponent;
import com.slprime.chromatictooltips.component.SpaceTooltipComponent;
import com.slprime.chromatictooltips.component.TextTooltipComponent;
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
            return this.navigationBox.getBlock() + TooltipFontContext.getFontRenderer().FONT_HEIGHT;
        }
    }

    protected Map<String, SectionBox> sectionBoxCache = new HashMap<>();
    protected Map<String, SpaceTooltipComponent> spacingCache = new HashMap<>();

    protected int mainAxisOffset = 6;
    protected int crossAxisOffset = -18;
    protected TooltipSectionBox tooltipSectionBox;

    protected TooltipContext lastContext = null;
    protected int lastRevision = 0;

    protected SectionTooltipComponent pagedTooltipComponent = null;
    protected int currentPage = 0;
    protected int totalPages = 0;
    protected Point lastPosition = null;

    protected Predicate<ItemStack> filter;
    protected TooltipStyle tooltipStyle;

    public TooltipRenderer(TooltipStyle style) {
        this.filter = ItemStackFilterParser.parse(style.getAsString("filter", ""));
        this.tooltipSectionBox = new TooltipSectionBox(style);
        this.tooltipStyle = style;

        if (!style.containsKey("divider")) {
            this.spacingCache.put("divider", new SpaceTooltipComponent(new TooltipStyle(createDefaultDivider())));
        }

        final int[] offset = style.getAsProperty(
            "offset",
            new String[][] { new String[] { "main" }, new String[] { "cross" }, },
            new int[] { this.mainAxisOffset, this.crossAxisOffset });

        this.mainAxisOffset = offset[0];
        this.crossAxisOffset = offset[1];
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

    @Override
    public boolean matches(ItemStack stack) {
        return this.filter == null || this.filter.test(stack);
    }

    @Override
    public TooltipStyle getStyle() {
        return this.tooltipStyle;
    }

    @Override
    public SectionBox getSectionBox(String path) {
        return this.sectionBoxCache.computeIfAbsent(path, p -> new SectionBox(this.tooltipStyle.getAsStyle(p)));
    }

    @Override
    public SpaceTooltipComponent getSpacing(String path) {
        return this.spacingCache.computeIfAbsent(path, p -> new SpaceTooltipComponent(this.tooltipStyle.getAsStyle(p)));
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

    protected List<ITooltipComponent> prepareComponents(List<SectionTooltipComponent> components) {
        final List<ITooltipComponent> result = new ArrayList<>(components);

        if (result.size() > 1 && getSpacing("hr").getHeight() > 0) {
            result.add(1, getSpacing("hr"));
        }

        return result;
    }

    protected List<SectionTooltipComponent> paginateComponents(SectionTooltipComponent component, int maxWidth,
        int maxHeight) {
        maxWidth = Math
            .max(Math.min(this.tooltipSectionBox.getMaxWidth(), maxWidth), this.tooltipSectionBox.getMinWidth());
        maxHeight = Math.max(maxHeight, this.tooltipSectionBox.getMinHeight());

        final List<SectionTooltipComponent> pages = new ArrayList<>();
        ITooltipComponent[] split = component.paginate(this.lastContext, maxWidth, maxHeight);
        final SectionTooltipComponent firstPage = (SectionTooltipComponent) split[0];
        final int paginationHeight = this.tooltipSectionBox.getNavigationHeight();

        if (split.length == 1) {
            return Collections.singletonList(firstPage);
        }

        while (component != null) {
            split = component.paginate(this.lastContext, maxWidth, maxHeight - paginationHeight);
            pages.add((SectionTooltipComponent) split[0]);

            if (split.length > 1) {
                component = (SectionTooltipComponent) split[1];
            } else {
                component = null;
            }

        }

        return pages;
    }

    protected void addNavigation(SectionTooltipComponent component, int currentPage, int totalPages) {
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
                new SectionTooltipComponent(
                    "navigation",
                    this.tooltipSectionBox.navigationBox,
                    Collections.singletonList(new TextTooltipComponent(text))));
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
            final Dimension freeSpace = displaySize();
            final int maxWidth = freeSpace.width - this.tooltipSectionBox.getInline();
            final int maxHeight = freeSpace.height - this.tooltipSectionBox.getBlock();

            final List<ITooltipComponent> components = prepareComponents(this.lastContext.getSections());

            final List<SectionTooltipComponent> pages = paginateComponents(
                new SectionTooltipComponent("page", this.tooltipSectionBox, components),
                maxWidth,
                maxHeight);

            this.totalPages = pages.size();
            this.currentPage = Math.max(0, Math.min(this.currentPage, this.totalPages - 1));
            this.pagedTooltipComponent = pages.get(this.currentPage);

            if (this.totalPages > 1) {
                addNavigation(this.pagedTooltipComponent, this.currentPage + 1, this.totalPages);
            }

            this.lastPosition = prepareTooltipPosition(
                this.pagedTooltipComponent.getWidth(),
                this.pagedTooltipComponent.getHeight(),
                this.lastContext.getAnchorBounds(),
                freeSpace);
        }

        if (!this.pagedTooltipComponent.isEmpty()) {
            drawContent();
            ClientUtil.postEvent(new RenderTooltipEvent(context, this.pagedTooltipComponent, this.lastPosition));
        }
    }

    protected Dimension displaySize() {
        Minecraft mc = ClientUtil.mc();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        return new Dimension(res.getScaledWidth(), res.getScaledHeight());
    }

    protected void drawContent() {
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        ClientUtil.incZLevel(DEFAULT_Z_INDEX);

        this.pagedTooltipComponent
            .draw(this.lastPosition.x, this.lastPosition.y, this.pagedTooltipComponent.getWidth(), this.lastContext);

        ClientUtil.incZLevel(-DEFAULT_Z_INDEX);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableStandardItemLighting();
    }

    protected Point prepareTooltipPosition(int width, int height, Rectangle anchor, Dimension freeSpace) {
        final int offsetMain = anchor.width == 0 && anchor.height == 0 ? this.mainAxisOffset : 0;
        final int offsetCross = anchor.width == 0 && anchor.height == 0 ? this.crossAxisOffset : 0;
        final Point rightPoint = new Point(anchor.x + anchor.width + offsetMain, anchor.y + offsetCross);
        final Point leftPoint = new Point(anchor.x - width - offsetMain, anchor.y + offsetCross);
        final Point topPoint = new Point(anchor.x + offsetCross, anchor.y - height - offsetMain);
        final Point bottomPoint = new Point(anchor.x + offsetCross, anchor.y + anchor.height + offsetMain);

        if (rightPoint.x + width <= freeSpace.width) {
            rightPoint.y = clamp(rightPoint.y, 0, freeSpace.height - height);
            return rightPoint;
        }

        if (leftPoint.x >= 0) {
            leftPoint.y = clamp(leftPoint.y, 0, freeSpace.height - height);
            return leftPoint;
        }

        if (bottomPoint.y + height <= freeSpace.height) {
            bottomPoint.x = clamp(bottomPoint.x, 0, freeSpace.width - width);
            return bottomPoint;
        }

        if (topPoint.y >= 0) {
            topPoint.x = clamp(topPoint.x, 0, freeSpace.width - width);
            return topPoint;
        }

        // more space on right side
        if (anchor.x < freeSpace.width - anchor.x - anchor.width) {
            rightPoint.y = clamp(rightPoint.y, 0, freeSpace.height - height);
            rightPoint.x = clamp(rightPoint.x, 0, freeSpace.width - width);
            return rightPoint;
        } else {
            leftPoint.y = clamp(leftPoint.y, 0, freeSpace.height - height);
            leftPoint.x = clamp(leftPoint.x, 0, freeSpace.width - width);
            return leftPoint;
        }

    }

    protected int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

}
