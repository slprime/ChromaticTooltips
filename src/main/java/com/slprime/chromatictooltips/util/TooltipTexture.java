package com.slprime.chromatictooltips.util;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.slprime.chromatictooltips.ChromaticTooltips;
import com.slprime.chromatictooltips.api.TooltipStyle;

public class TooltipTexture {

    protected static class Animation {

        public long lastFrameTime = System.currentTimeMillis();
        public int currentFrameIndex = 0;
        public int[] frameTimes;
        public int[] frameIndices;
    }

    protected enum RepeatFit {

        FLOOR,
        CEIL,
        CLIP,
        STRETCH;

        public static RepeatFit fromString(String str) {
            if ("floor".equalsIgnoreCase(str)) {
                return FLOOR;
            } else if ("ceil".equalsIgnoreCase(str)) {
                return CEIL;
            } else if ("clip".equalsIgnoreCase(str)) {
                return CLIP;
            } else if ("stretch".equalsIgnoreCase(str)) {
                return STRETCH;
            }
            return FLOOR;
        }

    }

    protected class Repeat {

        public int limit = Integer.MAX_VALUE;
        public int gap = 0;
        public RepeatFit fit = RepeatFit.FLOOR;

        public Repeat(int limit, int gap, RepeatFit fit) {
            this.limit = limit;
            this.gap = gap;
            this.fit = fit;
        }
    }

    private final ResourceLocation resourceLocation;

    private final int x;
    private final int y;

    private final int width;
    private final int height;

    private final int textureWidth;
    private final int textureHeight;

    protected Animation animation;

    protected double[][] screenTexCoords = null;
    protected int[][] inlineSlices = null;
    protected int[][] blockSlices = null;
    protected double[][] calculatedInlineSizes = null;
    protected double[][] calculatedBlockSizes = null;
    protected double lastInlineWidth;
    protected double lastBlockWidth;

    protected Repeat repeatInline;
    protected Repeat repeatBlock;

    public TooltipTexture(TooltipStyle style) {
        final String path = style.getAsString("path", "textures/tooltip.png");
        this.resourceLocation = new ResourceLocation(path.contains(":") ? path : ChromaticTooltips.MODID + ":" + path);

        ClientUtil.bindTexture(this.resourceLocation);
        this.textureWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        this.textureHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

        final int[] region = style.getAsProperty(
            "region",
            new String[][] { new String[] { "x" }, new String[] { "y" }, new String[] { "width" },
                new String[] { "height" }, },
            new int[] { 0, 0, this.textureWidth, this.textureHeight });

        this.x = region[0];
        this.y = region[1];
        this.width = region[2];
        this.height = region[3];

        if (style.containsKey("repeat") && style.get("repeat")
            .isJsonPrimitive()) {
            this.repeatInline = this.repeatBlock = prepareRepeat(style.get("repeat"));
            this.inlineSlices = this.blockSlices = new int[][] { { this.height, 0 } };
        } else {

            if (style.containsKey("repeatInline")) {
                this.repeatInline = prepareRepeat(style.get("repeatInline"));
                this.inlineSlices = new int[][] { { this.width, 0 } };
            } else if (style.containsKey("repeat.inline")) {
                this.repeatInline = prepareRepeat(style.get("repeat.inline"));
                this.inlineSlices = new int[][] { { this.width, 0 } };
            } else if (style.get("slice") != null && style.get("slice")
                .isJsonArray()) {
                    this.inlineSlices = prepareTextureSlice(style.getAsJsonArray("slice", new JsonArray()), this.width);
                } else if (style.get("sliceInline") != null && style.get("sliceInline")
                    .isJsonArray()) {
                        this.inlineSlices = prepareTextureSlice(
                            style.getAsJsonArray("sliceInline", new JsonArray()),
                            this.width);
                    } else {
                        this.inlineSlices = prepareTextureSlice(
                            style.getAsJsonArray("slice.inline", new JsonArray()),
                            this.width);
                    }

            if (style.containsKey("repeatBlock")) {
                this.repeatBlock = prepareRepeat(style.get("repeatBlock"));
                this.blockSlices = new int[][] { { this.height, 0 } };
            } else if (style.containsKey("repeat.block")) {
                this.repeatBlock = prepareRepeat(style.get("repeat.block"));
                this.blockSlices = new int[][] { { this.height, 0 } };
            } else if (style.get("slice") != null && style.get("slice")
                .isJsonArray()) {
                    this.blockSlices = prepareTextureSlice(style.getAsJsonArray("slice", new JsonArray()), this.height);
                } else if (style.get("sliceBlock") != null && style.get("sliceBlock")
                    .isJsonArray()) {
                        this.blockSlices = prepareTextureSlice(
                            style.getAsJsonArray("sliceBlock", new JsonArray()),
                            this.height);
                    } else {
                        this.blockSlices = prepareTextureSlice(
                            style.getAsJsonArray("slice.block", new JsonArray()),
                            this.height);
                    }

        }

        if (style.containsKey("animation")) {
            prepareAnimation(style.getAsStyle("animation"));
        }
    }

