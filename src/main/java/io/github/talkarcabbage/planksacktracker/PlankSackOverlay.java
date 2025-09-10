package io.github.talkarcabbage.planksacktracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class PlankSackOverlay extends WidgetItemOverlay {
    private SackTrackerConfig config;
    private PlankSackManager manager;

    private BufferedImage plankImage;
    private BufferedImage oakImage;
    private BufferedImage teakImage;
    private BufferedImage mahoganyImage;

    private boolean overlayIconsWorking = false;

    public PlankSackOverlay(PlankSackManager sackManager, SackTrackerConfig config) {
        this.config = config;
        this.manager = sackManager;
        showOnInventory();
        plankImage = ImageUtil.loadImageResource(PlankSackOverlay.class, "/plank_small.png");
        oakImage = ImageUtil.loadImageResource(PlankSackOverlay.class, "/oak_small.png");
        teakImage = ImageUtil.loadImageResource(PlankSackOverlay.class, "/teak_small.png");
        mahoganyImage = ImageUtil.loadImageResource(PlankSackOverlay.class, "/mahogany_small.png");
        if (Utils.allNotNull(plankImage, oakImage, teakImage, mahoganyImage)) {
            overlayIconsWorking = true;
        }
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        if (itemId!= ItemID.PLANK_SACK) return;
        graphics.setFont(FontManager.getRunescapeSmallFont());
        int startingDrawX = widgetItem.getCanvasLocation().getX();
        int startingDrawY = widgetItem.getCanvasLocation().getY() + 10;
        if (manager.shouldDisplayQuestionMark()) {
            drawString(graphics, "?",startingDrawX+8,startingDrawY);
        }
        else if (manager.getCurrentPlankSack().isEmpty() && config.displayZeroWhenEmpty()) {
            drawString(graphics, "0",startingDrawX+8,startingDrawY);
        } else {
            if (config.showOverlayIcons()) {
                renderWithIcons(graphics, new Point(startingDrawX, startingDrawY));
            } else {
                renderWithoutIcons(graphics, new Point(startingDrawX, startingDrawY));
            }
        }
    }

    private void renderWithIcons(Graphics2D graphics, Point startingXY) {
        PlankStorageSet currentSack = manager.getCurrentPlankSack();
        var x = startingXY.getX();
        var y = startingXY.getY();

        if (currentSack.getPlanks()>0) {
            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), plankImage);
            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.PLANK, currentSack), config.textColor());
            y += 10;
        }
        if (currentSack.getOaks()>0) {
            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), oakImage);
            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.OAK, currentSack), config.textColor());
            y+=10;
        }
        if (currentSack.getTeaks()>0) {
            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), teakImage);
            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.TEAK, currentSack), config.textColor());
            y+=10;
        }
        if (currentSack.getMahoganies()>0) {
            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), mahoganyImage);
            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.MAHOGANY, currentSack), config.textColor());
        }
    }

    private void renderWithoutIcons(Graphics2D graphics, Point startingXY) {
        PlankStorageSet currentSack = manager.getCurrentPlankSack();
        var x = startingXY.getX();
        var y = startingXY.getY();

        if (currentSack.getPlanks()>0) {
            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.PLANK, currentSack), config.textColor());
            y += 10;
        }
        if (currentSack.getOaks()>0) {
            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.OAK, currentSack), config.textColor());
            y+=10;
        }
        if (currentSack.getTeaks()>0) {
            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.TEAK, currentSack), config.textColor());
            y+=10;
        }
        if (currentSack.getMahoganies()>0) {
            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.MAHOGANY, currentSack), config.textColor());
        }
    }
    private String getOverlayTextByConfig(PlankTier tier, PlankStorageSet storage) {
        switch (config.textType()) {
            case NONE:
                return String.valueOf(storage.getTierAmount(tier));
            case ABBREVIATED:
                switch (tier) {
                    case PLANK:
                        return "P:" + storage.getTierAmount(tier);
                    case OAK:
                        return "O:" + storage.getTierAmount(tier);
                    case TEAK:
                        return "T:" + storage.getTierAmount(tier);
                    case MAHOGANY:
                        return "M:" + storage.getTierAmount(tier);
                }
            case FULL:
                switch (tier) {
                    case PLANK:
                        return "Planks:\t" + storage.getTierAmount(tier);
                    case OAK:
                        return "Oaks:\t" + storage.getTierAmount(tier);
                    case TEAK:
                        return "Teaks:\t" + storage.getTierAmount(tier);
                    case MAHOGANY:
                        return "Mahogany:\t" + storage.getTierAmount(tier);
                }
        }
        return "";
    }



    private void drawString(Graphics2D graphics, String text, int x, int y) {
        graphics.setColor(Color.WHITE);
        var texts = text.split("\n");
        var fontMulti = graphics.getFont().getSize();
        for (int i = 0; i < texts.length; i++) {
            graphics.drawString(texts[i], x, y+(fontMulti*i));
        }
    }
}
