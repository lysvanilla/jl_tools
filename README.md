# 通用工具集

这是一个用于处理简历文档和数据处理的Java工具集，提供了简历解析、导出、文档处理、网页内容抓取和HTTP客户端等功能。

## 功能特性

### 简历解析 (TableBasedResumeParser)
- 支持解析Word格式的简历文档
- 基于表格结构解析简历信息
- 提取关键信息：基本信息、工作经历、项目经验等
- 支持.docx格式
- 自动识别和提取表格中的结构化数据
- 支持多种表格布局格式

### 简历导出 (ResumeWordExporter)
- 使用poi-tl模板引擎导出简历
- 支持自定义简历模板
- 批量处理多个简历文件
- 自动计算工作年限、任职时间等信息
- 支持项目经验表格的自定义渲染
- 自动处理日期格式和计算
- 支持自定义输出路径和文件名
- 支持项目经验描述自动编号与格式化

### 文档合并工具 (WordDocumentMerger)
- 支持合并多个Word文档
- 保持原始文档格式
- 支持.docx和.doc格式（.doc格式待实现）
- 按文件名排序处理
- 自动添加分页符
- 保持原始文档的样式和格式
- 支持自定义分页符样式

### 表格合并工具 (TableMerger)
- 从多个Word文档中提取表格
- 合并表格到单个文档
- 保持原始表格格式和样式
- 自动添加表格间距
- 支持批量处理
- 保持表格的原始宽度和对齐方式
- 支持自定义表格间距

### 文档内容合并功能
#### 主要类：DocumentBodyMerger
- 功能：合并多个文档的正文内容（包括段落和表格）
- 特性：
  - 同时处理段落和表格内容
  - 保持原始格式和样式
  - 按文档内容顺序合并
  - 支持批量处理
- 格式保持：
  - 段落格式（字体、大小、样式）
  - 表格结构和宽度
  - 文本格式化属性

### ETL 映射工具 (cn.sunline.mapping)
#### 主要类：
- **TableToEtlMapp**: 将物理模型转换为 ETL 映射文档
- **EtlMappToTable**: 将 ETL 映射文档转换为物理模型
- **GenEtlMappExcel**: 生成 ETL 映射 Excel 文档
- **BatchStandardizedModelExcel**: 批量标准化物理模型
- **BatchStandardizedMappExcel**: 批量标准化映射文档

功能特性：
- 支持物理模型与 ETL 映射文档的双向转换
- 自动处理字段映射关系
- 支持批量标准化处理
- 保持原始格式和样式
- 支持自定义模板

### 表格处理工具 (cn.sunline.table)
#### 主要类：
- **ChineseToEnglishTranslator**: 中英文翻译工具
- **DdlTemplateFiller**: DDL 建表语句生成器
- **ExcelTableStructureReader**: Excel 表格结构读取器
- **StandardizedMappingRelationReader**: 标准化映射关系读取器

功能特性：
- 支持中英文翻译
- 自动生成 DDL 建表语句
- 读取和处理 Excel 表格结构
- 处理标准化映射关系

### Excel 处理工具 (cn.sunline.excel)
#### 主要类：
- **ExcelMerger**: Excel 文件合并工具
- **ExcelSheetSplitter**: Excel 工作表拆分工具
- **IndexExcelWrite**: 指标 Excel 写入工具

功能特性：
- 支持多个 Excel 文件合并
- 支持工作表拆分
- 支持指标数据写入
- 保持原始格式和样式

### 用户界面 (cn.sunline.ui)
#### 主要类：
- **JavaFXInterface**: JavaFX 界面实现
- **JavaFXInterfaceForMain**: JavaFX 主界面实现

功能特性：
- 提供图形用户界面
- 支持功能选择和参数配置
- 实时日志显示
- 支持文件选择和处理

### 命令处理 (cn.sunline.command)
#### 主要类：
- **CommandHandler**: 命令处理器
- **BatchCommand**: 批处理命令
- **SingleCommand**: 单次处理命令

功能特性：
- 解析和执行命令行指令
- 支持批量处理多个文件
- 支持单一文件处理
- 命令参数验证和错误处理

### 功能模块 (cn.sunline.function)
#### 主要类：
- **FunctionManager**: 功能管理器
- **FunctionRegistry**: 功能注册表
- **FunctionExecutor**: 功能执行器
- **FunctionContext**: 功能上下文

