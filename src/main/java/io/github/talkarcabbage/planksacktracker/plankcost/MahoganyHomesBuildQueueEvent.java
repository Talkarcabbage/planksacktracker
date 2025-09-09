package io.github.talkarcabbage.planksacktracker.plankcost;

import io.github.talkarcabbage.planksacktracker.PlankStorageSet;
import lombok.Getter;

import static io.github.talkarcabbage.planksacktracker.Constants.*;

public class MahoganyHomesBuildQueueEvent implements PlankBuildQueueEvent {
    PlankStorageSet cost;
    @Getter
    int currentTick;

    public MahoganyHomesBuildQueueEvent(PlankStorageSet cost, int currentTick) {
        this.cost=cost;
        this.currentTick = currentTick;
    }

    /**
     *
     * Provide the modifierRatio as a value of 1.0 plus any xp modifiers
     * such as the carpenter's set (e.g. 1.025)
     * XP returned is multiplied by this value.
     */
    @Override
    public XP getExpectedXP(double modifierRatio) {
        XP xp = new XP(0);
        xp = xp.addMultiple(PLANK_MAHOGANY_HOMES_XP, cost.getPlanks());
        xp = xp.addMultiple(OAK_MAHOGANY_HOMES_XP, cost.getOaks());
        xp = xp.addMultiple(TEAK_MAHOGANY_HOMES_XP, cost.getTeaks());
        xp = xp.addMultiple(MAHOGANY_MAHOGANY_HOMES_XP, cost.getMahoganies());
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

}
