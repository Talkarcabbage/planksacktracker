package io.github.talkarcabbage.planksacktracker.planksack;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

@Slf4j
public class PlankSackManager {
    private final Client client;

    public PlankSackManager(Client client) {
        this.client = client;
    }

    public PlankStorageSet getCurrentPlankSack() {
        return new PlankStorageSet(
                client.getVarbitValue(VarbitID.PLANK_SACK_PLAIN),
                client.getVarbitValue(VarbitID.PLANK_SACK_OAK),
                client.getVarbitValue(VarbitID.PLANK_SACK_TEAK),
                client.getVarbitValue(VarbitID.PLANK_SACK_MAHOGANY),
                client.getVarbitValue(VarbitID.PLANK_SACK_ROSEWOOD),
                client.getVarbitValue(VarbitID.PLANK_SACK_IRONWOOD),
                client.getVarbitValue(VarbitID.PLANK_SACK_CAMPHOR)
        );
    }
}
