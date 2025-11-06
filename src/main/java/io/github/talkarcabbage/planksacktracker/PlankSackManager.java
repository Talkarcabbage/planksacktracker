package io.github.talkarcabbage.planksacktracker;

import io.github.talkarcabbage.planksacktracker.plankcost.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;

import static io.github.talkarcabbage.planksacktracker.Confidence.*;
import static io.github.talkarcabbage.planksacktracker.SackTrackerPlugin.PLUGIN_GROUP_ID;

@Slf4j
public class PlankSackManager {

    @Inject
    private int expectingSawmillInventoryChangeTimestamp;

    private final SackTrackerPlugin plugin;

    private PlankStorageSet preBuildInventoryContents = new PlankStorageSet(0,0,0,0,0,0,0);
    private final LinkedList<GenericPlankBuildQueueEvent> pendingBuildQueue = new LinkedList<>();

    @Getter
    private PlankStorageSet currentPlankSack = new PlankStorageSet(0,0,0,0,0,0,0);

    //XP value we had at the most recent time we got an XP drop. Initialize to -2 until we have our xp values
    @Setter
    @Getter
    private XP mostRecentXP = new XP(-2);
    //Amount of XP that our XP went up by when we gained XP. Initialize to -1 until we have our xp values. Make sure it's not equal to the other xp at initialization to avoid comparison issues
    @Setter
    @Getter
    private XP mostRecentXPDrop = new XP(-1);

    //Primarily used to put a ? on the sack instead of the assumed contents
    private boolean plankSackContentsNeedManualUpdate = true;

    @Getter
    private boolean expectingPlankSackInventoryChange = false;
    private int expectingPlankSackInventoryChangeTimestamp = -1;

    @Getter
    private boolean expectingSawmillInventoryChange = false;
    private PlankStorageSet expectedSawmillResults = PlankStorageSet.emptySet();

    @Setter
    private PlankTier mostRecentPlankMakeTier = PlankTier.UNKNOWN;

    @Getter
    @Setter
    private int mostRecentChatContinueClick = 0;
    @Getter
    @Setter
    private int mostRecentPlankMakeCastClick = 0;
    @Getter
    @Setter
    private int mostRecentBuildOptionClick = 0;
    @Getter
    @Setter
    private PlayerAction mostRecentAction = PlayerAction.UNKNOWN;

    @Getter
    @Setter
    private PlankTier mahoganyHomesContractTier = PlankTier.UNKNOWN;
    private PlankTier mostRecentlyDetectedUsedPlank = PlankTier.UNKNOWN;
    private PlankTier mostRecentlyGuessedPlankFromXP = PlankTier.UNKNOWN;

    private Confidence plankSackContentConfidence = NONE;

    public PlankSackManager(SackTrackerPlugin plugin) {
        this.plugin = plugin;
    }

    public void setExpectingPlankSackInventoryChangeViaClickingSack(boolean expecting, PlankStorageSet preInventory) {
        preBuildInventoryContents = preInventory;
        expectingPlankSackInventoryChange=expecting;
        expectingPlankSackInventoryChangeTimestamp = plugin.client.getTickCount();
    }

    public void setExpectingSawmillInventoryChange(PlankStorageSet expectedIncoming) {
        this.expectedSawmillResults = expectedIncoming;
        this.expectingSawmillInventoryChange = true;
        this.expectingSawmillInventoryChangeTimestamp = plugin.client.getTickCount();
    }

    /**
     * Update the config entry to represent the new contents of the planksack,
     * primarily for external data sharing with other plugins.
     */
    public void updateConfigPlankSackContents() {
        if (plankSackContentsNeedManualUpdate) {
            plugin.configManager.setRSProfileConfiguration(PLUGIN_GROUP_ID, SackTrackerConfig.PLANK_SACK_TRACKER_CONFIG_SACK_CONTENTS_KEY, new ArrayList<Integer>(0));
            if (plugin.getConfig().replacePlankSackPlugin()) {
                ExternalInteractions.setUnknownDataShareReplacePlankSack(plugin.configManager);
            }
            return;
        }
        ExternalInteractions.setDataShareConfigContents(plugin.configManager, currentPlankSack, plugin);
        ExternalInteractions.setDataShareReplacePlankSack(plugin.configManager, currentPlankSack, plugin);
    }

