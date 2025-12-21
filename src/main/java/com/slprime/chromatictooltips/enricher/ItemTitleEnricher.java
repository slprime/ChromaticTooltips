package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipStyle;
import com.slprime.chromatictooltips.component.TextTooltipComponent;
import com.slprime.chromatictooltips.event.ItemTitleEnricherEvent;
import com.slprime.chromatictooltips.util.ClientUtil;

public class ItemTitleEnricher implements ITooltipEnricher {

    protected static class StackTitleTooltipComponent extends TextTooltipComponent {

        protected ITooltipComponent identifierComponent;

        public StackTitleTooltipComponent(String title, int titleColor, ITooltipComponent identifierComponent) {
            super(title + " ", titleColor);
            this.width += identifierComponent.getWidth();
            this.identifierComponent = identifierComponent;
        }

        @Override
        public ITooltipComponent[] paginate(TooltipContext context, int maxWidth, int maxHeight) {
            return new ITooltipComponent[] { this };
        }

        @Override
        public void draw(int x, int y, int availableWidth, TooltipContext context) {
            final int identifierWidth = this.identifierComponent.getWidth();
            super.draw(x, y, availableWidth, context);
            this.identifierComponent.draw(x + availableWidth - identifierWidth, y, identifierWidth, context);
        }

    }

    @Override
    public List<ITooltipComponent> enrich(TooltipContext context) {
        final ItemStack stack = context.getStack();

        if (stack == null) {
            return null;
        }

        final List<ITooltipComponent> components = new ArrayList<>();
        final TooltipStyle style = context.getAsStyle("stackTitle");
        final ITooltipComponent identifierComponent = new TextTooltipComponent(
            getAdvancedInfo(stack),
            style.getAsColor("identifierColor", 0xff555555));
        final ItemTitleEnricherEvent event = new ItemTitleEnricherEvent(context, stack.getDisplayName());

        ClientUtil.postEvent(event);

        components.add(
            new StackTitleTooltipComponent(
                prepareItemDisplayName(stack, event.displayName),
                style.getAsColor("titleColor", 0xffffffff),
                identifierComponent));

        if (event.displaySubtitle != null && !event.displaySubtitle.isEmpty()) {
            components
                .add(new TextTooltipComponent(event.displaySubtitle, style.getAsColor("subtitleColor", 0xff555555)));
        }

        return components;
    }

    private String prepareItemDisplayName(ItemStack stack, String displayName) {
        final String rarityColor = stack.getRarity() != EnumRarity.common ? stack.getRarity().rarityColor.toString()
            : "";

        if (stack.hasDisplayName()) {
            return rarityColor + EnumChatFormatting.ITALIC + displayName;
        }

        return rarityColor + displayName;
    }

    private String getAdvancedInfo(ItemStack stack) {

        if (ClientUtil.mc().gameSettings.advancedItemTooltips) {
            final int itemId = Item.getIdFromItem(stack.getItem());

            if (stack.getHasSubtypes()) {
                return String.format(" #%04d/%d", Integer.valueOf(itemId), Integer.valueOf(stack.getItemDamage()));
            } else {
                return String.format(" #%04d", Integer.valueOf(itemId));
            }

        } else if (!stack.hasDisplayName() && stack.getItem() == Items.filled_map) {
            return " #" + stack.getItemDamage();
        }

        return "";
    }

}
