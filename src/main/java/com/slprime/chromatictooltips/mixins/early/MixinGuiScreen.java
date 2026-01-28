package com.slprime.chromatictooltips.mixins.early;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.slprime.chromatictooltips.TooltipHandler;

@Mixin(priority = 999, value = GuiScreen.class)
public class MixinGuiScreen extends Gui {

    /**
     * @author SLPrime
     * @reason Replace default tooltip rendering with Chromatic Tooltips rendering.
     */
    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    protected void renderToolTip(ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
        TooltipHandler.drawHoveringText(stack, null);
        ci.cancel();
    }

    /**
     * @author SLPrime
     * @reason Replace default tooltip rendering with Chromatic Tooltips rendering.
     */
    @Inject(method = "drawHoveringText", remap = false, at = @At("HEAD"), cancellable = true)
    protected void drawHoveringText(List<String> textLines, int mouseX, int mouseY, FontRenderer font,
        CallbackInfo ci) {

        if (textLines != null && !textLines.isEmpty()) {
            TooltipHandler.drawHoveringText(textLines);
        }

        ci.cancel();
    }

}
