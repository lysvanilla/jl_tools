package data;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import java.util.*;

public class CreditDataHutoolGenerator {
    // 核心配置：各字段可选值（使用Hutool集合工具简化定义）
    private static final List<String> DATA_DATE_LIST = CollUtil.newArrayList("20251030", "20251130"); // 早→晚
    private static final String FIRST_LEVEL_ORG = "总行";
    private static final Map<String, String> THIRD_TO_SECOND_ORG = new HashMap<>();
    private static final List<String> THIRD_LEVEL_ORG_LIST = CollUtil.newArrayList(
            "长沙万家丽支行", "长沙开福支行", "湘潭建设支行", "湘潭韶山支行"
    );
    private static final String CURRENCY = "人民币";
    private static final List<String> CUSTOMER_TYPE_LIST = CollUtil.newArrayList("个人", "对公");
    private static final List<String> MAIN_GUARANTEE_TYPE_LIST = CollUtil.newArrayList(
            "质押贷款", "抵押贷款", "保证贷款", "信用贷款"
    );
    private static final List<String> CREDIT_BUSINESS_TYPE_LIST = CollUtil.newArrayList(
            "流动资金贷款", "法人账户透支", "项目贷款", "项目贷款（银团）", "一般固定资产贷款",
            "住房按揭贷款", "商用房贷款", "汽车贷款", "助学贷款", "消费贷款", "个人经营性贷款",
            "票据贴现", "买断式转贴现", "贸易融资业务", "融资租赁业务", "垫款", "委托贷款"
    );
    private static final List<String> LOAN_ISSUE_TYPE_LIST = CollUtil.newArrayList(
            "新增", "无还本续贷", "借新还旧", "重组贷款"
    );
    private static final List<String> PAYMENT_METHOD_LIST = CollUtil.newArrayList(
            "自主支付", "受托支付", "混合支付"
    );
    private static final List<String> LOAN_CLASSIFICATION_LIST = CollUtil.newArrayList(
            "正常", "关注", "次级", "可疑", "损失"
    );
    private static final List<String> REPAYMENT_METHOD_LIST = CollUtil.newArrayList(
            "按月", "按季", "按半年", "按年", "利随本清", "分期付息一次还本"
    );
    private static final List<String> INTEREST_CALCULATION_LIST = CollUtil.newArrayList(
            "按月结息", "按季结息", "按半年结息", "按年结息", "不定期结息", "不记利息", "利随本清"
    );
    private static final List<String> INDUSTRY_LIST = CollUtil.newArrayList(
            "2.1农、林、牧、渔业", "2.2采矿业", "2.3制造业", "2.4电力、热力、燃气及水的生产和供应业",
            "2.5建筑业", "2.6批发和零售业", "2.7交通运输、仓储和邮政业", "2.8住宿和餐饮业",
            "2.9信息传输、软件和信息技术服务业", "2.10金融业", "2.11房地产业", "2.12租赁和商务服务业",
            "2.13科学研究和技术服务业", "2.14水利、环境和公共设施管理业", "2.15居民服务、修理和其他服务业",
            "2.16教育", "2.17卫生和社会工作", "2.18文化、体育和娱乐业", "2.19公共管理、社会保障和社会组织",
            "2.20国际组织", "2.21.1个人贷款-信用卡", "2.21.2个人贷款-汽车", "2.21.3个人贷款-住房按揭贷款",
            "2.21.4个人贷款-其他", "2.22买断式转贴现", "2.23买断其他票据类资产", "3.对境外贷款"
    );
    private static final List<String> YES_NO_LIST = CollUtil.newArrayList("是", "否");
    private static final List<String> LOAN_STATUS_LIST = CollUtil.newArrayList(
            "正常", "结清", "逾期", "核销", "转让"
    );
    private static final List<String> ENTERPRISE_SCALE_LIST = CollUtil.newArrayList(
            "大型企业", "中型企业", "小型企业", "微型企业"
    );
    private static final List<String> RESIDENT_FLAG_LIST = CollUtil.newArrayList("非居民", "居民");
    private static final List<String> TERM_TYPE_LIST = CollUtil.newArrayList("短期", "中长期");
    private static final List<String> REMAINING_TERM_LIST = CollUtil.newArrayList(
            "次日", "2日至7日", "8日至30日", "31日至90日", "91日至1年", "1年至5年",
            "5年至10年", "10年以上", "逾期"
    );
    private static final List<String> REGION_LIST = CollUtil.newArrayList(
            "长沙市", "株洲市", "湘潭市", "衡阳市", "邵阳市", "岳阳市", "常德市",
            "张家界市", "益阳市", "郴州市", "永州市", "怀化市", "娄底市"
    );

    // 存储借据号对应的发放金额和早日期余额（Hutool简化Map初始化）
    private static final Map<String, Integer> CREDIT_NO_TO_ISSUE_AMOUNT = new HashMap<>();
    private static final Map<String, Integer> CREDIT_NO_TO_EARLY_BALANCE = new HashMap<>();

    static {
        // 初始化三级机构→二级机构映射
        THIRD_TO_SECOND_ORG.put("长沙万家丽支行", "长沙分行");
        THIRD_TO_SECOND_ORG.put("长沙开福支行", "长沙分行");
        THIRD_TO_SECOND_ORG.put("湘潭建设支行", "湘潭分行");
        THIRD_TO_SECOND_ORG.put("湘潭韶山支行", "湘潭分行");
    }

