package io.github.talkarcabbage.planksacktracker;

import io.github.talkarcabbage.planksacktracker.plankcost.XP;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Common constants or effective constants for use in other classes.
 */
@Slf4j
public final class Constants {
    public static final String CAST_MENU_OPTION = "Cast";
    /**
     * Map of the plank costs of each mahogany homes build location
     */
    private static final Map<Integer, Integer> mahoganyHotspotsMap = new HashMap<Integer, Integer>(120);
    private static boolean initDone = false;

    public static int getMahoganyHomesPlankCost(int hotspotID) {
        if (!initDone) initHotspots();
        return mahoganyHotspotsMap.getOrDefault(hotspotID, 0);
    }

    private static void addHotspot(int hotspotID, int plankCost) {
        mahoganyHotspotsMap.put(hotspotID, plankCost);
    }
    public static void initHotspots( ) {
        if (initDone) return;
        // Jess (Upstairs)
        addHotspot(40173, 2); // Cabinet (1)
        addHotspot(40174, 2); // Cabinet (2)
        addHotspot(40171, 2); // Drawers (1)
        addHotspot(40172, 2); // Drawers (2)
        addHotspot(40175, 3); // Bed
        addHotspot(40177, 1); // Grandfather Clock
        addHotspot(40176, 3); // Table

        // Noella
        addHotspot(40157, 2); // Cupboard
        addHotspot(40156, 2); // Dresser
        addHotspot(40160, 2); // Drawers
        addHotspot(40158, 1); // Hat Stand
        addHotspot(40161, 3); // Table (1)
        addHotspot(40162, 3); // Table (2)
        addHotspot(40163, 1); // Grandfather clock
        addHotspot(40159, 1); // Mirror

        // Ross
        addHotspot(40169, 2); // Bed
        addHotspot(40167, 3); // Double Bed
        addHotspot(40165, 2); // Drawers (1)
        addHotspot(40166, 2); // Drawers (2)
        addHotspot(40168, 1); // Hat Stand
        addHotspot(40170, 1); // Mirror

        // Larry
        addHotspot(40095, 2); // Drawers (1)
        addHotspot(40096, 2); // Drawers (2)
        addHotspot(40099, 1); // Grandfather Clock
        addHotspot(40298, 1); // Hat stand
        addHotspot(40097, 3); // Table (1)
        addHotspot(40098, 3); // Table (2)

        // Norman (Upstairs)
        addHotspot(40092, 2); // Bookshelf
        addHotspot(40091, 3); // Double Bed
        addHotspot(40093, 2); // Drawers
        addHotspot(40089, 1); // Grandfather Clock
        addHotspot(40094, 2); // Small Table
        addHotspot(40090, 3); // Table

        // Tau
        addHotspot(40086, 2); // Cupboard
        addHotspot(40295, 1); // Hat Stand
        addHotspot(40087, 2); // Shelves (1)
        addHotspot(40088, 2); // Shelves (2)
        addHotspot(40084, 3); // Table (1)
        addHotspot(40085, 3); // Table (2)

        // Barbara
        addHotspot(40013, 2); // Bed
        addHotspot(40014, 1); // Chair (1)
        addHotspot(40015, 1); // Chair (2)
        addHotspot(40294, 2); // Drawers
        addHotspot(40012, 3); // Table
        addHotspot(40011, 1); // Grandfather Clock

        // Leela
        addHotspot(40292, 2); // Cupboard
        addHotspot(40007, 2); // Small Table (1)
        addHotspot(40008, 2); // Small Table (2)
        addHotspot(40291, 3); // Double Bed
        addHotspot(40010, 1); // Mirror
        addHotspot(40009, 3); // Table

        // Mariah
        addHotspot(40004, 2); // Bed
        addHotspot(40288, 2); // Cupboard
        addHotspot(40289, 1); // Hat Stand
        addHotspot(40003, 2); // Shelves
        addHotspot(40005, 2); // Small Table (1)
        addHotspot(40006, 2); // Small Table (2)
        addHotspot(40002, 3); // Table

        // Bob
        addHotspot(39981, 4); // Large table
        addHotspot(39985, 2); // Bookcase (1)
        addHotspot(39986, 2); // Bookcase (2)
        addHotspot(39983, 2); // Cabinet (1)
        addHotspot(39984, 2); // Cabinet (2)
        addHotspot(39982, 1); // Grandfather Clock
        addHotspot(39987, 2); // Wardrobe
        addHotspot(39988, 2); // Drawers

        // Jeff
        addHotspot(39990, 2); // Bookcase
        addHotspot(39996, 1); // Chair
        addHotspot(39993, 2); // Drawers
        addHotspot(39994, 2); // Dresser
        addHotspot(39989, 3); // Table
        addHotspot(39991, 2); // Shelves
        addHotspot(39995, 1); // Mirror
        addHotspot(39992, 3); // Bed

        // Sarah
        addHotspot(39998, 2); // Bed
        addHotspot(39999, 2); // Dresser
        addHotspot(40001, 2); // Shelves
        addHotspot(40000, 2); // Small table
        addHotspot(39997, 3); // Table

        initDone = true;
    }

