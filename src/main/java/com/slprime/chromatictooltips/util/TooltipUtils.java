package com.slprime.chromatictooltips.util;

import java.awt.Point;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.api.TooltipModifier;
import com.slprime.chromatictooltips.config.GeneralConfig;

import cpw.mods.fml.common.eventhandler.Event;

public class TooltipUtils {

    private static class GuiHook extends GuiScreen {

        public void incZLevel(float f) {
            this.zLevel += f;
        }

        public RenderItem getItemRenderer() {
            return itemRender;
        }

    }

    protected static String[] tooltipBlacklistLines;
    protected static Predicate<String> tooltipBlacklistLinesPattern;
    protected static final Pattern COLOR_CODES_PATTERN = Pattern
        .compile("^\\s*§([0-9A-F]).*", Pattern.CASE_INSENSITIVE);
    private static final int ALT_HASH = 1 << 27;
    private static final int SHIFT_HASH = 1 << 26;
    private static final int CTRL_HASH = 1 << 25;
    private static final GuiHook gui = new GuiHook();

    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static void bindTexture(ResourceLocation resourceLocation) {
        mc().getTextureManager()
            .bindTexture(resourceLocation);
    }

    public static String translate(String key, Object... params) {
        return StatCollector.translateToLocalFormatted(ChromaticTooltips.MODID + "." + key, params);
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
        GL11.glTranslatef(0f, 0f, delta);
    }

    public static RenderItem getItemRenderer() {
        return gui.getItemRenderer();
    }

    public static Point getMousePosition() {
        final ScaledResolution res = getScaledResolution();
        final Minecraft mc = mc();
        return new Point(
            Mouse.getX() * res.getScaledWidth() / mc.displayWidth,
            res.getScaledHeight() - Mouse.getY() * res.getScaledHeight() / mc.displayHeight - 1);
    }

    public static ScaledResolution getScaledResolution() {
        final Minecraft mc = mc();
        return new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
    }

    public static int getTooltipScale() {
        return Math.max(1, getScaledResolution().getScaleFactor() + GeneralConfig.scaleFactor);
    }

    public static TooltipModifier getActiveModifier() {
        if (TooltipUtils.isShiftKeyDown()) {
            return TooltipModifier.SHIFT;
        } else if (TooltipUtils.isCtrlKeyDown()) {
            return TooltipModifier.CTRL;
        } else if (TooltipUtils.isAltKeyDown()) {
            return TooltipModifier.ALT;
        }
        return TooltipModifier.NONE;
    }

    public static String applyBaseColorIfAbsent(String str, EnumChatFormatting baseColor) {

        if (!TooltipUtils.COLOR_CODES_PATTERN.matcher(str)
            .find()) {
            return baseColor + str;
        }

        return str;
    }

    public static int getColorCodeIndex(String str) {
        final Matcher matcher = COLOR_CODES_PATTERN.matcher(str);

        if (matcher.matches()) {
            return "0123456789abcdef".indexOf(
                matcher.group(1)
                    .toLowerCase());
        }

        return 15;
    }

    public static boolean isBlacklistedLine(String line) {

        if (GeneralConfig.tooltipBlacklistLines.length == 0) {
            return false;
        }

        if (tooltipBlacklistLines != GeneralConfig.tooltipBlacklistLines) {
            tooltipBlacklistLines = GeneralConfig.tooltipBlacklistLines;
            tooltipBlacklistLinesPattern = ln -> false;

            for (int i = 0; i < tooltipBlacklistLines.length; i++) {
                final String rule = tooltipBlacklistLines[i];
                try {
                    final Pattern pattern = Pattern.compile(rule, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                    tooltipBlacklistLinesPattern = tooltipBlacklistLinesPattern.or(
                        ln -> pattern.matcher(ln)
                            .find());
                } catch (Exception ignored) {}
            }
        }

        return tooltipBlacklistLinesPattern.test(EnumChatFormatting.getTextWithoutFormattingCodes(line));
    }

    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    public static boolean isShiftKeyDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    public static boolean isCtrlKeyDown() {
        if (Minecraft.isRunningOnMac) {
            return Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA);
        }
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    public static int getMetaHash() {
        int hash = 0;

        if (isAltKeyDown()) {
            hash = hash | ALT_HASH;
        }

        if (isShiftKeyDown()) {
            hash = hash | SHIFT_HASH;
        }

        if (isCtrlKeyDown()) {
            hash = hash | CTRL_HASH;
        }

        return hash;
    }

}
