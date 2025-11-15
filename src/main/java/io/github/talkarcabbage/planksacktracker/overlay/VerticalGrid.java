package io.github.talkarcabbage.planksacktracker.overlay;

import io.github.talkarcabbage.planksacktracker.*;
import io.github.talkarcabbage.planksacktracker.planksack.PlankSackManager;
import io.github.talkarcabbage.planksacktracker.planksack.PlankStorageSet;
import io.github.talkarcabbage.planksacktracker.planksack.PlankTier;
import io.github.talkarcabbage.planksacktracker.util.Entry;
import net.runelite.api.Point;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;

import static io.github.talkarcabbage.planksacktracker.overlayenums.OverlayTextType.NONE;

public class VerticalGrid {
    private final PlankSackManager manager;
    private final SackTrackerConfig config;

    private static final int INITIAL_X_OFFSET = -2;
    private static final int INITIAL_Y_OFFSET = 12;

    public VerticalGrid(PlankSackManager manager, SackTrackerConfig config) {
        this.manager = manager;
        this.config = config;
    }

    void drawGridVertical(Graphics2D graphics, WidgetItem widgetItem) {
        var widthBetween = 20;


        var font = FontManager.getRunescapeSmallFont();
        var currentSack = manager.getCurrentPlankSack();
        boolean rowFinished = false;
        var x = widgetItem.getCanvasLocation().getX() + INITIAL_X_OFFSET + getStartingWidthOffset(currentSack);
        var y = widgetItem.getCanvasLocation().getY() + INITIAL_Y_OFFSET;
        var labelWidth = config.textType()==NONE?0:10;

        if (config.alwaysDisableLabelsInGrid()) {
            labelWidth=0;
        } else if (labelWidth>0) {
            widthBetween+=4;
            if (config.showOverlayIcons()) widthBetween+=6; // If icons are on AND labels are on
        }

        int column = 0;
        for (Entry<PlankTier, Integer> nextPlank : currentSack) {
            String label = null;
            BufferedImage image = config.showOverlayIcons()?Images.getIconForTier(nextPlank.getKey()):null;
            if (config.textType()!=NONE && !config.alwaysDisableLabelsInGrid()) {
                label = getGridOverlayTextByConfig(nextPlank.getKey());
            }
            if (rowFinished) {
                drawSingleSmallPlankOverlay(graphics, image, label, font, config.textColor(), config.numberColor(), ""+nextPlank.getValue(), x+(widthBetween*column), y+18, labelWidth);
                column++;
                rowFinished = false;
            } else {
                drawSingleSmallPlankOverlay(graphics, image, label, font, config.textColor(), config.numberColor(), ""+nextPlank.getValue(), x+(widthBetween*column), y, labelWidth);
                rowFinished = true;
            }
        }
    }

    static protected void drawSingleSmallPlankOverlay(Graphics2D graphics, @Nullable BufferedImage icon, @Nullable String label, Font font, Color textColor, Color numberColor, String amount, int x, int y, int labelWidth) {
        var runningXOffset = 0;
        graphics.setFont(font);
        int fontHeight = graphics.getFontMetrics(font).getHeight();
        if (label!=null && label.isEmpty()) label=null;

        if (icon!=null) {
            OverlayUtil.renderImageLocation(graphics, new Point(x+runningXOffset, y-(fontHeight+1)), icon);
            runningXOffset+=5;
        }
        runningXOffset+=5;
        if (label!=null) {
            PlankSackOverlay.drawStringShadowed(graphics, font, label, x+runningXOffset, y, textColor);
        }
        PlankSackOverlay.drawStringShadowed(graphics, font, amount,x+runningXOffset+labelWidth, y, numberColor);
    }

    private String getGridOverlayTextByConfig(PlankTier tier) {
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
        return "";
    }
    private int getStartingWidthOffset(PlankStorageSet planks) {
        var count = planks.countTypes();
        if (count<=4) return 0;
        if (count<=6) return -6;
        return 0;
    }
}
