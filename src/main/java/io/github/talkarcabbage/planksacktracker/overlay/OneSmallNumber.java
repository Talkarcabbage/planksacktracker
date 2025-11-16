package io.github.talkarcabbage.planksacktracker.overlay;

import io.github.talkarcabbage.planksacktracker.SackTrackerConfig;
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

//Definitely wasn't tempted to name this One Small Favour
public class OneSmallNumber {

    private final PlankSackManager manager;
    private final SackTrackerConfig config;


    public OneSmallNumber(PlankSackManager manager, SackTrackerConfig config) {
        this.manager = manager;
        this.config = config;
    }

    public void drawOneSmallNumber(Graphics2D graphics, WidgetItem widgetItem) {
        var currentPlankSack = manager.getCurrentPlankSack();
        graphics.setFont(FontManager.getRunescapeFont());
        int startingDrawX = widgetItem.getCanvasLocation().getX();
        int startingDrawY = widgetItem.getCanvasLocation().getY() + 31;
        BufferedImage image = null;
        var horizontalOffset = (currentPlankSack.getTotalPlanks()>9?-5:-3);

        if (config.showOverlayIcons()) {
            if (currentPlankSack.isMonoContent()) {
                image = Images.getIconForTier(currentPlankSack.getMonoType());
            } else {
                image = Images.plankImage;
            }
        }
        if (currentPlankSack.isEmpty() && config.displayZeroWhenEmpty()) {
            drawString(graphics, FontManager.getRunescapeSmallFont(), "0", startingDrawX+9+horizontalOffset, startingDrawY, config.numberColor());
        } else if (!currentPlankSack.isEmpty()){
            if (image!=null && config.enableIconOneNumber()) {
                drawStackedPlankIcons(graphics, startingDrawX+horizontalOffset+20, startingDrawY-(graphics.getFontMetrics().getHeight()+13), currentPlankSack) ;
            }
            drawString(graphics, FontManager.getRunescapeSmallFont(), ""+currentPlankSack.getTotalPlanks(), startingDrawX+9+horizontalOffset, startingDrawY, config.numberColor());
        }
    }
    private void drawString(Graphics2D graphics, Font font, String label, int x, int y, Color color) {
        graphics.setFont(font);
        graphics.setColor(Color.BLACK);
        graphics.drawString(label, x + 1, y + 1);
        graphics.setColor(color);
        graphics.drawString(label, x, y);
    }

    //For drawing the plank icons to the left of the big number in a way that takes up minimal space
    private void drawStackedPlankIcons(Graphics2D graphics, int x, int y, PlankStorageSet planks) {
        var numDone = 0;
        var types = planks.countTypes();
        if (types>3) y-=4;
        for (Entry<PlankTier, Integer> plank : planks) {
            var tier = plank.getKey();
            var icon = Images.getIconForTier(tier);
            OverlayUtil.renderImageLocation(graphics, new Point(x, y), icon);
            x -= 4;
            numDone++;
            if (numDone==3) {
                y+=7;
                if (types>6) {
                    x+=11; //Make a little extra space for the 7th plank icon
                } else {
                    x+=10;
                }
            }
        }
    }
}