    /**
     * The only intended method for replacing the contents of the sack.
     * It also updates the current profile config to reflect the new contents.
     * @param newContents
     */
    public void setPlankSackContents(PlankStorageSet newContents) {
        currentPlankSack = newContents;
        updateConfigPlankSackContents();
    }

    /**
     * Call this method to set the new total XP as well as update the most recent XP drop to reflect
     * this new value. This method expects your TOTAL xp from the drop.
     * This method will return true if the XP given seems to reflect an actual xp gain.
     * @param newXPTotal
     */
    public boolean updateXP(XP newXPTotal) {
        if (mostRecentXP.getServerXPValue()<1) { //Probably a first login, but could also be some other issue
            mostRecentXPDrop = new XP(0);
            mostRecentXP = newXPTotal;
            return false;
        } else if (mostRecentXP.equals(mostRecentXPDrop)) { //We got a construction update, but it was 0, so we should probably ignore it entirely
            return false;
        } else {
            mostRecentXPDrop = newXPTotal.subtract(mostRecentXP);
            mostRecentXP = newXPTotal;
            return true;
        }
    }

    /**
     * Updates the inventory to represent based on the given contents provided.
     * Returns the difference between the two inventories as a representation of PlankStorageSet
     * e.g. if two teak planks were used, the result is a PlankStorageSet(0,0,-2,0)
     * This is SPECIFICALLY for the inventory, NOT the planksack.
     * @param newInventoryPlankContents
     * @return
     */
    public PlankStorageSet updatePlayerInventory(PlankStorageSet newInventoryPlankContents) {
        PlankStorageSet returnVal = newInventoryPlankContents.subtract(preBuildInventoryContents);
        preBuildInventoryContents = newInventoryPlankContents;
        return returnVal;
    }

    /**
     * Returns true if the contents of the planksack are such that we don't know
     * for sure if the contents are correct.
     */
    public boolean shouldDisplayQuestionMark() {
        return plankSackContentsNeedManualUpdate;
    }
    public boolean isConfidenceLow() {
        return plankSackContentConfidence != HIGH;
    }
    public void setContentsConfidence(Confidence confidence) {
        this.plankSackContentConfidence = confidence;
    }
    public Confidence getContentsConfidence() {
        return this.plankSackContentConfidence;
    }

    /**
     * Adds a plank build queue to the pending queue events. We generally use this to make sure that our
     * build lines up with what we expect it to cost vs with our xp drop.
     * @param queueEvent
     */
    public void addBuildEventToQueue(GenericPlankBuildQueueEvent queueEvent) {
        pendingBuildQueue.add(queueEvent);
    }

    /**
     * Convenience method that passes in null and 0 for the xp rates, respectively
     * @param plankCount
     * @param newInventory
     * @return
     */
    public Pair<PlankStorageSet, Confidence> getProbableMHPlankUsage(int plankCount, @Nullable PlankStorageSet newInventory) {
        return getProbableMHPlankUsage(plankCount, newInventory,null, 0);
    }

