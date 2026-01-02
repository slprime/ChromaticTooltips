package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.slprime.chromatictooltips.api.AttributeModifierData;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.event.AttributeEnricherEvent;
import com.slprime.chromatictooltips.util.ClientUtil;
import com.slprime.chromatictooltips.util.TooltipFontContext;

public class AttributeModifierEnricher implements ITooltipEnricher {

    protected static class InlineComponent implements ITooltipComponent {

        protected static final int PADDING = 4;
        protected List<List<ITooltipComponent>> lines;
        protected int width = 0;
        protected int height = 0;

        public InlineComponent(List<List<ITooltipComponent>> lines) {
            this.lines = lines;

            for (List<ITooltipComponent> line : this.lines) {
                int lineWidth = -PADDING;
                int lineHeight = 0;

                for (ITooltipComponent component : line) {
                    lineWidth += component.getWidth() + PADDING;
                    lineHeight = Math.max(lineHeight, component.getHeight());
                }

                this.width = Math.max(this.width, lineWidth);
                this.height += lineHeight;
            }
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        public int getSpacing() {
            return TooltipFontContext.DEFAULT_SPACING;
        }

        @Override
        public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
            final List<List<ITooltipComponent>> lines = new ArrayList<>();
            int lineWidth = -PADDING;

            lines.add(new ArrayList<>());

            for (List<ITooltipComponent> line : this.lines) {
                for (ITooltipComponent component : line) {

                    if (lineWidth + component.getWidth() > maxWidth) {
                        lineWidth = -PADDING;
                        lines.add(new ArrayList<>());
                    }

                    lines.get(lines.size() - 1)
                        .add(component);
                    lineWidth += component.getWidth() + PADDING;
                }
            }

            return new ITooltipComponent[] { new InlineComponent(lines) };
        }

        @Override
        public void draw(int x, int y, int availableWidth, TooltipContext context) {
            int offsetY = 0;

            for (List<ITooltipComponent> line : this.lines) {
                int lineWidth = 0;
                int lineHeight = 0;

                for (ITooltipComponent component : line) {
                    component.draw(x + lineWidth, y + offsetY, availableWidth - lineWidth, context);
                    lineWidth += component.getWidth() + PADDING;
                    lineHeight = Math.max(lineHeight, component.getHeight());
                }
                offsetY += lineHeight;
            }
        }
    }

    protected static final Map<String, String> ATTRIBUTE_ICONS = new HashMap<>();
    protected boolean showOnlyIcons = false;

    static {
        ATTRIBUTE_ICONS.put("generic.attackDamage", "attributes/attack_damage.png");
        ATTRIBUTE_ICONS.put("generic.maxHealth", "attributes/max_health.png");
        ATTRIBUTE_ICONS.put("generic.knockbackResistance", "attributes/knockback_resistance.png");
        ATTRIBUTE_ICONS.put("generic.movementSpeed", "attributes/movement_speed.png");
    }

    public AttributeModifierEnricher(boolean showOnlyIcons) {
        this.showOnlyIcons = showOnlyIcons;
    }

    @Override
    public String sectionId() {
        return this.showOnlyIcons ? "attributes:icons" : "attributes";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<EnricherMode> mode() {
        return this.showOnlyIcons ? EnumSet.of(EnricherMode.DEFAULT)
            : EnumSet.of(EnricherMode.DEFAULT, EnricherMode.SHIFT);
    }

    protected boolean shownIcons(TooltipContext context) {
        return EnricherConfig.attributeModifierIconsEnabled && context.getRenderer()
            .getEnricherModes("attributes:icons", EnumSet.of(EnricherMode.DEFAULT))
            .contains(context.getEnricherMode());
    }

    @Override
    public List<ITooltipComponent> build(TooltipContext context) {
        final ItemStack stack = context.getStack();

        if (stack == null || this.showOnlyIcons && !EnricherConfig.attributeModifierIconsEnabled) {
            return null;
        }

        final boolean shownIcons = !this.showOnlyIcons && shownIcons(context);
        final List<AttributeModifierData> attributeModifiers = getAttributeModifiers(context, stack);
        final List<ITooltipComponent> attributeModifiersList = new ArrayList<>();

        for (final AttributeModifierData attributeData : attributeModifiers) {
            if (this.showOnlyIcons && attributeData.hasIcon()) {
                attributeModifiersList.add(attributeData.getIconComponent());
            } else if (!this.showOnlyIcons && (!shownIcons || !attributeData.hasIcon())) {
                attributeModifiersList.add(attributeData.getTextComponent());
            }
        }

        if (this.showOnlyIcons && !attributeModifiersList.isEmpty()) {
            return Collections.singletonList(new InlineComponent(Collections.singletonList(attributeModifiersList)));
        } else {
            return attributeModifiersList;
        }

    }

    protected static List<AttributeModifierData> getAttributeModifiers(TooltipContext context, ItemStack stack) {
        final List<AttributeModifierData> attributeModifiers = new ArrayList<>();

        for (Map.Entry<String, AttributeModifier> entry : stack.getAttributeModifiers()
            .entries()) {
            final String attributeName = StatCollector.translateToLocal("attribute.name." + entry.getKey());
            attributeModifiers.add(
                new AttributeModifierData(attributeName, entry.getValue(), stack)
                    .withIcon(ATTRIBUTE_ICONS.getOrDefault(entry.getKey(), null)));

            if (!ATTRIBUTE_ICONS.containsKey(entry.getKey())) {
                System.out.println("Missing icon for attribute: " + entry.getKey());
            }
        }

        if (stack.getItem() instanceof ItemArmor armor) {
            attributeModifiers.add(
                new AttributeModifierData(
                    ClientUtil.translate(
                        "enricher.attributes.armor.text",
                        ClientUtil.formatNumbers(armor.damageReduceAmount)),
                    ClientUtil.translate(
                        "enricher.attributes.armor.icon",
                        ClientUtil.formatNumbers(armor.damageReduceAmount)),
                    armor.damageReduceAmount,
                    "attributes/armor.png"));
        }

        final AttributeEnricherEvent event = new AttributeEnricherEvent(context, attributeModifiers);
        ClientUtil.postEvent(event);

        Collections.sort(
            event.attributeModifiers,
            (AttributeModifierData a, AttributeModifierData b) -> Double.compare(b.getValue(), a.getValue()));

        if (stack.hasTagCompound() && stack.getTagCompound()
            .getBoolean("Unbreakable")) {
            final String textLine = StatCollector.translateToLocal("item.unbreakable");
            event.attributeModifiers.add(new AttributeModifierData(textLine, null, 0, null));
        }

        if (stack.isItemStackDamageable()) {
            final int maxDamage = stack.getMaxDamage();
            final int value = maxDamage - stack.getItemDamageForDisplay();
            final String textLine = ClientUtil.translate("enricher.attributes.durability.text", value, maxDamage);
            final String iconLine = ClientUtil.translate("enricher.attributes.durability.icon", value, maxDamage);
            event.attributeModifiers
                .add(new AttributeModifierData(textLine, iconLine, value, "attributes/durability.png"));
        }

        return event.attributeModifiers;
    }

}
