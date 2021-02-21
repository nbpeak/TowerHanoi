package org.nbpeak.game.towerHanoi.control;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.nbpeak.game.towerHanoi.control.skin.BlockSkin;

import java.util.Comparator;
import java.util.Random;

/**
 * 汉诺塔中的方块
 */
public class Block extends Control {

    public final static double DEFAULT_HEIGHT = 30;
    public final static double DEFAULT_MIN_WIDTH = 50;

    public static final int MAX_BLOCK_SIZE = 10;

    private static int TOTAL_BLOCK_SIZE = MAX_BLOCK_SIZE;

    private int putOrder;

    private final static ObservableList<Block> ALL_BLOCKS = FXCollections.observableArrayList();

    public final static double MAX_WIDTH;

    static {// 初始化方块集合，最多10个。
        double w = DEFAULT_MIN_WIDTH;
        double maxW = w;
        for (int i = 0; i < MAX_BLOCK_SIZE; i++) {
            Random random = new Random();
            int index = MAX_BLOCK_SIZE - i;
            Block block = new Block(i + 1, index);
            block.setPrefSize(w, DEFAULT_HEIGHT);
            block.fill(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            ALL_BLOCKS.add(block);
            maxW = w;
            w += 30;
        }
        MAX_WIDTH = maxW;
    }

    /**
     * 方块编号
     */
    private IntegerProperty nodeNum;

    public Block(Integer nodeNum) {
        this(nodeNum, 0);
    }

    public Block(Integer nodeNum, Integer putOrder) {
        super();
        this.setNodeNum(nodeNum);
        this.putOrder = putOrder;
    }

    /**
     * 返回方块的放入顺序
     * @return
     */
    public int getPutOrder() {
        return putOrder;
    }

    /**
     * 在方块集合中找到最大的putOrder，然后在此基础上加1，表示当前方块是最后放入
     */
    public void addPutOrder() {
        putOrder = ALL_BLOCKS.stream().max(Comparator.comparingInt(Block::getPutOrder)).map(Block::getPutOrder).orElse(getPutOrder()) + 1;
    }

    public int getNodeNum() {
        return nodeNum == null ? 0 : nodeNum.get();
    }

    public IntegerProperty nodeNumProperty() {
        if (nodeNum == null) {
            nodeNum = new IntegerPropertyBase() {
                @Override
                public Object getBean() {
                    return Block.this;
                }

                @Override
                public String getName() {
                    return "nodeNum";
                }
            };
        }
        return nodeNum;
    }

    public void setNodeNum(int nodeNum) {
        if (!nodeNumProperty().isBound())
            nodeNumProperty().setValue(nodeNum);
    }

    /**
     * 填充方块颜色
     * @param paint
     */
    public void fill(Paint paint) {
        setBackground(createBackground(paint));
    }

    private Background createBackground(Paint paint) {
        return new Background(new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BlockSkin(this);
    }

    public static int getTotalBlockSize() {
        return TOTAL_BLOCK_SIZE;
    }

    /**
     * 获取指定数量的方块
     * @param size
     * @return
     */
    public static ObservableList<Block> getBlocks(int size) {
        resetOrder();
        ObservableList<Block> list = FXCollections.observableArrayList();
        if (size < MAX_BLOCK_SIZE) {
            list.addAll(ALL_BLOCKS.subList(0, size));
        } else {
            list.addAll(ALL_BLOCKS);
        }
        TOTAL_BLOCK_SIZE = list.size();
        return list;
    }

    /**
     * 重设方块顺序
     */
    private static void resetOrder() {
        for (int i = 0; i < ALL_BLOCKS.size(); i++) {
            int index = MAX_BLOCK_SIZE - i;
            ALL_BLOCKS.get(i).putOrder = index;
        }
    }
}
