package cn.sunline.ui;

import cn.sunline.config.AppConfig;
import cn.sunline.exception.ExceptionHandler;
import cn.sunline.service.CommandFunctionService;
import cn.sunline.vo.Function;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.*;
import java.nio.charset.StandardCharsets;

// 不再使用静态导入

/**
 * JavaFX界面实现
 */
@Slf4j
public class JavaFXInterface extends Application {
    private CommandFunctionService functionService;
    private ComboBox<String> functionComboBox;
    private TextField fileNameField;
    private TextField modelFileNameField;
    private Label modelFileNameLabel;
    private StyleClassedTextArea logArea;
    private Label descriptionLabel; // 功能说明标签
    private static final String APPENDER_NAME = "JavaFXTextAreaAppender";
    private PipedOutputStream pipeOut;
    private PipedInputStream pipeIn;
    private Button browseModelButton;

    @Override
    public void start(Stage primaryStage) {
        // 设置默认字符编码
        System.setProperty("file.encoding", "UTF-8");

        // 初始化基于命令模式的功能服务
        functionService = new CommandFunctionService();

        // 创建界面组件
        VBox root = new VBox(12); // 减少组件间距
        root.setPadding(new Insets(12));
        root.getStyleClass().add("root");

        // 功能面板容器
        VBox functionPanel = new VBox(10);
        functionPanel.getStyleClass().add("panel");
        functionPanel.setPadding(new Insets(12));

        // 功能选择区域（包含下拉框和说明标签）
        HBox functionSelectionBox = new HBox(10);
        functionSelectionBox.setAlignment(Pos.CENTER_LEFT); // 设置垂直居中对齐

        // 功能标签和下拉框
        Label functionLabel = new Label("选择功能:");
        functionLabel.setMinHeight(Control.USE_PREF_SIZE); // 确保标签高度适合内容
        functionLabel.setPrefWidth(100); // 减少标签宽度

        functionComboBox = new ComboBox<>();
        functionComboBox.setPrefWidth(220); // 减少宽度
        functionComboBox.getItems().addAll(functionService.getAllFunctionNames());
        // 增加可见行数，使下拉列表显示更多选项
        functionComboBox.setVisibleRowCount(15);
        functionComboBox.getSelectionModel().selectFirst();

        // 功能说明标签
        descriptionLabel = new Label();
        descriptionLabel.setWrapText(true); // 允许文本换行
        descriptionLabel.setMinHeight(Control.USE_PREF_SIZE); // 确保标签高度适合内容
        descriptionLabel.getStyleClass().add("description-label");
        HBox.setHgrow(descriptionLabel, Priority.ALWAYS); // 让描述标签占据剩余空间

        // 添加到功能选择区域
        functionSelectionBox.getChildren().addAll(functionLabel, functionComboBox, descriptionLabel);

        // 添加选择变化监听器，同时更新模型文件可见性和功能说明
        functionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateModelFileVisibility(newVal);
            updateFunctionDescription(newVal);
        });

        // 添加分隔线
        TilePane separator = new TilePane();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #e0e0e0;");
        separator.setPadding(new Insets(0, 0, 0, 0));
        separator.setMaxWidth(Double.MAX_VALUE);

        // 文件输入区域
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setPadding(new Insets(5, 0, 5, 0));

        // 设置列宽
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(100);
        labelCol.setPrefWidth(100);

        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setFillWidth(true);
        fieldCol.setPrefWidth(220); // 与ComboBox宽度相同

        ColumnConstraints buttonCol = new ColumnConstraints();
        buttonCol.setMinWidth(70);
        buttonCol.setMaxWidth(70);

        inputGrid.getColumnConstraints().addAll(labelCol, fieldCol, buttonCol);

        // 文件名输入框
        Label fileNameLabel = new Label("输入文件名:");
        fileNameField = new TextField();
        fileNameField.setPromptText("请输入文件路径或名称");
        fileNameField.setPrefWidth(220); // 与ComboBox宽度相同

        // 添加浏览按钮
        Button browseButton = new Button("浏览");
        browseButton.setPrefWidth(60);
        browseButton.setOnAction(e -> browseFile(fileNameField));

        // 添加文件输入组件到网格
        inputGrid.add(fileNameLabel, 0, 0);
        inputGrid.add(fileNameField, 1, 0);
        inputGrid.add(browseButton, 2, 0);

        // 模型文件名输入框（可选）
        modelFileNameLabel = new Label("输入模型文件名:");
        modelFileNameField = new TextField();
        modelFileNameField.setPromptText("请输入模型文件路径或名称");
        modelFileNameField.setPrefWidth(220); // 与ComboBox宽度相同

        // 添加模型文件浏览按钮
        browseModelButton = new Button("浏览");
        browseModelButton.setPrefWidth(60);
        browseModelButton.setOnAction(e -> browseFile(modelFileNameField));

        // 添加模型文件输入组件到网格
        inputGrid.add(modelFileNameLabel, 0, 1);
        inputGrid.add(modelFileNameField, 1, 1);
        inputGrid.add(browseModelButton, 2, 1);

        // 执行按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        // 执行按钮
        Button executeButton = new Button("执行");
        executeButton.setPrefWidth(90);
        executeButton.setOnAction(e -> executeFunction());

        buttonBox.getChildren().add(executeButton);

        // 将所有元素添加到功能面板
        functionPanel.getChildren().addAll(functionSelectionBox, separator, inputGrid, buttonBox);

        // 日志区域容器
        VBox logPanel = new VBox(5);
        logPanel.getStyleClass().add("log-panel");
        logPanel.setPadding(new Insets(10));
        VBox.setVgrow(logPanel, Priority.ALWAYS);

        // 日志区域标题
        Label logLabel = new Label("执行日志");
        logLabel.getStyleClass().add("title-label");

        // 日志显示区域 - 使用支持样式的StyleClassedTextArea
        logArea = new StyleClassedTextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(280); // 减少高度
        logArea.setWrapText(false); // 关闭自动换行，使日志内容可以水平滚动
        VBox.setVgrow(logArea, Priority.ALWAYS); // 使日志区域占据剩余空间

        // 应用CSS样式
        logArea.getStylesheets().add(getClass().getResource("/log-styles.css").toExternalForm());
        logArea.getStyleClass().add("log-area");

        // 添加日志组件到日志面板
        logPanel.getChildren().addAll(logLabel, logArea);

        // 添加所有面板到根布局
        root.getChildren().addAll(functionPanel, logPanel);

        // 初始化模型文件输入框的可见性和功能说明
        updateModelFileVisibility(functionComboBox.getValue());
        updateFunctionDescription(functionComboBox.getValue());

        // 设置场景
        int width = AppConfig.getIntProperty("ui.window.width", 800);
        int height = AppConfig.getIntProperty("ui.window.height", 700); // 增加高度
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("/log-styles.css").toExternalForm());

        // 直接使用硬编码标题，避免配置文件编码问题
        primaryStage.setTitle("风险数据集市自动化工具");

        // 设置应用程序图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(getClass().getResourceAsStream("/images/app_icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            log.warn("无法加载应用图标: {}", e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        // 先配置Log4j Appender，再配置控制台捕获
        // 这个顺序很重要，确保Log4j优先配置
        setupLog4jAppender();
        setupConsoleCapture();

        // 输出欢迎信息
        appendToLog("=== 欢迎使用风险数据集市自动化工具 ===\n", false);
        appendToLog("请选择功能并输入必要参数后点击\"执行\"按钮\n\n", false);
    }

    /**
     * 打开文件选择对话框
     */
    private void browseFile(TextField targetField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件");

        // 添加常用文件过滤器
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel文件", "*.xlsx", "*.xls"),
            new FileChooser.ExtensionFilter("CSV文件", "*.csv"),
            new FileChooser.ExtensionFilter("文本文件", "*.txt"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        // 获取上次目录
        String lastDir = AppConfig.getProperty("ui.last.directory");
        if (lastDir != null && !lastDir.isEmpty()) {
            File initialDir = new File(lastDir);
            if (initialDir.exists() && initialDir.isDirectory()) {
                fileChooser.setInitialDirectory(initialDir);
            }
        }

        // 显示对话框
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            targetField.setText(selectedFile.getAbsolutePath());

            // 保存目录供下次使用 - AppConfig没有提供setProperty方法，此处仅保存文件路径到文本框
            //AppConfig.setProperty("ui.last.directory", selectedFile.getParent());
            // 在后续版本可以实现配置的保存功能
        }
    }

    /**
     * 向日志区域添加文本
     */
    private void appendToLog(String message, boolean isError) {
        Platform.runLater(() -> {
            int startPosition = logArea.getLength();
            logArea.appendText(message);
            if (isError) {
                logArea.setStyleClass(startPosition, logArea.getLength(), "error-text");
            } else {
                logArea.setStyleClass(startPosition, logArea.getLength(), "normal-text");
            }
            logArea.moveTo(logArea.getLength());
            logArea.requestFollowCaret();
        });
    }

    /**
     * 配置Log4j日志输出到TextArea
     */
    private void setupLog4jAppender() {
        try {
            // 获取Log4j上下文
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();

            // 创建一个布局，指定日志格式
            PatternLayout layout = PatternLayout.newBuilder()
                    .withPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n")
                    .withCharset(StandardCharsets.UTF_8)
                    .build();

            // 创建自定义的Appender
            TextAreaAppender appender = new TextAreaAppender(APPENDER_NAME, null, layout, logArea);
            appender.start();

            // 检查是否已存在相同名称的Appender，如果有则先移除
            if (config.getAppenders().containsKey(APPENDER_NAME)) {
                config.getRootLogger().removeAppender(APPENDER_NAME);
            }

            // 将Appender添加到配置中
            config.addAppender(appender);

            // 为根Logger添加Appender
            LoggerConfig rootLogger = config.getRootLogger();
            rootLogger.addAppender(appender, Level.ALL, null);

            // 不再需要针对cn.sunline包单独配置，因为log4j2.xml中已注释掉相关配置
            // 所有日志现在都会通过根日志器处理

            // 更新上下文配置
            context.updateLoggers();

            // 直接向logArea添加信息，确保至少能看到这条信息
            //appendToLog("日志系统已初始化 - " + new java.util.Date() + "\n", false);

            //log.info("Log4j日志重定向配置完成");

        } catch (Exception e) {
            // 如果配置失败，至少在UI上显示错误信息
            appendToLog("配置日志重定向失败: " + e.getMessage() + "\n", true);
            e.printStackTrace();
        }
    }

    /**
     * 自定义Log4j Appender，将日志输出到TextArea
     */
    private class TextAreaAppender extends AbstractAppender {
        private final StyleClassedTextArea textArea;

        protected TextAreaAppender(String name,
                                  org.apache.logging.log4j.core.Filter filter,
                                  Layout<? extends Serializable> layout,
                                  StyleClassedTextArea textArea) {
            super(name, filter, layout, true, null);
            this.textArea = textArea;
        }

        @Override
        public void append(LogEvent event) {
            if (textArea != null) {
                try {
                    // 使用布局格式化日志事件
                    final String formattedMessage = new String(getLayout().toByteArray(event));
                    final boolean isError = event.getLevel().equals(org.apache.logging.log4j.Level.ERROR);

                    // 调用辅助方法添加日志，设置适当的颜色
                    appendToLog(formattedMessage, isError);

                } catch (Exception e) {
                    System.err.println("格式化日志事件失败: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 设置控制台输出捕获 - 只捕获标准输出和错误输出，不与Log4j冲突
     */
    private void setupConsoleCapture() {
        try {
            // 创建管道输入/输出流
            pipeOut = new PipedOutputStream();
            pipeIn = new PipedInputStream(pipeOut);

            // 创建并启动读取线程
            Thread reader = new Thread(() -> {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(pipeIn));
                    String line;
                    while ((line = br.readLine()) != null) {
                        final String text = line;
                        // 使用普通样式显示控制台输出，不添加前缀
                        appendToLog(text + "\n", false);
                    }
                } catch (IOException e) {
                    // 忽略管道断开异常 - 这通常发生在应用程序关闭时或执行结束时
                    if (!(e instanceof java.io.InterruptedIOException) &&
                        !e.getMessage().contains("Pipe broken") &&
                        !e.getMessage().contains("Stream closed")) {
                        log.warn("控制台捕获异常: {}", e.getMessage());
                    }
                }
            });
            reader.setDaemon(true);
            reader.setName("Console-Capture-Thread");
            reader.start();

            // 重定向标准输出和错误输出 - 但不覆盖Log4j的配置
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;

            System.setOut(new PrintStream(new TeeOutputStream(originalOut, pipeOut), true));
            System.setErr(new PrintStream(new TeeOutputStream(originalErr, pipeOut), true));
        } catch (Exception e) {
            appendToLog("设置控制台捕获失败: " + e.getMessage() + "\n", true);
            log.error("设置控制台捕获失败", e);
        }
    }

    /**
     * 辅助类：同时输出到两个流，具有更好的错误处理机制
     */
    private static class TeeOutputStream extends OutputStream {
        private final OutputStream out1;
        private final OutputStream out2;

        public TeeOutputStream(OutputStream out1, OutputStream out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        @Override
        public void write(int b) throws IOException {
            try {
                out1.write(b);
            } catch (IOException e) {
                // 忽略第一个流的错误
            }

            try {
                out2.write(b);
            } catch (IOException e) {
                // 忽略第二个流的错误
            }
        }

        @Override
        public void write(byte[] b) throws IOException {
            try {
                out1.write(b);
            } catch (IOException e) {
                // 忽略第一个流的错误
            }

            try {
                out2.write(b);
            } catch (IOException e) {
                // 忽略第二个流的错误
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                out1.write(b, off, len);
            } catch (IOException e) {
                // 忽略第一个流的错误
            }

            try {
                out2.write(b, off, len);
            } catch (IOException e) {
                // 忽略第二个流的错误
            }
        }

        @Override
        public void flush() throws IOException {
            try {
                out1.flush();
            } catch (IOException e) {
                // 忽略第一个流的错误
            }

            try {
                out2.flush();
            } catch (IOException e) {
                // 忽略第二个流的错误
            }
        }

        @Override
        public void close() throws IOException {
            try {
                out1.close();
            } catch (IOException e) {
                // 忽略关闭第一个流的错误
            }

            try {
                out2.close();
            } catch (IOException e) {
                // 忽略关闭第二个流的错误
            }
        }
    }

    /**
     * 根据选择的功能更新模型文件输入框的可见性
     */
    private void updateModelFileVisibility(String functionName) {
        Function function = functionService.getFunction(functionName);
        boolean isVisible = function != null && function.getModelFileNameLabel() != null;
        modelFileNameLabel.setVisible(isVisible);
        modelFileNameLabel.setManaged(isVisible); // 设置managed属性以便在不可见时不占用布局空间
        modelFileNameField.setVisible(isVisible);
        modelFileNameField.setManaged(isVisible);
        browseModelButton.setVisible(isVisible);
        browseModelButton.setManaged(isVisible);

        log.debug("功能[{}]的模型文件输入框可见性设置为: {}", functionName, isVisible);
    }

    /**
     * 更新功能说明标签
     */
    private void updateFunctionDescription(String functionName) {
        Function function = functionService.getFunction(functionName);
        String description = function != null ? function.getFunctionDescriptions() : "";
        descriptionLabel.setText(description);
        log.debug("功能[{}]的说明已更新: {}", functionName, description);
    }

    private void executeFunction() {
        try {
            // 清空日志区域
            logArea.clear();
            appendToLog("===== 开始执行功能 =====\n", false);

            String selectedFunction = functionComboBox.getValue();
            log.info("开始执行功能: {}", selectedFunction);

            String fileName = fileNameField.getText();
            String modelFileName = modelFileNameField.isVisible() ? modelFileNameField.getText() : "";

            // 记录执行参数
            log.info("执行参数 - 文件名: {}, 模型文件名: {}", fileName, modelFileName);

            // 获取功能对象
            Function function = functionService.getFunction(selectedFunction);
            if (function == null) {
                throw new IllegalArgumentException("无效的功能: " + selectedFunction);
            }

            // 调用服务层处理业务逻辑 - 将在单独的线程中执行以避免UI阻塞
            new Thread(() -> {
                try {
                    // 使用MDC记录上下文
                    org.slf4j.MDC.put("ui_function", selectedFunction);

                    // 执行功能
                    functionService.executeFunction(selectedFunction, fileName, modelFileName);

                    Platform.runLater(() -> {
                        log.info("功能执行成功");
                        appendToLog("===== 功能执行成功 =====\n", false);
                    });
                } catch (Exception e) {
                    final String errorMsg = e.getMessage();
                    Platform.runLater(() -> {
                        // 只记录一次错误日志，避免重复
                        log.error("功能执行失败: {}", errorMsg, e);
                        appendToLog("===== 功能执行失败 =====\n", true);
                        appendToLog("错误信息: " + errorMsg + "\n", true);

                        // 保留异常处理
                        ExceptionHandler.handle(e);
                    });
                } finally {
                    // 清理MDC上下文
                    org.slf4j.MDC.remove("ui_function");
                }
            }).start();
        } catch (Exception e) {
            log.error("功能执行准备失败: {}", e.getMessage(), e);
            appendToLog("===== 功能执行准备失败 =====\n", true);
            appendToLog("错误信息: " + e.getMessage() + "\n", true);

            // 保留异常处理
            ExceptionHandler.handle(e);
        }
    }

    @Override
    public void stop() {
        // 应用程序关闭时的清理操作
        try {
            if (pipeOut != null) {
                pipeOut.close();
            }
            if (pipeIn != null) {
                pipeIn.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            super.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 启动应用程序
     */
    public static void main(String[] args) {
        launch(args);
    }
}