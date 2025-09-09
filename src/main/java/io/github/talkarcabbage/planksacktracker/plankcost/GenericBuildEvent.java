package io.github.talkarcabbage.planksacktracker.plankcost;

import io.github.talkarcabbage.planksacktracker.PlankStorageSet;

public class GenericBuildEvent implements PlankBuildEvent {
    private final XP xPDrop;
    private final PlankStorageSet inventoryAfterBuild;
    private final int serverTick;

    public GenericBuildEvent(XP xp, PlankStorageSet inventoryAfterBuild, int serverTick) {
        this.xPDrop = xp;
        this.inventoryAfterBuild = inventoryAfterBuild;
        this.serverTick = serverTick;
    }

    @Override
    public XP getXPDrop() {
        return xPDrop;
    }

    @Override
    public PlankStorageSet getInventoryAfterBuild() {
        return inventoryAfterBuild;
    }

    @Override
    public int getServerTick() {
        return serverTick;
    }
}
