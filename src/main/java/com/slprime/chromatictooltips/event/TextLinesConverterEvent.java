package com.slprime.chromatictooltips.event;

import java.util.ArrayList;
import java.util.List;

import com.slprime.chromatictooltips.api.TooltipTarget;

public class TextLinesConverterEvent extends TooltipEvent {

    public List<Object> list;

    public TextLinesConverterEvent(TooltipTarget target, List<?> list) {
        super(target);
        this.list = new ArrayList<>(list);
    }

}
