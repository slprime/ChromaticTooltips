package com.slprime.chromatictooltips.mixins.late;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.slprime.chromatictooltips.TooltipHandler;

@Mixin(GuiContainerCreative.class)
public class MixinGuiContainerCreative {

    @Shadow
    private static int selectedTabIndex;

    /**
     * @author SLPrime
     * @reason Custom tooltip rendering for creative search tab
     */
    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    protected void renderToolTip(ItemStack itemIn, int x, int y, CallbackInfo ci) {
        if (selectedTabIndex == CreativeTabs.tabAllSearch.getTabIndex()) {
            final List<String> textLines = new ArrayList<>();
            CreativeTabs creativetabs = itemIn.getItem()
                .getCreativeTab();

            if (creativetabs == null && itemIn.getItem() == Items.enchanted_book) {
                final Map<Integer, Integer> map = EnchantmentHelper.getEnchantments(itemIn);

                if (map.size() == 1) {
                    final Enchantment enchantment = Enchantment.enchantmentsList[((Integer) map.keySet()
                        .iterator()
                        .next()).intValue()];
                    final CreativeTabs[] acreativetabs = CreativeTabs.creativeTabArray;

                    for (int l = 0; l < acreativetabs.length; ++l) {
                        final CreativeTabs creativetabs1 = acreativetabs[l];
                        if (creativetabs1.func_111226_a(enchantment.type)) {
                            creativetabs = creativetabs1;
                            break;
                        }
                    }
                }
            }

            if (creativetabs != null) {
                textLines.add(
                    "" + EnumChatFormatting.BOLD
                        + EnumChatFormatting.BLUE
                        + I18n.format(creativetabs.getTranslatedTabLabel()));
            }

            TooltipHandler.drawHoveringText(itemIn, textLines);
        } else {
            TooltipHandler.drawHoveringText(itemIn, Collections.emptyList());
        }

        ci.cancel();
    }

}
