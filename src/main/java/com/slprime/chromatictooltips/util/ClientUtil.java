package com.slprime.chromatictooltips.util;

import java.awt.Point;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.slprime.chromatictooltips.ChromaticTooltips;

import cpw.mods.fml.common.eventhandler.Event;

public class ClientUtil {

    private static class GuiHook extends GuiScreen {

        public void incZLevel(float f) {
            this.zLevel += f;
        }

        public RenderItem getItemRenderer() {
            return itemRender;
        }

    }

    private static final Map<Locale, DecimalFormat> decimalFormatters = new HashMap<>();
    private static final int ALT_HASH = 1 << 27;
    private static final int SHIFT_HASH = 1 << 26;
    private static final int CTRL_HASH = 1 << 25;
    private static final GuiHook gui = new GuiHook();

    private static DecimalFormat getDecimalFormat() {
        return decimalFormatters.computeIfAbsent(Locale.getDefault(Locale.Category.FORMAT), locale -> {
            final DecimalFormat numberFormat = new DecimalFormat(); // uses the necessary locale inside anyway
            numberFormat.setGroupingUsed(true);
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setRoundingMode(RoundingMode.HALF_UP);

            final DecimalFormatSymbols decimalFormatSymbols = numberFormat.getDecimalFormatSymbols();
            decimalFormatSymbols.setGroupingSeparator(','); // Use sensible separator for best clarity.
            numberFormat.setDecimalFormatSymbols(decimalFormatSymbols);

            return numberFormat;
        });
    }

    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static String translate(String key, Object... params) {
        return StatCollector.translateToLocalFormatted(ChromaticTooltips.MODID + "." + key, params);
    }

    public static String formatNumbers(BigInteger aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static String formatNumbers(long aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static String formatNumbers(double aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static boolean postEvent(Event event) {
        return MinecraftForge.EVENT_BUS.post(event);
    }

    public static GuiContainer getGuiContainer() {
        return mc().currentScreen instanceof GuiContainer gui ? gui : null;
    }

    public static InventoryPlayer getPlayerInventory() {
        final EntityClientPlayerMP thePlayer = mc().thePlayer;
        return thePlayer == null ? null : thePlayer.inventory;
    }

    public static void incZLevel(float delta) {
        gui.incZLevel(delta);
    }

    public static RenderItem getItemRenderer() {
        return gui.getItemRenderer();
    }

    public static Point getMousePosition() {
        final Minecraft mc = mc();
        final ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        return new Point(
            Mouse.getX() * res.getScaledWidth() / mc.displayWidth,
            res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1);
    }

    public static boolean altKey() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    public static boolean shiftKey() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    public static boolean controlKey() {
        if (Minecraft.isRunningOnMac) {
            return Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA);
        }
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    public static int getMetaHash() {
        int hash = 0;

        if (altKey()) {
            hash = hash | ALT_HASH;
        }

        if (shiftKey()) {
            hash = hash | SHIFT_HASH;
        }

        if (controlKey()) {
            hash = hash | CTRL_HASH;
        }

        return hash;
    }

}
