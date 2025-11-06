package io.github.talkarcabbage.planksacktracker;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class PlankStorageSet {
    private final int planks;
    private final int oaks;
    private final int teaks;
    private final int mahoganies;
    private final int rosewoods;
    private final int ironwoods;
    private final int camphors;

    public PlankStorageSet(int planks, int oaks, int teaks, int mahoganies, int rosewoods, int ironwoods, int camphors) {
        this.planks=planks;
        this.oaks=oaks;
        this.teaks=teaks;
        this.mahoganies=mahoganies;
        this.rosewoods = rosewoods;
        this.ironwoods = ironwoods;
        this.camphors = camphors;
    }
    /**
     * Returns true if the storage set only contains one type of plank
     */
    public boolean isMonoContent() {
        int count = 0;
        if (planks != 0) count++;
        if (oaks != 0) count++;
        if (teaks != 0) count++;
        if (mahoganies != 0) count++;
        if (rosewoods != 0) count++;
        if (ironwoods != 0) count++;
        if (camphors != 0) count++;
        return count == 1;
    }

    /**
     * Returns true if every plank value in this storage set is 0
     * @return
     */
    public boolean isEmpty() {
        return planks == 0 && oaks == 0 && teaks == 0 && mahoganies == 0;
    }

    public int getRemainingSackSpace() {
        return 28-(planks+oaks+teaks+mahoganies+rosewoods+ironwoods+camphors);
    }

    public String toPrintableString() {
        StringBuilder sb = new StringBuilder();
        boolean comma = false;
        if (planks!=0) {
            sb.append(planks).append(" planks");
            comma=true;
        }
        if (oaks!=0) {
            if (comma) sb.append(", ");
            sb.append(oaks).append(" oak planks");
            comma=true;
        }
        if (teaks!=0) {
            if (comma) sb.append(", ");
            sb.append(teaks).append(" teak planks");
            comma=true;
        }
        if (mahoganies!=0) {
            if (comma) sb.append(", ");
            sb.append(mahoganies).append(" mahogany planks");
            comma=true;
        }
        if (rosewoods != 0) {
            if (comma) sb.append(", ");
            sb.append(rosewoods).append(" rosewood planks");
            comma = true;
        }
        if (ironwoods != 0) {
            if (comma) sb.append(", ");
            sb.append(ironwoods).append(" ironwood planks");
            comma = true;
        }
        if (camphors != 0) {
            if (comma) sb.append(", ");
            sb.append(camphors).append(" camphor planks");
        }
        return sb.toString();
    }

    public String toOverlayString() {
        var sb = new StringBuilder();
        if (planks!=0) sb.append(planks).append(" plank\n");
        if (oaks!=0) sb.append(oaks).append(" oak\n");
        if (teaks!=0) sb.append(teaks).append(" teak\n");
        if (mahoganies!=0) sb.append(mahoganies).append(" mahogany\n");
        if (rosewoods!=0) sb.append(rosewoods).append(" rosewood\n");
        if (ironwoods!=0) sb.append(ironwoods).append(" ironwood\n");
        if (camphors!=0) sb.append(camphors).append(" camphor");
        return sb.toString();
    }

    public PlankTier getMonoType( ) {
        if (!this.isMonoContent()) return PlankTier.UNKNOWN;
        if (planks!=0) return PlankTier.PLANK;
        if (oaks!=0) return PlankTier.OAK;
        if (teaks!=0) return PlankTier.TEAK;
        if (mahoganies!=0) return PlankTier.MAHOGANY;
        if (rosewoods!=0) return PlankTier.ROSEWOOD;
        if (ironwoods!=0) return PlankTier.IRONWOOD;
        if (camphors!=0) return PlankTier.CAMPHOR;
        return PlankTier.UNKNOWN;
    }

    public int getTotalPlanks() {
        return planks+oaks+teaks+mahoganies+rosewoods+ironwoods+camphors;
    }

    /**
     * Returns the number of planks matching the given tier of plank
     */
    public int getTierAmount(PlankTier tier) {
        switch (tier){
            case PLANK: return planks;
            case OAK: return oaks;
            case TEAK: return teaks;
            case MAHOGANY: return mahoganies;
            case ROSEWOOD: return rosewoods;
            case IRONWOOD: return ironwoods;
            case CAMPHOR: return camphors;
            default: return 0;
        }
    }

}
