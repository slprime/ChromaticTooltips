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
import com.slprime.chromatictooltips.util.ClientUtil;

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
        final ItemStack stack = context.getStack();

        if (stack == null) {
            return null;
        }

        return new TooltipLines(itemInformation(stack.copy()));
    }

    protected List<String> itemInformation(ItemStack stack) {
        final Minecraft mc = ClientUtil.mc();
        final List<String> namelist = new ArrayList<>();
        namelist.add(stack.getDisplayName()); // same mods expecting the first line to be the item name

        try {
            stack.getItem()
                .addInformation(stack, mc.thePlayer, namelist, mc.gameSettings.advancedItemTooltips);

            if (stack.hasTagCompound() && stack.getTagCompound()
                .hasKey("display", 10)) {
                addItemColorAndLore(stack, namelist, mc.gameSettings.advancedItemTooltips);
            }

            ForgeEventFactory.onItemTooltip(stack, mc.thePlayer, namelist, mc.gameSettings.advancedItemTooltips);
        } catch (Exception e) {}

        namelist.remove(0); // remove temporary name added for information gathering

        return namelist;
    }

    protected void addItemColorAndLore(ItemStack stack, List<String> arraylist, boolean advancedItemTooltips) {
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
