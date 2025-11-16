package io.github.talkarcabbage.planksacktracker;

import io.github.talkarcabbage.planksacktracker.overlayenums.OverlayStyle;
import io.github.talkarcabbage.planksacktracker.overlayenums.OverlayTextType;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("planksacktracker")
public interface SackTrackerConfig extends Config {

    String TEXT_COLOR = "textcolor";
    String NUMBER_COLOR = "numbercolor";
    String PLANK_ICONS = "plankicons";
    String OVERLAY_STYLE = "overlaystyle";
    String PLANK_NAMES = "planknames";
    String DISPLAY_ZERO = "displayzerowhenempty";
    String ENABLE_ICON_FOR_ONE_BIG_NUMBER = "disableonebignumbericon";
    String ALWAYS_DISABLE_LABELS_IN_GRID = "disablelabelsingrid";
    String TOOLTIP_TEXT_COLOR = "tooltiptextcolor";
    String TOOLTIP_NUMBER_COLOR = "tooltipnumbercolor";
    String ENABLE_TOOLTIP = "enabletooltip";

    @ConfigSection(
            name = "Overlay",
            description = "Overlay settings",
            position = 0
    )
    String overlaySettings = "overlaysettings";

    @ConfigItem(
            keyName = TEXT_COLOR,
            name="Overlay text color",
            description = "The font color used to display the names of planks.",
            position = 1,
            section=overlaySettings
    )
    default Color textColor() { return Color.WHITE; }
    @ConfigItem(
            keyName = NUMBER_COLOR,
            name="Overlay number color",
            description = "The font color used to display the plank quantities.",
            position = 2,
            section=overlaySettings
    )
    default Color numberColor() { return Color.YELLOW; }

    @ConfigItem(
            keyName = OVERLAY_STYLE,
            name="Overlay style",
            description = "Change the style of how the overlay renders. " +
                    "<br/><br/>" +
                    "Dynamic (default) - Switch between One Big Number/Vertical/Horizontal Grid based on how many different types of planks are in the sack.<br/>" +
                    "Tooltip Only (disable overlay) - Disable the overlay; the tooltip will still show when hovering the plank sack.<br/>" +
                    "One Big Number - Show the entire contents as one large number, with or without icons.<br/>" +
                    "One Small Number - Show the entire contents as one small number on the bottom left corner, with or without icons.<br/>" +
                    "Vertical - Show each plank in the sack on its own line.<br/>" +
                    "Vertical/Horizontal Grids - Show the planks in a 2-wide or 2-tall grid. Does not support showing long plank names.",
            position = 3,
            section=overlaySettings
    )
    default OverlayStyle overlayStyle() {
        return OverlayStyle.DYNAMIC;
    }

    @ConfigItem(
            keyName = PLANK_ICONS,
            name="Show overlay plank icons",
            description = "Enable or disable the plank icons on the overlay.",
            position = 4,
            section=overlaySettings
    )
    default boolean showOverlayIcons() { return true; }


    @ConfigItem(
            keyName = PLANK_NAMES,
            name = "Plank name display",
            description =  "Change if/how the names of each plank in the plank sack are displayed on the overlay. Affects the vertical display style, and grids if labels for grids are not set to always hidden.",
            position = 5,
            section=overlaySettings
    )
    default OverlayTextType textType() {
        return OverlayTextType.LETTER;
    }

    @ConfigItem(
            keyName = ALWAYS_DISABLE_LABELS_IN_GRID,
            name = "Always hide labels in grids",
            description =  "Hides labels when using the grid view, regardless of if they are enabled. Only short labels will be used, if enabled, to reduce text overlap.",
            position = 6,
            section=overlaySettings
    )
    default boolean alwaysDisableLabelsInGrid() {
        return true;
    }

    @ConfigItem(
            keyName = DISPLAY_ZERO,
            name = "Display zero when empty",
            description =  "If true, displays a zero on the overlay when the sack is empty.",
            position = 7,
            section=overlaySettings
    )
    default boolean displayZeroWhenEmpty() {
        return true;
    }


    @ConfigItem(
            keyName = ENABLE_ICON_FOR_ONE_BIG_NUMBER,
            name = "Enable icons for One Big Number",
            description =  "If this is disabled, the One Big Number style will not show icons, even when icons are enabled.",
            position = 8,
            section=overlaySettings
    )
    default boolean enableIconOneBigNumber() {
        return true;
    }

    @ConfigSection(
            name = "Tooltip",
            description = "Settings for the tooltip, shown when hovering on the plank sack.",
            position = 9,
            closedByDefault = true
    )
    String tooltipSettings = "tooltipsettings";

    @ConfigItem(
            keyName = TOOLTIP_TEXT_COLOR,
            name = "Tooltip text color",
            description =  "The color of the plank name in the tooltip, shown when hovering on the plank sack, if enabled.",
            position = 10,
            section=tooltipSettings
    )
    default Color tooltipTextColor() {
        return Color.YELLOW;
    }

    @ConfigItem(
            keyName = TOOLTIP_NUMBER_COLOR,
            name = "Tooltip number color",
            description =  "The color of the quantity of each plank in the tooltip, shown when hovering on the plank sack, if enabled.",
            position = 11,
            section=tooltipSettings
    )
    default Color tooltipNumberColor() {
        return Color.WHITE;
    }

    @ConfigItem(
            keyName = ENABLE_TOOLTIP,
            name = "Show tooltip",
            description =  "When enabled, display a tooltip when hovering on the plank sack listing the current contents.",
            position = 12,
            section=tooltipSettings
    )
    default boolean enableTooltip() {
        return true;
    }

}
