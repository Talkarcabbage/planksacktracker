package io.github.talkarcabbage.planksacktracker;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

@Getter
@Slf4j
public class PlankStorageSet {
    private final int planks;
    private final int oaks;
    private final int teaks;
    private final int mahoganies;

    public PlankStorageSet(int planks, int oaks, int teaks, int mahoganies) {
        this.planks=planks;
        this.oaks=oaks;
        this.teaks=teaks;
        this.mahoganies=mahoganies;
    }

    /**
     * Returns a copy of the provided storage.
     * @param copy
     */
    public PlankStorageSet(PlankStorageSet copy) {
        this(copy.planks, copy.oaks, copy.teaks, copy.mahoganies);
    }

    public static PlankStorageSet createFromInventory(ItemContainer container) {
        var items = container.getItems();
        int planks = 0, oaks=0, teaks=0, mahoganies = 0;
        for (Item item : items) {
            var id = item.getId();
            switch (Utils.getFromItemID(id)) {
                case PLANK:
                    planks++;
                    break;
                case OAK:
                    oaks++;
                    break;
                case TEAK:
                    teaks++;
                    break;
                case MAHOGANY:
                    mahoganies++;
                    break;
                default:
            }
        }
        return new PlankStorageSet(planks,oaks,teaks,mahoganies);
    }

    /**
     * Convenience method to create a storage set that only contains
     * the specified tier of planks
     * @param planks
     * @param tier
     */
    public static PlankStorageSet createFromTier(int planks, PlankTier tier) {
        switch (tier) {
            case PLANK:
                return new PlankStorageSet(planks,0,0,0);
            case OAK:
                return new PlankStorageSet(0,planks,0,0);
            case TEAK:
                return new PlankStorageSet(0,0,planks,0);
            case MAHOGANY:
                return new PlankStorageSet(0,0,0,planks);
            default:
                log.warn("Tried to create from tier but it doesn't exist!");
                return new PlankStorageSet(0,0,0,0);
        }
    }

    public static PlankStorageSet emptySet() {
        return new PlankStorageSet(0,0,0,0);
    }

    /**
     * Returns true if the storage set only contains one type of plank
     * @return
     */
    public boolean isMonoContent() {
        int count = 0;
        if (planks != 0) count++;
        if (oaks != 0) count++;
        if (teaks != 0) count++;
        if (mahoganies != 0) count++;
        return count == 1;
    }

    /**
     * Returns true if every plank value in this storage set is 0
     * @return
     */
    public boolean isEmpty() {
        return planks == 0 && oaks == 0 && teaks == 0 && mahoganies == 0;
    }

    public PlankStorageSet add(PlankStorageSet other){
        return new PlankStorageSet(
                this.planks+other.planks,
                this.oaks+other.oaks,
                this.teaks+other.teaks,
                this.mahoganies+other.mahoganies
        );
    }

    /**
     * Returns a PlankStorageSet that represents the difference between
     * this set minus the other one.
     * @param other
     * @return
     */
    public PlankStorageSet subtract(PlankStorageSet other) {
        return new PlankStorageSet(
                this.planks-other.planks,
                this.oaks-other.oaks,
                this.teaks-other.teaks,
                this.mahoganies-other.mahoganies
        );
    }

    public int getRemainingSackSpace() {
        return 28-(planks+oaks+teaks+mahoganies);
    }

    public String toPrintableString() {
        StringBuilder sb = new StringBuilder();
        boolean comma = false;
        if (planks!=0) {
            sb.append(planks).append(" planks");
            comma=true;
        } if (oaks!=0) {
            if (comma) sb.append(", ");
            sb.append(oaks).append(" oak planks");
            comma=true;
        } if (teaks!=0) {
            if (comma) sb.append(", ");
            sb.append(teaks).append(" teak planks");
            comma=true;
        } if (mahoganies!=0) {
            if (comma) sb.append(", ");
            sb.append(mahoganies).append(" mahogany planks");
            comma=true;
        }
        return sb.append("").toString();
    }

    public String toOverlayString() {
        var sb = new StringBuilder();
        if (planks!=0) sb.append(planks).append(" plank\n");
        if (oaks!=0) sb.append(oaks).append(" oak\n");
        if (teaks!=0) sb.append(teaks).append(" teak\n");
        if (mahoganies!=0) sb.append(mahoganies).append(" mahogany");
        return sb.toString();

    }

    public PlankTier getMonoType( ) {
        if (!this.isMonoContent()) return PlankTier.UNKNOWN;
        if (planks!=0) return PlankTier.PLANK;
        if (oaks!=0) return PlankTier.OAK;
        if (teaks!=0) return PlankTier.TEAK;
        if (mahoganies!=0) return PlankTier.MAHOGANY;
        return PlankTier.UNKNOWN;
    }

    public int getTotalPlanks() {
        return planks+oaks+teaks+mahoganies;
    }

    /**
     * Returns true if any of the plank values stored in this
     * storage are negative.
     * @return
     */
    public boolean hasNegativeValues() {
        return (planks<0||oaks<0||teaks<0||mahoganies<0);
    }

    /**
     * Returns the number of planks matching the given tier of plank
     * @return
     */
    public int getTierAmount(PlankTier tier) {
        switch (tier){
            case PLANK: return planks;
            case OAK: return oaks;
            case TEAK: return teaks;
            case MAHOGANY: return mahoganies;
            default: return 0;
        }
    }

}
