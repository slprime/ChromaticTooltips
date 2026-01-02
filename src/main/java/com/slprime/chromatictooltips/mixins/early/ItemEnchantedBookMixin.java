package com.slprime.chromatictooltips.mixins.early;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEnchantedBook.class)
public class ItemEnchantedBookMixin extends Item {

    @Inject(method = "addInformation", at = @At("HEAD"), cancellable = true)
    private void onlySuper(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced,
        CallbackInfo ci) {
        super.addInformation(stack, player, tooltip, advanced);
        ci.cancel();
    }
}
