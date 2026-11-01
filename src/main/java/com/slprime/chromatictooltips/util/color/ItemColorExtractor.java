package com.slprime.chromatictooltips.util.color;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.api.TooltipTarget;

public final class ItemColorExtractor {

    private static final class Tones {

        final float h1, s1, v1, h2, s2, v2;
        final boolean neutral;

        private Tones(float h1, float s1, float v1, float h2, float s2, float v2) {
            this.h1 = h1;
            this.s1 = s1;
            this.v1 = v1;
            this.h2 = h2;
            this.s2 = s2;
            this.v2 = v2;
            this.neutral = TooltipPalette.isNeutral(s1, v1, s2, v2);
        }

        static Tones single(float h, float s, float v) {
            return new Tones(h, s, v, wrapHue(h + 0.085f), s, Math.max(0.25f, v * 0.85f));
        }

        static Tones twoTone(float h1, float s1, float v1, float h2, float s2, float v2) {
            return new Tones(h1, s1, v1, h2, s2, v2);
        }

        static Tones lerp(Tones a, Tones b, float t) {
            if (t <= 0.002f) return a;
            if (t >= 0.998f) return b;
            return new Tones(
                lerpHue(a.h1, b.h1, t),
                a.s1 + (b.s1 - a.s1) * t,
                a.v1 + (b.v1 - a.v1) * t,
                lerpHue(a.h2, b.h2, t),
                a.s2 + (b.s2 - a.s2) * t,
                a.v2 + (b.v2 - a.v2) * t);
        }

        TooltipPalette toPalette() {
            return TooltipPalette.fromTones(h1, s1, v1, h2, s2, v2);
        }
    }

    private static final class PaletteEntry implements Supplier<TooltipPalette> {

        private static final long PERIOD_MS = 5500L;

        final Tones primary;
        final Tones secondary;

        PaletteEntry(Tones single) {
            this.primary = single;
            this.secondary = null;
        }

        PaletteEntry(List<Tones> frames) {
            final Tones first = frames.get(0);
            Tones mostDifferent = first;
            float maxDist = 0f;

            for (int i = 1; i < frames.size(); i++) {
                final Tones candidate = frames.get(i);
                if (candidate.neutral != first.neutral) continue;

                float dh = Math.abs(candidate.h1 - first.h1);
                if (dh > 0.5f) dh = 1.0f - dh;
                final float dist = dh * 2.0f + Math.abs(candidate.s1 - first.s1) * 0.4f
                    + Math.abs(candidate.v1 - first.v1) * 0.3f;

                if (dist > maxDist) {
                    maxDist = dist;
                    mostDifferent = candidate;
                }
            }

            if (maxDist < 0.10f) {
                Tones best = first;
                float bestScore = best.s1 * best.v1;

                for (Tones frame : frames) {
                    final float score = frame.s1 * frame.v1;
                    if (score > bestScore) {
                        bestScore = score;
                        best = frame;
                    }
                }

                this.primary = best;
                this.secondary = null;
            } else {
                this.primary = first;
                this.secondary = Tones.lerp(first, mostDifferent, Math.min(1.0f, 0.38f / maxDist));
            }
        }

        @Override
        public TooltipPalette get() {
            if (this.secondary == null) {
                return this.primary.toPalette();
            }

            final double phase = (System.currentTimeMillis() % PERIOD_MS) / (double) PERIOD_MS;
            final float wave = (float) (0.5 - 0.5 * Math.cos(phase * 2.0 * Math.PI));
            return Tones.lerp(this.primary, this.secondary, wave)
                .toPalette();
        }
    }

