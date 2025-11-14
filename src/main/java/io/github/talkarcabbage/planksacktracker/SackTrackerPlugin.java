package io.github.talkarcabbage.planksacktracker;

import com.google.inject.Provides;
import io.github.talkarcabbage.planksacktracker.overlay.PlankSackOverlay;
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
    @Inject
    Client client;
    @Inject
    @Getter
    private SackTrackerConfig config;
    @Inject
    private OverlayManager overlayManager;
    private PlankSackOverlay overlay = null;

    @Inject
    private TooltipManager tooltipManager;

    public static final String PLUGIN_GROUP_ID = "planksacktracker";

    @Override
    protected void startUp() {
        PlankSackManager sackManager = new PlankSackManager(this);

        overlay = new PlankSackOverlay(this, sackManager, config, client, tooltipManager );
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
    }

    @Provides
    SackTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SackTrackerConfig.class);
    }
}
