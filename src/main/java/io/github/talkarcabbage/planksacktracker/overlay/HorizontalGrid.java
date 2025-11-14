package io.github.talkarcabbage.planksacktracker.overlay;

import io.github.talkarcabbage.planksacktracker.*;
import io.github.talkarcabbage.planksacktracker.overlayenums.OverlayTextType;
import net.runelite.api.Point;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;

import static io.github.talkarcabbage.planksacktracker.overlayenums.OverlayTextType.NONE;

public class HorizontalGrid {

    private final PlankSackManager manager;
    private final SackTrackerConfig config;
    private final PlankSackOverlay overlay;

    public HorizontalGrid(PlankSackManager manager, SackTrackerConfig config, PlankSackOverlay overlay) {
        this.manager = manager;
        this.config = config;
        this.overlay = overlay;
    }

    void drawGridHorizontal(Graphics2D graphics, WidgetItem widgetItem) {
        var widthBetween = 20;
        var font = FontManager.getRunescapeSmallFont();
        var currentSack = manager.getCurrentPlankSack();
        boolean columnFinished = false;
        var x = widgetItem.getCanvasLocation().getX()-2;
        var y = widgetItem.getCanvasLocation().getY() + 10 + getHorizontalGridStartingHeightOffset(currentSack);
        var labelWidth = config.textType()==NONE?0: 10;
        var rowHeightMultiplier = getHorizontalGridHeightSpacing(currentSack);
        if (config.alwaysDisableLabelsInGrid()) {
            labelWidth=0;
        } else if (labelWidth>0) {
            widthBetween+=6;
            x-=4;
            if (config.showOverlayIcons()) {
                widthBetween+=6; // If icons are on AND labels are on
                x-=9;
            }
        }

        int row = 0;
        for (Entry<PlankTier, Integer> nextPlank : currentSack) {
            String label = null;
            BufferedImage image = config.showOverlayIcons()?Images.getIconForTier(nextPlank.getKey()):null;
            if (config.textType()!=NONE && !config.alwaysDisableLabelsInGrid()) {
                label = overlay.getOverlayTextByConfig(nextPlank.getKey(), currentSack, OverlayTextType.LETTER);
            }
            if (columnFinished) {
                drawSingleSmallPlankOverlay(graphics, image, label, font, config.textColor(), config.numberColor(), ""+nextPlank.getValue(), x+widthBetween, y+(rowHeightMultiplier*row), labelWidth);
                row++;
                columnFinished = false;
            } else {
                drawSingleSmallPlankOverlay(graphics, image, label, font, config.textColor(), config.numberColor(), ""+nextPlank.getValue(), x, y+(rowHeightMultiplier*row), labelWidth );
                columnFinished = true;
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

    private int getHorizontalGridStartingHeightOffset(PlankStorageSet planks) {
        var count = planks.countTypes();
        if (count<=4) return 2;
        if (count<=6) return 0;
        return -4;
    }

    private int getHorizontalGridHeightSpacing(PlankStorageSet planks) {
        var count = planks.countTypes();
        if (count<=2) return 21;
        if (count<=4) return 18;
        if (count<=6) return 15;
        return 12;
    }

}
