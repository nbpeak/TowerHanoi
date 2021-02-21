package org.nbpeak.game.towerHanoi.control.skin;

import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.nbpeak.game.towerHanoi.control.Block;

/**
 * 方块的皮肤
 */
public class BlockSkin extends SkinBase<Block> {

    private Label textLabel;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public BlockSkin(Block control) {
        super(control);

        initialize();
        control.requestLayout();
        textLabel.textProperty().bind(control.nodeNumProperty().asString());

        control.setBorder(createBorder());
    }

    private Border createBorder() {
        return new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS));
    }

    private void initialize() {
        textLabel = new Label();
        textLabel.setMouseTransparent(true); // 标签设置成鼠标穿透，防止鼠标在标签上按下时无法触发方块的事件

        getChildren().addAll(textLabel);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
    }
}
