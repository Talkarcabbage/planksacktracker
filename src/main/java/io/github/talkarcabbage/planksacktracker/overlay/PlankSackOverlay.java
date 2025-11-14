package io.github.talkarcabbage.planksacktracker.overlay;

import io.github.talkarcabbage.planksacktracker.*;
import io.github.talkarcabbage.planksacktracker.overlayenums.OverlayTextType;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class PlankSackOverlay extends WidgetItemOverlay {
    private final SackTrackerConfig config;
    private final PlankSackManager manager;
    private final SackTrackerPlugin trackerPlugin;

    // Overlay styles
    private final OneBigNumber oneBigNumber;
    private final HorizontalGrid horizontalGrid;
    private final VerticalGrid verticalGrid;
    private final Vertical vertical;

    //Tooltip renderer
    private final SackTooltip tooltip;

    public static final int ABBREVIATED_WIDTH_BUFFER = 23; // Used to set spacing for abbreviated text overlays
    public static final int NON_ABBREVIATED_WIDTH_BUFFER = 49; // Used to set spacing for long text overlays
    private final Client client;

    TooltipManager tooltipManager;

    public PlankSackOverlay(SackTrackerPlugin plugin, PlankSackManager sackManager, SackTrackerConfig config, Client client, TooltipManager tooltipManager) {
        this.trackerPlugin = plugin;
        this.config = config;
        this.manager = sackManager;
        this.client = client;
        this.tooltipManager = tooltipManager;
        showOnInventory();

        oneBigNumber = new OneBigNumber(sackManager, config, this);
        horizontalGrid = new HorizontalGrid(sackManager, config, this);
        verticalGrid = new VerticalGrid(sackManager, config, this);
        vertical = new Vertical(sackManager, config, this);
        tooltip = new SackTooltip(sackManager, tooltipManager, this);
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        if (itemId!= ItemID.PLANK_SACK) return;
        var currentPlankSack = manager.getCurrentPlankSack();
        graphics.setFont(FontManager.getRunescapeSmallFont());

        if (currentPlankSack.isEmpty() && config.displayZeroWhenEmpty()) {
            oneBigNumber.drawOneBigNumber(graphics, widgetItem);
        } else {

            switch (config.overlayStyle()) {
                case TOOLTIP_ONLY:
                    break;
                case ONE_BIG_NUMBER:
                    oneBigNumber.drawOneBigNumber(graphics, widgetItem);
                    break;
                case DYNAMIC:
                    drawDynamic(graphics, widgetItem);
                    break;
                case VERTICAL_GRID:
                    verticalGrid.drawGridVertical(graphics, widgetItem);
                    break;
                case HORIZONTAL_GRID:
                    horizontalGrid.drawGridHorizontal(graphics, widgetItem);
                    break;
                case VERTICAL:
                    vertical.drawVertical(graphics, widgetItem);
                    break;
            }
        }
        if (isHovered(widgetItem, client.getMouseCanvasPosition())) {
            tooltip.renderTooltip(graphics, itemId, widgetItem);
        }
    }

    private boolean isHovered(WidgetItem widgetItem, Point mousePosition) {
        return (widgetItem.getCanvasBounds().contains(mousePosition.getX(), mousePosition.getY()));
    }

    //Commonly shared method for getting overlay text used in other overlays
    String getOverlayTextFull(PlankTier tier, PlankStorageSet storage) {
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

    String getOverlayTextByConfig(PlankTier tier, PlankStorageSet storage, @Nullable OverlayTextType textType) {
        var type = config.textType();
        if (textType!=null) type=textType;
        switch (type) {
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

    private void drawDynamic(Graphics2D graphics, WidgetItem widgetItem) {
        var totalTypes = manager.getCurrentPlankSack().countTypes();
        switch (totalTypes) {
            case 0:
            case 1:
                oneBigNumber.drawOneBigNumber(graphics, widgetItem);
                break;
            case 2:
            case 3:
                vertical.drawVertical(graphics, widgetItem);
                break;
            case 4:
                horizontalGrid.drawGridHorizontal(graphics, widgetItem);
                break;
            case 5:
            case 6:
            case 7:
                oneBigNumber.drawOneBigNumber(graphics, widgetItem);
                break;
            default:
                log.warn("The plank sack, somehow, contains more plank types than should be possible! The overlay may not work correctly.");
                oneBigNumber.drawOneBigNumber(graphics, widgetItem);
        }
    }

    static protected void drawStringShadowed(Graphics2D graphics, Font font, String label, int x, int y, Color color) {
        graphics.setColor(Color.BLACK);
        graphics.setFont(font);
        graphics.drawString(label, x + 1, y + 1);
        graphics.setColor(color);
        graphics.drawString(label, x, y);
    }

}
