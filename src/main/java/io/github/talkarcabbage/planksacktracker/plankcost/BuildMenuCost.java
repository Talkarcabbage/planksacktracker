package io.github.talkarcabbage.planksacktracker.plankcost;

import io.github.talkarcabbage.planksacktracker.PlankStorageSet;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BuildMenuCost {
    @Getter
    private int buildMenuID;
    @Getter
    private int uiKeybindID;
    @Getter
    private PlankStorageSet cost;

}
