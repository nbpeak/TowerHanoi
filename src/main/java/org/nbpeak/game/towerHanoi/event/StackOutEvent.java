package org.nbpeak.game.towerHanoi.event;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import org.nbpeak.game.towerHanoi.control.Block;

public class StackOutEvent extends Event {

    public static final EventType<StackOutEvent> STACK_OUT = new EventType<>(Event.ANY, "STACK_OUT");

    private Block block;

    private Point2D point;

    public StackOutEvent(Block block, Point2D point) {
        super(STACK_OUT);
        this.block = block;
        this.point = point;
    }

    public Point2D getPoint() {
        return point;
    }

    public Block getBlock() {
        return block;
    }
}
