package com.slprime.chromatictooltips.util.color;

import java.util.function.IntUnaryOperator;

@FunctionalInterface
public interface ColorValue {

    int resolve(TooltipPalette palette);

    /** False when the result never depends on the palette, so callers can skip computing one. */
    default boolean isDynamic() {
        return true;
    }

    static ColorValue constant(int argb) {
        return new ColorValue() {

            @Override
            public int resolve(TooltipPalette palette) {
                return argb;
            }

            @Override
            public boolean isDynamic() {
                return false;
            }
        };
    }

    static ColorValue[] constants(int[] argb) {
        final ColorValue[] values = new ColorValue[argb.length];

        for (int i = 0; i < argb.length; i++) {
            values[i] = constant(argb[i]);
        }

        return values;
    }

    static ColorValue map(ColorValue base, IntUnaryOperator operator) {

        if (base == null) {
            return null;
        } else if (!base.isDynamic()) {
            return constant(operator.applyAsInt(base.resolve(TooltipPalette.DEFAULT)));
        }

        return palette -> operator.applyAsInt(base.resolve(palette));
    }

}