    // Sawmill prices
    public static final int PLANK_GOLD_COST = 100;
    public static final int OAK_GOLD_COST = 250;
    public static final int TEAK_GOLD_COST = 500;
    public static final int MAHOGANY_GOLD_COST = 1500;

    // Sawmill Widget IDs
    public static final int SKILL_MENU_OPTION_1 = 17694734; //DOUBLE CHECK THESE TODO
    public static final int SKILL_MENU_OPTION_2 = 17694735;
    public static final int SKILL_MENU_OPTION_3 = 17694736;
    public static final int SKILL_MENU_OPTION_4 = 17694737;

    // Plank Make costs


    // Planksack options
    public static final String FILL_STRING_OPTION = "Fill";
    public static final String EMPTY_STRING_OPTION = "Empty";
    public static final String USE_STRING_OPTION = "Use";

    // XP Rates for the mahogany homes plank usages
    public static final XP PLANK_MAHOGANY_HOMES_XP = new XP(225, true);
    public static final XP OAK_MAHOGANY_HOMES_XP = new XP(48);
    public static final XP TEAK_MAHOGANY_HOMES_XP = new XP(72);
    public static final XP MAHOGANY_MAHOGANY_HOMES_XP = new XP(112);

    // XP Rates for the mahogany homes repair usages
    public static final XP PLANK_MAHOGANY_HOMES_REPAIR_XP = new XP(1275, true);
    public static final XP OAK_MAHOGANY_HOMES_REPAIR_XP = new XP(160);
    public static final XP TEAK_MAHOGANY_HOMES_REPAIR_XP = new XP(190);
    public static final XP MAHOGANY_MAHOGANY_HOMES_REPAIR_XP = new XP(240);
    public static final XP MAHOGANY_HOMES_STEEL_REPAIR_XP = new XP(120);

    public static final String MAHOGANY_HOMES_PLUGIN_GROUP_ID = "MahoganyHomes";
    public static final String MAHOGANY_HOMES_PLUGIN_TIER_KEY = "currentTier";

    public static final String PLANK_SACK_PLUGIN_GROUP_ID = "planksack";
    public static final String PLANK_SACK_PLUGIN_CONTENTS_KEY = "plankcount";

    // Menu text labels for clicking on interactions
    public static final String TALK_MENU_OPTION = "Talk-to";
    public static final String CONTINUE_MENU_OPTION = "Continue";
    public static final String MENU_REMOVE = "Remove";

    public static final String MENU_BUILD_TEXT = "Build";
    public static final String MENU_REPAIR_TEXT = "Repair";
    public static final String MENU_OPTION_MAKE = "Make";

    public static final int SCRIPT_BUILD_MENU_ENTRY_CLICKED = 1405;
    public static final int SCRIPT_ENTRY_ADDED_TO_BUILD_MENU = 1404;
    public static final int SCRIPT_KEYBIND_PRESS_BUILD_MENU = 1632;
    /**
     * This event fires, twice, for every keybind in the menu
     */
    public static final int SCRIPT_SKILL_MENU_KEYBIND_PREFIRE = 2051;
    /**
     * This fires when you click an option in the UI
     */
    public static final int SCRIPT_SKILL_MENU_UI_PREFIRE = 2050;
    /**
     * This event fires when you choose an item in the skilling menu (eg sawmill)
     */
    public static final int SCRIPT_SKILL_MENU_ACTION_CHOSEN = 2052;
    public static final int SCRIPT_CONFIRM_DIALOG_FOR_PLANK_MAKE = 58;

    public static final int SCRIPT_PREPARE_SKILL_MENU = 2046;

    public static final int DADDYS_HOME_BUILD_CLICK_EVENT = 7987; //Probably has other uses
    public static final int DADDYS_HOME_BUILD_MENU_OPTION_BUILDER_EVENT = 7988;
}
