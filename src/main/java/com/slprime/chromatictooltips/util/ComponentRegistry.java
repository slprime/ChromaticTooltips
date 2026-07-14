package com.slprime.chromatictooltips.util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.slprime.chromatictooltips.api.ITooltipComponent;

public class ComponentRegistry {

    private static final int ID_BITS = 12;
    private static final int ID_MASK = (1 << ID_BITS) - 1;

    private static final int GEN_BITS = 32 - ID_BITS;
    private static final int GEN_MASK = (1 << GEN_BITS) - 1;

    private static final int CAPACITY = 4096;

    private static final String COMPONENT_PREFIX = "§z";
    private static final String PERMANENT_COMPONENT_PREFIX = "§Z";

    private final Map<String, ITooltipComponent> permanentValues = new HashMap<>();
    private final Map<ITooltipComponent, String> permanentReverse = new IdentityHashMap<>();
    private int permanentCursor = 0;

    private final ITooltipComponent[] temporaryValues = new ITooltipComponent[CAPACITY];
    private final Map<ITooltipComponent, Integer> temporaryReverse = new IdentityHashMap<>(CAPACITY);
    private final int[] generation = new int[CAPACITY];
    private int temporaryCursor = 0;

    public String addTemporary(ITooltipComponent value) {
        final Integer existing = this.temporaryReverse.get(value);

        if (existing != null) {
            return COMPONENT_PREFIX + makeToken(existing);
        }

        final int id = this.temporaryCursor;

        if (this.temporaryValues[id] != null) {
            this.temporaryReverse.remove(this.temporaryValues[id]);
            this.generation[id] = (this.generation[id] + 1) & GEN_MASK;
        }

        this.temporaryValues[id] = value;
        this.temporaryReverse.put(value, id);
        this.temporaryCursor = (this.temporaryCursor + 1) % this.temporaryValues.length;

        return COMPONENT_PREFIX + makeToken(id);
    }

    private int makeToken(int id) {
        return ((this.generation[id] & GEN_MASK) << ID_BITS) | id;
    }

    public ITooltipComponent getTemporary(String line) {

        if (line.startsWith(COMPONENT_PREFIX)) {
            try {
                final int token = Integer.parseInt(line.substring(COMPONENT_PREFIX.length()));
                final int id = token & ID_MASK;
                final int gen = token >>> ID_BITS;

                if (id >= this.temporaryValues.length) return null;
                if (this.generation[id] != gen) return null;

                return this.temporaryValues[id];
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    public ITooltipComponent get(String line) {
        ITooltipComponent component = getPermanent(line);

        if (component == null) {
            component = getTemporary(line);
        }

        return component;
    }

    public String addPermanent(ITooltipComponent value) {
        final String existing = this.permanentReverse.get(value);

        if (existing != null) {
            return existing;
        }

        final String key = PERMANENT_COMPONENT_PREFIX + (this.permanentCursor++);
        this.permanentValues.put(key, value);
        this.permanentReverse.put(value, key);
        return key;
    }

    public ITooltipComponent getPermanent(String line) {
        return this.permanentValues.get(line);
    }

    public boolean removePermanent(String key) {
        final ITooltipComponent value = this.permanentValues.remove(key);

        if (value != null) {
            this.permanentReverse.remove(value);
            return true;
        }

        return false;
    }

    public boolean removePermanent(ITooltipComponent value) {
        final String key = this.permanentReverse.remove(value);

        if (key != null) {
            this.permanentValues.remove(key);
            return true;
        }

        return false;
    }

}
