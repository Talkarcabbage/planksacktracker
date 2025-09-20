package io.github.talkarcabbage.planksacktracker.plankcost;

import io.github.talkarcabbage.planksacktracker.PlankStorageSet;

/**
 * This is when we click 'build' or something similar
 */
public interface GenericPlankBuildQueueEvent {
    XP getExpectedXP(double modifierRatio);
    PlankStorageSet getPlankCost();
    int getServerTick();
}