功能特性：
- 统一功能注册和管理
- 功能调用与执行
- 功能上下文传递
- 功能扩展与插件支持
- 功能调用链管理

### 索引处理 (cn.sunline.index)
#### 主要类：
- **IndexBuilder**: 索引构建器
- **IndexSearcher**: 索引搜索器
- **IndexConfig**: 索引配置
- **IndexDocument**: 索引文档

功能特性：
- 文档索引创建与管理
- 全文搜索支持
- 索引优化与维护
- 批量索引处理
- 高效检索与过滤

### SQLite支持 (cn.sunline.sqlite)
#### 主要类：
- **SqliteManager**: SQLite管理器
- **SqliteConnector**: SQLite连接器
- **SqliteQuery**: SQLite查询
- **SqliteTransaction**: SQLite事务

功能特性：
- 轻量级数据存储
- 事务管理与并发控制
- 数据查询与缓存
- 数据库迁移与更新
- 连接池管理

### 配置管理 (cn.sunline.config)
#### 主要类：
- **AppConfig**: 应用配置管理类
- **DatabaseConfigManager**: 数据库配置管理类

功能特性：
- 集中管理应用配置
- 支持数据库配置
- 配置文件加载和解析
- 配置项访问和修改
- 支持多种类型的配置项获取（字符串、整数、布尔值）
- 支持默认值设置

### 工具类 (cn.sunline.util)
#### 主要类：
- **BasicInfo**: 基础信息工具类
- **StringUtils**: 字符串处理工具类
- **FileUtils**: 文件处理工具类
- **DateUtils**: 日期处理工具类
- **ExcelUtils**: Excel处理工具类
- **WordUtils**: Word文档处理工具类
- **ArgsUtil**: 参数解析工具类

功能特性：
- 提供基础信息和工具方法
- 字符串处理与格式化
- 文件操作与路径处理
- 日期和时间格式化与计算
- Excel文件读写和处理
- Word文档操作与格式化
- 命令行参数解析
- 通用数据转换

### 异常处理 (cn.sunline.exception)
#### 主要类：
- **BusinessException**: 业务异常类

功能特性：
- 统一的异常处理机制
- 业务异常定义和处理
- 错误码管理
- 异常信息传递

### 服务层 (cn.sunline.service)
#### 主要类：
- **FunctionService**: 功能服务类
- **CommandFunctionService**: 命令功能服务类

功能特性：
- 功能执行管理
- 数据处理与转换
- 参数验证和处理
- 错误处理和日志记录
- 功能映射管理

### 数据对象 (cn.sunline.vo)
#### 主要类：
- **EtlMapp**: ETL 映射实体类
- **TableStructure**: 表结构实体类
- **TableFieldInfo**: 表字段信息实体类
- **BatchTaskVO**: 批量任务对象
- **ConfigVO**: 配置对象

功能特性：
- 数据模型定义
- 实体关系映射
- 批量任务配置与管理
- 配置信息存储与获取
- 数据验证和转换
- 数据持久化支持

### 项目经验表格渲染 (ProjectExperienceTablePolicy)
- 功能：动态处理项目经验表格
- 特性：
  - 自动合并单元格
  - 自动调整宽度
  - 保持表格样式
  - 设置垂直居中对齐
  - 支持多行描述文本
  - 自动处理描述序号
  - 保留描述中的换行符
  - 支持自定义渲染规则

## 项目结构

