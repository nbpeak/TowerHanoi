package org.nbpeak.game.towerHanoi.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import org.nbpeak.game.towerHanoi.event.StackInEvent;
import org.nbpeak.game.towerHanoi.event.StackOutEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 汉诺塔堆栈，只能通过 {@link #putBlock(Block)} 放入方块，不需要给外部暴露{@link #getChildren()}方法，所以继承{@link #Region}就可以了
 */
public class Stack extends Region {

    private static final double DEFAULT_LINE_HEIGHT = 400;

    /**
     * 内容面板
     */
    private StackPane contentPane;

    /**
     * 存放方块的集合
     */
    private ObservableList<Node> blocks;

    /**
     * 方块开始移动时的点坐标
     */
    private Point2D dragStart;

    /**
     * 拖动方块的代理
     */
    private BlockProxy proxy;

    /**
     * 代理的显示区域
     */
    private VBox proxyBox;

    private StringProperty name = new SimpleStringProperty(this, "name");

    /**
     * 方块开始移动时（鼠标按下）
     */
    private final EventHandler<MouseEvent> blockStartDrag = event -> {
        EventTarget target = event.getTarget();
        if (!(target instanceof Block)) {
            return;
        }

        // 判断当前被点击的方块是不是最顶上的
        Block block = (Block) target;
        Block topBlock = getTopBlock();
        if (!topBlock.equals(block)) {
            return;
        }

        // 创建一个代理，用于随鼠标拖动
        proxy = createProxy(block);
        dragStart = new Point2D(event.getX(), event.getY());
    };

    /**
     * 方块被拖动时
     */
    private final EventHandler<MouseEvent> blockDragged = event -> {
        EventTarget target = event.getTarget();
        if (!(target instanceof Block) || proxy == null) {
            return;
        }

        // 移动代理方块的位置，Translate是相对于控件初始位置移动
        double x = proxy.getTranslateX(), y = proxy.getTranslateY();
        proxy.setTranslateX(x + (event.getX() - dragStart.getX()));
        proxy.setTranslateY(y + (event.getY() - dragStart.getY()));
        dragStart = new Point2D(event.getX(), event.getY());
    };

    /**
     * 方块结束拖动（鼠标释放）
     */
    private EventHandler<? super MouseEvent> blockDragOver = event -> {
        if (proxy == null) {
            return;
        }
        Block block = proxy.getBlock();// 取出被代理的方块
        proxyBox.getChildren().remove(proxy);// 移除掉代理方块
        proxy = null;
        EventTarget target = event.getTarget();
        if (!(target instanceof Block)) {
            return;
        }
        // 触发出栈事件，传入出栈的方块和鼠标指针相对于场景的坐标点
        fireEvent(new StackOutEvent(block, new Point2D(event.getSceneX(), event.getSceneY())));
    };

    public Stack() {
        this("");
    }

    public Stack(String name) {
        super();
        setName(name);
        initialize();
    }

    /**
     * 初始化汉诺塔堆栈
     */
    private void initialize() {
        contentPane = new StackPane();// 最底层的容器
        VBox box1 = createVBox(Pos.BOTTOM_CENTER); // 用于画背景线和汉诺塔标签
        VBox box2 = createStackBox(Pos.BOTTOM_CENTER);// 用于存放方块
        proxyBox = createVBox(Pos.TOP_LEFT);// 显示代理的容器，在最顶上
        proxyBox.setMouseTransparent(true);// 设置代理容器为鼠标穿透，就不会影响到下层方块的点击事件
        box1.setMouseTransparent(true);
        contentPane.setAlignment(Pos.BOTTOM_CENTER);
        Label label = new Label();
        label.textProperty().bind(name);
        label.setTranslateY(30);
        label.setTranslateX(30);
        label.setFont(Font.font(30));
        Line line = new Line();
        line.setTranslateY(-1);
        line.setStrokeWidth(5);
        // 背景区域的尺寸发生了变化，调整中心线的位置
        box1.layoutBoundsProperty().addListener(((observable, oldValue, newValue) -> {
            line.setEndY(getLineHeight(newValue.getHeight() / 3 * 2));
        }));
        box1.getChildren().addAll(label, line);
        box2.setFillWidth(false); // 让容器内的方块宽度不随容器变化
        proxyBox.setFillWidth(false);
        blocks = box2.getChildren();
        contentPane.getChildren().addAll(box1, box2, proxyBox);
        getChildren().add(contentPane);
    }

    private double getLineHeight(double height) {
        double blockHeight = Block.DEFAULT_HEIGHT * Block.getTotalBlockSize() + 50;
        return Math.max(Math.max(height, DEFAULT_LINE_HEIGHT), blockHeight);
    }

    private VBox createVBox(Pos pos) {
        VBox vBox = new VBox();
        vBox.setAlignment(pos);
        return vBox;
    }

    private VBox createStackBox(Pos pos) {
        VBox vBox = new StackBox();
        vBox.setAlignment(pos);
        return vBox;
    }

    private BlockProxy createProxy(Block block) {
        BlockProxy blockProxy = new BlockProxy(block);
        proxyBox.getChildren().add(blockProxy);
        return blockProxy;
    }

    /**
     * 控件尺寸发生变化时，调整子控件的尺寸
     */
    @Override
    protected void layoutChildren() {
        contentPane.resize(getWidth(), getHeight());
    }

    /**
     * 方块放入汉诺塔堆栈
     * @param block
     */
    public void putBlock(Block block) {
        Objects.requireNonNull(block);
        if (blocks.contains(block)) { // 已经存在，不用再放入
            return;
        }

        // 判断要放入的方块是否比堆栈最顶部的方块大
        Block topBlock = getTopBlock();
        if (topBlock != null && topBlock.getNodeNum() < block.getNodeNum()) {
            return;
        }

        setBlockMouseEvent(block); // 重新设置方块在此堆栈中的鼠标事件
        block.addPutOrder(); // 放入顺序+1
        blocks.add(block); // 方块入栈
        fireEvent(new StackInEvent(getBlockSize())); // 触发方块入栈事件
    }

    private void setBlockMouseEvent(Block block) {
        block.setOnMousePressed(blockStartDrag); // 给方块设置鼠标按下事件
        block.setOnMouseDragged(blockDragged); // 给方块设置鼠标拖动事件
        block.setOnMouseReleased(blockDragOver); // 给方块设置鼠标释放事件
    }

    /**
     * 初始化堆栈中的方块
     * @param blockSize 方块的数量
     */
    public void initBlocks(int blockSize) {
        ObservableList<Block> blocks = Block.getBlocks(blockSize);
        blocks.forEach(block -> {
            setBlockMouseEvent(block);
        });
        clear();
        this.blocks.addAll(blocks);
    }

    public void clear() {
        this.blocks.clear();
    }

    public int getBlockSize() {
        return blocks.size();
    }

    /**
     * 获取堆栈中最顶部的方块
     * @return
     */
    private Block getTopBlock() {
        return blocks.stream()
                .map(node -> ((Block) node))
                .max(Comparator.comparingInt(Block::getPutOrder))
                .orElse(null);
    }

    /**
     * 方块入栈事件
     */
    private ObjectProperty<EventHandler<StackInEvent>> onStackIn = new ObjectPropertyBase<EventHandler<StackInEvent>>() {
        @Override
        protected void invalidated() {
            setEventHandler(StackInEvent.STACK_IN, get());
        }

        @Override
        public Object getBean() {
            return Stack.this;
        }

        @Override
        public String getName() {
            return "onStackIn";
        }
    };

    /**
     * 方块出栈事件
     */
    private ObjectProperty<EventHandler<StackOutEvent>> onStackOut = new ObjectPropertyBase<EventHandler<StackOutEvent>>() {
        @Override
        protected void invalidated() {
            setEventHandler(StackOutEvent.STACK_OUT, get());
        }

        @Override
        public Object getBean() {
            return Stack.this;
        }

        @Override
        public String getName() {
            return "onStackOut";
        }
    };

    public EventHandler<StackInEvent> getOnStackIn() {
        return onStackIn.get();
    }

    public ObjectProperty<EventHandler<StackInEvent>> onStackInProperty() {
        return onStackIn;
    }

    public void setOnStackIn(EventHandler<StackInEvent> onStackIn) {
        this.onStackIn.set(onStackIn);
    }

    public EventHandler<StackOutEvent> getOnStackOut() {
        return onStackOut.get();
    }

    public ObjectProperty<EventHandler<StackOutEvent>> onStackOutProperty() {
        return onStackOut;
    }

    public void setOnStackOut(EventHandler<StackOutEvent> onStackOut) {
        this.onStackOut.set(onStackOut);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * 汉诺塔的容器，由于VBox会将最后放入的元素放在最底部，而汉诺塔的方块是最后放入的在最顶部，所以每次显示时需要将容器中的元素重新排序。
     * 排序规则按{@link Block#getPutOrder()}进行倒序排列，方块每次放入汉诺塔堆栈时，会调用{@link Block#addPutOrder()}，
     * 此方法会将方块的排序顺序设置成最大putOrder+1。这样即可保证每次最后放入汉诺塔堆栈的方块在最上面。
     */
    class StackBox extends VBox {
        @Override
        protected List<Block> getManagedChildren() {
            List<Block> managedChildren = super.getManagedChildren();
            managedChildren.sort(Collections.reverseOrder(Comparator.comparingInt(Block::getPutOrder)));// 让最后放入的方块在最上面
            return managedChildren;
        }
    }
}