    /**
     * Guesses the planks used for a mahogany homes build, using factors such as inventory changes, xp drops,
     * existing planksack contents, and previous builds.
     * @param plankCount
     * @param newInventory
     * @param xpGained
     * @param xpModifier
     * @return
     */
    public Pair<PlankStorageSet, Confidence> getProbableMHPlankUsage(int plankCount, @Nullable PlankStorageSet newInventory, @Nullable XP xpGained, double xpModifier) {
        //In some circumstances, we will have XP drops available to help determine plank types.
        //If we are provided a new inventory, figure out the difference if any.
        PlankStorageSet diff = null;
        if (newInventory!=null) {
            diff = preBuildInventoryContents.subtract(newInventory);
        }

        // If the player inventory will have changed with this new inventory,
        // It means we almost certainly just used up an inventory plank since
        // The build option should be refreshing the cached inventory contents when clicked
        if (diff!=null && diff.isMonoContent()) {
            return new Pair<>(PlankStorageSet.createFromTier(plankCount, diff.getMonoType()), HIGH);
        }

        //If we know it's only one type of plank in the sack AND we know the contents aren't empty
        if (currentPlankSack.isMonoContent()) {
            return new Pair<>(PlankStorageSet.createFromTier(plankCount, currentPlankSack.getMonoType()), HIGH);
        }

        // If we were provided the XP drop from the build, and it directly lines up with one of the plank types
        if (xpGained!=null) {
            var expected = Utils.expectedMHPlankFromXP(Utils.getCurrentXPModifier(plugin), xpGained);
            if (!expected.equals(PlankTier.UNKNOWN)) {
                return new Pair<>(PlankStorageSet.createFromTier(plankCount, expected), LOW);
            }
        }

        //If we know what the contract is
        mahoganyHomesContractTier = ExternalInteractions.getCurrentMHPluginContractTier(plugin.configManager);
        if (mahoganyHomesContractTier != PlankTier.UNKNOWN) {
            return new Pair<>(PlankStorageSet.createFromTier(plankCount, mahoganyHomesContractTier), LOW);
        }

        //If we only have a certain type of plank in the inventory and the planksack contents don't make it clear
        if (preBuildInventoryContents.isMonoContent()) {
            return new Pair<>(PlankStorageSet.createFromTier(plankCount, preBuildInventoryContents.getMonoType()), NONE);
        }

        //The only things left to go on is what we used up last time...
        if (mostRecentlyDetectedUsedPlank!= PlankTier.UNKNOWN) {
            //mahoganyHomesContractTier = mostRecentlyDetectedUsedPlank;
            return new Pair<>(PlankStorageSet.createFromTier(plankCount, mostRecentlyDetectedUsedPlank), NONE);
        }

        //...Or guessing by xp from previous attempts
        if (mostRecentlyGuessedPlankFromXP != PlankTier.UNKNOWN) {
            return new Pair<>(PlankStorageSet.createFromTier(plankCount, mostRecentlyGuessedPlankFromXP), NONE);
        }

        //If all else fails and we don't even know what we used before
        //plankSackContentsNeedManualUpdate=true;
        return new Pair<>(new PlankStorageSet(0,0,0,0,0,0,0), NONE);
    }

    /**
     * Runs a build event, which
     * - Verifies the event is valid
     * - Checks if the event matches the expected XP drop
     * - Updates the planksack to reflect its new expected contents
     * - Updates the player inventory to reflect the new inventory from the event
     * @param buildEvent
     */
    public void runQueuedBuild(GenericBuildEvent buildEvent) {
        PlankStorageSet newInven = buildEvent.getInventoryAfterBuild();
        GenericPlankBuildQueueEvent mostRecentMatchingQueueEvent;

        // If the pending queue is empty, we're either...
        // A: Dealing with a mahogany homes event that couldn't figure out the right planks, or
        // B: Doing something wonky and going to have to guess the planks anyway.
        if (pendingBuildQueue.isEmpty() || pendingBuildQueue.getLast() instanceof MahoganyHomesBuildQueueEvent || pendingBuildQueue.getLast() instanceof MahoganyHomesRepairQueueEvent) {
            processMahoganyHomesBuildEvent(buildEvent);
            return;
        }

        mostRecentMatchingQueueEvent = pendingBuildQueue.getLast();

        if ((buildEvent.getServerTick() - mostRecentMatchingQueueEvent.getServerTick() > 100)) {
            //It's been over a minute since they clicked on this build option and it still hasn't
            //gone through, so just ignore it and clear the build log
            pendingBuildQueue.clear();
            preBuildInventoryContents = buildEvent.getInventoryAfterBuild();
            return;
        }

        consumePlanksForBuild(newInven, mostRecentMatchingQueueEvent);
    }

    public void handlePlankMakeCast(int vouchers) {
        var amount = (vouchers>0?2:1);
        if (currentPlankSack.getRemainingSackSpace()>0) {
            addToPlankSackAsFits(PlankStorageSet.createFromTier(amount, mostRecentPlankMakeTier));
        }
    }

