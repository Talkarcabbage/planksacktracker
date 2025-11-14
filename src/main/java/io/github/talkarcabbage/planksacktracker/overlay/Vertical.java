package io.github.talkarcabbage.planksacktracker.overlay;

import io.github.talkarcabbage.planksacktracker.Entry;
import io.github.talkarcabbage.planksacktracker.PlankSackManager;
import io.github.talkarcabbage.planksacktracker.PlankTier;
import io.github.talkarcabbage.planksacktracker.SackTrackerConfig;
import io.github.talkarcabbage.planksacktracker.overlayenums.OverlayTextType;
import net.runelite.api.Point;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;

import static io.github.talkarcabbage.planksacktracker.overlay.PlankSackOverlay.ABBREVIATED_WIDTH_BUFFER;
import static io.github.talkarcabbage.planksacktracker.overlay.PlankSackOverlay.NON_ABBREVIATED_WIDTH_BUFFER;

public class Vertical {
    private final PlankSackManager manager;
    private final SackTrackerConfig config;
    private final PlankSackOverlay overlay;

    public Vertical(PlankSackManager manager, SackTrackerConfig config, PlankSackOverlay overlay) {
        this.manager = manager;
        this.config = config;
        this.overlay = overlay;
    }

    void drawVertical(Graphics2D graphics, WidgetItem widgetItem) {
        var currentSack = manager.getCurrentPlankSack();
        var x = widgetItem.getCanvasLocation().getX();
        var y = widgetItem.getCanvasLocation().getY();
        var labelSize = getCurrentLabelValueOffset();
        var horizontalOffset = -4;
        var verticalSpacing = 12;

        if (config.textType()== OverlayTextType.LONG) {
            horizontalOffset += -7;
        }

        for (Entry<PlankTier, Integer> nextPlank : currentSack) {
            var label = getVerticalOverlayLabelByConfig(nextPlank.getKey());
            if (label.isEmpty()) label=null;
            var image = config.showOverlayIcons()?(Images.getIconForTier(nextPlank.getKey())):null;
            drawSingleVerticalEntry(graphics, image, label, config.textColor(), config.numberColor(), nextPlank.getValue(),x+horizontalOffset,y+=verticalSpacing, labelSize);
        }
    }
    private int getCurrentLabelValueOffset() {
        switch (config.textType()) {
            case NONE:
                return 0;
            case LETTER:
                return ABBREVIATED_WIDTH_BUFFER;
            case LONG:
                return NON_ABBREVIATED_WIDTH_BUFFER;
            default:
                return 0;
        }
    }

    static protected void drawSingleVerticalEntry(Graphics2D graphics, @Nullable BufferedImage icon, @Nullable String label, Color textColor, Color numberColor, int amount, int x, int y, int labelWidth) {
        var font = FontManager.getRunescapeSmallFont();
        var runningXOffset = 0;
        graphics.setFont(font);
        int fontHeight = graphics.getFontMetrics(font).getHeight();
        if (label!=null && label.isEmpty()) label=null;

        if (icon!=null) {
            OverlayUtil.renderImageLocation(graphics, new Point(x+runningXOffset, y-(fontHeight+1)), icon);
            runningXOffset+=6;
        }
        runningXOffset+=5;
        if (label!=null) {
            PlankSackOverlay.drawStringShadowed(graphics, font, label, x+runningXOffset, y, textColor);
        } else {
            runningXOffset+=16; // Since the number amount right-justifies, if we don't add this it will overlap into the plank icon area.
        }
        runningXOffset-=rightJustifyXValue(graphics, ""+amount);
        PlankSackOverlay.drawStringShadowed(graphics, font, ""+amount,x+runningXOffset+labelWidth, y, numberColor);
    }

    static private int rightJustifyXValue(Graphics2D graphics, String string) {
        return graphics.getFontMetrics().stringWidth(string);
    }

    private String getVerticalOverlayLabelByConfig(PlankTier tier) {
        switch (config.textType()) {
            case NONE:
                return "";
            case LETTER:
                switch (tier) {
                    case PLANK:
                        return "P:";
                    case OAK:
                        return "O:";
                    case TEAK:
                        return "T:";
                    case MAHOGANY:
                        return "M:";
                    case ROSEWOOD:
                        return "R:";
                    case IRONWOOD:
                        return "I:";
                    case CAMPHOR:
                        return "C:";
                }
            case LONG:
                switch (tier) {
                    case PLANK:
                        return "Plank:\t";
                    case OAK:
                        return "Oak:\t";
                    case TEAK:
                        return "Teak:\t";
                    case MAHOGANY:
                        return "Mohag:\t";
                    case ROSEWOOD:
                        return "Rose:\t";
                    case IRONWOOD:
                        return "Iron:\t";
                    case CAMPHOR:
                        return "Camph:\t";
                }
        }
        return "";
    }
}
