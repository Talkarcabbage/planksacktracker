package io.github.talkarcabbage.planksacktracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ChatMessageManager {

    private final SackTrackerPlugin plugin;
    @Inject
    private SackTrackerConfig config;
    private Map<String, Boolean> sentMessages = new HashMap<>();

    public ChatMessageManager(SackTrackerPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(String ID, String message) {
        if (!config.chatInfo()) return;
        if (config.alwaysDisplayWarnings() || !sentMessages.containsKey(ID)) {
            plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
        }
    }
    public void sendStaticMessage(String messageID) {
        if (!config.chatInfo()) return;
        if (config.alwaysDisplayWarnings() || !sentMessages.containsKey(messageID)) {
            plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", messageID, null);
        }
    }

    // Static Messages
    public static final String WARNING_UNKNOWN_PLANK_USED = "";
    public static final String NEGATIVE_PLANKS = "The plank sack tracker detected more planks used than you have. The plugin works best with only one type of plank in the sack. Re-check the contents!";
    public static final String WRONG_TYPE_DETECTED_MH = "The plank sack tracker expected a different plank than it detected was used. The plugin works best with only one type of plank in the sack. Re-check the contents!";

    //IDs
    public static final String WARNING_GUESSED = "GUESSED_PLANKS";
}