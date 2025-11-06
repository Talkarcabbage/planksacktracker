package io.github.talkarcabbage.planksacktracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("planksacktracker")
public interface SackTrackerConfig extends Config {

    String TEXT_COLOR = "textcolor";
    String PLANK_ICONS = "plankicons";
    String PLANK_NAMES = "planknames";
    String DISPLAY_ZERO = "displayzerowhenempty";

    @ConfigItem(
            keyName = TEXT_COLOR,
            name="Overlay text color",
            description = "",
            position = 3
    )
    default Color textColor() { return new Color(255,255,255); }

    @ConfigItem(
            keyName = PLANK_ICONS,
            name="Overlay Icons",
            description = "Enable or disable the plank icons on the overlay.",
            position = 4
    )
    default boolean showOverlayIcons() { return true; }

    @ConfigItem(
            keyName = PLANK_NAMES,
            name = "Plank Name Display",
            description =  "How, and whether, to display the names of each plank in the plank sack on the overlay.",
            position = 5
    )
    default OverlayTextType textType() {
        return OverlayTextType.FULL;
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

}
