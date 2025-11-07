package io.github.talkarcabbage.planksacktracker;

import io.github.talkarcabbage.planksacktracker.overlayenums.OverlayTextType;
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

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;

import static io.github.talkarcabbage.planksacktracker.overlayenums.OverlayTextType.NONE;

@Slf4j
public class PlankSackOverlay extends WidgetItemOverlay {
    private final SackTrackerConfig config;
    private final PlankSackManager manager;
    private final SackTrackerPlugin trackerPlugin;

    private final BufferedImage plankImage;
    private final BufferedImage oakImage;
    private final BufferedImage teakImage;
    private final BufferedImage mahoganyImage;
    private final BufferedImage rosewoodImage;
    private final BufferedImage ironwoodImage;
    private final BufferedImage camphorImage;

    private final int abbreviatedWidthBuffer = 11;
    private final int nonAbbreviatedWidthBuffer = 40;

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
        rosewoodImage = ImageUtil.loadImageResource(PlankSackOverlay.class, "/rosewood_small.png");
        ironwoodImage = ImageUtil.loadImageResource(PlankSackOverlay.class, "/ironwood_small.png");
        camphorImage = ImageUtil.loadImageResource(PlankSackOverlay.class, "/camphor_small.png");

        if (!Utils.allNotNull(plankImage, oakImage, teakImage, mahoganyImage, rosewoodImage, ironwoodImage, camphorImage)) {
            log.error("Some or all of the icons for the plank sack tracker plugin failed to load!");
        }
        this.tooltipManager = tooltipManager;
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        if (itemId!= ItemID.PLANK_SACK) return;
        var currentPlankSack = manager.getCurrentPlankSack();
        graphics.setFont(FontManager.getRunescapeSmallFont());

        if (currentPlankSack.isEmpty() && config.displayZeroWhenEmpty()) {
            drawOneBigNumber(graphics, widgetItem);
        } else {

            switch (config.overlayStyle()) {
                case TOOLTIP_ONLY:
                    break;
                case ONE_BIG_NUMBER:
                    drawOneBigNumber(graphics, widgetItem);
                    break;
                case DYNAMIC:
                    drawDynamic(graphics, widgetItem);
                    break;
                case GRID_VERTICAL:
                    drawGridVertical(graphics, widgetItem);
                    break;
                case GRID_HORIZONTAL:
                    drawGridHorizontal(graphics, widgetItem);
                    break;
                case VERTICAL:
                    drawVertical(graphics, widgetItem);
                    break;
            }

//            if (config.showOverlayIcons()) {
//                renderWithIcons(graphics, new Point(startingDrawX, startingDrawY));
//            } else {
//                renderWithoutIcons(graphics, new Point(startingDrawX, startingDrawY));
//            }
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
        }
        tb.append(currentPlankSack.getCamphors()>0?getOverlayTextFull(PlankTier.CAMPHOR, currentPlankSack)+"\n":"");
        tooltipManager.add(new Tooltip(tb.toString()));
    }

    private boolean isHovered(WidgetItem widgetItem, Point mousePosition) {
        return (widgetItem.getCanvasBounds().contains(mousePosition.getX(), mousePosition.getY()));
    }

