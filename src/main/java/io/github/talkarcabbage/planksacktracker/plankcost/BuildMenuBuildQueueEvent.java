package io.github.talkarcabbage.planksacktracker.plankcost;

import io.github.talkarcabbage.planksacktracker.Confidence;
import io.github.talkarcabbage.planksacktracker.PlankStorageSet;

public class BuildMenuBuildQueueEvent implements GenericPlankBuildQueueEvent {

    private final int currentTick;
    private final PlankStorageSet cost;

    public BuildMenuBuildQueueEvent(int currentTick, PlankStorageSet menuCost) {
        this.currentTick = currentTick;
        this.cost = menuCost;
    }

    @Override
    public XP getExpectedXP(double modifierRatio) {
        return null;
    }

    @Override
    public PlankStorageSet getPlankCost() {
        return cost;
    }

    @Override
    public int getServerTick() {
        return currentTick;
    }

    @Override
    public Confidence getConfidence() {
        return Confidence.HIGH;
    }
}
