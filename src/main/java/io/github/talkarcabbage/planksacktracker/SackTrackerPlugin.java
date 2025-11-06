package io.github.talkarcabbage.planksacktracker;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
        name = "Plank Sack Tracker"
)
public class SackTrackerPlugin extends Plugin {

    /*
    For anyone that needs to know while reading through this code:
    The plugin updates the configManager via
    plugin.configManager.setRSProfileConfiguration(PLUGIN_GROUP_ID, SackTrackerConfig.PLANK_SACK_TRACKER_CONFIG_SACK_CONTENTS_KEY, list);
    to reflect the contents of the planksack whenever a change is detected.
    It is cleared if the plugin shuts down.
    It is set to an empty list IF the contents are unknown.
    Else it is in the format: [Planks, Oaks, Teaks, Mahoganies] (even if the particular type of plank is 0)
     */

    @Inject
    protected Client client;
    @Inject
    @Getter
    private SackTrackerConfig config;
    @Inject
    private OverlayManager overlayManager;
    private PlankSackManager sackManager = new PlankSackManager(this);
    private PlankSackOverlay overlay = null;

    @Inject
    private TooltipManager tooltipManager;

    public static final String PLUGIN_GROUP_ID = "planksacktracker";

    @Override
    protected void startUp() throws Exception {
        sackManager = new PlankSackManager(this);
        overlay = new PlankSackOverlay(this, sackManager, config, tooltipManager );
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
    }

    @Provides
    SackTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SackTrackerConfig.class);
    }
}
