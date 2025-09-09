package io.github.talkarcabbage.planksacktracker.plankcost;

import io.github.talkarcabbage.planksacktracker.PlankStorageSet;

/**
 * For when a build event actually happens. Usually, this is when we detect an XP drop and/or a different type of event
 *
 */
public interface PlankBuildEvent {
    XP getXPDrop();
    PlankStorageSet getInventoryAfterBuild();
    /**
     * The server tick this build event happened on
     * @return
     */
    int getServerTick();
}
