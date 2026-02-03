package com.slprime.chromatictooltips.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum NumberFormat {

    SI,
    E,
    POWER;

    private static final int SIGNIFICANT_DIGITS = 3;
    private static final int DIVISION_BASE = 1000;
    private static final char[] SI_POSTFIXES = "kMGTPE".toCharArray();
    private static final Map<Locale, DecimalFormat> decimalFormatters = new HashMap<>();

    private static final DecimalFormat SI_FORMAT;

    static {
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');

        SI_FORMAT = new DecimalFormat(".#;0.#");
        SI_FORMAT.setDecimalFormatSymbols(symbols);
        SI_FORMAT.setRoundingMode(RoundingMode.DOWN);
    }

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

    public static String formatWithCommas(double number) {
        return getDecimalFormat().format(number);
    }

    public static String formatWithCommas(long number) {
        return getDecimalFormat().format(number);
    }

    public String format(long number) {
        switch (this) {
            case SI:
                return toSI(number, 4);
            case E:
                return toE(number);
            case POWER:
                return toPower(number);
            default:
                return getDecimalFormat().format(number);
        }
    }

    public String format(long number, int detailCutoffPower) {

        if (number < pow10(detailCutoffPower)) {
            return getDecimalFormat().format(number);
        }

        return format(number);
    }

    private static String toE(long number) {
        if (number == 0) {
            return "0";
        }

        int exponent = (int) Math.log10(number);
        int scale = SIGNIFICANT_DIGITS - 1;

        long divisor = pow10(exponent - scale);
        long mantissaInt = number / divisor;

        long intPart = mantissaInt / pow10(scale);
        long fracPart = mantissaInt % pow10(scale);

        return intPart + "." + padZeros(fracPart, scale) + "e" + exponent;
    }

    private static String toPower(long number) {
        if (number == 0) {
            return "0";
        }

        int exponent = (int) Math.log10(number);
        int scale = SIGNIFICANT_DIGITS - 1;

        long divisor = pow10(exponent - scale);
        long mantissaInt = number / divisor;

        long intPart = mantissaInt / pow10(scale);
        long fracPart = mantissaInt % pow10(scale);

        return intPart + "." + padZeros(fracPart, scale) + " x 10^" + exponent;
    }

    private static long pow10(int exp) {
        long r = 1;

        for (int i = 0; i < exp; i++) {
            r *= 10;
        }

        return r;
    }

    private static String padZeros(long value, int width) {
        final String s = Long.toString(value);
        final int missing = width - s.length();
        return missing <= 0 ? s : "000000000000000000".substring(0, missing) + s;
    }

    private static String toSI(long number, int width) {
        final String numberString = Long.toString(number);
        int size = numberString.length();

        if (size <= width) {
            return numberString;
        }

        long base = number;
        double last = base * DIVISION_BASE;
        int exponent = -1;
        char postfix = 0;

        while (size > width && exponent + 1 < SI_POSTFIXES.length) {
            last = base;
            base /= DIVISION_BASE;
            exponent++;

            postfix = SI_POSTFIXES[exponent];
            size = Long.toString(base)
                .length() + 1;
        }

        final String withPrecision = SI_FORMAT.format(last / DIVISION_BASE) + postfix;
        final String withoutPrecision = base + String.valueOf(postfix);

        return (withPrecision.length() <= width) ? withPrecision : withoutPrecision;
    }
}
