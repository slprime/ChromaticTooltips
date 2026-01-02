package com.slprime.chromatictooltips.api;

import java.util.UUID;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.slprime.chromatictooltips.component.AttributeModifierComponent;
import com.slprime.chromatictooltips.component.TextComponent;
import com.slprime.chromatictooltips.util.ClientUtil;

public class AttributeModifierData {

    // copied from Item
    protected static final UUID ITEM_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

    protected String textLine;
    protected String iconLine;
    protected double value;
    protected String icon;

    public AttributeModifierData(String textLine, String iconLine, double value, String icon) {
        this.textLine = textLine;
        this.iconLine = iconLine;
        this.value = value;
        this.icon = icon;
    }

    public AttributeModifierData(String attributeName, AttributeModifier modifier, ItemStack stack) {
        this.value = getModifiedAmount(modifier, stack);

        final String operationKey = (this.value > 0 ? "plus." : "take.") + modifier.getOperation();
        this.textLine = StatCollector.translateToLocalFormatted(
            "attribute.modifier." + operationKey,
            ClientUtil.formatNumbers(Math.abs(this.value)),
            attributeName);
        this.iconLine = ClientUtil
            .translate("enricher.attributes." + operationKey, ClientUtil.formatNumbers(Math.abs(this.value)));;
    }

    public AttributeModifierData withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public boolean hasIcon() {
        return this.icon != null;
    }

    public double getValue() {
        return this.value;
    }

    public static double getModifiedAmount(AttributeModifier modifier, ItemStack stack) {
        double amount = modifier.getAmount();

        if (modifier.getID() == ITEM_UUID) {
            amount += EnchantmentHelper.func_152377_a(stack, EnumCreatureAttribute.UNDEFINED);
        }

        if (modifier.getOperation() != 0) {
            amount *= 100;
        }

        return amount;
    }

    public ITooltipComponent getIconComponent() {

        if (this.icon != null && this.iconLine != null) {
            return new AttributeModifierComponent(this.icon, this.iconLine);
        }

        return null;
    }

    public ITooltipComponent getTextComponent() {
        return this.textLine != null
            ? new TextComponent(ClientUtil.applyBaseColorIfAbsent(this.textLine, TooltipLines.BASE_COLOR))
            : null;
    }

}
