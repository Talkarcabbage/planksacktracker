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

import java.awt.*;
import java.awt.image.BufferedImage;

public class OneBigNumber {
    private final PlankSackManager manager;
    private final SackTrackerConfig config;

    public OneBigNumber(PlankSackManager manager, SackTrackerConfig config) {
        this.manager = manager;
        this.config = config;
    }

    public void drawOneBigNumber(Graphics2D graphics, WidgetItem widgetItem) {
        var currentPlankSack = manager.getCurrentPlankSack();
        graphics.setFont(FontManager.getRunescapeFont());
        int startingDrawX = widgetItem.getCanvasLocation().getX();
        int startingDrawY = widgetItem.getCanvasLocation().getY() + 24;
        BufferedImage image = null;
        var horizontalOffset = (currentPlankSack.getTotalPlanks()>9?-1:1);

        if (config.showOverlayIcons()) {
            if (currentPlankSack.isMonoContent()) {
                image = Images.getIconForTier(currentPlankSack.getMonoType());
            } else {
                image = Images.plankImage;
            }
        }
        if (currentPlankSack.isEmpty() && config.displayZeroWhenEmpty()) {
            drawBigStringThickShadowed(graphics, FontManager.getRunescapeFont(), "0", startingDrawX+9+horizontalOffset, startingDrawY, config.numberColor());
        } else if (!currentPlankSack.isEmpty()){
            if (image!=null && config.enableIconOneBigNumber()) {
                drawStackedPlankIcons(graphics, startingDrawX+horizontalOffset, startingDrawY-(graphics.getFontMetrics().getHeight()-2), currentPlankSack) ;
            }
            drawBigStringThickShadowed(graphics, FontManager.getRunescapeFont(), ""+currentPlankSack.getTotalPlanks(), startingDrawX+9+horizontalOffset, startingDrawY, config.numberColor());
        }
    }

    private static final Color SLIGHTLY_FADED_BLACK = new Color(0f,0f,0f,0.7f);
    private static final Color MEDIUM_FADED_BLACK = new Color(0f,0f,0f,0.45f);
    private static final Color VERY_FADED_BLACK = new Color(0f,0f,0f,0.3f);
    private void drawBigStringThickShadowed(Graphics2D graphics, Font font, String label, int x, int y, Color color) {
        graphics.setFont(font);
        graphics.setColor(SLIGHTLY_FADED_BLACK);
        graphics.drawString(label, x + 1, y + 1);
        graphics.setColor(MEDIUM_FADED_BLACK);
        graphics.drawString(label, x + 2, y + 1);
        graphics.setColor(VERY_FADED_BLACK);
        graphics.drawString(label, x + 1, y + 2);
        graphics.drawString(label, x + 2, y + 2);
        graphics.setColor(color);
        graphics.drawString(label, x, y);
    }

    //For drawing the plank icons to the left of the big number in a way that takes up minimal space
    private void drawStackedPlankIcons(Graphics2D graphics, int x, int y, PlankStorageSet planks) {
        var numDone = 0;
        var types = planks.countTypes();
        if (types>3) y-=6;
        for (Entry<PlankTier, Integer> plank : planks) {
            var tier = plank.getKey();
            var icon = Images.getIconForTier(tier);
            OverlayUtil.renderImageLocation(graphics, new Point(x, y), icon);
            x -= 4;
            numDone++;
            if (numDone==3) {
                y+=10;
                if (types>6) {
                    x+=11; //Make a little extra space for the 7th plank icon
                } else {
                    x+=10;
                }
            }
        }
    }

}
