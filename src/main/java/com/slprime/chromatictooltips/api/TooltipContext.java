package com.slprime.chromatictooltips.api;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.slprime.chromatictooltips.component.SectionTooltipComponent;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class TooltipContext {

    protected ItemStack stack;
    protected long animationStartTime;
    protected String context;
    protected List<ITooltipComponent> contextTooltip = new ArrayList<>();
    protected List<SectionTooltipComponent> lines = new ArrayList<>();
    protected Rectangle anchorBounds = new Rectangle(0, 0, 0, 0);

    protected final ITooltipRenderer renderer;
    protected int revision = 0;
    protected int mouseX;
    protected int mouseY;

    public TooltipContext(TooltipRequest request, ITooltipRenderer renderer) {
        this.renderer = renderer;
        this.context = request.context;
        this.animationStartTime = System.currentTimeMillis();
        this.stack = request.stack != null ? request.stack.copy() : null;
        this.contextTooltip.addAll(request.tooltip.buildComponents(this));
    }

    public void setStack(ItemStack stack) {
        this.stack = stack != null ? stack.copy() : null;
        this.revision++;
    }

    public void setPosition(Point mouse) {
        this.mouseX = mouse.x;
        this.mouseY = mouse.y;
        setAnchorBounds(mouse.x, mouse.y, 0, 0);
    }

    public String getContextName() {
        return this.context;
    }

    public long getAnimationStartTime() {
        return this.animationStartTime;
    }

    public int getMouseX() {
        return this.mouseX;
    }

    public int getMouseY() {
        return this.mouseY;
    }

    public ITooltipRenderer getRenderer() {
        return this.renderer;
    }

    public TooltipStyle getAsStyle(String path) {
        return this.renderer.getStyle()
            .getAsStyle(path);
    }

    public void setAnchorBounds(int x, int y, int width, int height) {

        if (this.anchorBounds.x == x && this.anchorBounds.y == y
            && this.anchorBounds.width == width
            && this.anchorBounds.height == height) {
            return;
        }

        this.anchorBounds.setBounds(x, y, width, height);
        this.revision++;
    }

    public Rectangle getAnchorBounds() {
        return this.anchorBounds;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public List<ITooltipComponent> getContextTooltip() {
        return Collections.unmodifiableList(this.contextTooltip);
    }

    public List<SectionTooltipComponent> getSections() {
        return Collections.unmodifiableList(this.lines);
    }

    public void addSection(int index, String sectionId, List<ITooltipComponent> components) {
        if (components == null || components.isEmpty()) return;
        index = Math.max(0, Math.min(index, this.lines.size()));
        this.lines
            .add(index, new SectionTooltipComponent(sectionId, this.renderer.getSectionBox(sectionId), components));
        this.revision++;
    }

    public void addSection(String sectionId, List<ITooltipComponent> components) {
        if (components == null || components.isEmpty()) return;
        this.lines.add(new SectionTooltipComponent(sectionId, this.renderer.getSectionBox(sectionId), components));
        this.revision++;
    }

    public int getRevision() {
        return this.revision;
    }

    public void clear() {
        this.lines.clear();
        this.revision++;
    }

    public boolean isEmpty() {
        return this.lines.isEmpty();
    }

    public void drawString(String text, int x, int y) {
        TooltipFontContext.drawString(text, x, y);
    }

    public void drawString(String text, int x, int y, int color) {
        TooltipFontContext.drawString(text, x, y, color);
    }

}
