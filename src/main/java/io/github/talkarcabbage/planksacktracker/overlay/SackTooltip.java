package io.github.talkarcabbage.planksacktracker.overlay;

import io.github.talkarcabbage.planksacktracker.PlankSackManager;
import io.github.talkarcabbage.planksacktracker.PlankTier;
import io.github.talkarcabbage.planksacktracker.Utils;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import java.awt.*;

public class SackTooltip {

    private final PlankSackManager manager;
    private final TooltipManager tooltipManager;
    private final PlankSackOverlay overlay;

    public SackTooltip(PlankSackManager manager, TooltipManager tooltipManager, PlankSackOverlay overlay) {
        this.manager = manager;
        this.tooltipManager = tooltipManager;
        this.overlay = overlay;
    }

    void renderTooltip(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        var currentPlankSack = manager.getCurrentPlankSack();
        var brLength = 0;
        StringBuilder tb = new StringBuilder();

        if (currentPlankSack.isEmpty()) {
            tooltipManager.add(new net.runelite.client.ui.overlay.tooltip.Tooltip("Empty!"));
            return;
        }

        tb.append(currentPlankSack.getPlanks()>0?overlay.getOverlayTextFull(PlankTier.PLANK, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getOaks()>0?overlay.getOverlayTextFull(PlankTier.OAK, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getTeaks()>0?overlay.getOverlayTextFull(PlankTier.TEAK, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getMahoganies()>0?overlay.getOverlayTextFull(PlankTier.MAHOGANY, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getRosewoods()>0?overlay.getOverlayTextFull(PlankTier.ROSEWOOD, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getIronwoods()>0?overlay.getOverlayTextFull(PlankTier.IRONWOOD, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
        }
        tb.append(currentPlankSack.getCamphors()>0?overlay.getOverlayTextFull(PlankTier.CAMPHOR, currentPlankSack)+"\n":"");
        tooltipManager.add(new net.runelite.client.ui.overlay.tooltip.Tooltip(tb.toString()));
    }

}
