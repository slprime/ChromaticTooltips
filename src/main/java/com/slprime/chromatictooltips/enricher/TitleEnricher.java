package com.slprime.chromatictooltips.enricher;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.component.TextTooltipComponent;
import com.slprime.chromatictooltips.event.ItemTitleEnricherEvent;
import com.slprime.chromatictooltips.util.ClientUtil;

public class TitleEnricher implements ITooltipEnricher {

    protected static class StackTitleTooltipComponent extends TextTooltipComponent {

        protected ITooltipComponent identifierComponent;

        public StackTitleTooltipComponent(String title, ITooltipComponent identifierComponent) {
            super(title + " ");
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
    public String sectionId() {
        return "title";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.HEADER;
    }

    @Override
    public EnumSet<EnricherMode> mode() {
        return EnumSet.of(EnricherMode.ALWAYS);
    }

    @Override
    public List<ITooltipComponent> build(TooltipContext context) {
        final ItemStack stack = context.getStack();

        if (stack == null) {
            final List<ITooltipComponent> lines = context.getContextTooltip();

            if (!lines.isEmpty() && lines.get(0) instanceof TextTooltipComponent title) {
                final String line = title.getLines()
                    .get(0);
                return Collections.singletonList(
                    new TextTooltipComponent(EnumChatFormatting.WHITE + line.replaceAll("^(?:§[0-9a-fk-or])+", "")));
            }

            return null;
        } else {
            return itemTitle(context, stack);
        }

    }

    protected List<ITooltipComponent> itemTitle(TooltipContext context, ItemStack stack) {
        final ITooltipComponent identifierComponent = new TextTooltipComponent(
            EnumChatFormatting.DARK_GRAY + getAdvancedInfo(stack));
        final ItemTitleEnricherEvent event = new ItemTitleEnricherEvent(context, stack.getDisplayName());

        ClientUtil.postEvent(event);

        return Collections.singletonList(
            new StackTitleTooltipComponent(prepareItemDisplayName(stack, event.displayName), identifierComponent));
    }

    private String prepareItemDisplayName(ItemStack stack, String displayName) {
        final String rarityColor = stack.getRarity() == EnumRarity.common ? ""
            : stack.getRarity().rarityColor.toString();

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
