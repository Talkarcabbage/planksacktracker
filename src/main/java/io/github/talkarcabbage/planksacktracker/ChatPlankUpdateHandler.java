package io.github.talkarcabbage.planksacktracker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.util.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ChatPlankUpdateHandler {
    private static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("^([A-Za-z ]+):\\s*(\\d+)$");

    @Getter
    private int serverTick;

    public ChatPlankUpdateHandler(){
    }

    /**
     * Returns whether the current tick is the same one that this handler was initialized on
     * to ensure it only runs for one check cycle.
     * This may or may not be necessary depending on the check-click implementation, but
     * is likely useful as a safeguard against other chat messages.
     * @param currentTick
     * @return
     */
    public boolean isCurrent(int currentTick) {
        return currentTick==serverTick;
    }

    public PlankStorageSet parseChatMessage(String message, int currentTick) {
        if (this.serverTick>0 && currentTick>this.serverTick) {
            log.warn("Encountered a late chat update for plank contents: {}", message);
        } else {
            this.serverTick=currentTick;
        }
        var parsed = parsePlankMessage(message);
        log.info(parsed.getOne());
        switch (parsed.getOne()) {
            case BASIC_PLANK_MESSAGE:
                return PlankStorageSet.createFromTier(parsed.getTwo(), PlankTier.PLANK);
            case OAK_PLANK_MESSAGE:
                return PlankStorageSet.createFromTier(parsed.getTwo(), PlankTier.OAK);
            case TEAK_PLANK_MESSAGE:
                return PlankStorageSet.createFromTier(parsed.getTwo(), PlankTier.TEAK);
            case MAHOGANY_PLANK_MESSAGE:
                return PlankStorageSet.createFromTier(parsed.getTwo(), PlankTier.MAHOGANY);
            case ROSEWOOD_PLANK_MESSAGE:
                return PlankStorageSet.createFromTier(parsed.getTwo(), PlankTier.ROSEWOOD);
            case IRONWOOD_PLANK_MESSAGE:
                return PlankStorageSet.createFromTier(parsed.getTwo(), PlankTier.IRONWOOD);
            case CAMPHOR_PLANK_MESSAGE:
                return PlankStorageSet.createFromTier(parsed.getTwo(), PlankTier.CAMPHOR);
            default:
                // TODO something is funky if we got here. New plank types?
                return PlankStorageSet.emptySet();
        }
    }

    private Pair<String, Integer> parsePlankMessage(String message) {
        var modified = Text.removeTags(message);
        modified = modified.replace('\u00A0', ' ');
        Matcher matcher = CHAT_MESSAGE_PATTERN.matcher(modified.trim());
        if (matcher.matches()) {
            String type = matcher.group(1).trim();
            int amount = Integer.parseInt(matcher.group(2));
            return new Pair<>(type, amount);
        }
        throw new IllegalArgumentException("Invalid plank message format: " + message);
    }

    private static final String BASIC_PLANK_MESSAGE = "Basic planks";
    private static final String OAK_PLANK_MESSAGE = "Oak planks";
    private static final String TEAK_PLANK_MESSAGE = "Teak planks";
    private static final String MAHOGANY_PLANK_MESSAGE = "Mahogany planks";
    private static final String ROSEWOOD_PLANK_MESSAGE = "Rosewood planks";
    private static final String IRONWOOD_PLANK_MESSAGE = "Ironwood planks";
    private static final String CAMPHOR_PLANK_MESSAGE = "Camphor planks";

}
