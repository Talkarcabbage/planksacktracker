package io.github.talkarcabbage.planksacktracker;

import io.github.talkarcabbage.planksacktracker.plankcost.OverlayTextType;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("planksacktracker")
public interface SackTrackerConfig extends Config {

    String PLANK_SACK_TRACKER_CONFIG_SACK_CONTENTS_KEY = "planksackcontents";

    String INFO_CHAT = "infochat";
    String INFO_CHAT_ALWAYS = "infochatalways";
    String TEXT_COLOR = "textcolor";
    String PLANK_ICONS = "plankicons";
    String PLANK_NAMES = "planknames";
    String DISPLAY_ZERO = "displayzerowhenempty";
    String OVERWRITE_PLUGIN = "overwritepsplugin";

    @ConfigItem(
            keyName = INFO_CHAT,
            name = "Chat Warnings/Info",
            description =  "If checked, the plugin will display warning and informative messages in chat, such as when it cannot determine the plank sack contents.",
            position = 1
    )
    default boolean chatInfo() {return true;}

    @ConfigItem(
            keyName = INFO_CHAT_ALWAYS,
            name = "Always display warnings",
            description = "If unchecked, each warning message will only be displayed once in a session.",
            position = 2
    )
    default boolean alwaysDisplayWarnings() {return false;}

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

    @ConfigItem(
            keyName = OVERWRITE_PLUGIN,
            name = "Override Plank Sack MH Data",
            description =
                    "[Experimental]<br>" +
                    "If checked, the plugin will override the data<br>" +
                    "that the Plank Sack plugin [by Enriath] would normally provide <br>" +
                    "that is used by plugins such as mahogany homes. <br><br>" +
                    "Be aware, it shares ALL planks similarly to how the other plugin would,<br>" +
                    "so a plank sack with multiple types of planks may not give you the desired results!",
            position = 8
    )
    default boolean replacePlankSackPlugin() {
        return false;
    }

}
