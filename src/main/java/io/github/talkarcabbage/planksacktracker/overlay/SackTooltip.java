package io.github.talkarcabbage.planksacktracker.overlay;

import io.github.talkarcabbage.planksacktracker.planksack.PlankSackManager;
import io.github.talkarcabbage.planksacktracker.planksack.PlankStorageSet;
import io.github.talkarcabbage.planksacktracker.planksack.PlankTier;
import io.github.talkarcabbage.planksacktracker.SackTrackerConfig;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

public class SackTooltip {

    private static final String BR = "</br>";

    private final PlankSackManager manager;
    private final TooltipManager tooltipManager;
    private final SackTrackerConfig config;

    public SackTooltip(PlankSackManager manager, TooltipManager tooltipManager, SackTrackerConfig config) {
        this.manager = manager;
        this.tooltipManager = tooltipManager;
        this.config = config;
    }

    void renderTooltip() {
        var currentPlankSack = manager.getCurrentPlankSack();
        var brLength = 0;
        StringBuilder tb = new StringBuilder();

        if (currentPlankSack.isEmpty()) {
            tooltipManager.add(new Tooltip("Empty!"));
            return;
        }

        tb.append(currentPlankSack.getPlanks()>0? getTooltipTierText(PlankTier.PLANK, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(BR);
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getOaks()>0? getTooltipTierText(PlankTier.OAK, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(BR);
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getTeaks()>0? getTooltipTierText(PlankTier.TEAK, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(BR);
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getMahoganies()>0? getTooltipTierText(PlankTier.MAHOGANY, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(BR);
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getRosewoods()>0? getTooltipTierText(PlankTier.ROSEWOOD, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(BR);
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getIronwoods()>0? getTooltipTierText(PlankTier.IRONWOOD, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(BR);
        }
        tb.append(currentPlankSack.getCamphors()>0? getTooltipTierText(PlankTier.CAMPHOR, currentPlankSack)+"\n":"");
        tooltipManager.add(new Tooltip(tb.toString()));
    }

    //Commonly shared method for getting overlay text used in other overlays
    String getTooltipTierText(PlankTier tier, PlankStorageSet storage) {
        var textColor = config.tooltipTextColor();
        var numberColor = config.tooltipNumberColor();

        switch (tier) {
            case PLANK:
                return color(padTwo(storage.getTierAmount(tier)), numberColor) + color(" Basic planks", textColor);
            case OAK:
                return color(padTwo(storage.getTierAmount(tier)), numberColor) + color(" Oak planks", textColor);
            case TEAK:
                return color(padTwo(storage.getTierAmount(tier)), numberColor) + color(" Teak planks", textColor);
            case MAHOGANY:
                return color(padTwo(storage.getTierAmount(tier)), numberColor) + color(" Mahogany planks", textColor);
            case ROSEWOOD:
                return color(padTwo(storage.getTierAmount(tier)), numberColor) + color(" Rosewood planks", textColor);
            case IRONWOOD:
                return color(padTwo(storage.getTierAmount(tier)), numberColor) + color(" Ironwood planks", textColor);
            case CAMPHOR:
                return color(padTwo(storage.getTierAmount(tier)), numberColor) + color(" Camphor planks", textColor);
        }
        return "";
    }

    private String padTwo(int number) {
        if (number==1) return number+"  "; //1 is very thin on rs font
        if (number<9) return number+" ";
        return number+"";
    }

    public static String color(String text, Color color) {
        return ColorUtil.wrapWithColorTag(text, color);
    }

}
