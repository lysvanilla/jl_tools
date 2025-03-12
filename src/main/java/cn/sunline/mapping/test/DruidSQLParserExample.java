package cn.sunline.mapping.test;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;

import java.util.*;

public class DruidSQLParserExample {

    public static void main(String[] args) {
        String sql = "SELECT t1.col1, t2.col2 FROM schema1.table1 t1 JOIN schema2.table2 t2 ON t1.id = t2.id WHERE t1.col1 > 10;";
        parseSQL(sql);
    }

    public static void parseSQL(String sql) {
        // 创建MySQL语句解析器
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        // 解析SQL语句
        List<SQLStatement> statementList = parser.parseStatementList();

        for (SQLStatement statement : statementList) {
            if (statement instanceof SQLSelectStatement) {
                SQLSelectStatement selectStatement = (SQLSelectStatement) statement;
                SQLSelect select = selectStatement.getSelect();
                SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) select.getQuery();

                // 目标表模式名、目标表、目标字段
                List<String> targetTableSchemas = new ArrayList<>();
                List<String> targetTables = new ArrayList<>();
                List<String> targetColumns = new ArrayList<>();

                // 来源表模式名、来源表、来源字段
                List<String> sourceTableSchemas = new ArrayList<>();
                List<String> sourceTables = new ArrayList<>();
                List<String> sourceColumns = new ArrayList<>();

                // 关联表、关联条件
                List<String> joinTables = new ArrayList<>();
                List<String> joinConditions = new ArrayList<>();

                // 过滤条件
                List<String> filterConditions = new ArrayList<>();

                // 处理目标字段
                for (SQLSelectItem item : queryBlock.getSelectList()) {
                    SQLExpr expr = item.getExpr();
                    if (expr instanceof SQLIdentifierExpr) {
                        targetColumns.add(((SQLIdentifierExpr) expr).getName());
                    } else if (expr instanceof SQLPropertyExpr) {
                        SQLPropertyExpr propertyExpr = (SQLPropertyExpr) expr;
                        targetColumns.add(propertyExpr.getName());
                        String tableAlias = ((SQLIdentifierExpr) propertyExpr.getOwner()).getName();
                        processTableAlias(queryBlock.getFrom(), tableAlias, targetTableSchemas, targetTables);
                    }
                }

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

                // 处理过滤条件
                SQLExpr where = queryBlock.getWhere();
                if (where != null) {
                    filterConditions.add(where.toString());
                }

                // 打印结果
                System.out.println("目标表模式名: " + targetTableSchemas);
                System.out.println("目标表: " + targetTables);
                System.out.println("目标字段: " + targetColumns);
                System.out.println("来源表模式名: " + sourceTableSchemas);
                System.out.println("来源表: " + sourceTables);
                System.out.println("关联表: " + joinTables);
                System.out.println("关联条件: " + joinConditions);
                System.out.println("过滤条件: " + filterConditions);
            }
        }
    }

    private static void processTableAlias(SQLTableSource tableSource, String tableAlias, List<String> tableSchemas, List<String> tables) {
        if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource exprTableSource = (SQLExprTableSource) tableSource;
            if (exprTableSource.getAlias() != null && exprTableSource.getAlias().equals(tableAlias)) {
                if (exprTableSource.getExpr() instanceof SQLPropertyExpr) {
                    SQLPropertyExpr tableExpr = (SQLPropertyExpr) exprTableSource.getExpr();
                    tableSchemas.add(((SQLIdentifierExpr) tableExpr.getOwner()).getName());
                    tables.add(tableExpr.getName());
                } else if (exprTableSource.getExpr() instanceof SQLIdentifierExpr) {
                    tableSchemas.add("");
                    tables.add(((SQLIdentifierExpr) exprTableSource.getExpr()).getName());
                }
            }
        } else if (tableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTableSource = (SQLJoinTableSource) tableSource;
            processTableAlias(joinTableSource.getLeft(), tableAlias, tableSchemas, tables);
            processTableAlias(joinTableSource.getRight(), tableAlias, tableSchemas, tables);
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
}