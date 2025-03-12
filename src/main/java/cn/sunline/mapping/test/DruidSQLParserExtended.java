package cn.sunline.mapping.test;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DruidSQLParserExtended {

    public static void main(String[] args) {
        String sql = "INSERT INTO RDM.M_TX_CLS_INDX(\n" +
                "  DATA_DT, -- 数据日期\n" +
                "  CUST_NO, -- 客户编号\n" +
                "  CERT_ID, -- 身份证号\n" +
                "  USCD, -- 统一社会信用代码\n" +
                "  INDX_NO, -- 指标编号\n" +
                "  INDX_NM, -- 指标名称\n" +
                "  INDX_VAL -- 指标值\n" +
                "  )\n" +
                " SELECT\n" +
                " T1.DATA_DT AS DATA_DT,\n" +
                " T1.CUST_NO, -- 客户编号\n" +
                " T1.DOC_NO   AS ID_CD, --证件号码\n" +
                " T2.USCD , -- 统一社会信用代码\n" +
                " '000011',  -- 指标编号\n" +
                " '股东客户近12个月月均收款金额', -- 指标名称\n" +
                " SUM(IFNULL(T2.TXN_AMT,0))   --指标值\n" +
                " FROM C_PT_INDV_CUST_BASIC T1    --个人客户基本信息\n" +
                " INNER JOIN IREP_SETTLE_LOAN_TRANS_FLOW T2 ON T2.CUST_NO = T1.CUST_NO --结算贷款数据\n" +
                " INNER JOIN (\n" +
                "    select W2.SHAREHD_DOC_NO as SHAREHD_DOC_NO, W1.CORP_CUST_NAME as CORP_CUST_NAME\n" +
                "     from C_PT_CORP_CUST_BASIC W1\n" +
                "     inner join C_PT_CORP_CUST_SHAREHD W2 on W1.CUST_NO = W2.CUST_NO \n" +
                "     where multiply(ifnull(W2.INVEST_RATIO,0),100) > 10 and W2.SHAREHD_DOC_NO is not null\n" +
                "      ) T3\n" +
                "  ON T3.SHAREHD_DOC_NO = T1.DOC_NO --结算贷款数据\n" +
                " INNER JOIN IREP_SETTLE_LOAN_TRANS_FLOW T4 ON T4.CUST_NO = T1.CUST_NO --结算贷款数据\n" +
                " WHERE T2.DEBT_CRD_IND_CD = 'C' --借贷方向\n" +
                " AND T2.TXN_DT >= formatDateTime(toDate(addMonths(today(),-12)), '%Y%m') -- TXN_DT 交易月份 yyyymm格式\n" +
                " AND T2.TXN_DT <= formatDateTime(toDate(addMonths(today(),-1)), '%Y%m')\n" +
                "group by T1.DOC_NO,T1.DATA_DT,T1.CUST_NO, T2.USCD;";
        parseSQL(sql);
    }

    public static void parseSQL(String sql) {
        OracleStatementParser parser = new OracleStatementParser(sql);
        //List<SQLStatement> statementList = parser.parseStatementList();
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, DbType.oracle,true);
        for (SQLStatement statement : statementList) {
            if (statement instanceof SQLInsertStatement) {
                SQLInsertStatement insertStatement = (SQLInsertStatement) statement;
                SQLName targetTableName = insertStatement.getTableName();
                List<String> targetTableSchema = new ArrayList<>();
                List<String> targetTable = new ArrayList<>();
                List<String> targetColumns = new ArrayList<>();

                if (targetTableName instanceof SQLPropertyExpr) {
                    SQLPropertyExpr propertyExpr = (SQLPropertyExpr) targetTableName;
                    targetTableSchema.add(((SQLIdentifierExpr) propertyExpr.getOwner()).getName());
                    targetTable.add(propertyExpr.getName());
                } else if (targetTableName instanceof SQLIdentifierExpr) {
                    targetTableSchema.add("");
                    targetTable.add(((SQLIdentifierExpr) targetTableName).getName());
                }

                for (SQLExpr column : insertStatement.getColumns()) {
                    if (column instanceof SQLIdentifierExpr) {
                        targetColumns.add(((SQLIdentifierExpr) column).getName());
                    }
                }

                SQLSelect select = insertStatement.getQuery();
                SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) select.getQuery();

                List<SQLSelectItem> insertSelectList = queryBlock.getSelectList();
                for (SQLSelectItem selectItem : insertSelectList) {
                    System.out.println(selectItem.getExpr().toString());
                    System.out.println("getAfterCommentsDirect:"+selectItem.getAfterCommentsDirect());
                    SQLExpr sqlExpr = selectItem.getExpr();
                    if (sqlExpr instanceof SQLPropertyExpr) {
                        SQLPropertyExpr propertyExpr = (SQLPropertyExpr) sqlExpr;
                    }
                }

                List<String> sourceTableSchemas = new ArrayList<>();
                List<String> sourceTables = new ArrayList<>();
                List<String> sourceColumns = new ArrayList<>();
                List<String> joinTables = new ArrayList<>();
                List<String> joinConditions = new ArrayList<>();
                List<String> filterConditions = new ArrayList<>();

                // 处理来源表和关联表
                SQLTableSource from = queryBlock.getFrom();
                if (from instanceof SQLJoinTableSource) {
                    SQLJoinTableSource joinTableSource = (SQLJoinTableSource) from;
                    processTableSource(joinTableSource.getLeft(), sourceTableSchemas, sourceTables);
                    processTableSource(joinTableSource.getRight(), sourceTableSchemas, sourceTables, joinTables);
                    if (joinTableSource.getCondition() != null) {
                        joinConditions.add(joinTableSource.getCondition().toString());
                    }
                } else {
                    processTableSource(from, sourceTableSchemas, sourceTables);
                }

                // 处理来源字段
                for (SQLSelectItem item : queryBlock.getSelectList()) {
                    SQLExpr expr = item.getExpr();
                    extractSourceColumns(expr, sourceColumns);
                }

                // 处理过滤条件
                SQLExpr where = queryBlock.getWhere();
                if (where != null) {
                    filterConditions.add(where.toString());
                }

                // 打印结果
                System.out.println("目标表模式名: " + targetTableSchema);
                System.out.println("目标表: " + targetTable);
                System.out.println("目标字段: " + targetColumns);
                System.out.println("来源表模式名: " + sourceTableSchemas);
                System.out.println("来源表: " + sourceTables);
                System.out.println("来源字段: " + sourceColumns);
                System.out.println("关联表: " + joinTables);
                System.out.println("关联条件: " + joinConditions);
                System.out.println("过滤条件: " + filterConditions);
            } else if (statement instanceof SQLCreateTableStatement) {
                SQLCreateTableStatement createTableStatement = (SQLCreateTableStatement) statement;
                SQLName targetTableName = createTableStatement.getName();
                List<String> targetTableSchema = new ArrayList<>();
                List<String> targetTable = new ArrayList<>();
                List<String> targetColumns = new ArrayList<>();
                List<String> targetColumnChineseNames = new ArrayList<>();
                List<String> targetColumnTypes = new ArrayList<>();

                if (targetTableName instanceof SQLPropertyExpr) {
                    SQLPropertyExpr propertyExpr = (SQLPropertyExpr) targetTableName;
                    targetTableSchema.add(((SQLIdentifierExpr) propertyExpr.getOwner()).getName());
                    targetTable.add(propertyExpr.getName());
                } else if (targetTableName instanceof SQLIdentifierExpr) {
                    targetTableSchema.add("");
                    targetTable.add(((SQLIdentifierExpr) targetTableName).getName());
                }

                List<SQLColumnDefinition> columnDefinitions = createTableStatement.getColumnDefinitions();
                for (SQLColumnDefinition columnDefinition : columnDefinitions) {
                    targetColumns.add(columnDefinition.getName().getSimpleName());
                    SQLExpr commentExpr = columnDefinition.getComment();
                    if (commentExpr instanceof SQLCharExpr) {
                        SQLCharExpr charExpr = (SQLCharExpr) commentExpr;
                        targetColumnChineseNames.add(charExpr.getText());
                    } else {
                        targetColumnChineseNames.add("");
                    }
                    // 获取字段类型
                    SQLDataType dataType = columnDefinition.getDataType();
                    if (dataType != null) {
                        targetColumnTypes.add(dataType.toString());
                    } else {
                        targetColumnTypes.add("");
                    }
                }

                // 打印结果
                System.out.println("目标表模式名: " + targetTableSchema);
                System.out.println("目标表: " + targetTable);
                System.out.println("目标字段: " + targetColumns);
                System.out.println("目标字段中文名: " + targetColumnChineseNames);
                System.out.println("目标字段类型: " + targetColumnTypes);
                System.out.println("来源表模式名: []");
                System.out.println("来源表: []");
                System.out.println("来源字段: []");
                System.out.println("关联表: []");
                System.out.println("关联条件: []");
                System.out.println("过滤条件: []");
            }
        }
    }

    private static void processTableSource(SQLTableSource tableSource, List<String> tableSchemas, List<String> tables) {
        processTableSource(tableSource, tableSchemas, tables, null);
    }

    private static void processTableSource(SQLTableSource tableSource, List<String> tableSchemas, List<String> tables, List<String> joinTables) {
        if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
            if (exprTableSource.getExpr() instanceof SQLPropertyExpr) {
                SQLPropertyExpr tableExpr = (SQLPropertyExpr) exprTableSource.getExpr();
                tableSchemas.add(((SQLIdentifierExpr) tableExpr.getOwner()).getName());
                tables.add(tableExpr.getName());
                if (joinTables != null) {
                    joinTables.add(tableExpr.getName());
                }
            } else if (exprTableSource.getExpr() instanceof SQLIdentifierExpr) {
                tableSchemas.add("");
                tables.add(((SQLIdentifierExpr) exprTableSource.getExpr()).getName());
                if (joinTables != null) {
                    joinTables.add(((SQLIdentifierExpr) exprTableSource.getExpr()).getName());
                }
            }
        } else if (tableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTableSource = (SQLJoinTableSource) tableSource;
            processTableSource(joinTableSource.getLeft(), tableSchemas, tables);
            processTableSource(joinTableSource.getRight(), tableSchemas, tables, joinTables);
        }
    }

    private static void extractSourceColumns(SQLExpr expr, List<String> sourceColumns) {
        if (expr instanceof SQLPropertyExpr) {
            SQLPropertyExpr propertyExpr = (SQLPropertyExpr) expr;
            sourceColumns.add(propertyExpr.getName());
        } else if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) expr;
            extractSourceColumns(binaryOpExpr.getLeft(), sourceColumns);
            extractSourceColumns(binaryOpExpr.getRight(), sourceColumns);
        } else if (expr instanceof SQLCaseExpr) {
            SQLCaseExpr caseExpr = (SQLCaseExpr) expr;
            sourceColumns.add(caseExpr.toString());
            List<SQLObject> sqlObjectList = caseExpr.getChildren();
            for (SQLObject child : sqlObjectList) {
                if (child instanceof SQLExpr) {
                    extractSourceColumns((SQLExpr) child, sourceColumns);
                }
            }
        } else if (expr instanceof SQLAggregateExpr) {
            SQLAggregateExpr aggregateExpr = (SQLAggregateExpr) expr;
            sourceColumns.add(aggregateExpr.toString());
            for (SQLExpr param : aggregateExpr.getParameters()) {
                extractSourceColumns(param, sourceColumns);
            }
        } else if (expr instanceof SQLCharExpr) {
            SQLCharExpr charExpr = (SQLCharExpr) expr;
            sourceColumns.add("'" + charExpr.getText() + "'");
        }
    }

    private static String extractComment(String selectItemStr) {
        int commentIndex = selectItemStr.indexOf("--");
        if (commentIndex != -1) {
            return selectItemStr.substring(commentIndex + 2).trim();
        }
        return "";
    }
}