    private void processMahoganyHomesBuildEvent(GenericBuildEvent buildEvent) {
        //TODO confidence implementation
        PlankStorageSet newInven = buildEvent.getInventoryAfterBuild();
        GenericPlankBuildQueueEvent mostRecentMatchingQueueEvent;
        boolean xpMatches = false;

        if (buildEvent.getXPDrop().isZero()) {
            return; //Left open-ended in case we need to cover 0-xp builds in the future
        }
        if (pendingBuildQueue.isEmpty()) {
            if (buildEvent.getXPDrop().roughEquals(Constants.MAHOGANY_HOMES_STEEL_REPAIR_XP.multiply(Utils.getCurrentXPModifier(plugin)))) {
                // Range repair, probably.
                return;
            }
            if (plugin.client.getTickCount()-mostRecentChatContinueClick<3 || mostRecentAction==PlayerAction.TALK) {
                // Just did a conversation, not a build, so probably claiming XP from MH
                return;
            }
            if (!buildEvent.getInventoryAfterBuild().equals(preBuildInventoryContents)) plankSackContentsNeedManualUpdate = true;
            if (buildEvent.getInventoryAfterBuild().getTotalPlanks()!= preBuildInventoryContents.getTotalPlanks()) {
                mostRecentlyDetectedUsedPlank = buildEvent.getInventoryAfterBuild().subtract(preBuildInventoryContents).getMonoType();
                mostRecentlyGuessedPlankFromXP = mostRecentlyDetectedUsedPlank;
            } else {
                mostRecentlyGuessedPlankFromXP = Utils.expectedMHPlankFromXP(Utils.getCurrentXPModifier(plugin), buildEvent.getXPDrop());
            }
            var guessedCostByXP = Utils.expectedCostFromXP(1, buildEvent.getXPDrop(), currentPlankSack);
            if (guessedCostByXP.isMonoContent()) {
                mostRecentMatchingQueueEvent = new MahoganyHomesBuildQueueEvent(guessedCostByXP, buildEvent.getServerTick(), LOW);
            } else {
                plankSackContentsNeedManualUpdate = true;
                return;
            }
        } else {
            mostRecentMatchingQueueEvent = pendingBuildQueue.getLast(); //Making sure it's initialized
        }

        if ((buildEvent.getServerTick() - mostRecentMatchingQueueEvent.getServerTick() > 100)) {
            //It's been over a minute since they clicked on this build option and it still hasn't
            //gone through, so just ignore it and clear the build log
            pendingBuildQueue.clear();
            preBuildInventoryContents = buildEvent.getInventoryAfterBuild();
            return;
        }

        while (!pendingBuildQueue.isEmpty()) {
            mostRecentMatchingQueueEvent = pendingBuildQueue.removeLast();
            if (buildEvent.getXPDrop().roughEquals(mostRecentMatchingQueueEvent.getExpectedXP(Utils.getCurrentXPModifier(plugin)))) {
                xpMatches=true;
                break;
            }
        }
        if (!xpMatches) {
            log.debug("Received a plank build event with no XP match: {} vs expected: {}", buildEvent.getXPDrop(), mostRecentMatchingQueueEvent.getExpectedXP(1));
        }
        var probablePair = getProbableMHPlankUsage(mostRecentMatchingQueueEvent.getPlankCost().getTotalPlanks(), buildEvent.getInventoryAfterBuild());

        PlankStorageSet probableDetected = probablePair.getOne();
        var queueConfidence = mostRecentMatchingQueueEvent.getConfidence();

        PlankTier probableDetectedType = probableDetected.getMonoType();
        PlankTier expectedType = mostRecentMatchingQueueEvent.getPlankCost().getMonoType();

        if (queueConfidence!=HIGH && probableDetectedType!=expectedType) {
            plugin.getChatManager().sendStaticMessage(ChatMessageManager.WRONG_TYPE_DETECTED_MH);
            plankSackContentsNeedManualUpdate = true;
        }

        consumePlanksForBuild(newInven, mostRecentMatchingQueueEvent);
    }

