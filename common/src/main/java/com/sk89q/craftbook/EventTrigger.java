package com.sk89q.craftbook;
import com.sk89q.worldedit.BlockWorldVector;

public class EventTrigger {

    private BlockWorldVector triggerLocation = null;
    private LocalPlayer triggerPlayer = null;

    public EventTrigger(BlockWorldVector triggerLocation) {

        this.triggerLocation = triggerLocation;
    }

    public EventTrigger(LocalPlayer triggerPlayer) {

        this.triggerPlayer = triggerPlayer;
    }

    public EventTrigger(BlockWorldVector triggerLocation, LocalPlayer triggerPlayer) {

        this.triggerLocation = triggerLocation;
        this.triggerPlayer = triggerPlayer;
    }

    /**
     * @return true if there is a trigger block
     */
    public boolean hasTriggerBlock() {

        return triggerLocation != null;
    }

    /**
     * @param triggerLocation the trigger block's location
     */
    public void setTriggerBlock(BlockWorldVector triggerLocation) {

        this.triggerLocation = triggerLocation;
    }

    /**
     * @return the trigger block
     */
    public BlockWorldVector getTriggerBlock() {

        return triggerLocation;
    }

    /**
     * @return true if there is a trigger player
     */
    public boolean hasTriggerPlayer() {

        return triggerPlayer != null;
    }

    /**
     * @param triggerPlayer the trigger player
     */
    public void setTriggerPlayer(LocalPlayer triggerPlayer) {

        this.triggerPlayer = triggerPlayer;
    }

    /**
     * @return the trigger player
     */
    public LocalPlayer getTriggerPlayer() {

        return triggerPlayer;
    }
}