//    private void renderWithIcons(Graphics2D graphics, Point startingXY) {
//        PlankStorageSet currentSack = manager.getCurrentPlankSack();
//        var x = startingXY.getX();
//        var y = startingXY.getY();
//
//        if (currentSack.getPlanks()>0) {
//            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), plankImage);
//            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.PLANK, currentSack, null), config.textColor());
//            y += 10;
//        }
//        if (currentSack.getOaks()>0) {
//            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), oakImage);
//            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.OAK, currentSack, null), config.textColor());
//            y+=10;
//        }
//        if (currentSack.getTeaks()>0) {
//            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), teakImage);
//            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.TEAK, currentSack, null), config.textColor());
//            y+=10;
//        }
//        if (currentSack.getMahoganies()>0) {
//            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), mahoganyImage);
//            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.MAHOGANY, currentSack, null), config.textColor());
//        }
//        if (currentSack.getRosewoods()>0) {
//            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), rosewoodImage);
//            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.ROSEWOOD, currentSack, null), config.textColor());
//        }
//        if (currentSack.getIronwoods()>0) {
//            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), ironwoodImage);
//            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.IRONWOOD, currentSack, null), config.textColor());
//        }
//        if (currentSack.getCamphors()>0) {
//            OverlayUtil.renderImageLocation(graphics, new Point(x,y-12), camphorImage);
//            OverlayUtil.renderTextLocation(graphics, new Point(x+10, y), getOverlayTextByConfig(PlankTier.CAMPHOR, currentSack, null), config.textColor());
//        }
//    }
//
//    private void renderWithoutIcons(Graphics2D graphics, Point startingXY) {
//        PlankStorageSet currentSack = manager.getCurrentPlankSack();
//        var x = startingXY.getX();
//        var y = startingXY.getY();
//
//        if (currentSack.getPlanks()>0) {
//            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.PLANK, currentSack, null), config.textColor());
//            y += 10;
//        }
//        if (currentSack.getOaks()>0) {
//            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.OAK, currentSack, null), config.textColor());
//            y+=10;
//        }
//        if (currentSack.getTeaks()>0) {
//            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.TEAK, currentSack, null), config.textColor());
//            y+=10;
//        }
//        if (currentSack.getMahoganies()>0) {
//            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.MAHOGANY, currentSack, null), config.textColor());
//            y+=10;
//        }
//        if (currentSack.getRosewoods()>0) {
//            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.ROSEWOOD, currentSack, null), config.textColor());
//            y+=10;
//        }
//        if (currentSack.getIronwoods()>0) {
//            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.IRONWOOD, currentSack, null), config.textColor());
//            y+=10;
//        }
//        if (currentSack.getCamphors()>0) {
//            OverlayUtil.renderTextLocation(graphics, new Point(x, y), getOverlayTextByConfig(PlankTier.CAMPHOR, currentSack, null), config.textColor());
//        }
//    }

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

    private String getOverlayTextByConfig(PlankTier tier, PlankStorageSet storage, @Nullable OverlayTextType textType) {
        var type = config.textType();
        if (textType!=null) type=textType;
        switch (type) {
            case NONE:
                return "";
            case ABBREVIATED:
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
            case FULL:
                switch (tier) {
                    case PLANK:
                        return "Planks:\t";
                    case OAK:
                        return "Oaks:\t";
                    case TEAK:
                        return "Teaks:\t";
                    case MAHOGANY:
                        return "Mahogany:\t";
                    case ROSEWOOD:
                        return "Rosewood:\t";
                    case IRONWOOD:
                        return "Ironwood:\t";
                    case CAMPHOR:
                        return "Camphor:\t";
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

    private void drawOverlayOnly() {
        //PROBABLY JUST NO-OP
    }

    private void drawOneBigNumber(Graphics2D graphics, WidgetItem widgetItem) {
        var currentPlankSack = manager.getCurrentPlankSack();
        graphics.setFont(FontManager.getRunescapeFont());
        int startingDrawX = widgetItem.getCanvasLocation().getX();
        int startingDrawY = widgetItem.getCanvasLocation().getY() + 24;
        BufferedImage image = null;
        if (config.showOverlayIcons()) {
            if (currentPlankSack.isMonoContent()) {
                image = getIconForTier(currentPlankSack.getMonoType());
            } else {
                image = plankImage;
            }
        }
        if (currentPlankSack.isEmpty() && config.displayZeroWhenEmpty()) {
            if (image!=null && !config.disableIconWhenEmpty() && !config.disableIconOneBigNumber()) {
                OverlayUtil.renderImageLocation(graphics, new Point(startingDrawX-2, startingDrawY-(graphics.getFontMetrics().getHeight()-2)), image);
            }
            drawStringThickShadowed(graphics, FontManager.getRunescapeFont(), "0", startingDrawX+9, startingDrawY, config.numberColor());
        } else if (!currentPlankSack.isEmpty()){
            if (image!=null && !config.disableIconOneBigNumber()) {
                OverlayUtil.renderImageLocation(graphics, new Point(startingDrawX-2, startingDrawY-(graphics.getFontMetrics().getHeight()-2)), image);
            }
            drawStringThickShadowed(graphics, FontManager.getRunescapeFont(), ""+currentPlankSack.getTotalPlanks(), startingDrawX+9, startingDrawY, config.numberColor());
        }
    }

    private void drawDynamic(Graphics2D graphics, WidgetItem widgetItem) {
        var totalTypes = manager.getCurrentPlankSack().countTypes();
        switch (totalTypes) {
            case 0:
            case 1:
                drawOneBigNumber(graphics, widgetItem);
                break;
            case 2:
            case 3:
                drawVertical(graphics, widgetItem);
                break;
            case 4:
                drawGridHorizontal(graphics, widgetItem);
                break;
            case 5:
            case 6:
            case 7:
                drawOneBigNumber(graphics, widgetItem);
                break;
            default:
                log.warn("The plank sack contains more plank types than should be possible! The overlay won't work.");
        }
    }

    private void drawGridHorizontal(Graphics2D graphics, WidgetItem widgetItem) {
        var font = FontManager.getRunescapeSmallFont();
        var currentSack = manager.getCurrentPlankSack();
        boolean columnFinished = false;
        var x = widgetItem.getCanvasLocation().getX()-4;
        var y = widgetItem.getCanvasLocation().getY() + 10;
        var labelWidth = config.textType()==NONE?0:abbreviatedWidthBuffer;
        if (config.alwaysDisableLabelsInGrid()) labelWidth=0;

        int row = 0;
        for (Entry<PlankTier, Integer> nextPlank : currentSack) {
            String label = null;
            BufferedImage image = config.showOverlayIcons()?getIconForTier(nextPlank.getKey()):null;
            if (config.textType()!=NONE && !config.alwaysDisableLabelsInGrid()) {
                label = getOverlayTextByConfig(nextPlank.getKey(), currentSack, OverlayTextType.ABBREVIATED);
            }
            if (columnFinished) {
                drawSingleSmallPlankOverlay(graphics, image, label, font, config.textColor(), config.numberColor(), ""+nextPlank.getValue(), x+20, y+(20*row), labelWidth );
                row++;
                columnFinished = false;
            } else {
                drawSingleSmallPlankOverlay(graphics, image, label, font, config.textColor(), config.numberColor(), ""+nextPlank.getValue(), x, y+(18*row), labelWidth );
                columnFinished = true;
            }
        }
    }

    private void drawGridVertical(Graphics2D graphics, WidgetItem widgetItem) {
        var font = FontManager.getRunescapeSmallFont();
        var currentSack = manager.getCurrentPlankSack();
        boolean rowFinished = false;
        var x = widgetItem.getCanvasLocation().getX()-4;
        var y = widgetItem.getCanvasLocation().getY() + 10;
        var labelWidth = config.textType()==NONE?0:abbreviatedWidthBuffer;
        if (config.alwaysDisableLabelsInGrid()) labelWidth=0;

        int column = 0;
        for (Entry<PlankTier, Integer> nextPlank : currentSack) {
            String label = null;
            BufferedImage image = config.showOverlayIcons()?getIconForTier(nextPlank.getKey()):null;
            if (config.textType()!=NONE && !config.alwaysDisableLabelsInGrid()) {
                label = getOverlayTextByConfig(nextPlank.getKey(), currentSack, OverlayTextType.ABBREVIATED);
            }
            if (rowFinished) {
                drawSingleSmallPlankOverlay(graphics, image, label, font, config.textColor(), config.numberColor(), ""+nextPlank.getValue(), x+(20*column), y+18, labelWidth );
                column++;
                rowFinished = false;
            } else {
                drawSingleSmallPlankOverlay(graphics, image, label, font, config.textColor(), config.numberColor(), ""+nextPlank.getValue(), x+(20*column), y, labelWidth );
                rowFinished = true;
            }
        }
    }

    private void drawVertical(Graphics2D graphics, WidgetItem widgetItem) {
        var font = FontManager.getRunescapeSmallFont();
        var currentSack = manager.getCurrentPlankSack();
        var x = widgetItem.getCanvasLocation().getX()-4;
        var y = widgetItem.getCanvasLocation().getY();
        var offset = getCurrentLabelValueOffset();

        for (Entry<PlankTier, Integer> nextPlank : currentSack) {
            var label = getOverlayLabelByConfig(nextPlank.getKey());
            if (label.isEmpty()) label=null;
            var image = config.showOverlayIcons()?(getIconForTier(nextPlank.getKey())):null;
            drawSingleSmallPlankOverlay(graphics, image, label, font, config.textColor(), config.numberColor(), ""+nextPlank.getValue(),x,y+=12, offset);
        }
    }

    private void drawSingleSmallPlankOverlay(Graphics2D graphics, @Nullable BufferedImage icon, @Nullable String label, Font font, Color textColor, Color numberColor, String amount, int x, int y, int labelWidth) {
        var runningXOffset = 0;
        graphics.setFont(font);
        int fontHeight = graphics.getFontMetrics(font).getHeight();
        if (label!=null && label.isEmpty()) label=null;

        if (icon!=null) {
            OverlayUtil.renderImageLocation(graphics, new Point(x+runningXOffset, y-fontHeight), icon);
            runningXOffset+=10;
        }
        if (label!=null) {
            drawStringShadowed(graphics, font, label, x+runningXOffset, y, textColor);
            drawStringShadowed(graphics, font, amount,x+runningXOffset+labelWidth, y, numberColor);
        } else {
            drawStringShadowed(graphics, font, amount,x+runningXOffset+labelWidth, y,  numberColor);
        }
    }

    private void drawStringShadowed(Graphics2D graphics, Font font, String label, int x, int y, Color color) {
        graphics.setColor(Color.BLACK);
        graphics.setFont(font);
        graphics.drawString(label, x + 1, y + 1);
        graphics.setColor(color);
        graphics.drawString(label, x, y);
    }
    private void drawStringThickShadowed(Graphics2D graphics, Font font, String label, int x, int y, Color color) {
        graphics.setColor(Color.BLACK);
        graphics.setFont(font);
        graphics.drawString(label, x + 1, y + 1);
        graphics.drawString(label, x + 1, y + 2);
        graphics.drawString(label, x + 2, y + 1);
        graphics.drawString(label, x + 2, y + 2);
        graphics.setColor(color);
        graphics.drawString(label, x, y);
    }

    // (For the expected width of no/abbreviated/full labels on the overlay via config
    private int getCurrentLabelValueOffset() {
        switch (config.textType()) {
            case NONE:
                return 0;
            case ABBREVIATED:
                return abbreviatedWidthBuffer;
            case FULL:
                return nonAbbreviatedWidthBuffer;
            default:
                return 0;
        }
    }

    private String getOverlayLabelByConfig(PlankTier tier) {
        switch (config.textType()) {
            case NONE:
                return "";
            case ABBREVIATED:
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
            case FULL:
                switch (tier) {
                    case PLANK:
                        return "Planks:\t";
                    case OAK:
                        return "Oaks:\t";
                    case TEAK:
                        return "Teaks:\t";
                    case MAHOGANY:
                        return "Mahogany:\t";
                    case ROSEWOOD:
                        return "Rosewood:\t";
                    case IRONWOOD:
                        return "Ironwood:\t";
                    case CAMPHOR:
                        return "Camphor:\t";
                }
        }
        return "";
    }

    private BufferedImage getIconForTier(PlankTier tier) {
        switch (tier) {
            case PLANK:
                return plankImage;
            case OAK:
                return oakImage;
            case TEAK:
                return teakImage;
            case MAHOGANY:
                return mahoganyImage;
            case ROSEWOOD:
                return rosewoodImage;
            case IRONWOOD:
                return ironwoodImage;
            case CAMPHOR:
                return camphorImage;
            case UNKNOWN:
            default:
                return plankImage;
        }
    }

}
