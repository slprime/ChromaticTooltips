package com.slprime.chromatictooltips.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.util.SectionBox;

public class SectionComponent extends SectionBox {

    protected static int parentSectionSpacing = 0;

    protected List<ITooltipComponent> components = new ArrayList<>();
    protected ITooltipComponent pendingComponent = null;
    protected String sectionId = null;

    public SectionComponent(String sectionId, SectionBox box, List<ITooltipComponent> components) {
        super(box);
        this.sectionId = sectionId;
        addAllComponents(components);
    }

    public SectionComponent(String sectionId, TooltipStyle style, List<ITooltipComponent> components) {
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

        if (component instanceof SpaceComponent) {
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
        return (next == null || next instanceof SpaceComponent) ? 0 : curr.getSpacing();
    }

    public List<ITooltipComponent> getComponents() {
        return Collections.unmodifiableList(this.components);
    }

    public boolean isEmpty() {
        return this.components.isEmpty();
    }

    @Override
    public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
        final int parentSectionSpacing = SectionComponent.parentSectionSpacing;
        final List<ITooltipComponent> firstPage = new ArrayList<>();
        final List<ITooltipComponent> secondPage = new ArrayList<>();
        int componentSpacing = 0;
        int currentHeight = 0;

        maxHeight = Math.max(0, maxHeight - getBlock());
        maxWidth = Math.max(0, maxWidth - getInline());

        this.fontContext.pushContext();

        if (this.sectionSpacing >= 0) {
            SectionComponent.parentSectionSpacing = this.sectionSpacing;
        }

        for (ITooltipComponent component : this.components) {
            final int remainingHeight = maxHeight - currentHeight;

            if (secondPage.isEmpty() && component instanceof SpaceComponent) {
                componentSpacing = 0;
                if (firstPage.isEmpty() || firstPage.get(firstPage.size() - 1) instanceof SpaceComponent) {
                    continue;
                }
            }

            if (secondPage.isEmpty() && (remainingHeight > componentSpacing || firstPage.isEmpty())) {
                final ITooltipComponent[] split = component
                    .paginate(context, maxWidth, remainingHeight - componentSpacing);
                final ITooltipComponent firstComponent = split[0];

                if (!isEmptyComponent(firstComponent)) {
                    final int compHeight = firstComponent.getHeight();

                    if (firstPage.isEmpty() || remainingHeight >= compHeight + componentSpacing) {
                        currentHeight += compHeight + componentSpacing;
                        componentSpacing = firstComponent.getSpacing();
                        firstPage.add(firstComponent);
                    } else {
                        secondPage.add(firstComponent);
                    }
                }

                if (split.length > 1 && !isEmptyComponent(split[1])) {
                    secondPage.add(split[1]);
                }

            } else
                if ((!secondPage.isEmpty() || !(component instanceof SpaceComponent)) && !isEmptyComponent(component)) {
                    secondPage.add(component);
                }
        }

        this.fontContext.popContext();
        SectionComponent.parentSectionSpacing = parentSectionSpacing;

        if (secondPage.isEmpty()) {
            return new ITooltipComponent[] { createInstance(context, firstPage) };
        }

        return new ITooltipComponent[] { createInstance(context, firstPage), createInstance(context, secondPage) };
    }

    protected boolean isEmptyComponent(ITooltipComponent component) {
        return component == null || component.getHeight() <= (component instanceof SectionBox box ? box.getBlock() : 0);
    }

    protected ITooltipComponent createInstance(TooltipContext context, List<ITooltipComponent> components) {
        final SectionComponent section = new SectionComponent(this.sectionId, this, components);

        if (section.spacing < 0) {
            section.spacing = SectionComponent.parentSectionSpacing;
        }

        return section;
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