    protected void prepareAnimation(TooltipStyle style) {
        this.animation = new Animation();
        final int frametime = style.getAsInt("frametime", 1);
        final JsonArray frames = style.getAsJsonArray("frames", new JsonArray());
        final int maxFrames = this.textureHeight / this.height;

        if (frames.size() > 0) {
            this.animation.frameTimes = new int[frames.size()];
            this.animation.frameIndices = new int[frames.size()];

            for (int i = 0; i < frames.size(); i++) {
                if (frames.get(i)
                    .isJsonObject()) {
                    this.animation.frameIndices[i] = frames.get(i)
                        .getAsJsonObject()
                        .get("index")
                        .getAsInt();
                    this.animation.frameTimes[i] = frames.get(i)
                        .getAsJsonObject()
                        .get("time")
                        .getAsInt();
                } else {
                    this.animation.frameIndices[i] = frames.get(i)
                        .getAsInt();
                    this.animation.frameTimes[i] = frametime;
                }
            }

        } else if (style.getAsBoolean("framePingPong", false)) {
            this.animation.frameTimes = new int[maxFrames * 2];
            this.animation.frameIndices = new int[maxFrames * 2];

            for (int i = 0; i < maxFrames; i++) {
                this.animation.frameTimes[i] = frametime;
                this.animation.frameIndices[i] = i;

                this.animation.frameTimes[maxFrames - 1 - i] = frametime;
                this.animation.frameIndices[maxFrames - 1 - i] = i;
            }

        } else {
            this.animation.frameTimes = new int[maxFrames];
            this.animation.frameIndices = new int[maxFrames];

            for (int i = 0; i < maxFrames; i++) {
                this.animation.frameTimes[i] = frametime;
                this.animation.frameIndices[i] = i;
            }

        }

        if (this.animation.frameIndices.length <= 1) {
            this.animation = null;
        }
    }

    protected Repeat prepareRepeat(JsonElement props) {

        if (props.isJsonPrimitive() && props.getAsJsonPrimitive()
            .isBoolean()) {
            if (props.getAsBoolean()) {
                return new Repeat(Integer.MAX_VALUE, 0, RepeatFit.FLOOR);
            } else {
                return new Repeat(1, 0, RepeatFit.FLOOR);
            }
        } else if (props.isJsonPrimitive() && props.getAsJsonPrimitive()
            .isNumber()) {
                return new Repeat(props.getAsInt(), 0, RepeatFit.FLOOR);
            } else if (props.isJsonPrimitive() && props.getAsJsonPrimitive()
                .isString()) {
                    return new Repeat(Integer.MAX_VALUE, 0, RepeatFit.fromString(props.getAsString()));
                } else if (props.isJsonObject()) {
                    final JsonObject obj = props.getAsJsonObject();
                    final int limit = obj.has("limit") ? obj.get("limit")
                        .getAsInt() : Integer.MAX_VALUE;
                    final int gap = obj.has("gap") ? obj.get("gap")
                        .getAsInt() : 0;
                    final RepeatFit fit = obj.has("fit") ? RepeatFit.fromString(
                        obj.get("fit")
                            .getAsString())
                        : RepeatFit.FLOOR;

                    return new Repeat(limit, gap, fit);
                }

        return null;
    }

