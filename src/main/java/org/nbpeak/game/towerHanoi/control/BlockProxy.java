package org.nbpeak.game.towerHanoi.control;

import javafx.geometry.Point2D;

class BlockProxy extends Block {
    private Block block;

    public BlockProxy(Block block) {
        super(block.getNodeNum());
        this.block = block;
//        setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        setBackground(block.getBackground());
        setBorder(block.getBorder());
        setPrefSize(block.getPrefWidth(), block.getPrefHeight());
        Point2D p = block.localToParent(0, 0);
        setTranslateX(p.getX());
        setTranslateY(p.getY());
        setOpacity(0.4);
    }

    public Block getBlock() {
        return block;
    }
}
