package cn.sunline.mapping.test;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        String sql = "SELECT id, name /* 姓名 */, age AS \"age\" -- 年龄 FROM users;";

        // 解析 SQL 语句，使用支持注释的解析特性
        List<SQLStatement> statements = SQLUtils.parseStatements(sql, "oracle", SQLParserFeature.KeepComments);

        if (!statements.isEmpty()) {
            SQLStatement statement = statements.get(0);
            if (statement instanceof com.alibaba.druid.sql.ast.statement.SQLSelectStatement) {
                SQLSelect select = ((com.alibaba.druid.sql.ast.statement.SQLSelectStatement) statement).getSelect();
                if (select.getQuery() instanceof SQLSelectQueryBlock) {
                    SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) select.getQuery();
                    List<SQLSelectItem> selectItems = queryBlock.getSelectList();

                    for (SQLSelectItem selectItem : selectItems) {
                        // 尝试通过获取节点的注释列表来获取注释
                        List<String> comments = selectItem.getAfterCommentsDirect();
                        if (comments != null && !comments.isEmpty()) {
                            String comment = comments.get(0).trim();
                            System.out.println("字段: " + selectItem.getExpr() + ", 注释: " + comment);
                        }
                    }
                }
            }
        }
    }
}