    protected int[][] prepareTextureSlice(JsonArray style, int defaultSize) {
        final int[][] result = new int[style.size()][2];
        int resultIndex = 0;

        for (int i = 0; i < result.length; i++) {
            JsonElement element = style.get(i);
            int base = 0;
            int grow = 0;

            if (element.isJsonObject()) {
                final JsonObject slice = element.getAsJsonObject();

                if (slice.has("base")) {
                    base = slice.get("base")
                        .getAsInt();
                    grow = getSliceProperty(slice, "grow", 0, 1);
                }

            } else if (element.isJsonPrimitive()) {

                if (element.getAsJsonPrimitive()
                    .isNumber()) {
                    base = element.getAsInt();
                } else if (element.getAsJsonPrimitive()
                    .isBoolean()) {
                        grow = element.getAsBoolean() ? 1 : 0;
                    }

            }

            result[resultIndex++] = new int[] { base, grow };
        }

        if (resultIndex == 0) {
            return new int[][] { { defaultSize, 1 } };
        }

        if (resultIndex != result.length) {
            final int[][] resized = new int[resultIndex][2];
            System.arraycopy(result, 0, resized, 0, resultIndex);
            return resized;
        }

        return result;
    }

    private int getSliceProperty(JsonObject slice, String key, int minValue, int boolValue) {

        if (slice.has(key) && slice.get(key)
            .isJsonPrimitive()) {
            if (slice.get(key)
                .getAsJsonPrimitive()
                .isNumber()) {
                return Math.max(
                    minValue,
                    slice.get(key)
                        .getAsInt());
            } else if (slice.get(key)
                .getAsJsonPrimitive()
                .isBoolean()) {
                    return slice.get(key)
                        .getAsBoolean() ? boolValue : minValue;
                }
        }

        return minValue;
    }

    protected double[][] calculateTextureSlices(int[][] slices, double screenSize) {
        double[][] result = new double[slices.length][4]; // screenShift, screenSize, texShift, texSize
        int resultIndex = 0;
        double fixedSize = 0d;
        int growCount = 0;

        for (int i = 0; i < slices.length; i++) {
            if (slices[i][1] == 0) { // fixed
                fixedSize += slices[i][0];
            } else { // grow
                growCount += slices[i][1];
            }
        }

        final double growStep = growCount > 0 ? (screenSize - fixedSize) / (double) growCount : 0;
        double lastScreenShift = 0;
        double lastTexShift = 0;

        for (int i = 0; i < slices.length; i++) {

            if (slices[i][1] == 0) { // fixed
                result[resultIndex][0] = lastScreenShift;
                result[resultIndex][1] = slices[i][0];
                result[resultIndex][2] = lastTexShift;
                result[resultIndex][3] = slices[i][0];

                lastScreenShift += result[resultIndex][1];
                resultIndex++;
            } else { // growable

                if (slices[i][1] * growStep > 0) {
                    result[resultIndex][0] = lastScreenShift;
                    result[resultIndex][1] = slices[i][1] * growStep;
                    result[resultIndex][2] = lastTexShift;
                    result[resultIndex][3] = slices[i][0];
                    resultIndex++;
                }

                lastScreenShift += slices[i][1] * growStep;
            }

            lastTexShift += slices[i][0];
        }

        if (resultIndex != result.length) {
            final double[][] resized = new double[resultIndex][4];
            System.arraycopy(result, 0, resized, 0, resultIndex);
            return resized;
        }

        return result;
    }

