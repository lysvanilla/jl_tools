package cn.sunline.mapping;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.sunline.vo.etl.EtlGroup;
import cn.sunline.vo.etl.EtlGroupColMapp;
import cn.sunline.vo.etl.EtlGroupJoinInfo;
import cn.sunline.vo.etl.EtlMapp;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLExprImpl;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static cn.sunline.mapping.GenEtlMappExcel.genEtlMappExcel;

@Slf4j
public class SqlParserToEtlMapp {
    private static final String ETL_MAPP_TARGET_TABLE_PREFIX = "";
    private static String etlMappTargetTable = "";

    public static void main(String[] args) {
        EtlMapp etlMapp = getEtlGroupList("C:\\Users\\lysva\\Desktop\\EDWIEL.E_IREP_SETTLE_LOAN_TRANS_FLOW.sql");
        List<EtlMapp> etlMappList = new ArrayList<>();
        etlMappList.add(etlMapp);
        genEtlMappExcel(etlMappList);
        System.out.println("1");
    }

    public static EtlMapp getEtlGroupList(String dealFile) {
        String sql = dealsql(dealFile);
        String fileName = FileUtil.mainName(dealFile);
        EtlMapp etlMapp = new EtlMapp();
        etlMapp.setSheetName(fileName);
        String schemaName = fileName.substring(0, 3);
        String tableName = fileName.substring(4);
        etlMapp.setAttributionLevel(schemaName);
        etlMappTargetTable = schemaName + "." + tableName;
        etlMapp.setTableEnglishName(etlMappTargetTable);
        String dbType = "oracle";
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);
        List<EtlGroup> etlGroupList = new ArrayList<>();
        etlGroupList = getStateMent(statementList, etlGroupList);
        etlMapp.setEtlGroupList(etlGroupList);
        log.info("脚本解析完成:[{}]", dealFile);
        return etlMapp;
    }

    public static List<EtlGroup> getStateMent(List<SQLStatement> statementList, List<EtlGroup> etlGroupList) {
        for (int i = 0; i < statementList.size(); i++) {
            SQLStatement statement = statementList.get(i);
            log.warn("开始解析:[{}]-[{}]", i,statement.toString());
            if (statement instanceof SQLDeleteStatement) {
                if (i == 0) {
                    log.warn("delete语句，不解析:[{}]", statement.getClass().getName());
                } else {
                    //handleDeleteStatement((SQLDeleteStatement) statement, i, etlGroupList);
                }
            } else if (statement instanceof SQLInsertStatement) {
                handleInsertStatement((SQLInsertStatement) statement, i, etlGroupList);
            } else if (statement instanceof SQLCreateTableStatement) {
                handleCreateTableStatement((SQLCreateTableStatement) statement, i, etlGroupList);
            } else if (statement instanceof SQLForStatement || statement instanceof SQLSetStatement) {
                log.warn("deal statement failed,not support yet:[{}],[{}]", statement.getClass().getName(), statement);
            } else {
                log.warn("deal statement failed,not support yet:[{}],[{}]", statement.getClass().getName(), statement);
            }
        }
        return etlGroupList;
    }

    private static void handleDeleteStatement(SQLDeleteStatement sqlDeleteStatement, int i, List<EtlGroup> etlGroupList) {
        String tableName = String.valueOf(sqlDeleteStatement.getTableSource());
        EtlGroup etlGroup = new EtlGroup();
        etlGroup.setGroupId("第" + i + "组");
        etlGroup.setTargetTableEnglishName(tableName);
        etlGroup.setFilterCondition(String.valueOf(sqlDeleteStatement.getWhere()));
        etlGroup.setTemplateType("D1");
        etlGroupList.add(etlGroup);
    }

    private static void handleInsertStatement(SQLInsertStatement sqlInsertStatement, int i, List<EtlGroup> etlGroupList) {
        String tableName = String.valueOf(sqlInsertStatement.getTableSource());
        SQLSelectQuery sqlSelectQuery = sqlInsertStatement.getQuery().getQuery();
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            handleSelectQueryBlock(sqlInsertStatement, (SQLSelectQueryBlock) sqlSelectQuery, i, etlGroupList);
        } else if (sqlSelectQuery instanceof SQLUnionQuery) {
            handleUnionQuery(sqlInsertStatement, (SQLUnionQuery) sqlSelectQuery, i, etlGroupList);
        }
    }

    private static void handleSelectQueryBlock(SQLInsertStatement sqlInsertStatement, SQLSelectQueryBlock sqlSelectQueryBlock, int i, List<EtlGroup> etlGroupList) {
        EtlGroup etlGroup = new EtlGroup();
        etlGroup.setGroupId("第" + i + "组");
        etlGroup.setTargetTableEnglishName(String.valueOf(sqlInsertStatement.getTableSource()));
        etlGroup.setFilterCondition(StringUtils.defaultIfEmpty(String.valueOf(sqlSelectQueryBlock.getWhere()), ""));
        etlGroup.setGroupingCondition(StringUtils.removeStart(StringUtils.defaultIfEmpty(String.valueOf(sqlSelectQueryBlock.getGroupBy()), ""), "GROUP BY "));
        etlGroup.setSortingCondition(StringUtils.defaultIfEmpty(String.valueOf(sqlSelectQueryBlock.getOrderBy()), ""));
        etlGroup.setTemplateType(etlMappTargetTable.equals(String.valueOf(sqlInsertStatement.getTableSource())) ? "N2" : "N1");

        List<SQLExpr> insertColList = sqlInsertStatement.getColumns();
        List<SQLSelectItem> insertSelectList = sqlSelectQueryBlock.getSelectList();
        int colMapNum = Math.max(insertColList.size(), insertSelectList.size());
        for (int j = 0; j < colMapNum; j++) {
            EtlGroupColMapp etlGroupColMapp = new EtlGroupColMapp();
            SQLSelectItem selectItem = insertSelectList.get(j);
            SQLExpr sqlExpr = selectItem.getExpr();
            if (sqlExpr instanceof SQLPropertyExpr) {
                SQLPropertyExpr propertyExpr = (SQLPropertyExpr) sqlExpr;
                etlGroupColMapp.setSourceFieldEnglishName(propertyExpr.getName());
                // 检查 attributes 是否为 null
                if (propertyExpr.getAttributes() != null) {
                    Object commentAttr = propertyExpr.getAttributes().get("rowFormat.before_comment");
                    if (commentAttr != null) {
                        etlGroupColMapp.setSourceFieldChineseName(commentAttr.toString());
                    }
                }
            } else if (sqlExpr instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) sqlExpr;
                // 检查 attributes 是否为 null
                if (identifierExpr.getAttributes() != null) {
                    Object commentAttr = identifierExpr.getAttributes().get("rowFormat.before_comment");
                    if (commentAttr != null) {
                        etlGroupColMapp.setSourceFieldChineseName(commentAttr.toString());
                    }
                }
            } else if (sqlExpr instanceof SQLAggregateExpr) {
                SQLAggregateExpr aggregateExpr = (SQLAggregateExpr) sqlExpr;
                if (!aggregateExpr.getArguments().isEmpty()) {
                    etlGroupColMapp.setSourceFieldEnglishName(aggregateExpr.getArguments().get(0).toString());
                }
            } else if (sqlExpr instanceof SQLCharExpr) {
                // 处理 SQLCharExpr 的逻辑
            }
            /*if (selectItem != null && selectItem.getExpr() != null && selectItem.getExpr().computeDataType() != null) {
                etlGroupColMapp.setSourceFieldEnglishName(selectItem.getExpr().computeDataType().getName());
            } else {
                etlGroupColMapp.setSourceFieldEnglishName(""); // 或者设置一个默认值
            }*/
            etlGroupColMapp.setMappingRule(insertSelectList.get(j).toString());
            if (j < insertColList.size()) {
                etlGroupColMapp.setTargetFieldEnglishName(insertColList.get(j).toString());
            }
            etlGroup.getEtlGroupColMappList().add(etlGroupColMapp);
        }
        etlGroup = getTableSelect(sqlSelectQueryBlock, etlGroup);
        List<EtlGroupJoinInfo> eTLGroupJoinInfoListReverse = ListUtil.reverse(etlGroup.getEtlGroupJoinInfoList());
        etlGroup.setEtlGroupJoinInfoList(eTLGroupJoinInfoListReverse);
        etlGroupList.add(etlGroup);
    }


    private static void handleUnionQuery(SQLInsertStatement sqlInsertStatement, SQLUnionQuery sqlUnionQuery, int i, List<EtlGroup> etlGroupList) {
        List<SQLSelectQuery> selectQueryList = sqlUnionQuery.getRelations();
        for (int jj = 0; jj < selectQueryList.size(); jj++) {
            SQLSelectQuery selectQuery = selectQueryList.get(jj);
            if (selectQuery instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) selectQuery;
                EtlGroup etlGroup = new EtlGroup();
                etlGroup.setGroupId("第" + i + "组");
                etlGroup.setTargetTableEnglishName(String.valueOf(sqlInsertStatement.getTableSource()));
                etlGroup.setGroupRemarks("脚本中为union语法，mapping分拆两组显示");
                etlGroup.setFilterCondition(StringUtils.defaultIfEmpty(String.valueOf(sqlSelectQueryBlock.getWhere()), ""));
                etlGroup.setGroupingCondition(StringUtils.removeStart(StringUtils.defaultIfEmpty(String.valueOf(sqlSelectQueryBlock.getGroupBy()), ""), "GROUP BY "));
                etlGroup.setSortingCondition(StringUtils.defaultIfEmpty(String.valueOf(sqlSelectQueryBlock.getOrderBy()), ""));
                etlGroup.setTemplateType(etlMappTargetTable.equals(String.valueOf(sqlInsertStatement.getTableSource())) ? "N2" : "N1");

                List<SQLExpr> insertColList = sqlInsertStatement.getColumns();
                List<SQLSelectItem> insertSelectList = sqlSelectQueryBlock.getSelectList();
                for (int j = 0; j < insertColList.size(); j++) {
                    EtlGroupColMapp etlGroupColMapp = new EtlGroupColMapp();
                    etlGroupColMapp.setMappingRule(insertSelectList.get(j).toString());
                    etlGroupColMapp.setTargetFieldEnglishName(insertColList.get(j).toString());
                    etlGroup.getEtlGroupColMappList().add(etlGroupColMapp);
                }
                etlGroup = getTableSelect(sqlSelectQueryBlock, etlGroup);
                List<EtlGroupJoinInfo> eTLGroupJoinInfoListReverse = ListUtil.reverse(etlGroup.getEtlGroupJoinInfoList());
                etlGroup.setEtlGroupJoinInfoList(eTLGroupJoinInfoListReverse);
                etlGroupList.add(etlGroup);
            }
        }
    }

    private static void handleCreateTableStatement(SQLCreateTableStatement sqlCreateTableStatement, int i, List<EtlGroup> etlGroupList) {
        EtlGroup etlGroup = new EtlGroup();
        etlGroup.setGroupId("第" + i + "组");
        etlGroup.setTargetTableEnglishName(String.valueOf(sqlCreateTableStatement.getTableSource()));
        etlGroup.setTemplateType("Y2");
        if (sqlCreateTableStatement.getSelect() == null) {
            etlGroup.setTemplateType("Y3");
            List<SQLTableElement> createColList = sqlCreateTableStatement.getTableElementList();
            for (int j = 0; j < createColList.size(); j++) {
                SQLColumnDefinition sqlColumnDefinition = (SQLColumnDefinition) createColList.get(j);
                String colNameEn = sqlColumnDefinition.getColumnName();
                String colType = String.valueOf(sqlColumnDefinition.getDataType());
                String colNameCn = ""; // 默认值
                if (sqlColumnDefinition.getComment() != null) {
                    colNameCn = sqlColumnDefinition.getComment().toString().replaceAll("'", "");
                }
                EtlGroupColMapp etlGroupColMapp = new EtlGroupColMapp();
                etlGroupColMapp.setTargetFieldEnglishName(colNameEn);
                etlGroupColMapp.setTargetFieldType(colType);
                etlGroupColMapp.setTargetFieldChineseName(colNameCn);
                etlGroup.getEtlGroupColMappList().add(etlGroupColMapp);
            }
            etlGroupList.add(etlGroup);
        } else {
            SQLSelectQuery sqlSelectQuery = sqlCreateTableStatement.getSelect().getQuery();
            if (sqlSelectQuery instanceof SQLUnionQuery) {
                SQLUnionQuery sqlUnionQuery = (SQLUnionQuery) sqlSelectQuery;
                EtlGroupJoinInfo etlGroupJoinInfo = new EtlGroupJoinInfo();
                etlGroupJoinInfo.setSourceTableEnglishName("(" + String.valueOf(sqlUnionQuery.getParent()) + ")");
                etlGroup.getEtlGroupJoinInfoList().add(etlGroupJoinInfo);
                etlGroup.setGroupRemarks("create tables as 创建临时表");
                etlGroupList.add(etlGroup);
            } else if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
                etlGroup.setFilterCondition(StringUtils.defaultIfEmpty(String.valueOf(sqlSelectQueryBlock.getWhere()), ""));
                etlGroup.setGroupingCondition(StringUtils.removeStart(StringUtils.defaultIfEmpty(String.valueOf(sqlSelectQueryBlock.getGroupBy()), ""), "GROUP BY "));
                etlGroup.setSortingCondition(StringUtils.defaultIfEmpty(String.valueOf(sqlSelectQueryBlock.getOrderBy()), ""));

                List<SQLSelectItem> insertSelectList = sqlSelectQueryBlock.getSelectList();
                for (int j = 0; j < insertSelectList.size(); j++) {
                    EtlGroupColMapp etlGroupColMapp = new EtlGroupColMapp();
                    String selectColNameEn = insertSelectList.get(j).toString();
                    etlGroupColMapp.setMappingRule(selectColNameEn);
                    String colNameEn = StringUtils.defaultIfEmpty(insertSelectList.get(j).getAlias(), selectColNameEn);
                    etlGroupColMapp.setTargetFieldEnglishName(colNameEn);
                    etlGroup.getEtlGroupColMappList().add(etlGroupColMapp);
                }
                etlGroup = getTableSelect(sqlSelectQueryBlock, etlGroup);
                List<EtlGroupJoinInfo> eTLGroupJoinInfoListReverse = ListUtil.reverse(etlGroup.getEtlGroupJoinInfoList());
                etlGroup.setEtlGroupJoinInfoList(eTLGroupJoinInfoListReverse);
                etlGroupList.add(etlGroup);
            }
        }
    }

    public static EtlGroup getTableSelect(SQLSelectQuery sqlSelectQuery, EtlGroup etlGroup) {
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            EtlGroupJoinInfo etlGroupJoinInfo = new EtlGroupJoinInfo();
            SQLSelectQueryBlock sqlSelect = (SQLSelectQueryBlock) sqlSelectQuery;
            SQLTableSource fromTableSource = sqlSelect.getFrom();
            if (fromTableSource instanceof SQLJoinTableSource) {
                etlGroup = getTableSource((SQLJoinTableSource) fromTableSource, etlGroup);
            } else if (fromTableSource instanceof SQLExprTableSource) {
                handleExprTableSource((SQLExprTableSource) fromTableSource, etlGroupJoinInfo);
                etlGroup.getEtlGroupJoinInfoList().add(etlGroupJoinInfo);
            } else if (fromTableSource instanceof SQLSubqueryTableSource) {
                handleSubqueryTableSource((SQLSubqueryTableSource) fromTableSource, etlGroupJoinInfo);
                etlGroup.getEtlGroupJoinInfoList().add(etlGroupJoinInfo);
            } else {
                log.warn("deal SQLSelectQuery failed,not support yet:[{}],[{}]", sqlSelectQuery.getClass().getName(), sqlSelectQuery);
            }
        }
        return etlGroup;
    }

    private static void handleExprTableSource(SQLExprTableSource sqlTableSource, EtlGroupJoinInfo etlGroupJoinInfo) {
        SQLExprImpl exprImpl = (SQLExprImpl) sqlTableSource.getExpr();
        if (exprImpl instanceof SQLPropertyExpr) {
            SQLPropertyExpr propertyExpr = (SQLPropertyExpr) exprImpl;
            etlGroupJoinInfo.setSourceTableSchema(String.valueOf(propertyExpr.getOwner()));
            etlGroupJoinInfo.setSourceTableEnglishName(propertyExpr.getName());
        } else if (exprImpl instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) exprImpl;
            etlGroupJoinInfo.setSourceTableEnglishName(identifierExpr.getName());
            etlGroupJoinInfo.setSourceTableSchema("");
            log.error("关联表没有填写schema，表达式为[{}]", identifierExpr);
        } else {
            etlGroupJoinInfo.setSourceTableEnglishName(String.valueOf(exprImpl));
            etlGroupJoinInfo.setSourceTableSchema("");
            log.error("未支持类型:[{}]", sqlTableSource.getName());
        }
        etlGroupJoinInfo.setSourceTableAlias(sqlTableSource.getAlias());
        etlGroupJoinInfo.setComment(String.valueOf(sqlTableSource.getAttribute("rowFormat.after_comment")));
    }

    private static void handleSubqueryTableSource(SQLSubqueryTableSource subqueryTableSource, EtlGroupJoinInfo etlGroupJoinInfo) {
        SQLSelect sqlSelect = subqueryTableSource.getSelect();
        if (sqlSelect != null) {
            etlGroupJoinInfo.setSourceTableEnglishName("(" + sqlSelect + ")");
        } else {
            System.out.println("11");
        }
        etlGroupJoinInfo.setSourceTableAlias(subqueryTableSource.getAlias());
        etlGroupJoinInfo.setComment(String.valueOf(subqueryTableSource.getAttribute("rowFormat.after_comment")));
    }

    public static EtlGroup getTableSource(SQLTableSource sqlTableSource, EtlGroup etlGroup) {
        if (sqlTableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource table = (SQLJoinTableSource) sqlTableSource;
            handleJoinTableSourceRight(table, etlGroup);
            handleJoinTableSourceLeft(table, etlGroup);
        } else {
            log.warn("deal SQLTableSource failed,not support yet:[{}],[{}]", sqlTableSource.getClass().getName(), sqlTableSource);
        }
        return etlGroup;
    }
    private static void handleJoinTableSourceRight(SQLJoinTableSource table, EtlGroup etlGroup) {
        EtlGroupJoinInfo rightJoinInfo = new EtlGroupJoinInfo();
        rightJoinInfo.setJoinType(table.getJoinType().name);
        rightJoinInfo.setJoinCondition(String.valueOf(table.getCondition()));

        SQLTableSource tableRight = table.getRight();
        if (tableRight instanceof SQLExprTableSource) {
            handleExprTableSource((SQLExprTableSource) tableRight, rightJoinInfo);
        } else if (tableRight instanceof SQLSubqueryTableSource) {
            handleSubqueryTableSource((SQLSubqueryTableSource) tableRight, rightJoinInfo);
        } else {
            log.warn("Unsupported right table source type: {}", tableRight.getClass().getName());
        }
        etlGroup.getEtlGroupJoinInfoList().add(rightJoinInfo);
    }

    private static void handleJoinTableSourceLeft(SQLJoinTableSource table, EtlGroup etlGroup) {
        SQLTableSource tableLeft = table.getLeft();
        if (tableLeft instanceof SQLJoinTableSource) {
            getTableSource((SQLJoinTableSource) tableLeft, etlGroup);
        } else if (tableLeft instanceof SQLExprTableSource) {
            EtlGroupJoinInfo leftJoinInfo = new EtlGroupJoinInfo();
            handleExprTableSource((SQLExprTableSource) tableLeft, leftJoinInfo);
            etlGroup.getEtlGroupJoinInfoList().add(leftJoinInfo);
        } else if (tableLeft instanceof SQLSubqueryTableSource) {
            EtlGroupJoinInfo leftJoinInfo = new EtlGroupJoinInfo();
            handleSubqueryTableSource((SQLSubqueryTableSource) tableLeft, leftJoinInfo);
            etlGroup.getEtlGroupJoinInfoList().add(leftJoinInfo);
        } else {
            log.warn("Unsupported left table source type: {}", tableLeft.getClass().getName());
        }
    }

    public static String dealsql(String filePath) {
        return dealsql(filePath, "utf-8");
    }

    public static String dealsql(String filePath, String charSet) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charSet))) {
            StringBuffer contentBuffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                String processedLine = processLine(line);
                contentBuffer.append(processedLine).append("\r\n");
            }
            return contentBuffer.toString()
                    .replaceAll("distribute by hash\\(([^\\)]*)\\)", "")
                    .replaceAll("DISTRIBUTE BY HASH\\(([^\\)]*)\\)", "")
                    .replaceAll("with([^\\)]*)\\(([^\\)]*)\\)", "")
                    .replaceAll("with ([^\\)]*)\\(([^\\)]*)\\)", "")
                    .replaceAll("WITH ([^\\)]*)\\(([^\\)]*)\\)", "")
                    .replaceAll("to group group_version1", "")
                    .replaceAll("SIMILAR TO '", "= 'SIMILAR TO");
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }

    private static String processLine(String line) {
        String lowerLine = StringUtils.lowerCase(line).trim();
        if (isIgnoredLine(lowerLine)) {
            return "";
        }
        return line;
    }

    private static boolean isIgnoredLine(String line) {
        String[] ignoredPrefixes = {
                "\\timing", "\\echo", "begin", "do $$", "execute", "into",
                "if t_cnt > 0 then", "end;", "declare", "analyze", "drop",
                "end$", "end if;", "to group", "end ;"
        };
        for (String prefix : ignoredPrefixes) {
            if (line.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}