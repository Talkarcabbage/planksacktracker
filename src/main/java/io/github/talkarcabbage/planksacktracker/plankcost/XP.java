package io.github.talkarcabbage.planksacktracker.plankcost;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * Represents a quantity of in-game XP. The internal value is 10 times what the actual in-game
 * xp would display.
 * This class is effectively IMMUTABLE. Add/subtract and other methods return NEW instances.
 */
@Immutable
public final class XP {
    private final int actualXP;
    /**
     * Construct an XP value from an integer as it would be shown in the game.
     * The internally stored value is 10x this value to represent how the server actually
     * handles xp.
     * @param integerXP The XP value as portrayed in-game
     */
    public XP(int integerXP) {
        this(integerXP, false);
    }
    /**
     * Construct an XP value.
     * If premultiplied is true, the value will not be multiplied by 10.
     * This is used to simplify storing actual xp values without needing to use doubles.
     * @param xp The xp value
     * @param premultiplied Whether to multiply the given xp value by 10.
     */
    public XP(int xp, boolean premultiplied) {
        actualXP = premultiplied?xp:xp*10;
    }

    /**
     * Construct an XP value from a double that represents the XP as it would be shown in-game,
     * but with a rounded decimal value. Any decimal values beyond the 10s place are dropped, per
     * the OSRS wiki's description of how XP multiplication works.
     * @param xp
     */
    public XP(double xp, boolean premultiplied) {
        actualXP = premultiplied?(int)xp:(int)(xp*10);
    }
    public XP add(XP toAdd) {
        return new XP(this.actualXP+toAdd.actualXP, true);
    }
    public XP subtract(XP toSubtract) {
        return new XP(this.actualXP-toSubtract.actualXP, true);
    }
    public XP multiply(double ratio) {
        double result = actualXP*ratio;
        return new XP(result, true);
    }
    public XP addMultiple(XP toAdd, int timesToAdd) {
        int amountToAdd = toAdd.actualXP*timesToAdd;
        return new XP(this.actualXP+amountToAdd, true);
    }
    public String getPrintableXP() {
        return actualXP/10 + "." + actualXP%10;
    }
    public int getRoundedXP() {
        return Math.toIntExact(Math.round(((double)actualXP)/10.0d));
    }
    public double getXPAsDouble() {
        return ((double)actualXP)/10.0d;
    }
    /**
     * @return The server-side equivalent to the xp value, which is the amount shown in-game multiplied by 10.
     */
    public int getServerXPValue() {
        return actualXP;
    }
    @Override
    public String toString() {
        return getPrintableXP();
    }
    public boolean isZero() {
        return actualXP==0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof XP)) return false;
        XP xp = (XP) o;
        return actualXP == xp.actualXP;
    }

    /**
     * Returns whether this XP drop ROUGHLY matches the xp given.
     * There can be a difference between the two of up to 1.0 (10) to account for
     * server rounding values.
     * @param xp
     * @return
     */
    public boolean roughEquals(XP xp) {
        return (Math.abs(actualXP-xp.actualXP))<=10;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(actualXP);
    }
}
