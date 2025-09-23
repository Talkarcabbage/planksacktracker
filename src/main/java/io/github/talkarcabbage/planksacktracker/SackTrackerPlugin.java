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

    private ScriptPreFired mostRecent2051Fire = null;
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
                }
                break;
            }
            case SCRIPT_ENTRY_ADDED_TO_BUILD_MENU: {//1404
                tracker.addBuildMenuEventEntry(prefireEvent.getScriptEvent().getArguments());
                break;
            }
            case SCRIPT_KEYBIND_PRESS_BUILD_MENU: {//1632
                sackManager.updatePlayerInventory(PlankStorageSet.createFromInventory(client.getItemContainer(InventoryID.INV)));
                var cost = tracker.getPlankCostByUIKeybind(Utils.intFromObjectOrDefault(prefireEvent.getScriptEvent().getArguments()[2], 0));
                if (cost != null) {
                    sackManager.addBuildEventToQueue(new BuildMenuBuildQueueEvent(client.getTickCount(), cost));
                }
                break;
            }
            case SCRIPT_SKILL_MENU_KEYBIND_PREFIRE: {//2051
                log.info("Set the most recent 2051!");
                mostRecent2051Fire = prefireEvent;
                break;
            }
            case SCRIPT_SKILL_MENU_UI_PREFIRE:
                mostRecent2051Fire = null; //We want to invalidate the saved event
                break;
            case SCRIPT_SKILL_MENU_ACTION_CHOSEN: {//2052
                try {
                    // Theoretically, if the 2051 event specifically lines up with the sawmill plank ui options, it should work fine here.
                    // If it doesn't match, it should detect that in the method called and not do anything.
                    // We avoid doing anything if it's null, and we set it to null afterwards to make sure that it doesn't cache the wrong previous event.
                    if (mostRecent2051Fire != null) {
                        handleSawmillKeybindMake((Integer) mostRecent2051Fire.getScriptEvent().getArguments()[4]);
                    }
                } catch (Exception e) {
                    log.info("Cast failed for skill menu/sawmill keybind {}", e.getMessage());
                } finally {
                    log.info("Set mostRecent to null!");
                    mostRecent2051Fire = null;
                }
                break;
            }
            case SCRIPT_CONFIRM_DIALOG_FOR_PLANK_MAKE: {
                if (prefireEvent.getScriptEvent().getArguments().length > 2 && prefireEvent.getScriptEvent().getArguments()[1].toString().contains(" coins to make ")) { // Plank make with confirm button
                    sackManager.setMostRecentAction(PlayerAction.PLANK_MAKE_DIALOG);
                }
                break;
            }
        }
    }



    @Subscribe
    public void onChatMessage(ChatMessage chatEvent) {
        if (chatEvent.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }
        String msg = chatEvent.getMessage();
        if (msg.startsWith("Basic\u00A0planks:")) {
            sackManager.updateContentsFromLog(Utils.parsePlankSackChatLog(msg));
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
                sackManager.handlePlankMakeCast();
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
            PlankStorageSet newInventory = new PlankStorageSet(c.count(ItemID.WOODPLANK), c.count(ItemID.PLANK_OAK), c.count(ItemID.PLANK_TEAK), c.count(ItemID.PLANK_MAHOGANY));
            GenericBuildEvent newBuildEvent = new GenericBuildEvent(sackManager.getMostRecentXPDrop(), newInventory, client.getTickCount());
            sackManager.runQueuedBuild(newBuildEvent); //TODO these are not always MH builds
        } else {
            log.warn("Tried to update plank sack via inventory but it was null!");
        }

    }

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

        if (menuOption.equals(MENU_SAWMILL_MAKE)) {
            handleSawmillClickMake(event);
            log.info("Added a sawmill event");
        }
        //We can call this without issue since if the param is wrong
        //it will just ignore it.

        // MahoganyHomes build
        if (mhCost > 0 && !menuOption.equals(MENU_REMOVE)) {
            sackManager.setMostRecentAction(PlayerAction.CONSTRUCT);
            ItemContainer curInv = client.getItemContainer(InventoryID.INV);
            if (curInv != null) {
                sackManager.updatePlayerInventory(new PlankStorageSet(curInv.count(ItemID.WOODPLANK), curInv.count(ItemID.PLANK_OAK), curInv.count(ItemID.PLANK_TEAK), curInv.count(ItemID.PLANK_MAHOGANY)));
            }
            PlankStorageSet plankCost = sackManager.getProbableMHPlankUsage(mhCost, null);
            if (plankCost.isEmpty()) {
                return;
            }

            switch (menuOption) {
                case Constants.MENU_BUILD_TEXT:
                    sackManager.addBuildEventToQueue(new MahoganyHomesBuildQueueEvent(plankCost, client.getTickCount()));
                    log.info("Added a build event");
                    break;
                case Constants.MENU_REPAIR_TEXT:
                    sackManager.addBuildEventToQueue(new MahoganyHomesRepairQueueEvent(plankCost, client.getTickCount()));
                    log.info("Added a repair event");
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
        log.info("handle sawmill logged");
        var incomingPlanks = PlankStorageSet.emptySet();
        var currentInventory = client.getItemContainer(InventoryID.INV);
        if (currentInventory==null) {
            log.info("Inventory null for sawmill build");
            return; //Why would our inventory not exist though???
        }
        if (typeOfLog==PlankTier.UNKNOWN) {
            log.info(""+typeOfLog);
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

    public void handleSawmillKeybindMake(int plankUIWidgetID) {
        var typeOfLog = PlankTier.UNKNOWN;
        switch (plankUIWidgetID) {
            case PLANK_SAWMILL_ID:
                typeOfLog=PlankTier.PLANK;
                break;
            case OAK_PLANK_SAWMILL_ID:
                typeOfLog=PlankTier.OAK;
                break;
            case TEAK_PLANK_SAWMILL_ID:
                typeOfLog=PlankTier.TEAK;
                break;
            case MAHOGANY_PLANK_SAWMILL_ID:
                typeOfLog=PlankTier.MAHOGANY;
                break;
            default:
                log.warn("Intercepted a sawmill keybind event but found no matching plank!");
        }
        if (typeOfLog==PlankTier.UNKNOWN) return;
        handleSawmillBuild(typeOfLog);
    }

    private void handleSawmillClickMake(MenuOptionClicked event) {
        log.info("Sawmill click:" + event.getParam1() + " " );
        var typeOfLog = PlankTier.UNKNOWN;
        switch (event.getParam1()) {
            case PLANK_SAWMILL_ID:
                typeOfLog = PlankTier.PLANK;
                break;
            case OAK_PLANK_SAWMILL_ID:
                typeOfLog = PlankTier.OAK;
                break;
            case TEAK_PLANK_SAWMILL_ID:
                typeOfLog = PlankTier.TEAK;
                break;
            case MAHOGANY_PLANK_SAWMILL_ID:
                typeOfLog = PlankTier.MAHOGANY;
                break;
            default:
        }
        if (typeOfLog==PlankTier.UNKNOWN) return;
        handleSawmillBuild(typeOfLog);
    }






    private void handlePlankSackClickInteraction(MenuOptionClicked event) {
        if (event.getWidget()==null) return; // We expect a non-null for inventory widgets

        //If we're "use" item but all we did was highlight one, then we know we're not doing anything interesting.
        if (event.getMenuOption().equals(Constants.USE_STRING_OPTION) && event.getMenuAction()!=MenuAction.WIDGET_TARGET_ON_WIDGET) return;
        var useType = getItemUseType(event);
        switch (useType) {
            case USE_SACK_WITH_PLANK:
            case FILL_SACK:
            case EMPTY_SACK:
                var inv = client.getItemContainer(InventoryID.INV);
                if (inv!=null) log.info(PlankStorageSet.createFromInventory(inv).toPrintableString());
                if (inv!=null) sackManager.setExpectingPlankSackInventoryChangeViaClickingSack(true, PlankStorageSet.createFromInventory(inv));
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
