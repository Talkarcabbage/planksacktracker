package io.github.talkarcabbage.planksacktracker;

import io.github.talkarcabbage.planksacktracker.overlayenums.OverlayStyle;
import io.github.talkarcabbage.planksacktracker.overlayenums.OverlayTextType;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("planksacktracker")
public interface SackTrackerConfig extends Config {

    String TEXT_COLOR = "textcolor";
    String NUMBER_COLOR = "numbercolor";
    String PLANK_ICONS = "plankicons";
    String OVERLAY_STYLE = "overlaystyle";
    String PLANK_NAMES = "planknames";
    String DISPLAY_ZERO = "displayzerowhenempty";
    String DISABLE_ICON_WHEN_EMPTY = "disableiconwhenempty";
    String ENABLE_ICON_FOR_ONE_BIG_NUMBER = "disableonebignumbericon";
    String ALWAYS_DISABLE_LABELS_IN_GRID = "disablelabelsingrid";

    @ConfigItem(
            keyName = TEXT_COLOR,
            name="Overlay text color",
            description = "The font color used to display the names of planks.",
            position = 1
    )
    default Color textColor() { return Color.WHITE; }
    @ConfigItem(
            keyName = NUMBER_COLOR,
            name="Overlay number color",
            description = "The font color used to display the plank quantities.",
            position = 2
    )
    default Color numberColor() { return Color.YELLOW; }

    @ConfigItem(
            keyName = OVERLAY_STYLE,
            name="Overlay style",
            description = "Change the style of how the overlay renders. The default is dynamic, which changes based on how many different types of planks are in the sack.",
            position = 3
    )
    default OverlayStyle overlayStyle() {
        return OverlayStyle.DYNAMIC;
    }

    @ConfigItem(
            keyName = PLANK_ICONS,
            name="Show overlay plank icons",
            description = "Enable or disable the plank icons on the overlay.",
            position = 4
    )
    default boolean showOverlayIcons() { return true; }


    @ConfigItem(
            keyName = PLANK_NAMES,
            name = "Plank name display",
            description =  "How, and whether, to display the names of each plank in the plank sack on the overlay.",
            position = 5
    )
    default OverlayTextType textType() {
        return OverlayTextType.LONG;
    }

    @ConfigItem(
            keyName = DISPLAY_ZERO,
            name = "Display zero when empty",
            description =  "If true, displays a zero on the overlay when the sack is empty.",
            position = 6
    )
    default boolean displayZeroWhenEmpty() {
        return true;
    }

    @ConfigItem(
            keyName = DISABLE_ICON_WHEN_EMPTY,
            name = "Hide plank icon when empty",
            description =  "Hides the overlay plank icon, when the sack is empty, for certain styles when icons are otherwise enabled.",
            position = 7
    )
    default boolean disableIconWhenEmpty() {
        return true;
    }

    @ConfigItem(
            keyName = ENABLE_ICON_FOR_ONE_BIG_NUMBER,
            name = "Enable icons for One Big Number",
            description =  "Hides the plank icon for the 'one big number' style even when icons are enabled.",
            position = 8
    )
    default boolean enableIconOneBigNumber() {
        return true;
    }

    @ConfigItem(
            keyName = ALWAYS_DISABLE_LABELS_IN_GRID,
            name = "Always hide labels in grids",
            description =  "Hides labels when using the grid view, regardless of if they are enabled. Highly recommended to leave on for usability for those display types.",
            position = 9
    )
    default boolean alwaysDisableLabelsInGrid() {
        return true;
    }
}