    public static void main(String[] args) {
        // 使用Hutool ExcelWriter创建Excel
        ExcelWriter writer = ExcelUtil.getWriter(FileUtil.file("信贷数据_Hutool版.xlsx"));

        // 定义表头（Hutool简化表头写入）
        String[] headers = {
                "数据日期", "一级机构名称", "二级机构名称", "三级机构名称", "币种", "客户类型",
                "客户号", "信贷借据号", "主担保方式", "信贷业务种类", "贷款发放类型", "放款方式",
                "贷款五级分类", "还款方式", "计息方式", "客户所属行业", "贷款投向行业", "是否互联网贷款",
                "是否绿色贷款", "是否涉农贷款", "是否普惠型涉农贷款", "是否普惠型小微企业贷款", "是否科技贷款",
                "贷款状态", "企业规模", "居民非居民标志", "期限类型", "贷款剩余期限", "贷款分地区",
                "贷款余额", "贷款发放金额"
        };
        writer.writeHeadRow(Arrays.asList(headers));

        // 生成早日期（20251030）200条数据
        generateData(DATA_DATE_LIST.get(0), 1, 200, writer, true);
        // 生成晚日期（20251130）200条数据
        generateData(DATA_DATE_LIST.get(1), 201, 400, writer, false);

        // 自动调整列宽（Hutool一行实现）
        writer.autoSizeColumnAll();
        // 关闭writer，生成文件
        writer.close();

        System.out.println("Excel文件生成完成！路径：信贷数据_Hutool版.xlsx");
    }

    /**
     * 生成指定范围的信贷数据
     * @param dataDate 数据日期
     * @param startNo 客户/借据号起始值
     * @param endNo 客户/借据号结束值
     * @param writer Excel写入器
     * @param isEarlyDate 是否为早日期
     */
    private static void generateData(String dataDate, int startNo, int endNo, ExcelWriter writer, boolean isEarlyDate) {
        for (int i = startNo; i <= endNo; i++) {
            // Hutool简化字符串格式化（客户号/借据号）
            String customerNo = StrUtil.format("CU{:06d}", i);
            String creditNo = StrUtil.format("LN{:06d}", i);

            // 随机选三级机构（Hutool随机工具）
            String thirdOrg = RandomUtil.randomEle(THIRD_LEVEL_ORG_LIST);
            String secondOrg = THIRD_TO_SECOND_ORG.get(thirdOrg);

            // 发放金额：≥1万整数，早日期初始化，晚日期复用
            int issueAmount;
            if (isEarlyDate) {
                // Hutool随机整数（10000 ~ 10000000）
                issueAmount = RandomUtil.randomInt(10000, 10000001);
                CREDIT_NO_TO_ISSUE_AMOUNT.put(creditNo, issueAmount);
            } else {
                issueAmount = CREDIT_NO_TO_ISSUE_AMOUNT.get(creditNo);
            }

            // 贷款余额：≤发放金额+≥1万，早日期≥晚日期
            int balance;
            if (isEarlyDate) {
                balance = RandomUtil.randomInt(10000, issueAmount + 1);
                CREDIT_NO_TO_EARLY_BALANCE.put(creditNo, balance);
            } else {
                int earlyBalance = CREDIT_NO_TO_EARLY_BALANCE.get(creditNo);
                balance = RandomUtil.randomInt(10000, earlyBalance + 1);
            }

            // 贷款状态&剩余期限规则
            String loanStatus = RandomUtil.randomEle(LOAN_STATUS_LIST);
            String remainingTerm = CollUtil.contains(Arrays.asList("逾期", "核销"), loanStatus)
                    ? "逾期"
                    : RandomUtil.randomEle(REMAINING_TERM_LIST);

            // 组装一行数据（Hutool简化列表创建）
            List<Object> rowData = CollUtil.newArrayList(
                    dataDate,
                    FIRST_LEVEL_ORG,
                    secondOrg,
                    thirdOrg,
                    CURRENCY,
                    RandomUtil.randomEle(CUSTOMER_TYPE_LIST),
                    customerNo,
                    creditNo,
                    RandomUtil.randomEle(MAIN_GUARANTEE_TYPE_LIST),
                    RandomUtil.randomEle(CREDIT_BUSINESS_TYPE_LIST),
                    RandomUtil.randomEle(LOAN_ISSUE_TYPE_LIST),
                    RandomUtil.randomEle(PAYMENT_METHOD_LIST),
                    RandomUtil.randomEle(LOAN_CLASSIFICATION_LIST),
                    RandomUtil.randomEle(REPAYMENT_METHOD_LIST),
                    RandomUtil.randomEle(INTEREST_CALCULATION_LIST),
                    RandomUtil.randomEle(INDUSTRY_LIST),
                    RandomUtil.randomEle(INDUSTRY_LIST),
                    RandomUtil.randomEle(YES_NO_LIST),
                    RandomUtil.randomEle(YES_NO_LIST),
                    RandomUtil.randomEle(YES_NO_LIST),
                    RandomUtil.randomEle(YES_NO_LIST),
                    RandomUtil.randomEle(YES_NO_LIST),
                    RandomUtil.randomEle(YES_NO_LIST),
                    loanStatus,
                    RandomUtil.randomEle(ENTERPRISE_SCALE_LIST),
                    RandomUtil.randomEle(RESIDENT_FLAG_LIST),
                    RandomUtil.randomEle(TERM_TYPE_LIST),
                    remainingTerm,
                    RandomUtil.randomEle(REGION_LIST),
                    balance,
                    issueAmount
            );

            // 写入一行数据（Hutool一行实现）
            writer.writeRow(rowData);
        }
    }
}