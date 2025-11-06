package io.github.talkarcabbage.planksacktracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
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
    private SackTrackerPlugin trackerPlugin;

    private boolean overlayIconsWorking = false;

    TooltipManager tooltipManager;

    public PlankSackOverlay(SackTrackerPlugin plugin, PlankSackManager sackManager, SackTrackerConfig config, TooltipManager tooltipManager) {
        this.trackerPlugin = plugin;
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
        this.tooltipManager = tooltipManager;
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        if (itemId!= ItemID.PLANK_SACK) return;
        var currentPlankSack = manager.getCurrentPlankSack();
        graphics.setFont(FontManager.getRunescapeSmallFont());
        int startingDrawX = widgetItem.getCanvasLocation().getX();
        int startingDrawY = widgetItem.getCanvasLocation().getY() + 10;
        if (currentPlankSack.isEmpty() && config.displayZeroWhenEmpty()) {
            drawString(graphics, "0",startingDrawX+8,startingDrawY);
        } else {
            if (config.showOverlayIcons()) {
                renderWithIcons(graphics, new Point(startingDrawX, startingDrawY));
            } else {
                renderWithoutIcons(graphics, new Point(startingDrawX, startingDrawY));
            }
        }
        if (isHovered(widgetItem, trackerPlugin.client.getMouseCanvasPosition())) {
            renderTooltip(graphics, itemId, widgetItem);
        }
    }

    private void renderTooltip(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        var currentPlankSack = manager.getCurrentPlankSack();
        var brLength = 0;
        StringBuilder tb = new StringBuilder();

        if (currentPlankSack.isEmpty()) {
            tooltipManager.add(new Tooltip("Empty!"));
            return;
        }

        tb.append(currentPlankSack.getPlanks()>0?getOverlayTextFull(PlankTier.PLANK, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getOaks()>0?getOverlayTextFull(PlankTier.OAK, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getTeaks()>0?getOverlayTextFull(PlankTier.TEAK, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getMahoganies()>0?getOverlayTextFull(PlankTier.MAHOGANY, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getRosewoods()>0?getOverlayTextFull(PlankTier.ROSEWOOD, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getIronwoods()>0?getOverlayTextFull(PlankTier.IRONWOOD, currentPlankSack)+"\n":"");
        if (tb.length()>brLength) {
            tb.append(Utils.br());
            brLength = tb.length();
        }
        tb.append(currentPlankSack.getCamphors()>0?getOverlayTextFull(PlankTier.CAMPHOR, currentPlankSack)+"\n":"");
        tooltipManager.add(new Tooltip(tb.toString()));
    }

    private boolean isHovered(WidgetItem widgetItem, Point mousePosition) {
        return (widgetItem.getCanvasBounds().contains(mousePosition.getX(), mousePosition.getY()));
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
        if (currentSack.getRosewoods()>0) {
            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), mahoganyImage); //todo images
            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.ROSEWOOD, currentSack), config.textColor());
        }
        if (currentSack.getIronwoods()>0) {
            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), mahoganyImage);
            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.IRONWOOD, currentSack), config.textColor());
        }
        if (currentSack.getCamphors()>0) {
            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), mahoganyImage);
            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.CAMPHOR, currentSack), config.textColor());
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
            y+=10;
        }
        if (currentSack.getRosewoods()>0) {
            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.ROSEWOOD, currentSack), config.textColor());
            y+=10;
        }
        if (currentSack.getIronwoods()>0) {
            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.IRONWOOD, currentSack), config.textColor());
            y+=10;
        }
        if (currentSack.getCamphors()>0) {
            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.CAMPHOR, currentSack), config.textColor());
        }
    }

    private String getOverlayTextFull(PlankTier tier, PlankStorageSet storage) {

        switch (tier) {
            case PLANK:
                return Utils.white(padTwo(storage.getTierAmount(tier))) + Utils.yellow(" Basic planks");
            case OAK:
                return Utils.white(padTwo(storage.getTierAmount(tier))) + Utils.yellow(" Oak planks");
            case TEAK:
                return Utils.white(padTwo(storage.getTierAmount(tier))) + Utils.yellow(" Teak planks");
            case MAHOGANY:
                return Utils.white(padTwo(storage.getTierAmount(tier))) + Utils.yellow(" Mahogany planks");
            case ROSEWOOD:
                return Utils.white(padTwo(storage.getTierAmount(tier))) + Utils.yellow(" Rosewood planks");
            case IRONWOOD:
                return Utils.white(padTwo(storage.getTierAmount(tier))) + Utils.yellow(" Ironwood planks");
            case CAMPHOR:
                return Utils.white(padTwo(storage.getTierAmount(tier))) + Utils.yellow(" Camphor planks");
        }
        return "";
    }

    private String padTwo(int number) {
        if (number<9) return number+" ";
        return number+"";
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
                    case ROSEWOOD:
                        return "R:" + storage.getTierAmount(tier);
                    case IRONWOOD:
                        return "I:" + storage.getTierAmount(tier);
                    case CAMPHOR:
                        return "C:" + storage.getTierAmount(tier);
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
                    case ROSEWOOD:
                        return "Rosewood:\t" + storage.getTierAmount(tier);
                    case IRONWOOD:
                        return "Ironwood:\t" + storage.getTierAmount(tier);
                    case CAMPHOR:
                        return "Camphor:\t" + storage.getTierAmount(tier);
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
