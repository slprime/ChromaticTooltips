package com.slprime.chromatictooltips.enricher;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.api.TooltipTarget;
import com.slprime.chromatictooltips.component.TextComponent;
import com.slprime.chromatictooltips.event.TitleEnricherEvent;
import com.slprime.chromatictooltips.util.TooltipFontContext;
import com.slprime.chromatictooltips.util.TooltipUtils;

import cpw.mods.fml.common.registry.GameData;

public class TitleEnricher implements ITooltipEnricher {

    protected static class StackTitleTooltipComponent extends TextComponent {

        protected ITooltipComponent identifierComponent;

        public StackTitleTooltipComponent(String title, ITooltipComponent identifierComponent) {
            super(title);
            this.identifierComponent = identifierComponent;
        }

        public int getWidth() {
            int width = super.getWidth();

            if (identifierComponent.getWidth() > 0) {
                width += TooltipFontContext.getStringWidth(" ") + identifierComponent.getWidth();
            }

            return width;
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
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.NONE);
    }

    @Override
    public TooltipLines build(TooltipContext context) {

        if (context.getItem() != null) {
            return itemTitle(context.getTarget());
        } else if (context.getFluid() != null) {
            return fluidTitle(context.getTarget());
        } else {
            return defaultTitle(context);
        }

    }

    protected TooltipLines defaultTitle(TooltipContext context) {
        final List<ITooltipComponent> lines = context.getContextTooltip();

        if (lines.isEmpty()) {
            return null;
        }

        if (lines.get(0) instanceof TextComponent title) {
            String line = title.getLines()
                .get(0);

            if (TooltipUtils.getColorCodeIndex(line) == TooltipLines.BASE_COLOR.ordinal()) {
                line = EnumChatFormatting.WHITE + line.replaceAll("^(?:§[0-9a-fk-or])+", "");
            }
            return new TooltipLines(line);
        } else {
            return new TooltipLines(lines.get(0));
        }

    }

    protected TooltipLines fluidTitle(TooltipTarget target) {
        final FluidStack fluid = target.getFluid();
        final String displayName = fluid.getFluid()
            .getLocalizedName(fluid);
        final ITooltipComponent identifierComponent = new TextComponent(
            EnumChatFormatting.DARK_GRAY + getAdvancedInfo(fluid));
        final TitleEnricherEvent event = new TitleEnricherEvent(target, displayName);
        TooltipUtils.postEvent(event);

        return new TooltipLines(
            new StackTitleTooltipComponent(EnumChatFormatting.AQUA + event.displayName, identifierComponent));
    }

    protected TooltipLines itemTitle(TooltipTarget target) {
        final ItemStack stack = target.getItem();
        final ITooltipComponent identifierComponent = new TextComponent(
            EnumChatFormatting.DARK_GRAY + getAdvancedInfo(stack));
        final TitleEnricherEvent event = new TitleEnricherEvent(target, stack.getDisplayName());
        TooltipUtils.postEvent(event);

        return new TooltipLines(
            new StackTitleTooltipComponent(prepareItemDisplayName(stack, event.displayName), identifierComponent));
    }

    private String prepareItemDisplayName(ItemStack stack, String displayName) {
        final String rarityColor = stack.getRarity().rarityColor.toString();

        if (stack.hasDisplayName()) {
            return rarityColor + EnumChatFormatting.ITALIC + displayName;
        }

        return rarityColor + displayName;
    }

    private String getAdvancedInfo(ItemStack stack) {

        if (TooltipUtils.mc().gameSettings.advancedItemTooltips) {
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

    private String getAdvancedInfo(FluidStack fluidStack) {

        if (TooltipUtils.mc().gameSettings.advancedItemTooltips) {
            final int fluidId = GameData.getBlockRegistry()
                .getId(
                    fluidStack.getFluid()
                        .getBlock());

            if (fluidId == -1) {
                return "";
            }

            return String.format(" #%04d", Integer.valueOf(fluidId));
        }
        return "";
    }

}
