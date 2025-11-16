package io.github.talkarcabbage.planksacktracker.overlay;

import io.github.talkarcabbage.planksacktracker.planksack.PlankSackManager;
import io.github.talkarcabbage.planksacktracker.SackTrackerConfig;
import io.github.talkarcabbage.planksacktracker.overlayenums.OverlayStyle;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import java.awt.*;

@Slf4j
public class PlankSackOverlay extends WidgetItemOverlay {
    private final SackTrackerConfig config;
    private final PlankSackManager manager;

    // Overlay styles
    private final OneBigNumber oneBigNumber;
    private final HorizontalGrid horizontalGrid;
    private final VerticalGrid verticalGrid;
    private final Vertical vertical;
    private final OneSmallNumber oneSmallNumber;

    //Tooltip renderer
    private final SackTooltip tooltip;

    public static final int ABBREVIATED_WIDTH_BUFFER = 23; // Used to set spacing for abbreviated text overlays
    public static final int NON_ABBREVIATED_WIDTH_BUFFER = 49; // Used to set spacing for long text overlays
    private final Client client;

    public PlankSackOverlay(PlankSackManager sackManager, SackTrackerConfig config, Client client, TooltipManager tooltipManager) {
        this.config = config;
        this.manager = sackManager;
        this.client = client;
        showOnInventory();

        oneBigNumber = new OneBigNumber(sackManager, config);
        horizontalGrid = new HorizontalGrid(sackManager, config);
        verticalGrid = new VerticalGrid(sackManager, config);
        vertical = new Vertical(sackManager, config);
        tooltip = new SackTooltip(sackManager, tooltipManager, config);
        oneSmallNumber = new OneSmallNumber(sackManager, config);
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {
        if (itemId!= ItemID.PLANK_SACK) return;
        var currentPlankSack = manager.getCurrentPlankSack();
        graphics.setFont(FontManager.getRunescapeSmallFont());

        if (currentPlankSack.isEmpty() && config.displayZeroWhenEmpty() && config.overlayStyle()!= OverlayStyle.TOOLTIP_ONLY) {
            if (config.overlayStyle()==OverlayStyle.ONE_SMALL_NUMBER) {
                oneSmallNumber.drawOneSmallNumber(graphics, widgetItem);
            } else {
                oneBigNumber.drawOneBigNumber(graphics, widgetItem);
            }
        } else {

            switch (config.overlayStyle()) {
                case TOOLTIP_ONLY:
                    break;
                case ONE_BIG_NUMBER:
                    oneBigNumber.drawOneBigNumber(graphics, widgetItem);
                    break;
                case ONE_SMALL_NUMBER:
                    oneSmallNumber.drawOneSmallNumber(graphics, widgetItem);
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
        if (config.enableTooltip() && isHovered(widgetItem, client.getMouseCanvasPosition())) {
            tooltip.renderTooltip();
        }
    }

    private boolean isHovered(WidgetItem widgetItem, Point mousePosition) {
        return (widgetItem.getCanvasBounds().contains(mousePosition.getX(), mousePosition.getY()));
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