    private static final int ITEM_CACHE_SIZE = 2048;
    private static final Map<Long, PaletteEntry> ITEM_CACHE = new LinkedHashMap<Long, PaletteEntry>(256, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, PaletteEntry> eldest) {
            return size() > ITEM_CACHE_SIZE;
        }
    };
    private static final Map<Integer, PaletteEntry> FLUID_CACHE = new HashMap<>();

    private static final int FBO_SIZE = 32;
    private static final IntBuffer PIXEL_READ_BUFFER = BufferUtils.createIntBuffer(FBO_SIZE * FBO_SIZE);
    private static final IntBuffer VIEWPORT_BUFFER = BufferUtils.createIntBuffer(16);
    private static final int[] PIXEL_ARRAY = new int[FBO_SIZE * FBO_SIZE];
    private static final RenderBlocks SAMPLE_RENDER_BLOCKS = new RenderBlocks();
    private static Framebuffer offscreenFbo = null;
    private static boolean isSamplingFbo = false;

    public static void clearCache() {
        ITEM_CACHE.clear();
        FLUID_CACHE.clear();
    }

    /** Identity that stays the same while the target's own colors change, used to ease between palettes. */
    public static long getSmoothKey(TooltipTarget target) {
        final Fluid fluid = getFluid(target);

        if (fluid != null) {
            return 0x8000000000000000L | ((long) fluid.getID() & 0xFFFFFFFFL);
        } else if (target != null && target.isItem()) {
            final ItemStack stack = target.getItem();
            return (((long) Item.getIdFromItem(stack.getItem()) & 0xFFFFL) << 48)
                | (((long) getNormalizedMeta(stack.getItem(), stack) & 0xFFFFL) << 32);
        }

        return 0L;
    }

    /** Returns the live palette of the target, or null when it cannot be resolved right now. */
    public static Supplier<TooltipPalette> getSource(TooltipTarget target) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (target == null || mc == null || !mc.func_152345_ab()) {
            return null;
        }

        PaletteEntry entry = null;

        if (target.isFluid()) {
            entry = getFluidEntry(target.getFluid());
        } else if (target.isFluidContainer()) {
            entry = getFluidEntry(new FluidStack(target.getContainedFluid(), 1000));
        } else if (target.isItem() && !isSamplingFbo) {
            entry = getItemEntry(target.getItem());
        }

        return entry;
    }

    private static Fluid getFluid(TooltipTarget target) {
        if (target == null) return null;
        if (target.isFluid()) return target.getFluid() != null ? target.getFluid()
            .getFluid() : null;
        if (target.isFluidContainer()) return target.getContainedFluid();
        return null;
    }

    private static PaletteEntry getFluidEntry(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) {
            return null;
        }

        final Fluid fluid = stack.getFluid();
        PaletteEntry entry = FLUID_CACHE.get(fluid.getID());

        if (entry == null) {
            int fluidColor = 0xFFFFFF;
            IIcon icon = null;

            try {
                fluidColor = fluid.getColor(stack);
            } catch (Throwable ignored) {}

            try {
                icon = fluid.getIcon(stack);
            } catch (Throwable ignored) {}

            if (icon == null) {
                icon = fluid.getStillIcon();
            }

            entry = computeIconEntry(icon, 0, fluidColor);
            FLUID_CACHE.put(fluid.getID(), entry);
        }

        return entry;
    }

    private static PaletteEntry getItemEntry(ItemStack stack) {
        if (stack.getItem() == null) {
            return null;
        }

        final long key = computeItemKey(stack);
        PaletteEntry entry = ITEM_CACHE.get(key);

        if (entry == null) {
            entry = computeItemEntry(stack);
            ITEM_CACHE.put(key, entry);
        }

        return entry;
    }

    private static int getNormalizedMeta(Item item, ItemStack stack) {
        try {
            if (item.isDamageable() && !item.getHasSubtypes()) {
                return 0;
            }
            return stack.getItemDamage();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private static long computeItemKey(ItemStack stack) {
        final Item item = stack.getItem();
        final NBTTagCompound tag = stack.getTagCompound();
        int stateHash = tag != null ? tag.hashCode() : 0;

        try {
            stateHash = stateHash * 31 + item.getColorFromItemStack(stack, 0);
        } catch (Throwable ignored) {}

        try {
            if (!(item instanceof ItemBlock)) {
                final IIcon icon = item.requiresMultipleRenderPasses() ? item.getIcon(stack, 0)
                    : item.getIconIndex(stack);
                if (icon != null && icon.getIconName() != null) {
                    stateHash = stateHash * 31 + icon.getIconName()
                        .hashCode();
                }
            }
        } catch (Throwable ignored) {}

        return (((long) Item.getIdFromItem(item) & 0xFFFFL) << 48)
            | (((long) getNormalizedMeta(item, stack) & 0xFFFFL) << 32)
            | ((long) stateHash & 0xFFFFFFFFL);
    }

    private static PaletteEntry computeItemEntry(ItemStack stack) {
        final Item item = stack.getItem();
        int meta = 0;

        try {
            meta = stack.getItemDamage();
        } catch (Throwable ignored) {}

        boolean isMultiPass = false;

        try {
            isMultiPass = item.requiresMultipleRenderPasses() && item.getRenderPasses(meta) > 1;
        } catch (Throwable ignored) {}

        IIcon animCheckIcon = null;
        int animSpriteType = item.getSpriteNumber();

        if (!isMultiPass) {
            try {
                if (item instanceof ItemBlock) {
                    final Block block = Block.getBlockFromItem(item);
                    if (block != null) {
                        animCheckIcon = block.getIcon(2, meta);
                        animSpriteType = 0;
                    }
                } else {
                    animCheckIcon = item.requiresMultipleRenderPasses() ? item.getIcon(stack, 0)
                        : item.getIconIndex(stack);
                }
            } catch (Throwable ignored) {}

            if (animCheckIcon instanceof TextureAtlasSprite && animCheckIcon.getIconName() != null
                && ((TextureAtlasSprite) animCheckIcon).getFrameCount() > 1) {
                return computeIconEntry(animCheckIcon, animSpriteType, extractItemTint(stack, item));
            }
        }

        final Tones rendered = sampleRenderedItemStack(stack);

        ChromaticTooltips.LOG.warn(
            "[color-debug] {} multiPass={} fboEnabled={} sampled={} animIcon={} tint={}",
            stack,
            isMultiPass,
            OpenGlHelper.isFramebufferEnabled(),
            rendered == null ? "null"
                : String.format(
                    "h1=%.3f s1=%.3f v1=%.3f h2=%.3f s2=%.3f v2=%.3f neutral=%s",
                    rendered.h1,
                    rendered.s1,
                    rendered.v1,
                    rendered.h2,
                    rendered.s2,
                    rendered.v2,
                    rendered.neutral),
            animCheckIcon,
            Integer.toHexString(extractItemTint(stack, item)));

        if (rendered != null) {
            return new PaletteEntry(rendered);
        }

        if (animCheckIcon != null) {
            return computeIconEntry(animCheckIcon, animSpriteType, extractItemTint(stack, item));
        }

        final float[] hsv = rgbToHsv(extractItemTint(stack, item));
        return new PaletteEntry(Tones.single(hsv[0], hsv[1], hsv[2]));
    }

    private static int extractItemTint(ItemStack stack, Item item) {
        try {
            final int tint = item.getColorFromItemStack(stack, 0) & 0xFFFFFF;
            if (tint != 0xFFFFFF && tint != 0x000000) {
                return tint;
            }
        } catch (Throwable ignored) {}

        return 0xFFFFFF;
    }

    private static Tones sampleRenderedItemStack(ItemStack stack) {
        if (isSamplingFbo || !OpenGlHelper.isFramebufferEnabled()) {
            return null;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc == null || mc.getTextureManager() == null || mc.fontRenderer == null) {
            return null;
        }

        isSamplingFbo = true;
        int prevFbo = 0;
        boolean pushedAttrib = false;
        boolean pushedProjection = false;
        boolean pushedModelView = false;

        try {
            prevFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            VIEWPORT_BUFFER.clear();
            GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

            GL11.glPushAttrib(
                GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                    | GL11.GL_LIGHTING_BIT
                    | GL11.GL_DEPTH_BUFFER_BIT
                    | GL11.GL_TEXTURE_BIT
                    | GL11.GL_TRANSFORM_BIT
                    | GL11.GL_VIEWPORT_BIT);
            pushedAttrib = true;

            if (offscreenFbo == null) {
                offscreenFbo = new Framebuffer(FBO_SIZE, FBO_SIZE, true);
                offscreenFbo.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
            }

            offscreenFbo.framebufferClear();
            offscreenFbo.bindFramebuffer(true);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            pushedProjection = true;
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0D, 16.0D, 16.0D, 0.0D, -1000.0D, 3000.0D);

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            pushedModelView = true;
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, 0.0F, -2000.0F);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();

            final RenderItem renderItem = RenderItem.getInstance();
            final float prevZ = renderItem.zLevel;
            renderItem.zLevel = -50.0F;

            try {
                // No enchantment glint, it would tint the sampled colors purple
                if (!ForgeHooksClient.renderInventoryItem(
                    SAMPLE_RENDER_BLOCKS,
                    mc.getTextureManager(),
                    stack,
                    renderItem.renderWithColor,
                    renderItem.zLevel,
                    0.0F,
                    0.0F)) {
                    renderItem.renderItemIntoGUI(mc.fontRenderer, mc.getTextureManager(), stack, 0, 0, false);
                }
            } catch (Throwable ignored) {
                // A failing custom renderer can leave the tessellator mid-draw
                try {
                    Tessellator.instance.draw();
                } catch (Throwable ignored2) {}
            } finally {
                renderItem.zLevel = prevZ;
            }

            RenderHelper.disableStandardItemLighting();

            PIXEL_READ_BUFFER.clear();
            GL11.glReadPixels(
                0,
                0,
                FBO_SIZE,
                FBO_SIZE,
                GL12.GL_BGRA,
                GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                PIXEL_READ_BUFFER);
            PIXEL_READ_BUFFER.get(PIXEL_ARRAY);

            return analyzePixelBuffer(PIXEL_ARRAY, 0xFFFFFF);
        } catch (Throwable e) {
            ChromaticTooltips.LOG.warn("Failed to sample colors of {}", stack, e);
            return null;
        } finally {
            try {
                if (pushedModelView) {
                    GL11.glMatrixMode(GL11.GL_MODELVIEW);
                    GL11.glPopMatrix();
                }

                if (pushedProjection) {
                    GL11.glMatrixMode(GL11.GL_PROJECTION);
                    GL11.glPopMatrix();
                }
            } catch (Throwable ignored) {}

            if (pushedAttrib) {
                try {
                    GL11.glPopAttrib();
                } catch (Throwable ignored) {}
            }

            try {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, prevFbo);
                GL11.glViewport(
                    VIEWPORT_BUFFER.get(0),
                    VIEWPORT_BUFFER.get(1),
                    VIEWPORT_BUFFER.get(2),
                    VIEWPORT_BUFFER.get(3));
            } catch (Throwable ignored) {}

            isSamplingFbo = false;
        }
    }

    private static PaletteEntry computeIconEntry(IIcon icon, int spriteType, int tint) {
        if (icon == null || icon.getIconName() == null) {
            final float[] hsv = rgbToHsv(tint);
            return new PaletteEntry(Tones.single(hsv[0], hsv[1], hsv[2]));
        }

        if (icon instanceof TextureAtlasSprite) {
            final TextureAtlasSprite sprite = (TextureAtlasSprite) icon;

            try {
                final int frameCount = sprite.getFrameCount();

                if (frameCount > 0 && sprite.getIconWidth() > 0 && sprite.getIconHeight() > 0) {
                    final int step = Math.max(1, frameCount / 12);
                    final List<Tones> frames = new ArrayList<>();

                    for (int f = 0; f < frameCount; f += step) {
                        final int[][] frameData = sprite.getFrameTextureData(f);

                        if (frameData != null && frameData.length > 0 && frameData[0] != null) {
                            final Tones tones = analyzePixelBuffer(frameData[0], tint);
                            if (tones != null) frames.add(tones);
                        }
                    }

                    if (!frames.isEmpty()) {
                        return new PaletteEntry(frames);
                    }
                }
            } catch (Throwable ignored) {}
        }

        try {
            final IResource resource = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(getIconResourceLocation(icon.getIconName(), spriteType));

            try (InputStream is = resource.getInputStream()) {
                final BufferedImage img = ImageIO.read(is);

                if (img != null) {
                    final int width = img.getWidth();
                    final int totalHeight = img.getHeight();
                    final int frameHeight = Math.min(width, totalHeight);
                    final int frameCount = (width > 0 && totalHeight >= width) ? (totalHeight / width) : 1;
                    final int step = Math.max(1, frameCount / 12);
                    final List<Tones> frames = new ArrayList<>();

                    for (int f = 0; f < frameCount; f += step) {
                        final int[] pixels = img.getRGB(0, f * frameHeight, width, frameHeight, null, 0, width);
                        final Tones tones = analyzePixelBuffer(pixels, tint);
                        if (tones != null) frames.add(tones);
                    }

                    if (!frames.isEmpty()) {
                        return new PaletteEntry(frames);
                    }
                }
            }
        } catch (Throwable ignored) {}

        final float[] hsv = rgbToHsv(tint);
        return new PaletteEntry(Tones.single(hsv[0], hsv[1], hsv[2]));
    }

    private static Tones analyzePixelBuffer(int[] pixels, int tint) {
        final float tintR = ((tint >> 16) & 0xFF) / 255.0f;
        final float tintG = ((tint >> 8) & 0xFF) / 255.0f;
        final float tintB = (tint & 0xFF) / 255.0f;

        final double[] binScore = new double[12];
        final double[] binWeight = new double[12];
        final double[] binR = new double[12];
        final double[] binG = new double[12];
        final double[] binB = new double[12];

        double totalR = 0, totalG = 0, totalB = 0, totalWeight = 0, chromaticWeight = 0;
        int opaqueCount = 0;
        int chromaticCount = 0;
        final float[] hsv = new float[3];

        for (int argb : pixels) {
            final int alpha = (argb >>> 24) & 0xFF;
            if (alpha < 36) continue;

            final int ri = Math.min(255, Math.round((((argb >> 16) & 0xFF) / 255.0f) * tintR * 255f));
            final int gi = Math.min(255, Math.round((((argb >> 8) & 0xFF) / 255.0f) * tintG * 255f));
            final int bi = Math.min(255, Math.round(((argb & 0xFF) / 255.0f) * tintB * 255f));

            Color.RGBtoHSB(ri, gi, bi, hsv);
            final float h = hsv[0];
            final float s = hsv[1];
            final float v = hsv[2];
            final float chroma = s * v;

            if (v < 0.08f) continue;
            opaqueCount++;

            float weight = (alpha / 255.0f) * (0.35f + chroma * 1.15f) * (0.40f + v * 0.60f);
            if (v > 0.94f && s < 0.08f) {
                weight *= 0.20f;
            }

            totalR += ri * weight;
            totalG += gi * weight;
            totalB += bi * weight;
            totalWeight += weight;

            if (chroma >= 0.060f && s >= 0.09f) {
                chromaticCount++;
                chromaticWeight += weight;
                final int bin = Math.min(11, (int) (h * 12.0f));
                binScore[bin] += weight;
                binWeight[bin] += weight;
                binR[bin] += ri * weight;
                binG[bin] += gi * weight;
                binB[bin] += bi * weight;
            }
        }

        if (totalWeight <= 0.001 || opaqueCount == 0) {
            return null;
        }

        int bestBin = -1;
        double bestClusterScore = 0;

        for (int i = 0; i < 12; i++) {
            if (binWeight[i] <= 0) continue;
            final double cluster = binScore[(i + 11) % 12] * 0.5 + binScore[i] + binScore[(i + 1) % 12] * 0.5;
            if (bestBin == -1 || cluster > bestClusterScore) {
                bestBin = i;
                bestClusterScore = cluster;
            }
        }

        final float areaCoverage = chromaticCount / (float) opaqueCount;

        if (bestBin != -1 && areaCoverage >= 0.08f && chromaticWeight >= totalWeight * 0.10) {
            double w1 = 0, r1 = 0, g1 = 0, b1 = 0;

            for (int d = -1; d <= 1; d++) {
                final int idx = (bestBin + d + 12) % 12;
                w1 += binWeight[idx];
                r1 += binR[idx];
                g1 += binG[idx];
                b1 += binB[idx];
            }

            if (w1 > 0.001) {
                final float[] hsv1 = Color.RGBtoHSB(clamp255(r1 / w1), clamp255(g1 / w1), clamp255(b1 / w1), null);
                final float coverageScale = Math.min(1.0f, areaCoverage / 0.22f);
                final float finalS1 = hsv1[1] * (0.45f + 0.55f * coverageScale);

                int secondBin = -1;

                for (int i = 0; i < 12; i++) {
                    int dist = Math.abs(i - bestBin);
                    if (dist > 6) dist = 12 - dist;
                    if (dist >= 2 && binScore[i] > 0 && (secondBin == -1 || binScore[i] > binScore[secondBin])) {
                        secondBin = i;
                    }
                }

                if (secondBin != -1 && binScore[secondBin] >= binScore[bestBin] * 0.22) {
                    final double w2 = binWeight[secondBin];
                    final float[] hsv2 = Color.RGBtoHSB(
                        clamp255(binR[secondBin] / w2),
                        clamp255(binG[secondBin] / w2),
                        clamp255(binB[secondBin] / w2),
                        null);
                    return Tones.twoTone(
                        hsv1[0],
                        finalS1,
                        hsv1[2],
                        hsv2[0],
                        hsv2[1] * (0.45f + 0.55f * coverageScale),
                        hsv2[2]);
                }

                return Tones.single(hsv1[0], finalS1, hsv1[2]);
            }
        }

        Color.RGBtoHSB(
            clamp255(totalR / totalWeight),
            clamp255(totalG / totalWeight),
            clamp255(totalB / totalWeight),
            hsv);
        return Tones.single(0.60f, 0.0f, hsv[2]);
    }

    private static int clamp255(double value) {
        return Math.min(255, Math.max(0, (int) Math.round(value)));
    }

    private static ResourceLocation getIconResourceLocation(String iconName, int spriteType) {
        String domain = "minecraft";
        String path = iconName;
        final int colonIdx = iconName.indexOf(':');

        if (colonIdx >= 0) {
            domain = iconName.substring(0, colonIdx);
            path = iconName.substring(colonIdx + 1);
        }

        return new ResourceLocation(domain, (spriteType == 0 ? "textures/blocks/" : "textures/items/") + path + ".png");
    }

    private static float[] rgbToHsv(int rgb) {
        return Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
    }

    private static float wrapHue(float h) {
        return h - (float) Math.floor(h);
    }

    private static float lerpHue(float h1, float h2, float t) {
        float diff = h2 - h1;
        if (diff > 0.5f) diff -= 1.0f;
        if (diff < -0.5f) diff += 1.0f;
        return wrapHue(h1 + diff * t);
    }

}
