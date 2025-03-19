package cn.sunline.ui;

import cn.sunline.config.AppConfig;
import cn.sunline.exception.ExceptionHandler;
import cn.sunline.service.FunctionService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


/**
 * JavaFX界面实现
 */
@Slf4j
public class JavaFXInterface extends Application {
    private FunctionService functionService;
    private ComboBox<String> functionComboBox;
    private TextField fileNameField;
    private TextField modelFileNameField;
    private TextArea logArea;

    @Override
    public void start(Stage primaryStage) {
        // 设置默认字符编码
        System.setProperty("file.encoding", "UTF-8");
        
        functionService = new FunctionService();
        
        // 创建界面组件
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(10));
        
        // 功能选择下拉框
        Label functionLabel = new Label("选择功能:");
        functionComboBox = new ComboBox<>();
        functionComboBox.getItems().addAll(functionService.getAllFunctionNames());
        // 增加可见行数，使下拉列表显示更多选项
        functionComboBox.setVisibleRowCount(15); // 显示15行，根据需要可调整
        functionComboBox.getSelectionModel().selectFirst();
        
        // 文件名输入框
        Label fileNameLabel = new Label("输入文件名:");
        fileNameField = new TextField();
        
        // 模型文件名输入框（可选）
        Label modelFileNameLabel = new Label("输入模型文件名(可选):");
        modelFileNameField = new TextField();
        
        // 执行按钮
        Button executeButton = new Button("执行");
        executeButton.setOnAction(e -> executeFunction());
        
        // 日志显示区域
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(30);
        
        // 添加组件到布局
        root.getChildren().addAll(
            functionLabel, functionComboBox,
            fileNameLabel, fileNameField,
            modelFileNameLabel, modelFileNameField,
            executeButton, logArea
        );
        
        // 设置场景
        int width = AppConfig.getIntProperty("ui.window.width", 800);
        int height = AppConfig.getIntProperty("ui.window.height", 600);
        Scene scene = new Scene(root, width, height);
        
        // 直接使用硬编码标题，避免配置文件编码问题
        primaryStage.setTitle("风险数据集市自动化工具");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        log.info("UI界面初始化完成");
    }
    
    private void executeFunction() {
        try {
            String selectedFunction = functionComboBox.getValue();
            String fileName = fileNameField.getText();
            String modelFileName = modelFileNameField.getText();
            
            // 调用服务层处理业务逻辑
            functionService.executeFunction(selectedFunction, fileName, modelFileName);
            logArea.appendText("执行成功\n");
        } catch (Exception e) {
            logArea.appendText("执行失败: " + e.getMessage() + "\n");
            ExceptionHandler.handle(e);
        }
    }
    
    /**
     * 启动应用程序
     */
    public static void main(String[] args) {
        launch(args);
    }
} 