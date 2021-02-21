package org.nbpeak.game.towerHanoi;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.nbpeak.game.towerHanoi.control.Block;
import org.nbpeak.game.towerHanoi.control.Stack;
import org.nbpeak.game.towerHanoi.event.StackInEvent;
import org.nbpeak.game.towerHanoi.event.StackOutEvent;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GameMain extends Application {

    private final StringProperty stepProperty = new SimpleStringProperty("第 0 步");
    private final AtomicInteger stepCounter = new AtomicInteger();
    private final StringProperty timeProperty = new SimpleStringProperty("00:00.000");
    private final List<Stack> stacks = new ArrayList<>();

    private Stack firstStack;

    private Slider slider;

    private Button button;

    private Timeline timer;

    private IntegerProperty gameStatus = new IntegerPropertyBase() {
        @Override
        protected void invalidated() {
            switch (get()) {
                case 1:// 游戏开始
                    stacks.forEach(stack -> stack.setMouseTransparent(false));
                    button.setText("重来");
                    button.setOnAction(btnResetHandler);
                    slider.setDisable(true);
                    timer = createTimer();
                    timer.play();
                    break;
                case 2:// 重来
                    stepCounter.set(0);
                    stepProperty.setValue("第 0 步");
                    button.setText("开始");
                    timeProperty.setValue("00:00.000");
                    slider.setDisable(false);
                    button.setOnAction(btnStartHandler);
                    stacks.forEach(Stack::clear);
                    stacks.get(0).initBlocks((int) slider.getValue());
                case 3:// 游戏结束
                    stacks.forEach(stack -> stack.setMouseTransparent(true));
                    timer.stop();
                    break;
            }
        }

        @Override
        public Object getBean() {
            return org.nbpeak.game.towerHanoi.GameMain.this;
        }

        @Override
        public String getName() {
            return "gameStatus";
        }
    };

    /**
     * 创建计时器
     * @return
     */
    private Timeline createTimer() {
        AtomicLong counter = new AtomicLong();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1), event -> {
            long val = counter.getAndIncrement();
            String formattedTime = Instant.ofEpochMilli(val).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("mm:ss.SSS"));
            timeProperty.setValue(formattedTime);
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    private final EventHandler<ActionEvent> btnStartHandler = event -> {
        startGame();
    };

    private final EventHandler<ActionEvent> btnResetHandler = event -> {
        resetGame();
    };

    /**
     * 方块出栈时，移入到鼠标所在的堆栈面板
     */
    private final EventHandler<StackOutEvent> stackOutHandler = event -> {
        Point2D point = event.getPoint();
        Stack stack = findStackByPoint(point);
        if (stack == null) {
            return;
        }

        stack.putBlock(event.getBlock());
    };
    private EventHandler<StackInEvent> stackInHandler = event -> {
        if (event.getStackSize() == ((int) slider.getValue())) {// 所有的方块都进入最后一个堆栈面板时，游戏结束
            gameStatus.set(3);
        }
    };

    /**
     * 根据点坐标找堆栈面板
     * @param point
     * @return
     */
    private Stack findStackByPoint(Point2D point) {
        return stacks.stream()
                .filter(stack -> {
                    Point2D stackPoint = stack.localToScene(0, 0);// 获取堆栈面板相对于场景的坐标
                    double width = stack.getWidth();
                    double height = stack.getHeight();
                    double x = stackPoint.getX();
                    double y = stackPoint.getY();
                    double maxX = x + width;
                    double maxY = y + height;
                    return point.getX() >= x && point.getX() <= maxX && point.getY() >= y && point.getY() <= maxY;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 开始游戏
     */
    private void startGame() {
        gameStatus.set(1);
    }

    /**
     * 重置
     */
    private void resetGame() {
        gameStatus.set(2);
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(createContent()));
        stage.setMinHeight(600);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        stage.setMinWidth(Block.MAX_WIDTH * 3 + 100);
        stage.setTitle("汉诺塔 - v1.0");
        stage.show();
    }

    /**
     * 创建内容
     * @return
     */
    private Parent createContent() {
        VBox root = new VBox();
        root.setPadding(new Insets(0, 20, 20, 20));
        HBox topBox = createTop();
        HBox gameBox = createGameScene();
        root.getChildren().addAll(topBox, gameBox);
        return root;
    }

    /**
     * 创建游戏场景
     * @return
     */
    private HBox createGameScene() {
        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.BOTTOM_CENTER);
        VBox.setVgrow(hBox, Priority.ALWAYS);// 让hbox的高度随父容器VBox调整
        String[] labels = new String[]{"A", "B", "C"};
        for (int i = 0; i < 3; i++) {
            Stack stackPane = new Stack(labels[i]);// 堆栈面板
            stackPane.setMinWidth(Block.MAX_WIDTH);
            stackPane.setOnStackOut(stackOutHandler);// 设置块出栈事件
            HBox.setHgrow(stackPane, Priority.ALWAYS);// 让堆栈面板的宽度随父容器HBox调整
            if (i == 0) {
                firstStack = stackPane;
                firstStack.setMouseTransparent(true);// 游戏未开始，鼠标无法操作
                double size = slider.getValue();
                firstStack.initBlocks((int) size);// 初始化第一个堆栈面板中的方块数量
            }
            int finalI = i;
            stackPane.setOnStackIn(event -> {// 设置方块入栈事件
                if (gameStatus.get() == 1) {// 游戏开始了，才记步数
                    stepProperty.setValue("第 " + stepCounter.incrementAndGet() + " 步");
                }
                if (finalI == 2) {// 方块放入最后一个堆栈面板特殊处理
                    stackInHandler.handle(event);
                }
            });
            stacks.add(stackPane);
        }
        hBox.getChildren().addAll(stacks);
        return hBox;
    }

    /**
     * 创建顶部区域
     * @return
     */
    private HBox createTop() {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPrefHeight(50);

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(box, Priority.ALWAYS);
        button = new Button("开始");

        button.setMnemonicParsing(false);
        button.setOnAction(btnStartHandler);
        slider = new Slider(3, 10, 3);
        slider.setBlockIncrement(1);
        slider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            int val1 = oldValue.intValue();
            int val2 = newValue.intValue();
            if (val1 == val2) {
                return;
            }
            firstStack.initBlocks(val2);// 滑块的值有变化时重新调整第一个堆栈面板中的方块数量
        }));
        Label stepLabel = new Label();
        stepLabel.textProperty().bind(stepProperty);
        box.getChildren().addAll(button, slider, stepLabel);

        Label timeLabel = new Label();
        timeLabel.textProperty().bind(timeProperty);
        hBox.getChildren().addAll(box, timeLabel);
        return hBox;
    }
}
