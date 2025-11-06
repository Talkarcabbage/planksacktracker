package io.github.talkarcabbage.planksacktracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.util.ColorUtil;

import java.awt.*;

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
