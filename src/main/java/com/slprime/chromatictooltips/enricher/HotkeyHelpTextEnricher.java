package com.slprime.chromatictooltips.enricher;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.slprime.chromatictooltips.api.ITooltipComponent;
import com.slprime.chromatictooltips.api.ITooltipEnricher;
import com.slprime.chromatictooltips.api.TooltipContext;
import com.slprime.chromatictooltips.component.TextComponent;
import com.slprime.chromatictooltips.config.EnricherConfig;
import com.slprime.chromatictooltips.event.HotkeyEnricherEvent;
import com.slprime.chromatictooltips.util.ClientUtil;

public class HotkeyHelpTextEnricher implements ITooltipEnricher {

    protected String moreText;

    public HotkeyHelpTextEnricher() {
        this.moreText = ClientUtil.translate("enricher.hotkeys.helpText", ClientUtil.translate("key.alt"));
    }

    @Override
    public String sectionId() {
        return "hotkeys:help-text";
    }

    @Override
    public EnricherPlace place() {
        return EnricherPlace.BODY;
    }

    @Override
    public EnumSet<EnricherMode> mode() {
        return EnumSet.of(EnricherMode.DEFAULT);
    }

    @Override
    public List<ITooltipComponent> build(TooltipContext context) {

        if (!EnricherConfig.hotkeysEnabled || !EnricherConfig.hotkeysHelpTextEnabled) {
            return null;
        }

        final ITooltipComponent component = getHotkeysHelpText(context);

        return component == null ? null : Collections.singletonList(component);
    }

    protected TextComponent getHotkeysHelpText(TooltipContext context) {
        final HotkeyEnricherEvent event = new HotkeyEnricherEvent(context, new HashMap<>());
        ClientUtil.postEvent(event);

        event.hotkeys.remove(null);
        event.hotkeys.remove("");

        if (!event.hotkeys.isEmpty() && this.moreText != null) {
            return new TextComponent(this.moreText);
        }

        return null;
    }

}