    // The meat and butter part of a build using up planks
    private void consumePlanksForBuild(PlankStorageSet newInventory, GenericPlankBuildQueueEvent queuedEvent) {
        var planksFromSack = PlankStorageSet.emptySet();
        var planksUsedFromInv = preBuildInventoryContents.subtract(newInventory);
        planksFromSack = queuedEvent.getPlankCost().subtract(planksUsedFromInv);
        consumeFromPlankSack(planksFromSack);

        if (currentPlankSack.hasNegativeValues()) {
            plugin.getChatManager().sendStaticMessage(ChatMessageManager.NEGATIVE_PLANKS);
            plankSackContentsNeedManualUpdate=true;
        }
        updatePlayerInventory(newInventory);
        pendingBuildQueue.clear();
        normalizePlankSackContents();
    }

    public void updateContentsFromLog(PlankStorageSet newContents) {
        var diff = currentPlankSack.subtract(newContents);
        if (diff.isMonoContent()) {
            mostRecentlyDetectedUsedPlank = diff.getMonoType();
        }
        setPlankSackContents(newContents);
        plankSackContentsNeedManualUpdate = false;
    }

    /**
     * Used when the 'check' option is clicked to prepare for new contents.
     */
    public void resetPlankSackForChecking() {
        this.currentPlankSack = PlankStorageSet.emptySet();
        this.setContentsConfidence(HIGH);
        this.plankSackContentsNeedManualUpdate=false;
    }

    public void updatePlankTierFromLog(PlankStorageSet newContent) {
        this.currentPlankSack = this.currentPlankSack.add(newContent);
    }

    private void consumeFromPlankSack(PlankStorageSet contentUpdate) {
        setPlankSackContents(this.currentPlankSack.subtract(contentUpdate));
    }

    /**
     * Tries to add to planksack and returns the leftovers
     * ONLY ACCEPTS A MONO-TYPE TO ADD.
     * @param contentUpdate
     * @return
     */
    private PlankStorageSet addToPlankSackAsFits(PlankStorageSet contentUpdate) {
        if (contentUpdate.isEmpty()) return contentUpdate;
        if (contentUpdate.isMonoContent()) {
            var space = currentPlankSack.getRemainingSackSpace(); //Doubles as how many to add
            var planksToAdd = contentUpdate.getTotalPlanks();
            if (space==0) return contentUpdate; //None will go in
            if (contentUpdate.getTotalPlanks()>space) { //Some won't go in
                var diff = planksToAdd-space; //Leftovers
                addToPlankSack(PlankStorageSet.createFromTier(space, contentUpdate.getMonoType())); //Fill the spaces
                return PlankStorageSet.createFromTier(diff, contentUpdate.getMonoType());
            } else {
                addToPlankSack(contentUpdate);
                return PlankStorageSet.emptySet();
            }
        } else {
            plankSackContentsNeedManualUpdate=true;
            return contentUpdate;
        }
    }

    private void addToPlankSack(PlankStorageSet contentUpdate) {
        setPlankSackContents(currentPlankSack.add(contentUpdate));
    }

