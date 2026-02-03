package com.slprime.chromatictooltips.enricher;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.ForgeEventFactory;

import com.slprime.chromatictooltips.api.EnricherPlace;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.api.TooltipLines;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.api.TooltipTarget;
import com.slprime.chromatictooltips.event.ItemInfoEnricherEvent;
import com.slprime.chromatictooltips.util.TooltipUtils;

public class ItemInfoEnricher implements ITooltipEnricher {

    @Override
    public String sectionId() {
        return "itemInfo";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<TooltipModifier> mode() {
        return EnumSet.of(TooltipModifier.NONE, TooltipModifier.SHIFT);
    }

    @Override
    public TooltipLines build(TooltipContext context) {

        if (!context.getTarget()
            .isItem()) {
            return null;
        }

        return new TooltipLines(getItemInformation(context.getTarget()));
    }

    public static List<Object> getItemInformation(TooltipTarget target) {
        final Minecraft mc = TooltipUtils.mc();
        final List<String> tooltip = new ArrayList<>();
        final ItemStack stack = target.getItem()
            .copy();
        final String displayName = stack.getDisplayName();
        tooltip.add(displayName); // temporary name added for information gathering

        try {
            stack.getItem()
                .addInformation(stack, mc.thePlayer, tooltip, mc.gameSettings.advancedItemTooltips);

            if (stack.hasTagCompound() && stack.getTagCompound()
                .hasKey("display", 10)) {
                addItemColorAndLore(stack, tooltip, mc.gameSettings.advancedItemTooltips);
            }

            ForgeEventFactory.onItemTooltip(stack, mc.thePlayer, tooltip, mc.gameSettings.advancedItemTooltips);
        } catch (Exception e) {}

        if (!tooltip.isEmpty()) {
            tooltip.remove(0); // remove temporary name
        }

        final ItemInfoEnricherEvent event = new ItemInfoEnricherEvent(target, tooltip);
        TooltipUtils.postEvent(event);

        return event.tooltip;
    }

    protected static void addItemColorAndLore(ItemStack stack, List<String> arraylist, boolean advancedItemTooltips) {
        final NBTTagCompound nbttagcompound = stack.getTagCompound()
            .getCompoundTag("display");

        if (nbttagcompound.hasKey("color", 3)) {
            if (advancedItemTooltips) {
                arraylist.add(
                    "Color: #" + Integer.toHexString(nbttagcompound.getInteger("color"))
                        .toUpperCase());
            } else {
                arraylist.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.dyed"));
            }
        }

        if (nbttagcompound.func_150299_b("Lore") == 9) {
            final NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);

            for (int j = 0; j < nbttaglist1.tagCount(); ++j) {
                arraylist.add(
                    EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.ITALIC + nbttaglist1.getStringTagAt(j));
            }
        }
    }

}
