package io.github.talkarcabbage.planksacktracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PlankSackTrackerTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(SackTrackerPlugin.class);
        RuneLite.main(args);
    }

}