    /**
     * Normalize any negative values in the sack and invalidate the planksack as needing refreshed if any of them are.
     * TODO consider putting this into the PlankStorageSet code
     */
    private void normalizePlankSackContents() {
        if (currentPlankSack.getPlanks() < 0) {
            plankSackContentsNeedManualUpdate = true;
            setPlankSackContents(new PlankStorageSet(
                    0,
                    currentPlankSack.getOaks(),
                    currentPlankSack.getTeaks(),
                    currentPlankSack.getMahoganies(),
                    currentPlankSack.getRosewoods(),
                    currentPlankSack.getIronwoods(),
                    currentPlankSack.getCamphors()
            ));
        }
        if (currentPlankSack.getOaks() < 0) {
            plankSackContentsNeedManualUpdate = true;
            setPlankSackContents(new PlankStorageSet(
                    currentPlankSack.getPlanks(),
                    0,
                    currentPlankSack.getTeaks(),
                    currentPlankSack.getMahoganies(),
                    currentPlankSack.getRosewoods(),
                    currentPlankSack.getIronwoods(),
                    currentPlankSack.getCamphors()
            ));
        }
        if (currentPlankSack.getTeaks() < 0) {
            plankSackContentsNeedManualUpdate = true;
            setPlankSackContents(new PlankStorageSet(
                    currentPlankSack.getPlanks(),
                    currentPlankSack.getOaks(),
                    0,
                    currentPlankSack.getMahoganies(),
                    currentPlankSack.getRosewoods(),
                    currentPlankSack.getIronwoods(),
                    currentPlankSack.getCamphors()
            ));
        }
        if (currentPlankSack.getMahoganies() < 0) {
            plankSackContentsNeedManualUpdate = true;
            setPlankSackContents(new PlankStorageSet(
                    currentPlankSack.getPlanks(),
                    currentPlankSack.getOaks(),
                    currentPlankSack.getTeaks(),
                    0,
                    currentPlankSack.getRosewoods(),
                    currentPlankSack.getIronwoods(),
                    currentPlankSack.getCamphors()
            ));
        }
        if (currentPlankSack.getRosewoods() < 0) {
            plankSackContentsNeedManualUpdate = true;
            setPlankSackContents(new PlankStorageSet(
                    currentPlankSack.getPlanks(),
                    currentPlankSack.getOaks(),
                    currentPlankSack.getTeaks(),
                    currentPlankSack.getMahoganies(),
                    0,
                    currentPlankSack.getIronwoods(),
                    currentPlankSack.getCamphors()
            ));
        }
        if (currentPlankSack.getIronwoods() < 0) {
            plankSackContentsNeedManualUpdate = true;
            setPlankSackContents(new PlankStorageSet(
                    currentPlankSack.getPlanks(),
                    currentPlankSack.getOaks(),
                    currentPlankSack.getTeaks(),
                    currentPlankSack.getMahoganies(),
                    currentPlankSack.getRosewoods(),
                    0,
                    currentPlankSack.getCamphors()
            ));
        }
        if (currentPlankSack.getCamphors() < 0) {
            plankSackContentsNeedManualUpdate = true;
            setPlankSackContents(new PlankStorageSet(
                    currentPlankSack.getPlanks(),
                    currentPlankSack.getOaks(),
                    currentPlankSack.getTeaks(),
                    currentPlankSack.getMahoganies(),
                    currentPlankSack.getRosewoods(),
                    currentPlankSack.getIronwoods(),
                    0
            ));
        }
    }


    /**
     * Handles an inventory update when called manually.
     * If a sawmill build is pending, adjusts the planksack accordingly.
     * If the sawmill click was a long time ago, it times out the sawmill proc instead.
     * This is more or less just used for sawmilling
     * @param newInventory
     */
    public void processInventoryChange(PlankStorageSet newInventory) {
        var changes = preBuildInventoryContents.subtract(newInventory);
        log.info("INV update: {} | {} | {} | {}", changes.toPrintableString(), isExpectingPlankSackInventoryChange(), expectingSawmillInventoryChange, expectedSawmillResults.toPrintableString());

        processSackUpdate(changes); // Process inventory update if clicked plank sack

        var sawmillTimeDiff = plugin.client.getTickCount()-expectingSawmillInventoryChangeTimestamp;
        if (expectingSawmillInventoryChange && sawmillTimeDiff<5) {
            addToPlankSackAsFits(expectedSawmillResults);
            expectingSawmillInventoryChange = false;
        } else if (sawmillTimeDiff>=5) {
            expectingSawmillInventoryChange=false;
        }

    }

    /**
     * This method takes in a set of plank updates (positive or negative) and updates the saved storage to reflect it.
     * Probably primarily for when the inventory sack is clicked or etc
     * @param modifications
     */
    public void processSackUpdate(PlankStorageSet modifications) {
        if (plugin.client.getTickCount()-expectingPlankSackInventoryChangeTimestamp>5) {
            // A long time has passed since that click so we probably aren't still waiting for
            // an inventory update from the plank sack click

        } else {
            if (expectingPlankSackInventoryChange) {
                setPlankSackContents(currentPlankSack.add(modifications));
            }
        }
        expectingPlankSackInventoryChange = false;
    }

}