```
src/main/java/
├── cn/
│   ├── resume/
│   │   ├── TableBasedResumeParser.java    # 简历解析器
│   │   ├── ResumeWordExporter.java        # 简历导出器
│   │   ├── ResumeExcelWrite.java          # 简历Excel导出器
│   │   ├── entity/                        # 实体类
│   │   │   ├── Resume.java               # 简历实体
│   │   │   ├── WorkExperience.java       # 工作经历
│   │   │   └── ProjectExperience.java    # 项目经验
│   │   ├── policy/                        # 渲染策略
│   │   │   ├── ProjectExperienceTablePolicy.java  # 项目经验表格渲染策略
│   │   │   └── WorkExperienceTablePolicy.java    # 工作经验表格渲染策略
│   │   └── util/                          # 工具类
│   │       ├── WordDocumentMerger.java    # 文档合并工具
│   │       ├── DocumentBodyMerger.java    # 文档内容合并工具
│   │       └── TableMerger.java          # 表格合并工具
│   └── sunline/
│       ├── Main.java                      # 主程序入口
│       ├── command/                       # 命令处理
│       │   ├── CommandHandler.java       # 命令处理器
│       │   ├── BatchCommand.java         # 批处理命令
│       │   └── SingleCommand.java        # 单次处理命令
│       ├── config/                        # 配置管理
│       │   ├── AppConfig.java            # 应用配置
│       │   └── DatabaseConfigManager.java # 数据库配置
│       ├── constant/                      # 常量定义
│       │   └── AppConstants.java         # 应用常量
│       ├── exception/                     # 异常处理
│       │   └── BusinessException.java    # 业务异常
│       ├── excel/                         # Excel处理
│       │   ├── ExcelMerger.java          # Excel合并
│       │   ├── ExcelSheetSplitter.java   # Excel拆分
│       │   └── IndexExcelWrite.java      # 指标写入
│       ├── function/                      # 功能模块
│       │   ├── FunctionManager.java      # 功能管理器
│       │   ├── FunctionRegistry.java     # 功能注册表
│       │   ├── FunctionExecutor.java     # 功能执行器
│       │   └── FunctionContext.java      # 功能上下文
│       ├── http/                          # HTTP相关
│       │   ├── OAuthClient.java          # OAuth客户端
│       │   ├── HutoolOAuthClient.java    # 基于Hutool的OAuth客户端
│       │   └── WorkflowApiClient.java    # 工作流API客户端
│       ├── index/                         # 索引处理
│       │   ├── IndexBuilder.java         # 索引构建器
│       │   ├── IndexSearcher.java        # 索引搜索器
│       │   ├── IndexConfig.java          # 索引配置
│       │   └── IndexDocument.java        # 索引文档
│       ├── mapping/                       # ETL映射
│       │   ├── TableToEtlMapp.java       # 表转ETL映射
│       │   ├── EtlMappToTable.java       # ETL映射转表
│       │   ├── GenEtlMappExcel.java      # 生成ETL映射Excel
│       │   ├── BatchStandardizedModelExcel.java  # 批量标准化物理模型
│       │   └── BatchStandardizedMappExcel.java   # 批量标准化映射文档
│       ├── service/                       # 服务层
│       │   ├── FunctionService.java      # 功能服务
│       │   └── DataService.java          # 数据服务
│       ├── sqlite/                        # SQLite支持
│       │   ├── SqliteManager.java        # SQLite管理器
│       │   ├── SqliteConnector.java      # SQLite连接器
│       │   ├── SqliteQuery.java          # SQLite查询
│       │   └── SqliteTransaction.java    # SQLite事务
│       ├── table/                         # 表格处理
│       │   ├── ChineseToEnglishTranslator.java  # 中英文翻译
│       │   ├── DdlTemplateFiller.java    # DDL模板填充
│       │   ├── ExcelTableStructureReader.java   # Excel表格结构读取
│       │   └── StandardizedMappingRelationReader.java  # 标准化映射关系读取器
│       ├── ui/                            # 用户界面
│       │   ├── JavaFXInterface.java      # JavaFX界面
│       │   └── JavaFXInterfaceForMain.java # JavaFX主界面
│       ├── util/                          # 工具类
│       │   ├── BasicInfo.java            # 基础信息
│       │   ├── StringUtils.java          # 字符串工具
│       │   ├── FileUtils.java            # 文件工具
│       │   ├── DateUtils.java            # 日期工具
│       │   ├── ExcelUtils.java           # Excel工具
│       │   ├── WordUtils.java            # Word文档工具
│       │   └── ArgsUtil.java             # 参数解析
│       ├── vo/                            # 数据对象
│       │   ├── EtlMapp.java              # ETL映射实体
│       │   ├── TableStructure.java       # 表结构实体
│       │   ├── BatchTaskVO.java          # 批量任务对象
│       │   ├── ConfigVO.java             # 配置对象
│       │   └── TableFieldInfo.java       # 表字段信息
│       └── web/                           # Web相关
│           ├── WebContentReader_HNNX.java  # 湖南农信网页内容读取器
│           ├── WebContentReader_JXNX.java  # 江西农信网页内容读取器
│           ├── WebContentReader_JXYH.java  # 江西银行网页内容读取器
│           └── WebContentReader_SCNX.java  # 四川农信网页内容读取器
└── resources/
    ├── application.properties           # 应用配置文件
    ├── templates/                        # 模板目录
    │   ├── resume/                      # 简历模板
    │   ├── etl/                         # ETL映射模板
    │   └── ddl/                         # DDL模板
    └── images/                          # 图像资源目录
        └── app_icon.png                  # 应用图标
```

