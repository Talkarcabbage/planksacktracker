package io.github.talkarcabbage.planksacktracker.plankcost;

import io.github.talkarcabbage.planksacktracker.Confidence;
import io.github.talkarcabbage.planksacktracker.PlankStorageSet;

import static io.github.talkarcabbage.planksacktracker.Constants.*;

public class MahoganyHomesRepairQueueEvent implements GenericPlankBuildQueueEvent {
    PlankStorageSet cost;
    private final int currentTick;
    private Confidence confidence;

    public MahoganyHomesRepairQueueEvent(PlankStorageSet cost, int currentTick, Confidence confidence) {
        this.cost=cost;
        this.currentTick = currentTick;
        this.confidence = confidence;
    }

    @Override
    public XP getExpectedXP(double modifierRatio) {
        XP xp = new XP(0);
        xp = xp.addMultiple(PLANK_MAHOGANY_HOMES_REPAIR_XP, cost.getPlanks());
        xp = xp.addMultiple(OAK_MAHOGANY_HOMES_REPAIR_XP, cost.getOaks());
        xp = xp.addMultiple(TEAK_MAHOGANY_HOMES_REPAIR_XP, cost.getTeaks());
        xp = xp.addMultiple(MAHOGANY_MAHOGANY_HOMES_REPAIR_XP, cost.getMahoganies());
        xp = xp.multiply(modifierRatio);
        return xp;
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
        return null;
    }
}
