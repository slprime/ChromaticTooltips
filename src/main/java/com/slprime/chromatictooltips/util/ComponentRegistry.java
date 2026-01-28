package com.slprime.chromatictooltips.util;

import java.util.IdentityHashMap;
import java.util.Map;

import com.slprime.chromatictooltips.api.ITooltipComponent;

public class ComponentRegistry {

    private static final int ID_BITS = 12;
    private static final int ID_MASK = (1 << ID_BITS) - 1;

    private static final int GEN_BITS = 32 - ID_BITS;
    private static final int GEN_MASK = (1 << GEN_BITS) - 1;

    private static final int CAPACITY = 4096;

    private final ITooltipComponent[] values = new ITooltipComponent[CAPACITY];
    private final Map<ITooltipComponent, Integer> reverse = new IdentityHashMap<>(CAPACITY);
    private final int[] generation = new int[CAPACITY];

    private int cursor = 0;

    public int add(ITooltipComponent value) {
        final Integer existing = this.reverse.get(value);

        if (existing != null) {
            return makeToken(existing);
        }

        final int id = this.cursor;

        if (this.values[id] != null) {
            this.reverse.remove(this.values[id]);
            this.generation[id] = (this.generation[id] + 1) & GEN_MASK;
        }

        this.values[id] = value;
        this.reverse.put(value, id);
        this.cursor = (this.cursor + 1) % this.values.length;

        return makeToken(id);
    }

    public ITooltipComponent get(int token) {
        final int id = token & ID_MASK;
        final int gen = token >>> ID_BITS;

        if (id >= this.values.length) return null;
        if (this.generation[id] != gen) return null;

        return this.values[id];
    }

    public int getId(ITooltipComponent value) {
        final Integer id = this.reverse.get(value);
        return id != null ? makeToken(id) : -1;
    }

    private int makeToken(int id) {
        return ((this.generation[id] & GEN_MASK) << ID_BITS) | id;
    }

}