## 核心功能详解

### 简历解析功能
#### 主要类：TableBasedResumeParser
- 功能：解析Word格式的简历文档，提取结构化数据
- 支持的信息类型：
  - 基本信息（姓名、学历、专业等）
  - 工作经历（公司、职位、时间等）
  - 项目经验（项目名称、角色、时间等）
- 解析规则：
  - 基于表格结构识别信息
  - 支持多种表格布局
  - 自动处理日期格式

### 简历导出功能
#### 主要类：ResumeWordExporter
- 功能：将解析后的简历数据导出为Word文档
- 模板支持：
  - 自定义模板路径
  - 支持多种标记（{{name}}等）
  - 支持表格渲染策略
- 数据处理：
  - 自动计算工作年限
  - 计算任职时间
  - 处理日期格式
  - 自动处理项目经验描述的序号
  - 保留描述中的换行符
- 批量处理：
  - 支持目录批量处理
  - 自动生成输出文件名
  - 支持多种文件格式

### 文档合并功能
#### 主要类：WordDocumentMerger
- 功能：合并多个Word文档为一个文档
- 特性：
  - 保持原始格式
  - 自动分页
  - 文件排序
- 格式处理：
  - 保持段落样式
  - 保持字体格式
  - 保持表格结构

### 表格合并功能
#### 主要类：TableMerger
- 功能：从多个文档中提取并合并表格
- 特性：
  - 保持表格格式
  - 自动添加间距
  - 支持批量处理
- 格式保持：
  - 表格宽度
  - 单元格样式
  - 文本格式

### 文档内容合并功能
#### 主要类：DocumentBodyMerger
- 功能：合并多个文档的正文内容（包括段落和表格）
- 特性：
  - 同时处理段落和表格内容
  - 保持原始格式和样式
  - 按文档内容顺序合并
  - 支持批量处理
- 格式保持：
  - 段落格式（字体、大小、样式）
  - 表格结构和宽度
  - 文本格式化属性

### 网页内容抓取功能
#### 主要类：
- **WebContentReader_HNNX**: 湖南农信网页内容读取器
- **WebContentReader_JXNX**: 江西农信网页内容读取器
- **WebContentReader_JXYH**: 江西银行网页内容读取器
- **WebContentReader_SCNX**: 四川农信网页内容读取器

功能特性：
- 使用Jsoup解析HTML内容
- 支持批量抓取多页内容
- 自动提取链接和文本
- 支持按条件过滤内容
- 结果以Map形式返回

### HTTP客户端功能
#### 主要类：
- **OAuthClient**: 基于Apache HttpClient的OAuth客户端
- **HutoolOAuthClient**: 基于Hutool的OAuth客户端
- **WorkflowApiClient**: 工作流API客户端

功能特性：
- 支持OAuth 2.0认证
- 支持HTTPS请求
- 自定义SSL配置
- 支持表单参数提交
- 支持异步请求
- 灵活的响应处理

### 功能模块使用
```java
// 注册功能
FunctionRegistry registry = FunctionRegistry.getInstance();
registry.register("exportResume", new ResumeExportFunction());

// 执行功能
FunctionContext context = new FunctionContext();
context.put("inputFile", "path/to/input.docx");
context.put("outputFile", "path/to/output.docx");
FunctionExecutor.execute("exportResume", context);
```

### 命令处理
```java
// 处理单个命令
String[] args = {"--type", "resume", "--input", "path/to/file.docx", "--output", "path/to/output.docx"};
CommandHandler.handle(args);

// 批量处理命令
String[] batchArgs = {"--batch", "--type", "merge", "--dir", "path/to/directory", "--output", "path/to/output.docx"};
BatchCommand.execute(batchArgs);

// 自定义命令
SingleCommand command = new SingleCommand();
command.setType("custom");
command.setInputPath("path/to/input");
command.setOutputPath("path/to/output");
command.execute();
```

### 索引处理
```java
// 创建索引
IndexBuilder builder = new IndexBuilder("path/to/index");
builder.addDocument(new IndexDocument("doc1", "文档内容"));
builder.build();

// 搜索索引
IndexSearcher searcher = new IndexSearcher("path/to/index");
List<IndexDocument> results = searcher.search("查询关键词");
```

