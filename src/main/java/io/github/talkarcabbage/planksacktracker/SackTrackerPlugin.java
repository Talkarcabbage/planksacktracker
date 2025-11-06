package io.github.talkarcabbage.planksacktracker;

import com.google.inject.Provides;
import io.github.talkarcabbage.planksacktracker.plankcost.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import static io.github.talkarcabbage.planksacktracker.Confidence.LOW;
import static io.github.talkarcabbage.planksacktracker.Confidence.NONE;
import static io.github.talkarcabbage.planksacktracker.Constants.*;

@Slf4j
@PluginDescriptor(
        name = "Plank Sack Tracker"
)
public class SackTrackerPlugin extends Plugin {

    /*
    For anyone that needs to know while reading through this code:
    The plugin updates the configManager via
    plugin.configManager.setRSProfileConfiguration(PLUGIN_GROUP_ID, SackTrackerConfig.PLANK_SACK_TRACKER_CONFIG_SACK_CONTENTS_KEY, list);
    to reflect the contents of the planksack whenever a change is detected.
    It is cleared if the plugin shuts down.
    It is set to an empty list IF the contents are unknown.
    Else it is in the format: [Planks, Oaks, Teaks, Mahoganies] (even if the particular type of plank is 0)
     */

    @Inject
    protected Client client;
    @Inject
    @Getter
    private SackTrackerConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    ConfigManager configManager;
    private PlankSackManager sackManager = new PlankSackManager(this);
    private PlankSackOverlay overlay = null;
    private ChatMessageManager chatManager = null;

    @Inject
    private TooltipManager tooltipManager;

    public static final String PLUGIN_GROUP_ID = "planksacktracker";

    private ChatPlankUpdateHandler chatUpdateHandler;

    @Override
    protected void startUp() throws Exception {
        sackManager = new PlankSackManager(this);
        overlay = new PlankSackOverlay(this, sackManager, config, tooltipManager );
        overlayManager.add(overlay);
        chatManager = new ChatMessageManager(this, config);
    }

    protected ChatMessageManager getChatManager() {
        return chatManager;
    }