    protected int getRepeatCount(Repeat repeat, double texSize, double screenSize) {
        double repeatCount = (screenSize + repeat.gap) / (texSize + repeat.gap);

        if (repeat.fit == RepeatFit.FLOOR || repeat.fit == RepeatFit.STRETCH) {
            repeatCount = Math.floor(repeatCount);
        } else if (repeat.fit == RepeatFit.CEIL || repeat.fit == RepeatFit.CLIP) {
            repeatCount = Math.ceil(repeatCount);
        }

        return (int) Math.max(1, Math.min(repeatCount, repeat.limit));
    }

    protected double[][] calculateTextureRepeat(double screenSize, double texSize, Repeat repeat, TooltipAlign align) {
        final int repeatCount = getRepeatCount(repeat, texSize, screenSize);
        final double[][] sizes = new double[repeatCount][4]; // screenShift, screenSize, texShift, texSize
        double spaceBefore = 0;
        double spaceAfter = 0;

        if (align == TooltipAlign.START) {
            spaceAfter = Math.abs(screenSize - (repeatCount * texSize + (repeatCount - 1) * repeat.gap));
        } else if (align == TooltipAlign.CENTER) {
            spaceBefore = spaceAfter = Math
                .abs((screenSize - (repeatCount * texSize + (repeatCount - 1) * repeat.gap)) / 2d);
        } else if (align == TooltipAlign.END) {
            spaceBefore = Math.abs(screenSize - (repeatCount * texSize + (repeatCount - 1) * repeat.gap));
        }

        if (repeat.fit == RepeatFit.CLIP) {
            double lastScreenShift = 0;

            sizes[0][0] = 0;
            sizes[0][1] = Math.max(0, texSize - spaceBefore);
            sizes[0][2] = spaceBefore;
            sizes[0][3] = sizes[0][1];
            lastScreenShift += sizes[0][1] + repeat.gap;

            for (int i = 1; i < repeatCount - 1; i++) {
                sizes[i][0] = lastScreenShift;
                sizes[i][1] = texSize;
                sizes[i][2] = 0;
                sizes[i][3] = texSize;

                lastScreenShift += sizes[i][1] + repeat.gap;
            }

            if (repeatCount > 1) {
                sizes[repeatCount - 1][0] = lastScreenShift;
                sizes[repeatCount - 1][1] = Math.max(0, texSize - spaceAfter);
                sizes[repeatCount - 1][2] = 0;
                sizes[repeatCount - 1][3] = sizes[repeatCount - 1][1];
            }

        } else {
            double lastScreenShift = spaceBefore;

            for (int i = 0; i < repeatCount; i++) {
                sizes[i][0] = lastScreenShift;
                sizes[i][1] = texSize;
                sizes[i][2] = 0;
                sizes[i][3] = texSize;

                lastScreenShift += sizes[i][1] + repeat.gap;
            }
        }

        return sizes;
    }

    public double getWidth(double width) {

        if (this.repeatInline != null) {

            if (this.repeatInline.fit == RepeatFit.FLOOR) {
                return Math.floor((width + this.repeatInline.gap) / (this.width + this.repeatInline.gap)) * this.width;
            } else if (this.repeatInline.fit == RepeatFit.CEIL) {
                return Math.ceil((width + this.repeatInline.gap) / (this.width + this.repeatInline.gap)) * this.width;
            }

            return width;
        } else {
            double fixedSize = 0d;
            boolean hasGrow = false;

            for (int i = 0; i < this.inlineSlices.length; i++) {
                if (this.inlineSlices[i][1] == 0) { // fixed
                    fixedSize += this.inlineSlices[i][0];
                } else { // grow
                    hasGrow = true;
                    break;
                }
            }

            return hasGrow ? width : fixedSize;
        }
    }