### SQL连接操作
```java
// 建立连接
SqliteConnector connector = SqliteManager.getConnector("path/to/database.db");

// 执行查询
SqliteQuery query = connector.createQuery("SELECT * FROM users WHERE name = ?");
query.setParameter(1, "张三");
List<Map<String, Object>> results = query.executeQuery();

// 事务操作
SqliteTransaction transaction = connector.beginTransaction();
try {
    connector.execute("INSERT INTO users (name, age) VALUES (?, ?)", "李四", 30);
    transaction.commit();
} catch (Exception e) {
    transaction.rollback();
}
```

### 服务层操作
```java
// 使用功能服务
FunctionService functionService = new FunctionService();
boolean success = functionService.executeFunction("exportResume", "path/to/input.docx", "path/to/output.docx");

// 使用数据服务
DataService dataService = new DataService();
List<Map<String, Object>> data = dataService.queryData("SELECT * FROM table");
boolean saved = dataService.saveData(data, "targetTable");
```

### 数据对象操作
```java
// 使用批量任务对象
BatchTaskVO task = new BatchTaskVO();
task.setTaskName("数据迁移任务");
task.setSourcePath("path/to/source");
task.setTargetPath("path/to/target");
task.setTaskType("MIGRATION");

// 使用配置对象
ConfigVO config = new ConfigVO();
config.setConfigName("系统配置");
config.setConfigValue("value");
config.setDescription("配置描述");
```

### ETL 映射处理
```java
// 物理模型转 ETL 映射
String filePath = "path/to/physical/model.xlsx";
TableToEtlMapp.tableToEtlMapp(filePath);

// ETL 映射转物理模型
String mappingPath = "path/to/etl/mapping.xlsx";
EtlMappToTable.etlMappToTableMain(mappingPath);
```

### Excel 处理
```java
// Excel 合并
String inputDir = "path/to/excel/files";
String outputPath = "path/to/output.xlsx";
ExcelMerger.mergeExcelFiles(inputDir, outputPath);

// Excel 拆分
String inputFile = "path/to/input.xlsx";
String outputDir = "path/to/output/dir";
ExcelSheetSplitter.splitExcelSheets(inputFile, outputDir);
```

### 表格处理
```java
// 生成 DDL 语句
String inputFile = "path/to/table/structure.xlsx";
String outputPath = "path/to/output.sql";
DdlTemplateFiller.genDdlSql(inputFile, outputPath);

// 中英文翻译
String inputFile = "path/to/input.xlsx";
String outputPath = "path/to/output.xlsx";
ChineseToEnglishTranslator.writeTranslatorExcel(inputFile, outputPath);
```

### 用户界面使用
```java
// 启动 JavaFX 界面
JavaFXInterface.main(new String[]{});

// 启动 JavaFX 主界面
JavaFXInterfaceForMain.main(new String[]{});
```

### 命令行使用
```java
// 单一命令执行
String[] args = new String[]{"--type", "resume", "--input", "path/to/file.docx", "--output", "path/to/output.docx"};
CommandHandler.handle(args);

// 批量处理命令
String[] batchArgs = new String[]{"--batch", "--type", "merge", "--dir", "path/to/directory", "--output", "path/to/output.docx"};
BatchCommand.execute(batchArgs);
```

### 合并表格
```java
String inputDir = "path/to/documents";
String outputPath = "path/to/output.docx";
boolean success = TableMerger.mergeTablesFromDirectory(inputDir, outputPath);
```

### 合并文档内容
```java
String inputDir = "path/to/documents";
String outputPath = "path/to/output.docx";
boolean success = DocumentBodyMerger.mergeBodiesFromDirectory(inputDir, outputPath);
```

### 网页内容抓取
```java
// 湖南农信网页内容抓取
String url = "http://www.hnnxs.com/node/85.jspx";
Map<String, String> linkMap = WebContentReader_HNNX.extractLinksFromPage(url);

// 批量抓取多页内容
int startPage = 1;
int endPage = 10;
Map<String, String> allLinksMap = WebContentReader_SCNX.extractLinksByPage(startPage, endPage);
```

