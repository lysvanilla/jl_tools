package cn.sunline;

import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class JavaFXInterfaceForMain extends Application {
    private ComboBox<String> dealFunComboBox;
    private TextField fileNameTextField;
    private TextField modelFileNameTextField;
    private Label descriptionLabel; // 新增的说明标签
    private StyleClassedTextArea logTextArea;
    private Label modelFileNameLabel;
    private Label fileNameLabel; // 声明为类的成员变量
    private static final LinkedHashMap<String, String> CHINESE_TO_ENGLISH = new LinkedHashMap<>();
    private static final Map<String, String> FUNCTION_DESCRIPTIONS = new HashMap<>(); // 功能说明map

    // 添加显式初始化方法以确保地图在UI创建前初始化
    private void initializeMaps() {
        // 仅在映射为空时初始化，避免重复初始化
        if (CHINESE_TO_ENGLISH.isEmpty()) {
            log.info("初始化功能映射...");
            CHINESE_TO_ENGLISH.put("物理化", "wlh");
            CHINESE_TO_ENGLISH.put("物理模型生成DDL建表语句", "ddl");
            CHINESE_TO_ENGLISH.put("映射文档生成DML脚本", "dml");
            CHINESE_TO_ENGLISH.put("接口层物理模型生成映射文档", "gen_mapp");
            CHINESE_TO_ENGLISH.put("映射文档生成物理模型初稿", "gen_table");
            CHINESE_TO_ENGLISH.put("根据物理模型补充映射文档", "supp_mapp");
            CHINESE_TO_ENGLISH.put("更新映射文档到最新模板", "update_mapp");
            CHINESE_TO_ENGLISH.put("根据映射文档获取模型依赖表", "get_rela_tab");
            CHINESE_TO_ENGLISH.put("指标过程Excel文档转换标准模板", "zb");
            CHINESE_TO_ENGLISH.put("EXCEL拆分", "cf");
            CHINESE_TO_ENGLISH.put("EXCEL合并", "hb");
        }

        if (FUNCTION_DESCRIPTIONS.isEmpty()) {
            log.info("初始化功能描述...");
            FUNCTION_DESCRIPTIONS.put("物理化", "将Excel文件中的字段中文翻译为英文，并输出拆词匹配结果");
            FUNCTION_DESCRIPTIONS.put("物理模型生成DDL建表语句", "根据物理模型Excel生成DDL建表语句、简单的insert语句");
            FUNCTION_DESCRIPTIONS.put("映射文档生成DML脚本", "根据映射文档Excel生成DML脚本");
            FUNCTION_DESCRIPTIONS.put("接口层物理模型生成映射文档", "根据接口层表结构生成接口层映射文档");
            FUNCTION_DESCRIPTIONS.put("映射文档生成物理模型初稿", "根据映射文档生成物理模型初稿");
            FUNCTION_DESCRIPTIONS.put("根据物理模型补充映射文档", "根据物理模型的表结构信息，更新映射文档中的字段英文名、过滤条件");
            FUNCTION_DESCRIPTIONS.put("更新映射文档到最新模板", "更新已有的映射文档");
            FUNCTION_DESCRIPTIONS.put("根据映射文档获取模型依赖表", "读取映射文档中的表关联关系中的配置的源表英文名来识别依赖关系并生成Excel");
            FUNCTION_DESCRIPTIONS.put("指标过程Excel文档转换标准模板", "将风控指标转换为行里指标标准格式的模板");
            FUNCTION_DESCRIPTIONS.put("EXCEL拆分", "将Excel文件按规则拆分为多个文件");
            FUNCTION_DESCRIPTIONS.put("EXCEL合并", "将多个Excel文件合并为单一文件");
        }

        // 输出键集以进行调试
        //System.out.println("功能映射键: " + CHINESE_TO_ENGLISH.keySet());
        //System.out.println("功能描述键: " + FUNCTION_DESCRIPTIONS.keySet());
    }

    private static boolean appenderAdded = false;
    private double xOffset = 0;
    private double yOffset = 0;
    private static final int RESIZE_BORDER = 5;

    @Override
    public void start(Stage primaryStage) {
        // 确保映射和描述已经初始化
        initializeMaps();

        VBox inputPanel = createInputPanel();
        logTextArea = createLogTextArea();
        VBox root = createRootLayout(inputPanel, logTextArea);

        addCustomAppender(logTextArea);

        // 创建自定义标题栏
        HBox titleBar = createTitleBar(primaryStage);

        VBox mainLayout = new VBox();
        mainLayout.getChildren().addAll(titleBar, root);

        Scene scene = new Scene(mainLayout, 1200, 800);
        primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // 去除默认标题栏
        primaryStage.setScene(scene);

        // 添加窗口拖动功能
        titleBar.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged((MouseEvent event) -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        // 添加窗口调整大小功能
        scene.setOnMouseMoved((MouseEvent event) -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = scene.getWidth();
            double height = scene.getHeight();
            if (x < RESIZE_BORDER && y < RESIZE_BORDER) {
                scene.setCursor(Cursor.NW_RESIZE);
            } else if (x < RESIZE_BORDER && y > height - RESIZE_BORDER) {
                scene.setCursor(Cursor.SW_RESIZE);
            } else if (x > width - RESIZE_BORDER && y < RESIZE_BORDER) {
                scene.setCursor(Cursor.NE_RESIZE);
            } else if (x > width - RESIZE_BORDER && y > height - RESIZE_BORDER) {
                scene.setCursor(Cursor.SE_RESIZE);
            } else if (x < RESIZE_BORDER) {
                scene.setCursor(Cursor.W_RESIZE);
            } else if (x > width - RESIZE_BORDER) {
                scene.setCursor(Cursor.E_RESIZE);
            } else if (y < RESIZE_BORDER) {
                scene.setCursor(Cursor.N_RESIZE);
            } else if (y > height - RESIZE_BORDER) {
                scene.setCursor(Cursor.S_RESIZE);
            } else {
                scene.setCursor(Cursor.DEFAULT);
            }
        });
        scene.setOnMouseDragged((MouseEvent event) -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            Cursor cursor = scene.getCursor();
            if (cursor == Cursor.NW_RESIZE) {
                primaryStage.setX(primaryStage.getX() + x);
                primaryStage.setWidth(primaryStage.getWidth() - x);
                primaryStage.setY(primaryStage.getY() + y);
                primaryStage.setHeight(primaryStage.getHeight() - y);
            } else if (cursor == Cursor.SW_RESIZE) {
                primaryStage.setX(primaryStage.getX() + x);
                primaryStage.setWidth(primaryStage.getWidth() - x);
                primaryStage.setHeight(y);
            } else if (cursor == Cursor.NE_RESIZE) {
                primaryStage.setY(primaryStage.getY() + y);
                primaryStage.setWidth(x);
                primaryStage.setHeight(primaryStage.getHeight() - y);
            } else if (cursor == Cursor.SE_RESIZE) {
                primaryStage.setWidth(x);
                primaryStage.setHeight(y);
            } else if (cursor == Cursor.W_RESIZE) {
                primaryStage.setX(primaryStage.getX() + x);
                primaryStage.setWidth(primaryStage.getWidth() - x);
            } else if (cursor == Cursor.E_RESIZE) {
                primaryStage.setWidth(x);
            } else if (cursor == Cursor.N_RESIZE) {
                primaryStage.setY(primaryStage.getY() + y);
                primaryStage.setHeight(primaryStage.getHeight() - y);
            } else if (cursor == Cursor.S_RESIZE) {
                primaryStage.setHeight(y);
            }
        });

        primaryStage.show();
    }

    private HBox createTitleBar(Stage primaryStage) {
        HBox titleBar = new HBox();
        titleBar.setPadding(new Insets(10));
        // 修改背景颜色为蓝色
        titleBar.setStyle("-fx-background-color: #007BFF;");
        titleBar.setAlignment(Pos.CENTER_LEFT); // 设置整体对齐方式

        Text titleText = new Text("风险数据集市自动化工具");
        titleText.setFont(Font.font("微软雅黑", 24));
        titleText.setFill(Color.WHITE);

        // 添加一个空的Region作为弹簧，将closeButton推到最右边
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("关闭");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;");
        closeButton.setOnAction(e -> {
            log.info("程序正常退出");
            primaryStage.close();
        });

        titleBar.getChildren().addAll(titleText, spacer, closeButton);
        return titleBar;
    }

    private VBox createInputPanel() {
        VBox inputPanel = new VBox(10);
        inputPanel.setPadding(new Insets(20));

        Font font = Font.font("微软雅黑", 16);

        Label dealFunLabel = new Label("选择处理功能:");
        dealFunLabel.setFont(font);
        ObservableList<String> dealFunOptionsChinese = FXCollections.observableArrayList(
                CHINESE_TO_ENGLISH.keySet()
        );

        dealFunComboBox = new ComboBox<>(dealFunOptionsChinese);
        // 增加可见行数，使下拉列表显示更多选项
        dealFunComboBox.setVisibleRowCount(15); // 显示15行，根据需要可调整
        dealFunComboBox.getSelectionModel().selectFirst();
        // 设置更大的字体和更高的高度
        dealFunComboBox.setStyle("-fx-font-size: 16px;");

        // 创建功能说明标签
        descriptionLabel = new Label();
        descriptionLabel.setFont(font);
        descriptionLabel.setStyle("-fx-text-fill: #555555;");

        // 创建水平布局来放置下拉框和说明
        HBox dealFunBox = new HBox(15);
        dealFunBox.setAlignment(Pos.CENTER_LEFT);
        dealFunBox.getChildren().addAll(dealFunComboBox, descriptionLabel);
        // 添加调试信息
        log.debug("下拉框选项数量: {}",dealFunComboBox.getItems().size());
        log.debug("当前选中项: {}",dealFunComboBox.getValue());

        // 初始显示第一个功能的说明
        updateDescriptionLabel(dealFunComboBox.getValue());

        fileNameLabel = new Label("* 输入待物理化文件file_name:");
        fileNameLabel.setFont(font);
        fileNameTextField = new TextField();
        fileNameTextField.setFont(font);

        modelFileNameLabel = new Label("* 输入物理模型文件model_file_name:");
        modelFileNameLabel.setFont(font);
        modelFileNameTextField = new TextField();
        modelFileNameTextField.setFont(font);
        modelFileNameLabel.setVisible(false);
        modelFileNameLabel.setManaged(false);
        modelFileNameTextField.setVisible(false);
        modelFileNameTextField.setManaged(false);

        dealFunComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if ("根据物理模型补充映射文档".equals(newValue)) {
                changeModelFileNameFieldsStatus(modelFileNameLabel,modelFileNameTextField,true);
            } else {
                changeModelFileNameFieldsStatus(modelFileNameLabel,modelFileNameTextField,false);
            }
            // 更新说明标签
            updateDescriptionLabel(newValue);

            // 根据下拉框的值调整 fileNameLabel 的文字显示
            switch (newValue) {
                case "物理化":
                    fileNameLabel.setText("* 输入待物理化文件file_name:");
                    break;
                case "物理模型生成DDL建表语句":
                    fileNameLabel.setText("* 输入物理模型文件file_name:");
                    break;
                case "映射文档生成DML脚本":
                    fileNameLabel.setText("* 输入映射文档文件或者文件夹file_name:");
                    break;
                case "接口层物理模型生成映射文档":
                    fileNameLabel.setText("* 输入接口层物理模型文件file_name:");
                    break;
                case "映射文档生成物理模型初稿":
                    fileNameLabel.setText("* 输入映射文档文件或者文件夹file_name:");
                    break;
                case "根据物理模型补充映射文档":
                    fileNameLabel.setText("* 输入映射文档文件或者文件夹file_name:");
                    modelFileNameLabel.setText("* 输入物理模型文件model_file_name:");
                    break;
                case "更新映射文档到最新模板":
                    fileNameLabel.setText("* 输入映射文档文件或者文件夹file_name:");
                    break;
                case "根据映射文档获取模型依赖表":
                    fileNameLabel.setText("* 输入映射文档文件称或者文件夹file_name:");
                    break;
                case "指标过程Excel文档转换标准模板":
                    fileNameLabel.setText("* 输入指标过程Excel文件file_name:");
                    break;
                case "EXCEL拆分":
                    fileNameLabel.setText("* 输入待拆分Excel文件file_name:");
                    break;
                case "EXCEL合并":
                    fileNameLabel.setText("* 输入待合并Excel文件file_name:");
                    break;
                default:
                    fileNameLabel.setText("* 输入待物理化文件file_name:");
            }

        });

        Button executeButton = new Button("执行命令");
        executeButton.setFont(font);
        executeButton.setOnAction(e -> {
            try {
                String[] args = getCommandArgs();
                Main.main(args);
            } catch (Exception ex) {
                log.error("执行命令时发生错误: ", ex);
                logTextArea.appendText("执行命令时发生错误: " + ex.getMessage() + "\n");
            }
        });

        inputPanel.getChildren().addAll(dealFunLabel, dealFunBox, fileNameLabel, fileNameTextField,
                modelFileNameLabel, modelFileNameTextField, executeButton);
        return inputPanel;
    }

    // 添加更新说明标签的方法
    private void updateDescriptionLabel(String selectedFunction) {
        String description = FUNCTION_DESCRIPTIONS.getOrDefault(selectedFunction, "");
        descriptionLabel.setText(description);
    }

    private StyleClassedTextArea createLogTextArea() {
        StyleClassedTextArea textArea = new StyleClassedTextArea();
        textArea.getStyleClass().add("log-area");
        textArea.setEditable(false);
        textArea.setPrefHeight(400);
        textArea.setPrefWidth(800);
        
        // 启用水平滚动
        textArea.setWrapText(false);
        
        // 创建ScrollPane并添加textArea
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // 始终显示水平滚动条
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // 需要时显示垂直滚动条
        
        // 添加CSS样式
        textArea.getStylesheets().add(
            JavaFXInterfaceForMain.class.getResource("/log-styles.css").toExternalForm()
        );
        
        return textArea;
    }

    private VBox createRootLayout(VBox inputPanel, StyleClassedTextArea logTextArea) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(inputPanel, logTextArea);
        return root;
    }

    private void changeModelFileNameFieldsStatus(Label modelFileNameLabelVar,TextField modelFileNameTextFieldVar,boolean visibleFlag) {
        modelFileNameLabelVar.setVisible(visibleFlag);
        modelFileNameLabelVar.setManaged(visibleFlag);
        modelFileNameTextFieldVar.setVisible(visibleFlag);
        modelFileNameTextFieldVar.setManaged(visibleFlag);
    }


    private String[] getCommandArgs() {
        String dealFunChinese = dealFunComboBox.getValue();
        String dealFun = CHINESE_TO_ENGLISH.get(dealFunChinese);
        String fileName = fileNameTextField.getText();
        String modelFileName = modelFileNameTextField.getText();

        String[] args = new String[0];
        if (StringUtils.isNotEmpty(fileName) && StringUtils.isNotEmpty(modelFileName)) {
            args = new String[]{"f=" + dealFun, "file_name=" + fileName, "model_file_name=" + modelFileName};
        } else if (StringUtils.isNotEmpty(fileName)) {
            args = new String[]{"f=" + dealFun, "file_name=" + fileName};
        } else {
            args = new String[]{"f=" + dealFun};
        }
        return args;
    }

    private void addCustomAppender(StyleClassedTextArea textArea) {
        if (!appenderAdded) {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();
            // 严格检查 Appender 是否已经存在
            for (Appender appender : config.getAppenders().values()) {
                if (appender.getName().equals("FX_SWING_APPENDER")) {
                    appenderAdded = true;
                    log.debug("FX_SWING_APPENDER already exists, skipping addition.");
                    return;
                }
            }
            
            PatternLayout layout = PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n").build();
            FXSwingAppender swingAppender = new FXSwingAppender("FX_SWING_APPENDER", null, layout, false, textArea);
            swingAppender.start();
            config.addAppender(swingAppender);
            
            LoggerConfig rootLoggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            rootLoggerConfig.addAppender(swingAppender, Level.ALL, null);
            context.updateLoggers();
            appenderAdded = true;
            log.debug("FX_SWING_APPENDER added successfully.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 自定义 Log4j2 日志追加器
    static class FXSwingAppender extends AbstractAppender {
        private StyleClassedTextArea textArea;
        private String lastMessage = "";
        private long lastLogTime = 0;
        private static final long DEDUPLICATION_WINDOW_MS = 50;

        protected FXSwingAppender(String name, org.apache.logging.log4j.core.Filter filter,
                                org.apache.logging.log4j.core.Layout<?> layout, boolean ignoreExceptions,
                                StyleClassedTextArea textArea) {
            super(name, filter, layout, ignoreExceptions);
            this.textArea = textArea;
        }

        @Override
        public void append(org.apache.logging.log4j.core.LogEvent event) {
            String message = new String(getLayout().toByteArray(event));
            long currentTime = System.currentTimeMillis();

            // 避免短时间内的重复消息
            if (message.equals(lastMessage) &&
                    (currentTime - lastLogTime) < DEDUPLICATION_WINDOW_MS) {
                return;
            }
            lastMessage = message;
            lastLogTime = currentTime;

            javafx.application.Platform.runLater(() -> {
                // 获取当前文本长度，用于后续应用样式
                int startIndex = textArea.getLength();
                
                // 追加新消息
                textArea.appendText(message);
                
                // 检查是否为错误消息 - 通过消息内容或日志级别
                boolean isError = message.toLowerCase().contains("error") || 
                                 event.getLevel().equals(Level.ERROR);
                
                // 只对当前添加的消息应用样式
                if (isError) {
                    textArea.setStyleClass(startIndex, textArea.getLength(), "error-text");
                }else{
                    textArea.setStyleClass(startIndex, textArea.getLength(), "normal-text");
                }
                // 注意：不设置normal-text样式，让非错误消息使用默认样式
                
                // 滚动到底部
                textArea.moveTo(textArea.getLength());
                textArea.requestFollowCaret();
            });
        }
    }
}
