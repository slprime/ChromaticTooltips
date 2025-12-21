package com.slprime.chromatictooltips.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.util.SectionBox;

public class SectionTooltipComponent extends SectionBox {

    protected List<ITooltipComponent> components = new ArrayList<>();
    protected ITooltipComponent pendingComponent = null;
    protected String sectionId = null;

    public SectionTooltipComponent(String sectionId, SectionBox box, List<ITooltipComponent> components) {
        super(box);
        this.sectionId = sectionId;
        addAllComponents(components);
    }

    public SectionTooltipComponent(String sectionId, TooltipStyle style, List<ITooltipComponent> components) {
        super(style);
        this.sectionId = sectionId;
        addAllComponents(components);
    }

    public String getSectionId() {
        return this.sectionId;
    }

    public void clearPendingComponent() {
        this.pendingComponent = null;
    }

    public void addAllComponents(List<ITooltipComponent> components) {
        for (ITooltipComponent component : components) {
            addComponent(component);
        }
    }

    public void addComponent(ITooltipComponent component) {
        if (component == null) {
            return;
        }

        if (component instanceof SpaceTooltipComponent) {
            this.pendingComponent = component;
            return;
        }

        if (!this.components.isEmpty()) {

            if (this.pendingComponent != null) {
                this.components.add(this.pendingComponent);
                this.contentSize.width = Math.max(this.contentSize.width, this.pendingComponent.getWidth());
                this.contentSize.height += this.pendingComponent.getHeight();
            } else {
                this.contentSize.height += getEffectiveMarginAfter(
                    this.components.get(this.components.size() - 1),
                    component);
            }

        }

        this.components.add(component);
        this.contentSize.width = Math.max(this.contentSize.width, component.getWidth());
        this.contentSize.height += component.getHeight();
        this.pendingComponent = null;
    }

    protected int getEffectiveMarginAfter(ITooltipComponent curr, ITooltipComponent next) {
        return (next == null || next instanceof SpaceTooltipComponent) ? 0 : curr.getSpacing();
    }

    public List<ITooltipComponent> getComponents() {
        return Collections.unmodifiableList(this.components);
    }

    public boolean isEmpty() {
        return this.components.isEmpty();
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        final List<ITooltipComponent> firstPage = new ArrayList<>();
        final List<ITooltipComponent> secondPage = new ArrayList<>();
        boolean firstPageIsEmpty = true;
        int lastMarginBottom = 0;
        int currentHeight = 0;

        maxHeight -= getBlock();

        for (ITooltipComponent component : this.components) {
            final int remainingHeight = maxHeight - currentHeight - lastMarginBottom;

            if (firstPageIsEmpty || remainingHeight > 0) {
                final ITooltipComponent[] split = component.paginate(context, maxWidth, remainingHeight);
                final ITooltipComponent firstComponent = split[0];
                final int compHeight = firstComponent.getHeight();

                if (firstPageIsEmpty || remainingHeight >= compHeight) {

                    if (firstComponent instanceof SpaceTooltipComponent) {
                        lastMarginBottom = 0;
                        if (firstPageIsEmpty || firstPage.get(firstPage.size() - 1) instanceof SpaceTooltipComponent) {
                            continue;
                        }
                    }

                    currentHeight += compHeight + lastMarginBottom;
                    lastMarginBottom = firstComponent.getSpacing();
                    firstPage.add(firstComponent);
                    firstPageIsEmpty = false;
                } else {
                    currentHeight += compHeight;
                    secondPage.add(firstComponent);
                }

                if (split.length > 1) {
                    secondPage.add(split[1]);
                }

            } else {
                secondPage.add(component);
            }
        }

        if (secondPage.isEmpty()) {
            return new ITooltipComponent[] { createInstance(firstPage) };
        }

        return new ITooltipComponent[] { createInstance(firstPage), createInstance(secondPage) };
    }

    protected ITooltipComponent createInstance(List<ITooltipComponent> components) {
        return new SectionTooltipComponent(this.sectionId, this, components);
    }

    @Override
    public void drawContent(int x, int y, int width, int height, TooltipContext context) {
        final int size = this.components.size();
        int offsetY = 0;

        for (int i = 0; i < size; i++) {
            final ITooltipComponent component = components.get(i);
            component.draw(x, y + offsetY, width, context);
            offsetY += component.getHeight()
                + getEffectiveMarginAfter(component, (i + 1 < size) ? components.get(i + 1) : null);
        }

    }

}
