package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public abstract class BothTriggeredIC extends AbstractIC implements SelfTriggeredIC {

    public BothTriggeredIC(Server server, Sign block, boolean selfTriggered, Boolean risingEdge, String title, String signTitle, Integer clock) {
        super(server, block);
        this.risingEdge = risingEdge;
        this.selfTriggered = selfTriggered;
        this.title = title;
        this.signTitle = signTitle;
        this.clock = clock;
        this.triggered = false;
    }
    
    public BothTriggeredIC(Server server, Sign block, boolean selfTriggered, Boolean risingEdge, String title, String signTitle) {
        this(server, block, selfTriggered, risingEdge, title, signTitle, null);
    }
    
    public BothTriggeredIC(Server server, Sign block, String title, String signTitle) {
        this(server, block, true, null, title, signTitle);
    }
    
    private final Boolean risingEdge;
    private final boolean selfTriggered;
    private final String title;
    private final String signTitle;
    private final Integer clock;
    private boolean triggered;
    
    protected abstract void work(ChipState chip);
    
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
        if (this.risingEdge != null) {
            if (this.clock != null) {
                if (this.risingEdge == chip.getInput(this.clock) && !this.triggered) {
                    this.work(new DeclockedChipState(chip, this.clock));
                    this.triggered = true;
                } else if (this.risingEdge != chip.getInput(this.clock)) {
                    this.triggered = false;
                }
            } else {
                this.work(chip);
            }
        }
    }
    
    @Override
    public final void think(ChipState chip) {
        if (this.selfTriggered) {
            this.work(chip);
        }
    }
    
    private static class DeclockedChipState implements ChipState {
        
        private final ChipState state;
        private final Integer clock;
        
        public DeclockedChipState(ChipState state, Integer clock) {
            this.state = state;
            this.clock = clock;
        }
        
        /**
         * Moves all signals one forward if there is a clock pin.
         * @param pin The pin which will be accessed.
         * @return The pin in a declocked state.
         */
        private int declock(int pin) {
            if (pin < this.getInputCount() && this.clock != null && pin > this.clock) {
                return pin--;
            } else {
                return pin;
            }
        }

        @Override
        public boolean get(int pin) {
            //TODO: Maybe unsave? Maybe first input pin ≠ 0!
            return this.state.get(this.declock(pin));
        }

        @Override
        public boolean getInput(int inputIndex) {
            return this.state.getInput(this.declock(inputIndex));
        }

        @Override
        public boolean getOutput(int outputIndex) {
            return this.state.getOutput(outputIndex);
        }

        @Override
        public void set(int pin, boolean value) {
            //TODO: Maybe unsave? Maybe first input pin ≠ 0!
            this.state.set(this.declock(pin), value);
        }

        @Override
        public void setOutput(int outputIndex, boolean value) {
            this.state.setOutput(outputIndex, value);
        }

        @Override
        public boolean isTriggered(int pin) {
            //TODO: Maybe unsave? Maybe first input pin ≠ 0!
            return this.state.isTriggered(this.declock(pin));
        }

        @Override
        public boolean isValid(int pin) {
            //TODO: Maybe unsave? Maybe first input pin ≠ 0!
            return this.state.isValid(this.declock(pin));
        }

        @Override
        public int getInputCount() {
            if (this.clock == null) {
                return this.state.getInputCount();
            } else {
                return this.state.getInputCount() - 1;
            }
        }

        @Override
        public int getOutputCount() {
            return this.state.getOutputCount();
        }
        
    }
}
