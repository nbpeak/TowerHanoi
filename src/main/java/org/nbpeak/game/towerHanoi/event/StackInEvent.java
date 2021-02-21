package org.nbpeak.game.towerHanoi.event;

import javafx.event.Event;
import javafx.event.EventType;

public class StackInEvent extends Event {

    public static final EventType<StackInEvent> STACK_IN = new EventType<>(Event.ANY, "STACK_IN");

    private int stackSize;

    public StackInEvent(int stackSize) {
        super(STACK_IN);
        this.stackSize = stackSize;
    }

    public int getStackSize() {
        return stackSize;
    }
}
