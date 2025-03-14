package cn.sunline;

import cn.sunline.excel.ExcelMerger;
import cn.sunline.excel.ExcelSheetSplitter;
import cn.sunline.index.IndexExcelWrite;
import cn.sunline.mapping.*;
import cn.sunline.table.ChineseToEnglishTranslator;
import cn.sunline.table.DdlTemplateFiller;
import cn.sunline.util.BasicInfo;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static cn.sunline.util.ArgsUtil.parseArgs;
import static cn.sunline.util.BasicInfo.verifyLicense;

@Slf4j
public class SwingInterfaceForMain extends JFrame {
    private static final String VERSION = "202503050907";
    private JComboBox<String> dealFunComboBox;
    private JTextField fileNameTextField;
    private JTextField modelFileNameTextField;
    private JTextArea logTextArea;
    private JLabel modelFileNameLabel;
    private static final Map<String, String> CHINESE_TO_ENGLISH = new HashMap<>();
    static {
        CHINESE_TO_ENGLISH.put("物理化", "wlh");
        CHINESE_TO_ENGLISH.put("创建DDL建表语句", "ddl");
        CHINESE_TO_ENGLISH.put("创建DML脚本", "dml");
        CHINESE_TO_ENGLISH.put("接口层映射文档生成", "gen_mapp");
        CHINESE_TO_ENGLISH.put("物理模型初稿生成", "gen_table");
        CHINESE_TO_ENGLISH.put("补充映射文档模板", "supp_mapp");
        CHINESE_TO_ENGLISH.put("更新映射文档模板", "update_mapp");
        CHINESE_TO_ENGLISH.put("获取模型依赖表", "get_rela_tab");
        CHINESE_TO_ENGLISH.put("智能风控系统指标转换成标准模板", "zb");
        CHINESE_TO_ENGLISH.put("EXCEL拆分", "cf");
        CHINESE_TO_ENGLISH.put("EXCEL合并", "hb");
    }

    private static boolean appenderAdded = false;

    public SwingInterfaceForMain() {
        try {
            // 设置 Nimbus 外观和感觉
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("命令执行界面");
        // 将窗口大小调整为当前的两倍
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 创建输入面板
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 设置简洁风字体大小，使用支持中文的字体
        Font font = new Font("微软雅黑", Font.PLAIN, 16);

        JPanel dealFunPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dealFunLabel = new JLabel("选择处理函数 (f):");
        dealFunLabel.setFont(font);
        String[] dealFunOptionsChinese = {
                "物理化", "创建DDL建表语句", "创建DML脚本",
                "接口层映射文档生成", "物理模型初稿生成",
                "补充映射文档模板", "更新映射文档模板",
                "获取模型依赖表", "智能风控系统指标转换成标准模板",
                "EXCEL拆分", "EXCEL合并"
        };
        dealFunComboBox = new JComboBox<>(dealFunOptionsChinese);
        dealFunComboBox.setFont(font);
        dealFunPanel.add(dealFunLabel);
        dealFunPanel.add(dealFunComboBox);
        inputPanel.add(dealFunPanel);

        JPanel fileNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel fileNameLabel = new JLabel("输入 file_name:");
        fileNameLabel.setFont(font);
        fileNameTextField = new JTextField(30);
        fileNameTextField.setFont(font);
        fileNamePanel.add(fileNameLabel);
        fileNamePanel.add(fileNameTextField);
        inputPanel.add(fileNamePanel);

        JPanel modelFileNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modelFileNameLabel = new JLabel("输入 model_file_name:");
        modelFileNameLabel.setFont(font);
        modelFileNameTextField = new JTextField(30);
        modelFileNameTextField.setFont(font);
        modelFileNamePanel.add(modelFileNameLabel);
        modelFileNamePanel.add(modelFileNameTextField);
        // 初始时隐藏 modelFileNameLabel 和 modelFileNameTextField
        modelFileNameLabel.setVisible(false);
        modelFileNameTextField.setVisible(false);
        inputPanel.add(modelFileNamePanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton executeButton = new JButton("执行命令");
        executeButton.setFont(font);
        buttonPanel.add(executeButton);
        inputPanel.add(buttonPanel);

        // 为下拉框添加选择事件监听器
        dealFunComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedOption = (String) dealFunComboBox.getSelectedItem();
                if ("补充映射文档模板".equals(selectedOption)) {
                    modelFileNameLabel.setVisible(true);
                    modelFileNameTextField.setVisible(true);
                } else {
                    modelFileNameLabel.setVisible(false);
                    modelFileNameTextField.setVisible(false);
                }
            }
        });

        // 创建日志显示区域
        logTextArea = new JTextArea(30, 80);
        logTextArea.setFont(font);
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 添加组件到窗口
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 为执行按钮添加事件监听器
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dealFunChinese = (String) dealFunComboBox.getSelectedItem();
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
                Main.main(args);
            }
        });

        // 添加自定义日志追加器
        if (!appenderAdded) {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();
            // 检查是否已经添加过 SwingAppender
            Appender existingAppender = config.getAppender("SWING_APPENDER");
            if (existingAppender == null) {
                PatternLayout layout = PatternLayout.newBuilder().withPattern("%msg%n").build();
                SwingAppender swingAppender = new SwingAppender("SWING_APPENDER", null, layout, false, logTextArea);
                swingAppender.start();
                config.addAppender(swingAppender);
                LoggerConfig rootLoggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
                rootLoggerConfig.removeAppender("SWING_APPENDER"); // 确保先移除，避免重复添加
                rootLoggerConfig.addAppender(swingAppender, Level.ALL, null);
                context.updateLoggers();
                appenderAdded = true;
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingInterfaceForMain frame = new SwingInterfaceForMain();
                frame.setVisible(true);
            }
        });
    }

    // 自定义 Log4j2 日志追加器
    static class SwingAppender extends AbstractAppender {
        private JTextArea textArea;

        protected SwingAppender(String name, org.apache.logging.log4j.core.Filter filter,
                                org.apache.logging.log4j.core.Layout<?> layout, boolean ignoreExceptions,
                                JTextArea textArea) {
            super(name, filter, layout, ignoreExceptions);
            this.textArea = textArea;
        }

        @Override
        public void append(org.apache.logging.log4j.core.LogEvent event) {
            SwingUtilities.invokeLater(() -> {
                String message = new String(getLayout().toByteArray(event));
                textArea.append(message);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }
    }
}    