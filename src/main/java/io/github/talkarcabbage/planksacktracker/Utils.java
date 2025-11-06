package io.github.talkarcabbage.planksacktracker;

import io.github.talkarcabbage.planksacktracker.plankcost.XP;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.util.ColorUtil;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Utils {

    public static String br() {
        return "</br>";
    }

    public static String white(String text) {
        return ColorUtil.wrapWithColorTag(text, Color.WHITE);
    }

    public static String yellow(String text) {
        return ColorUtil.wrapWithColorTag(text, Color.YELLOW);
    }

    private static final String SACK_LOG_SEPARATOR="\u00A0";
    public static boolean checkWithinRange(int value, int expectedValue, int maxDifference) {
        int difference = Math.abs(expectedValue-value);
        return (difference<=maxDifference);
    }
    public static int intFromStringOrDefault(String integer, int defaultValue) {
        try {
            return Integer.parseInt(integer);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static final XP OVERLAP_XP = new XP(144);

    public static PlankStorageSet expectedCostFromXP(double modifier, XP givenXP, PlankStorageSet currentSack) {
        if (OVERLAP_XP.multiply(modifier).equals(givenXP)) {
            if (currentSack.getOaks()==0) {
                return PlankStorageSet.createFromTier(2, PlankTier.TEAK); //the only known overlaps...
            } else if (currentSack.getTeaks()==0) {
                return PlankStorageSet.createFromTier(3, PlankTier.OAK);//So if we have none of the other plank...
            }
            return PlankStorageSet.emptySet(); //If the xp overlaps with each other
        }
        var xpPlank = new XP(Constants.PLANK_MAHOGANY_HOMES_XP.getServerXPValue()*modifier, true);
        var xpOak = new XP(Constants.OAK_MAHOGANY_HOMES_XP.getServerXPValue()*modifier, true);
        var xpTeak = new XP(Constants.TEAK_MAHOGANY_HOMES_XP.getServerXPValue()*modifier, true);
        var xpMahogany = new XP(Constants.MAHOGANY_MAHOGANY_HOMES_XP.getServerXPValue()*modifier, true);

        //TODO this maybe doesn't work quite right
        //todo doubly so after the sailing stuff
        return new PlankStorageSet(
                getPlanksFromXPIfMatches(xpPlank, givenXP),
                getPlanksFromXPIfMatches(xpOak, givenXP),
                getPlanksFromXPIfMatches(xpTeak, givenXP),
                getPlanksFromXPIfMatches(xpMahogany, givenXP),
                0,
                0,
                0
        );

    }

    public static PlankTier expectedMHPlankFromXP(double modifier, XP givenXP) {
        var xpPlank = new XP(Constants.PLANK_MAHOGANY_HOMES_XP.getServerXPValue()*modifier, true);
        var xpOak = new XP(Constants.OAK_MAHOGANY_HOMES_XP.getServerXPValue()*modifier, true);
        var xpTeak = new XP(Constants.TEAK_MAHOGANY_HOMES_XP.getServerXPValue()*modifier, true);
        var xpMahogany = new XP(Constants.MAHOGANY_MAHOGANY_HOMES_XP.getServerXPValue()*modifier, true);

        if (xpPlank.roughEquals(Constants.PLANK_MAHOGANY_HOMES_REPAIR_XP)) return PlankTier.PLANK;
        if (xpPlank.roughEquals(Constants.OAK_MAHOGANY_HOMES_REPAIR_XP)) return PlankTier.OAK;
        if (xpPlank.roughEquals(Constants.TEAK_MAHOGANY_HOMES_REPAIR_XP)) return PlankTier.TEAK;
        if (xpPlank.roughEquals(Constants.MAHOGANY_MAHOGANY_HOMES_REPAIR_XP)) return PlankTier.MAHOGANY;

        if (checkToThree(xpPlank, givenXP)) {
            return PlankTier.PLANK;
        }
        if (checkToThree(xpOak, givenXP)) {
            return PlankTier.OAK;
        }
        if (checkToThree(xpTeak, givenXP)) {
            return PlankTier.TEAK;
        }
        if (checkToThree(xpMahogany, givenXP)) {
            return PlankTier.MAHOGANY;
        }
        return PlankTier.UNKNOWN;
    }
    private static boolean checkToThree(XP xpToCheck, XP givenXP) {
        if (xpToCheck.roughEquals(givenXP)) return true;
        if (xpToCheck.multiply(2).roughEquals(givenXP)) return true;
        if (xpToCheck.multiply(3).roughEquals(givenXP)) return true;
        return false;
    }
    private static int getPlanksFromXPIfMatches(XP xpToCheck, XP givenXP) {
            if (xpToCheck.roughEquals(givenXP)) return 1;
            if (xpToCheck.multiply(2).roughEquals(givenXP)) return 2;
            if (xpToCheck.multiply(3).roughEquals(givenXP)) return 3;
            return 0;
    }

    /**
     * Returns true if the itemID corresponds to any of the 4 common plank types
     * @param itemID
     * @return
     */
    public static boolean isPlank(int itemID) {
        return (itemID==ItemID.WOODPLANK || itemID== ItemID.PLANK_OAK || itemID==ItemID.PLANK_TEAK || itemID==ItemID.PLANK_MAHOGANY);
    }

    public static boolean isLog(int itemID) {
        return (itemID==ItemID.LOGS || itemID== ItemID.OAK_LOGS || itemID==ItemID.TEAK_LOGS || itemID==ItemID.MAHOGANY_LOGS);
    }

    public static PlankTier getFromItemID(int id) {
        switch (id) {
            case ItemID.WOODPLANK:
                return PlankTier.PLANK;
            case ItemID.PLANK_OAK:
                return PlankTier.OAK;
            case ItemID.PLANK_TEAK:
                return PlankTier.TEAK;
            case ItemID.PLANK_MAHOGANY:
                return PlankTier.MAHOGANY;
            //TODO: item IDS for the new 3 planks
            default:
                return PlankTier.UNKNOWN;
        }
    }

    public static double getCurrentXPModifier(SackTrackerPlugin plugin) {
        var modifier = 1.0;
        //TODO leagues support
        var equipped = plugin.client.getItemContainer(InventoryID.WORN);
        if (equipped==null) return modifier;

        var helmet = equipped.contains(ItemID.CONSTRUCTION_HAT);
        var shirt = equipped.contains(ItemID.CONSTRUCTION_SHIRT);
        var trousers = equipped.contains(ItemID.CONSTRUCTION_TROUSERS);
        var boots = equipped.contains(ItemID.CONSTRUCTION_BOOTS);
        if (helmet && shirt && trousers && boots) {
            return modifier*1.025;
        }
        if (helmet) modifier*=1.004;
        if (shirt) modifier*=1.008;
        if (trousers) modifier*=1.006;
        if (boots) modifier*=1.002;

        return modifier;
    }

    public static int intFromObjectOrDefault(Object o, int def) {
        if (o instanceof Integer) return (Integer)o;
        return def;
    }


    /**
     * Returns true if every var-arg provided is not null
     * @param objects
     * @return
     */
    public static boolean allNotNull(Object... objects) {
        for (var o : objects) {
            if (o==null) return false;
        }
        return true;
    }
}