### HTTP客户端使用
```java
// 使用Hutool的OAuth客户端
String url = "https://example.com/oauth/token";
HttpResponse response = HttpRequest.post(url)
        .setSSLProtocol("SSLv3")
        .form("client_id", "your_client_id")
        .form("client_secret", "your_client_secret")
        .form("username", "your_username")
        .form("password", "your_password")
        .form("grant_type", "password")
        .execute();

// 使用工作流API客户端
String accessToken = WorkflowApiClient.getAccessToken();
boolean result = WorkflowApiClient.addTaskDependency(accessToken, "TASK_123", "YES", "TASK_456");
```

## 配置说明

### 简历模板配置
- 模板路径：`D:\projects\jl_tools\template\doc\简历模版.docx`
- 支持的标记：
  - {{name}} - 姓名
  - {{title}} - 职位
  - {{education}} - 学历
  - {{projectExperiences}} - 项目经验
  - 其他自定义标记

### 输出路径配置
- 默认输出目录：`D:\projects\jl_tools\logs\output`
- 文件名格式：`简历_姓名_时间戳.docx`

### 表格合并配置
- 表格间距：500点
- 表格宽度：100%
- 分页符：自动添加

### 应用配置
- 配置文件：`application.properties`
- 主要类：`AppConfig`
- 配置项：
  - 应用名称和版本：`app.name`, `app.version`
  - 日志配置：`log.level`, `log.path`, `log.file.max.size`, `log.file.max.history`
  - 文件路径配置：`file.template.path`, `file.config.path`
  - UI配置：`ui.window.width`, `ui.window.height`, `ui.window.title`
  - 数据库连接信息
  - 其他系统参数

使用示例：
```java
// 获取字符串配置项
String appName = AppConfig.getProperty("app.name");

// 获取带默认值的字符串配置项
String logPath = AppConfig.getProperty("log.path", "logs");

// 获取整数配置项
int windowWidth = AppConfig.getIntProperty("ui.window.width", 800);

// 获取布尔配置项
boolean showSql = AppConfig.getBooleanProperty("db.show.sql", false);
```

### 数据库配置
- 配置文件：`db.setting`
- 配置项：
  - 数据库类型
  - 连接参数
  - 连接池设置
  - 事务配置

### 模板配置
- 模板目录：`template/`
- 模板文件：
  - ETL 映射模板
  - DDL 模板
  - 标准化模板
  - 其他业务模板

## 版本信息

当前版本：202503141826

## 依赖项

- Apache POI: 用于处理Word文档
- poi-tl: 用于模板渲染
- Lombok: 用于简化代码
- SLF4J: 用于日志记录
- Hutool: 用于常用工具方法和HTTP请求
- Jsoup: 用于解析HTML内容
- Apache HttpClient: 用于HTTP请求

## 注意事项

1. 简历模板要求：
   - 使用.docx格式
   - 需要包含特定的标记（如{{name}}、{{projectExperiences}}等）
   - 表格结构需要符合预期格式

2. 文件处理：
   - 输入文件需要是有效的Word文档
   - 建议使用.docx格式以获得最佳兼容性
   - 大量文件处理时注意内存使用

3. 输出路径：
   - 确保输出目录存在且有写入权限
   - 输出文件名会自动添加时间戳以避免覆盖

4. 性能考虑：
   - 大文件处理时注意内存使用
   - 批量处理时建议分批进行
   - 定期清理临时文件

5. 数据库操作：
   - 注意连接池配置
   - 及时关闭数据库连接
   - 处理事务边界

6. 内存使用：
   - 大文件处理时注意内存使用
   - 及时释放资源
   - 使用流式处理

7. 异常处理：
   - 捕获并处理所有异常
   - 记录详细的错误日志
   - 提供友好的错误提示

## 待优化项

1. 性能优化：
   - 优化大文件处理
   - 改进数据库操作
   - 优化内存使用

2. 功能增强：
   - 支持更多文件格式
   - 添加更多数据处理功能
   - 增强用户界面交互
   - 完善网页内容抓取功能
   - 扩展HTTP客户端功能

3. 代码质量：
   - 增加单元测试
   - 完善异常处理
   - 优化代码结构
   - 改进配置管理

4. 文档完善：
   - 添加详细的使用说明
   - 完善 API 文档
   - 添加示例代码
   - 更新网页内容抓取和HTTP客户端的文档

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进这个项目。在提交代码前，请确保：

1. 代码符合项目的编码规范
2. 添加了适当的注释和文档
3. 添加了必要的单元测试
4. 所有测试都能通过

## 许可证

本项目采用 MIT 许可证