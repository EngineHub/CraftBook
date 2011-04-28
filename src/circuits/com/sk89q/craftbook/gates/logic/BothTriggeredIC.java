package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public abstract class BothTriggeredIC extends AbstractIC implements SelfTriggeredIC {

    public BothTriggeredIC(Server server, Sign block, boolean selfTriggered, Boolean risingEdge, String title, String signTitle) {
        super(server, block);
        this.risingEdge = risingEdge;
        this.selfTriggered = selfTriggered;
        this.title = title;
        this.signTitle = signTitle;
    }
    
    public BothTriggeredIC(Server server, Sign block, String title, String signTitle) {
        this(server, block, true, null, title, signTitle);
    }
    
    private final Boolean risingEdge;
    private final boolean selfTriggered;
    private final String title;
    private final String signTitle;
    
    @Override
    public final boolean isActive() {
        return true;
    }

    @Override
    public final String getTitle() {
        return (this.selfTriggered ? "Self-triggered " : "") + this.title;
    }

    @Override
    public final String getSignTitle() {
        return (this.selfTriggered ? "ST " : "") + this.signTitle;
    }

    @Override
    public final void trigger(ChipState chip) {
        if ((risingEdge != null) || !(risingEdge ^ chip.getInput(0))) {
            this.think(chip);
        }
    }
}
