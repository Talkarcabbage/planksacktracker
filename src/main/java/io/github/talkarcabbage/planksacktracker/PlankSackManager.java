package io.github.talkarcabbage.planksacktracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.VarbitID;

@Slf4j
public class PlankSackManager {
    private final SackTrackerPlugin plugin;

    public PlankSackManager(SackTrackerPlugin plugin) {
        this.plugin = plugin;
    }

    public PlankStorageSet getCurrentPlankSack() {
        return new PlankStorageSet(
                plugin.client.getVarbitValue(VarbitID.PLANK_SACK_PLAIN),
                plugin.client.getVarbitValue(VarbitID.PLANK_SACK_OAK),
                plugin.client.getVarbitValue(VarbitID.PLANK_SACK_TEAK),
                plugin.client.getVarbitValue(VarbitID.PLANK_SACK_MAHOGANY),
                plugin.client.getVarbitValue(VarbitID.PLANK_SACK_ROSEWOOD),
                plugin.client.getVarbitValue(VarbitID.PLANK_SACK_IRONWOOD),
                plugin.client.getVarbitValue(VarbitID.PLANK_SACK_CAMPHOR)
        );
    }
}