    public double getHeight(double height) {

        if (this.repeatBlock != null) {

            if (this.repeatBlock.fit == RepeatFit.FLOOR) {
                return Math.floor((height + this.repeatBlock.gap) / (this.height + this.repeatBlock.gap)) * this.height;
            } else if (this.repeatBlock.fit == RepeatFit.CEIL) {
                return Math.ceil((height + this.repeatBlock.gap) / (this.height + this.repeatBlock.gap)) * this.height;
            }

            return height;
        } else {
            double fixedSize = 0d;
            boolean hasGrow = false;

            for (int i = 0; i < this.blockSlices.length; i++) {
                if (this.blockSlices[i][1] == 0) { // fixed
                    fixedSize += this.blockSlices[i][0];
                } else { // grow
                    hasGrow = true;
                    break;
                }
            }

            return hasGrow ? height : fixedSize;
        }

    }

    public void draw(double x, double y, double width, double height, TooltipAlign alignInline, TooltipAlign alignBlock,
        int mixColor) {
        final boolean hasBlend = GL11.glGetBoolean(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(mixColor >> 16 & 255, mixColor >> 8 & 255, mixColor & 255, mixColor >> 24 & 255);
        ClientUtil.bindTexture(this.resourceLocation);

        if (this.calculatedInlineSizes == null || this.lastInlineWidth != width) {

            if (this.repeatInline != null) {
                this.calculatedInlineSizes = calculateTextureRepeat(width, this.width, this.repeatInline, alignInline);
            } else {
                this.calculatedInlineSizes = calculateTextureSlices(this.inlineSlices, width);
            }

            this.lastInlineWidth = width;
        }

        if (this.calculatedBlockSizes == null || this.lastBlockWidth != height) {

            if (this.repeatBlock != null) {
                this.calculatedBlockSizes = calculateTextureRepeat(height, this.height, this.repeatBlock, alignBlock);
            } else {
                this.calculatedBlockSizes = calculateTextureSlices(this.blockSlices, height);
            }

            this.lastBlockWidth = height;
        }

        drawTexture(x, y, width, height);

        if (!hasBlend) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    protected void drawTexture(double x, double y, double width, double height) {

        if (this.animation == null) {
            drawTextureSlice(x, y, width, height, this.x, this.y);
        } else {
            int frameIndex = this.animation.currentFrameIndex;

            if ((System.currentTimeMillis() - this.animation.lastFrameTime) / 50
                > this.animation.frameTimes[frameIndex]) {
                frameIndex++;
                if (frameIndex >= this.animation.frameIndices.length) {
                    frameIndex = 0;
                }
                this.animation.currentFrameIndex = frameIndex;
                this.animation.lastFrameTime = System.currentTimeMillis();
            }

            int frameY = this.animation.frameIndices[frameIndex] * this.height;
            drawTextureSlice(x, y, width, height, this.x, this.y + frameY);
        }

    }

    protected void drawTextureSlice(double x, double y, double width, double height, int xTexture, int yTexture) {

        for (double[] inline : this.calculatedInlineSizes) {
            if (inline[1] <= 0 || inline[3] <= 0) continue;
            for (double[] block : this.calculatedBlockSizes) {
                if (block[1] <= 0 || block[3] <= 0) continue;
                drawQuad(
                    x + inline[0],
                    y + block[0],
                    inline[1],
                    block[1],
                    xTexture + inline[2],
                    yTexture + block[2],
                    xTexture + inline[2] + inline[3],
                    yTexture + block[2] + block[3]);
            }
        }

    }

    protected void drawQuad(double x, double y, double width, double height, double x1Texture, double y1Texture,
        double x2Texture, double y2Texture) {
        double uMin = x1Texture / this.textureWidth;
        double vMin = y1Texture / this.textureHeight;

        double uMax = x2Texture / this.textureWidth;
        double vMax = y2Texture / this.textureHeight;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y, 0, uMin, vMin);
        tessellator.addVertexWithUV(x, y + height, 0, uMin, vMax);
        tessellator.addVertexWithUV(x + width, y + height, 0, uMax, vMax);
        tessellator.addVertexWithUV(x + width, y, 0, uMax, vMin);
        tessellator.draw();
    }

}
