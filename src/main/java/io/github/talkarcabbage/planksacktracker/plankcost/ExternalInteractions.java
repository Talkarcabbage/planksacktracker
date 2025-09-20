package io.github.talkarcabbage.planksacktracker.plankcost;

import io.github.talkarcabbage.planksacktracker.*;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;

import java.util.ArrayList;

import static io.github.talkarcabbage.planksacktracker.SackTrackerPlugin.PLUGIN_GROUP_ID;

public class ExternalInteractions {
    public static PlankTier getCurrentMHPluginContractTier(ConfigManager manager) {
        var tier = manager.getConfiguration(Constants.MAHOGANY_HOMES_PLUGIN_GROUP_ID, Constants.MAHOGANY_HOMES_PLUGIN_TIER_KEY);
        if (tier==null) return PlankTier.UNKNOWN;
        var tierInt = Utils.intFromStringOrDefault(tier, -1);
        switch (tierInt) {
            case 1:
                return PlankTier.PLANK;
            case 2:
                return PlankTier.OAK;
            case 3:
                return PlankTier.TEAK;
            case 4:
                return PlankTier.MAHOGANY;
            default:
                return PlankTier.UNKNOWN;
        }
    }
    public static void setDataShareConfigContents(ConfigManager manager, PlankStorageSet currentPlankSack, SackTrackerPlugin plugin) {
        var list = new ArrayList<Integer>(4);
        list.add(currentPlankSack.getPlanks());
        list.add(currentPlankSack.getOaks());
        list.add(currentPlankSack.getTeaks());
        list.add(currentPlankSack.getMahoganies());
        manager.setRSProfileConfiguration(PLUGIN_GROUP_ID, SackTrackerConfig.PLANK_SACK_TRACKER_CONFIG_SACK_CONTENTS_KEY, list);
    }
    public static void setDataShareReplacePlankSack(ConfigManager manager, PlankStorageSet currentPlankSack, SackTrackerPlugin plugin) {
        if (plugin.getConfig().replacePlankSackPlugin()) {
            manager.setRSProfileConfiguration(Constants.PLANK_SACK_PLUGIN_GROUP_ID, Constants.PLANK_SACK_PLUGIN_CONTENTS_KEY, currentPlankSack.getTotalPlanks());
        }
    }
    public static void setUnknownDataShareReplacePlankSack(ConfigManager manager) {
        manager.setRSProfileConfiguration(Constants.PLANK_SACK_PLUGIN_GROUP_ID, Constants.PLANK_SACK_PLUGIN_CONTENTS_KEY, 0);
    }
    public static void resetDataShares(ConfigManager manager, SackTrackerPlugin plugin) {
        if (plugin.getConfig().replacePlankSackPlugin()) {
            manager.unsetRSProfileConfiguration(Constants.PLANK_SACK_PLUGIN_GROUP_ID, Constants.PLANK_SACK_PLUGIN_CONTENTS_KEY);
        }
        manager.unsetRSProfileConfiguration(PLUGIN_GROUP_ID, SackTrackerConfig.PLANK_SACK_TRACKER_CONFIG_SACK_CONTENTS_KEY);
    }

}
