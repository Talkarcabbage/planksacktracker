package io.github.talkarcabbage.planksacktracker.plankcost;

import io.github.talkarcabbage.planksacktracker.PlankStorageSet;
import io.github.talkarcabbage.planksacktracker.PlankTier;
import io.github.talkarcabbage.planksacktracker.Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DynamicBuildMenuTracker {

    private final List<BuildMenuCost> costMap = new ArrayList<>();

    public DynamicBuildMenuTracker() {

    }

    public PlankStorageSet getPlankCostByMenuEntry(int entry) {
        for (var it : costMap) {
            if (it.getBuildMenuID()==entry)
                return it.getCost();
        }
        return PlankStorageSet.emptySet();
    }

    public PlankStorageSet getPlankCostByUIKeybind(int key) {
        for (var it : costMap) {
            if (it.getUiKeybindID()==key)
                return it.getCost();
        }
        return PlankStorageSet.emptySet();
    }

    public void addBuildMenuEventEntry(Object[] entryArray) {
        var buildID = entryArray[2];
        var costList = entryArray[4];

        if (buildID instanceof Integer && costList instanceof String) {
            var buildIDInt = (Integer)buildID;
            var buildCost = PlankStorageSet.emptySet();

            var costListString = (String)costList;
            var costsTrimName = costListString.substring(costListString.indexOf("|")+1);
            var keyInList = (Integer)entryArray[1];

            var components = costsTrimName.split("<br>");
            for (var component : components) {
                component = component.trim();
                if (component.startsWith(PLANK_UI_TEXT)) {
                    buildCost = buildCost.add(PlankStorageSet.createFromTier(Utils.intFromStringOrDefault(component.substring(PLANK_UI_TEXT.length()), 0), PlankTier.PLANK));
                } else if (component.startsWith(OAK_UI_TEXT)) {
                    buildCost = buildCost.add(PlankStorageSet.createFromTier(Utils.intFromStringOrDefault(component.substring(OAK_UI_TEXT.length()), 0), PlankTier.OAK));
                } else if (component.startsWith(TEAK_UI_TEXT)) {
                    buildCost = buildCost.add(PlankStorageSet.createFromTier(Utils.intFromStringOrDefault(component.substring(TEAK_UI_TEXT.length()), 0), PlankTier.TEAK));
                } else if (component.startsWith(MAHOGANY_UI_TEXT)) {
                    buildCost = buildCost.add(PlankStorageSet.createFromTier(Utils.intFromStringOrDefault(component.substring(MAHOGANY_UI_TEXT.length()), 0), PlankTier.MAHOGANY));
                }
            }
            log.info("Added a cost entry: "+buildIDInt+" " + buildCost.toPrintableString());
            this.costMap.add(new BuildMenuCost(buildIDInt, keyInList , buildCost));
        } else {
            log.warn("Failed to parse a 1404 cost entry");
        }
    }

    private static final String PLANK_UI_TEXT = "Plank: ";
    private static final String OAK_UI_TEXT = "Oak plank: ";
    private static final String TEAK_UI_TEXT = "Teak plank: ";
    private static final String MAHOGANY_UI_TEXT = "Mahogany plank: ";

}