    @Override
    protected void shutDown() throws Exception {
        ExternalInteractions.resetDataShares(configManager, this);
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState().equals(GameState.LOGGED_IN)) {
            sackManager.updateConfigPlankSackContents();
        }
    }

    private DynamicBuildMenuTracker tracker = new DynamicBuildMenuTracker();

    //DEBUG CODE
    Map<Integer, Boolean> firedIDs = new HashMap<>(512);
    Map<Integer, Boolean> postfiredIDs = new HashMap<>(512);

    @Subscribe
    public void onScriptPreFired(ScriptPreFired prefireEvent) {
        switch (prefireEvent.getScriptId()) {
            case SCRIPT_BUILD_MENU_ENTRY_CLICKED: {// 1405
                sackManager.updatePlayerInventory(PlankStorageSet.createFromInventory(client.getItemContainer(InventoryID.INV)));
                // This event appears to fire specifically when constructing in the house.
                // It does not appear to fire for mahogany homes.
                var cost = tracker.getPlankCostByMenuEntry(Utils.intFromObjectOrDefault(prefireEvent.getScriptEvent().getArguments()[3], 0));
                if (cost != null) {
                    sackManager.addBuildEventToQueue(new BuildMenuBuildQueueEvent(client.getTickCount(), cost));
                } else {
                    log.info("Logged a 1405 but it didn't have a cost: {}", prefireEvent.getScriptEvent().getArguments());
                }
                tracker.clearList(); //prevent keybinds from detecting the wrong past events
                break;
            }
            case SCRIPT_KEYBIND_PRESS_BUILD_MENU: {//1632
                sackManager.updatePlayerInventory(PlankStorageSet.createFromInventory(client.getItemContainer(InventoryID.INV)));
                log.debug("1632 args[2] is {}", prefireEvent.getScriptEvent().getArguments()[2]);
                var cost = tracker.getPlankCostByUIKeybind(Utils.intFromObjectOrDefault(prefireEvent.getScriptEvent().getArguments()[2], 0));
                log.debug("Processed a 1632 build event {}", cost.toPrintableString());
                if (cost != null) {
                    log.debug("Added a 1632 build for cost {}", cost.toPrintableString());
                    sackManager.addBuildEventToQueue(new BuildMenuBuildQueueEvent(client.getTickCount(), cost));
                }
                tracker.clearList(); //prevent keybinds from detecting the wrong past events
                break;
            }
            case SCRIPT_ENTRY_ADDED_TO_BUILD_MENU: {//1404
                tracker.addBuildMenuEventEntry(prefireEvent.getScriptEvent().getArguments());
                break;
            }
            case DADDYS_HOME_BUILD_MENU_OPTION_BUILDER_EVENT: { //TODO is this used for other stuff? (this is the daddy's home hotspots menu builder event)
                tracker.addBuildMenuEventEntry(prefireEvent.getScriptEvent().getArguments());
                break;
            }
            case DADDYS_HOME_BUILD_CLICK_EVENT: { //(this is the daddy's home build click event?)
                sackManager.updatePlayerInventory(PlankStorageSet.createFromInventory(client.getItemContainer(InventoryID.INV)));
                // This event appears to fire specifically when constructing in the house.
                // It does not appear to fire for mahogany homes.
                var cost = tracker.getPlankCostByMenuEntry(Utils.intFromObjectOrDefault(prefireEvent.getScriptEvent().getArguments()[3], 0));
                if (cost != null) {
                    sackManager.addBuildEventToQueue(new BuildMenuBuildQueueEvent(client.getTickCount(), cost));
                } else {
                    log.debug("Logged a 7987 but it didn't have a cost: {}", prefireEvent.getScriptEvent().getArguments());
                }
                break;
            }
            case SCRIPT_SKILL_MENU_KEYBIND_PREFIRE: {//2051
                log.debug("Setting the most recent skill menu event from a key-press!");
                var args = prefireEvent.getScriptEvent().getArguments();
                try {
                    mostRecentSkillMultiEvent = new SkillMenuActivationEvent(SCRIPT_SKILL_MENU_KEYBIND_PREFIRE, (Integer) args[3], (Integer) args[4] );
                } catch (Exception e){
                    log.debug("Encountered an error parsing a skill-menu keypress");
                    mostRecentSkillMultiEvent = null;
                }
                break;
            }
            case SCRIPT_SKILL_MENU_UI_PREFIRE: //2050
                log.debug("Setting the most recent skill menu event from a click!");
                var args = prefireEvent.getScriptEvent().getArguments();
                try {
                    mostRecentSkillMultiEvent = new SkillMenuActivationEvent(SCRIPT_SKILL_MENU_UI_PREFIRE, (Integer) args[2], (Integer) args[3] );
                } catch (Exception e){
                    log.debug("Encountered an error parsing a skill-menu click");
                    mostRecentSkillMultiEvent = null;
                }
                break;
            case SCRIPT_SKILL_MENU_ACTION_CHOSEN: {//2052
                try {
                    // Theoretically, if the 2051 event specifically lines up with the sawmill plank ui options, it should work fine here.
                    // If it doesn't match, it should detect that in the method called and not do anything.
                    // We avoid doing anything if it's null, and we set it to null afterwards to make sure that it doesn't cache the wrong previous event.
                    if (mostRecentSkillMultiEvent != null && mostRecent2046LooksLikeSawmill) {
                        handleSawmillKeybindMake(mostRecentSkillMultiEvent);
                        log.debug("Handled sawmill make");
                    } else {
                        log.debug("Sawmill check failed: {} | {}", mostRecentSkillMultiEvent, mostRecent2046LooksLikeSawmill);
                    }
                } catch (Exception e) {
                    log.info("Cast failed for skill menu/sawmill activation {}", e.getMessage());
                } finally {
                    log.debug("Set mostRecentSkillMulti to null via 2052 handler!");
                    mostRecentSkillMultiEvent = null;
                }
                break;
            }
            case SCRIPT_PREPARE_SKILL_MENU: { // 2046 (skillmulti_setup for filtering sawmill ui specifically as best we can
                try {
                    log.debug("Information from prefire for 2046:" + (String) prefireEvent.getScriptEvent().getArguments()[2]);
                    mostRecent2046LooksLikeSawmill = ((String)prefireEvent.getScriptEvent().getArguments()[2]).contains("Wood - 100gp|Oak - 250gp|Teak - 500gp|Mahogany - 1,500gp|");
                    log.debug("2046 is: {}", mostRecent2046LooksLikeSawmill);
                } catch (Exception e){
                    log.warn("Encountered an issue while detecting whether a sawmill is being used:{}", e.getMessage());
                    mostRecent2046LooksLikeSawmill = false;
                }
            }
            case SCRIPT_CONFIRM_DIALOG_FOR_PLANK_MAKE: { //58
                if (prefireEvent.getScriptEvent().getArguments().length > 2 && prefireEvent.getScriptEvent().getArguments()[1].toString().contains(" coins to make ")) {
                    // Plank make with confirm button. There could be other matches, but it's hard to filter out and
                    // It's unlikely to cause issues since there are other places that detect the plank-make spell anyway
                    sackManager.setMostRecentAction(PlayerAction.PLANK_MAKE_DIALOG);
                }
                break;
            }
        }
    }
    private boolean mostRecent2046LooksLikeSawmill = false;
    private SkillMenuActivationEvent mostRecentSkillMultiEvent;

    @Subscribe
    public void onChatMessage(ChatMessage chatEvent) {
        if (chatEvent.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }
        if (!client.getItemContainer(InventoryID.INV).contains(ItemID.PLANK_SACK)) {
            log.debug("Skipping chat message because no plank sack is present in the inventory");
        }

        String msg = chatEvent.getMessage();

        try {
            var result = chatUpdateHandler.parseChatMessage(msg, client.getTickCount());
            if (result.isMonoContent()) {
                sackManager.updatePlankTierFromLog(result);
            } else {
                log.debug("Received a matching chat update but it didn't resolve to planks!: {}", msg);
            }
        } catch (IllegalArgumentException e) {
            //If it doesn't parse, we just assume it's not a plank message update and move on.
        }
    }

    private int previousMagicXP = 0;
    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (event.getSkill() == Skill.MAGIC) {
            var diff = event.getXp()-previousMagicXP;
            if (diff==0) {
                previousMagicXP = event.getXp();
                return;
            }
            if (client.getLocalPlayer().getAnimation() == AnimationID.DREAM_PLAYER_MAKE_PLANK_SPELL && (sackManager.getMostRecentAction() == PlayerAction.PLANK_MAKE_ANIMATION || sackManager.getMostRecentAction() == PlayerAction.PLANK_MAKE_DIALOG || sackManager.getMostRecentAction() == PlayerAction.PLANK_MAKE)) {
                sackManager.handlePlankMakeCast(numberOfSawmillVouchersAtTimeOfPlankMake);
                numberOfSawmillVouchersAtTimeOfPlankMake--; //Decrement as we autocast. Negative values shouldn't hurt anything.
            }
            previousMagicXP = event.getXp();
        }
        if (event.getSkill() != Skill.CONSTRUCTION) return;
        XP newXP = new XP(event.getXp());
        boolean didXPIncrease = sackManager.updateXP(newXP);
        if (!didXPIncrease) {
            return;
        }

        //It appears that by this point we already have our new inventory contents
        ItemContainer c = client.getItemContainer(InventoryID.INV);

        if (c != null) {
            PlankStorageSet newInventory = new PlankStorageSet(c.count(ItemID.WOODPLANK), c.count(ItemID.PLANK_OAK), c.count(ItemID.PLANK_TEAK), c.count(ItemID.PLANK_MAHOGANY), 0,0,0); //todo FIX WHEN NEW ITEMIDS
            GenericBuildEvent newBuildEvent = new GenericBuildEvent(sackManager.getMostRecentXPDrop(), newInventory, client.getTickCount());
            sackManager.runQueuedBuild(newBuildEvent); //TODO these are not always MH builds
        } else {
            log.warn("Tried to update plank sack via inventory but it was null!");
        }

    }
    private int numberOfSawmillVouchersAtTimeOfPlankMake = 0;
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event.getContainerId()==InventoryID.INV) {
            sackManager.processInventoryChange(PlankStorageSet.createFromInventory(event.getItemContainer()));
        }
    }

    public void handlePlankMakeCastMaybeClicked(MenuOptionClicked event) {
        var itemTargetID = event.getItemId();
        var type = PlankTier.UNKNOWN;

        if (!event.getMenuTarget().contains("Plank Make")) {
            sackManager.setMostRecentAction(PlayerAction.UNKNOWN);
            return;
        }

        switch (itemTargetID) {
            case ItemID.LOGS:
                type= PlankTier.PLANK;
                break;
            case ItemID.OAK_LOGS:
                type = PlankTier.OAK;
                break;
            case ItemID.TEAK_LOGS:
                type = PlankTier.TEAK;
                break;
            case ItemID.MAHOGANY_LOGS:
                type = PlankTier.MAHOGANY;
                break;
            default: // We didn't click a plank
                sackManager.setMostRecentAction(PlayerAction.UNKNOWN);
        }
        sackManager.setMostRecentPlankMakeTier(type);
        sackManager.setMostRecentAction(PlayerAction.PLANK_MAKE);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        int eventID = event.getId();
        int mhCost = Constants.getMahoganyHomesPlankCost(eventID);
        String menuOption = event.getMenuOption();

        if (menuOption.equals(Constants.CAST_MENU_OPTION) && event.getMenuAction()==MenuAction.WIDGET_TARGET_ON_WIDGET) {
            sackManager.setMostRecentAction(PlayerAction.UNKNOWN);
            numberOfSawmillVouchersAtTimeOfPlankMake = client.getItemContainer(InventoryID.INV).count(ItemID.FORESTRY_SAWMILL_VOUCHER); //Why oh why would the inventory be null when we clicked something in it
            handlePlankMakeCastMaybeClicked(event); // Will update from unknown if making planks
        }

        if (menuOption.equals(CONTINUE_MENU_OPTION) || menuOption.equals(TALK_MENU_OPTION)) {
            sackManager.setMostRecentChatContinueClick(client.getTickCount());
            if (sackManager.getMostRecentAction()==PlayerAction.PLANK_MAKE || sackManager.getMostRecentAction()==PlayerAction.PLANK_MAKE_DIALOG || sackManager.getMostRecentAction()==PlayerAction.PLANK_MAKE_ANIMATION) {
                // We should be in a situation here where the player clicked on something
                // In the dialog of plank-make
                sackManager.setMostRecentAction(PlayerAction.PLANK_MAKE_DIALOG);
                if (event.getParam0()==3){
                    sackManager.setMostRecentAction(PlayerAction.UNKNOWN);
                }
            } else {
                sackManager.setMostRecentAction(PlayerAction.TALK);
            }
        } else if (menuOption.equals(MENU_BUILD_TEXT) || menuOption.equals(MENU_REPAIR_TEXT)) {
            sackManager.setMostRecentAction(PlayerAction.CONSTRUCT);
        }

        // MahoganyHomes build
        if (mhCost > 0 && !menuOption.equals(MENU_REMOVE)) {
            sackManager.setMostRecentAction(PlayerAction.CONSTRUCT);
            ItemContainer curInv = client.getItemContainer(InventoryID.INV);
            if (curInv != null) {
                sackManager.updatePlayerInventory(new PlankStorageSet(curInv.count(ItemID.WOODPLANK), curInv.count(ItemID.PLANK_OAK), curInv.count(ItemID.PLANK_TEAK), curInv.count(ItemID.PLANK_MAHOGANY), 0,0,0)); //TODO update when new planks
            }
            var guessedPair = sackManager.getProbableMHPlankUsage(mhCost, null);
            PlankStorageSet plankCost = guessedPair.getOne();
            switch (guessedPair.getTwo()) {
                case HIGH:
                    break;
                case LOW:
                    if (sackManager.getContentsConfidence()!=NONE) {
                        sackManager.setContentsConfidence(LOW);
                    }
                    break;
                case NONE:
                    sackManager.setContentsConfidence(NONE);
                    break;
                default:
                    log.info("Confidence parsing wasn't updated to support:{}", guessedPair.getTwo());
            }
            if (plankCost.isEmpty()) {
                return;
            }

            switch (menuOption) {
                case Constants.MENU_BUILD_TEXT:
                    sackManager.addBuildEventToQueue(new MahoganyHomesBuildQueueEvent(plankCost, client.getTickCount(), guessedPair.getTwo()));
                    log.debug("Added a build event");
                    break;
                case Constants.MENU_REPAIR_TEXT:
                    sackManager.addBuildEventToQueue(new MahoganyHomesRepairQueueEvent(plankCost, client.getTickCount(), guessedPair.getTwo()));
                    log.debug("Added a repair event");
                    break;
                default:
                    log.warn("Build menu option didn't exist for plank sack: " + menuOption);
            }
        }

        if (!(event.getWidget() == null)) {
            if (event.getWidget().getItemId() == ItemID.PLANK_SACK || Utils.isPlank(event.getItemId())) { //Clicked planksack or a plank
                handlePlankSackClickInteraction(event);
            }
        }
    }

    private void handleSawmillBuild(PlankTier typeOfLog) {
        log.debug("handle sawmill logged");
        var incomingPlanks = PlankStorageSet.emptySet();
        var currentInventory = client.getItemContainer(InventoryID.INV);
        if (currentInventory==null) {
            log.debug("Inventory null for sawmill build");
            return; //Why would our inventory not exist though???
        }
        if (typeOfLog==PlankTier.UNKNOWN) {
            log.debug(""+typeOfLog);
            return; // Somewhat redundant, but if this code is reused later it's worth keeping in
        }
        int coins = currentInventory.count(ItemID.COINS);
        int logs;
        var toMake = 0;
        switch (typeOfLog) {
            case PLANK:
                logs = currentInventory.count(ItemID.LOGS);
                toMake = Math.min(logs, (coins/PLANK_GOLD_COST)+currentInventory.count(ItemID.FORESTRY_SAWMILL_VOUCHER));
                incomingPlanks = PlankStorageSet.createFromTier(toMake, PlankTier.PLANK);
                break;
            case OAK:
                logs = currentInventory.count(ItemID.OAK_LOGS);
                toMake = Math.min(logs, (coins/OAK_GOLD_COST)+currentInventory.count(ItemID.FORESTRY_SAWMILL_VOUCHER));
                incomingPlanks = PlankStorageSet.createFromTier(toMake, PlankTier.OAK);
                break;
            case TEAK:
                logs = currentInventory.count(ItemID.TEAK_LOGS);
                toMake = Math.min(logs, (coins/TEAK_GOLD_COST)+currentInventory.count(ItemID.FORESTRY_SAWMILL_VOUCHER));
                incomingPlanks = PlankStorageSet.createFromTier(toMake, PlankTier.TEAK);
                break;
            case MAHOGANY:
                logs = currentInventory.count(ItemID.MAHOGANY_LOGS);
                toMake = Math.min(logs, (coins/MAHOGANY_GOLD_COST)+currentInventory.count(ItemID.FORESTRY_SAWMILL_VOUCHER));
                incomingPlanks = PlankStorageSet.createFromTier(toMake, PlankTier.MAHOGANY);
                break;
        }
        sackManager.setExpectingSawmillInventoryChange(incomingPlanks);
        sackManager.setMostRecentAction(PlayerAction.USING_SAWMILL);
    }

    public void handleSawmillKeybindMake(SkillMenuActivationEvent skillMenuEvent) {
        var typeOfLog = PlankTier.UNKNOWN;
        var itemID = skillMenuEvent.getItemID();
        switch (itemID) {
            case ItemID.LOGS: {
                typeOfLog=PlankTier.PLANK;
                break;
            }
            case ItemID.OAK_LOGS: {
                typeOfLog=PlankTier.OAK;
                break;
            }
            case ItemID.TEAK_LOGS: {
                typeOfLog=PlankTier.TEAK;
                break;
            }
            case ItemID.MAHOGANY_LOGS: {
                typeOfLog=PlankTier.MAHOGANY;
                break;
            }
            default:
                log.warn("Intercepted a sawmill event but found no matching plank! This is a bug!");
        }
        if (typeOfLog==PlankTier.UNKNOWN) return;
        handleSawmillBuild(typeOfLog);
    }

    private void handlePlankSackClickInteraction(MenuOptionClicked event) {
        if (event.getWidget()==null) return; // We expect a non-null for inventory widgets

        //If we're "use" item but all we did was highlight one, then we know we're not doing anything interesting.
        if (event.getMenuOption().equals(Constants.USE_STRING_OPTION) && event.getMenuAction()!=MenuAction.WIDGET_TARGET_ON_WIDGET) return;
        var useType = getItemUseType(event); //TODO MAKE SURE THIS WORKS
        switch (useType) {
            case USE_SACK_WITH_PLANK:
            case FILL_SACK:
            case EMPTY_SACK:
                var inv = client.getItemContainer(InventoryID.INV);
                if (inv!=null) log.debug(PlankStorageSet.createFromInventory(inv).toPrintableString());
                if (inv!=null) sackManager.setExpectingPlankSackInventoryChangeViaClickingSack(true, PlankStorageSet.createFromInventory(inv));
                break;
            case CHECK_SACK:
                log.debug("Logged a check click");
                this.chatUpdateHandler = new ChatPlankUpdateHandler();
                sackManager.resetPlankSackForChecking();
                break;
        }

    }

    private ItemUseType getItemUseType(MenuOptionClicked event) {
        var useType = ItemUseType.UNKNOWN;
        var plankTypeUsed = PlankTier.UNKNOWN;

        if (event.getMenuAction()==MenuAction.WIDGET_TARGET_ON_WIDGET) {
            var selectedWidget = client.getSelectedWidget();
            if (selectedWidget!=null) { //Prevent NPE if somehow the item selected isn't there
                var plankInvolved = (Utils.isPlank(selectedWidget.getItemId())||Utils.isPlank(event.getWidget().getItemId()));
                var sackInvolved = (ItemID.PLANK_SACK == selectedWidget.getItemId() || ItemID.PLANK_SACK== event.getWidget().getItemId());
                useType = (plankInvolved&&sackInvolved)?ItemUseType.USE_SACK_WITH_PLANK:useType;
            }
        }
        if (event.getMenuOption().equals(Constants.FILL_STRING_OPTION)) {
            useType=(event.getItemId()==ItemID.PLANK_SACK)?ItemUseType.FILL_SACK:useType;
        }
        if (event.getMenuOption().equals(Constants.EMPTY_STRING_OPTION)) {
            useType=(event.getItemId()==ItemID.PLANK_SACK)?ItemUseType.EMPTY_SACK:useType;
        }
        if (event.getMenuOption().equals(Constants.CHECK_STRING_OPTION)) {
            useType=(event.getItemId()==ItemID.PLANK_SACK)?ItemUseType.CHECK_SACK:useType;
        }
        return useType;
    }

    private enum ItemUseType {
        USE_SACK_WITH_PLANK,
        FILL_SACK,
        EMPTY_SACK,
        CHECK_SACK,
        UNKNOWN
    }

    @Provides
    SackTrackerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SackTrackerConfig.class);
    }

    //Placeholder incase we need to be able to handle non-xp drop builds later
    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor() instanceof Player && ((Player)event.getActor()).getId()==client.getLocalPlayer().getId()) {
            Player playerActor = (Player)event.getActor();
            if (playerActor.getAnimation()== AnimationID.HUMAN_POH_BUILD) { //Our player is playing the build animation

            }
            if (playerActor.getAnimation() == AnimationID.DREAM_PLAYER_MAKE_PLANK_SPELL) {
                sackManager.setMostRecentAction(PlayerAction.PLANK_MAKE_ANIMATION);
            }
        }
    }

}
