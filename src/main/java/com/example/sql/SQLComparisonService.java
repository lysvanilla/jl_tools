package com.example.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.*;
import java.util.regex.Pattern;

public class SQLComparisonService {
    
    public static class ComparisonResult {
        private boolean isEquivalent;
        private List<String> differences;
        
        public ComparisonResult(boolean isEquivalent, List<String> differences) {
            this.isEquivalent = isEquivalent;
            this.differences = differences;
        }
        
        public boolean isEquivalent() {
            return isEquivalent;
        }
        
        public List<String> getDifferences() {
            return differences;
        }
    }
    
    private String preprocessSQL(String sql) {
        // Replace parameter placeholders with a value that JSqlParser can handle
        return sql.replaceAll("#\\{([^}]+)\\}", "'PARAM_$1'");
    }
    
    public ComparisonResult compareSQLStatements(String sql1, String sql2) {
        List<String> differences = new ArrayList<>();
        
        try {
            // Preprocess both SQL statements
            String processedSql1 = preprocessSQL(sql1);
            String processedSql2 = preprocessSQL(sql2);
            
            Statement stmt1 = CCJSqlParserUtil.parse(processedSql1);
            Statement stmt2 = CCJSqlParserUtil.parse(processedSql2);
            
            if (!(stmt1 instanceof Select) || !(stmt2 instanceof Select)) {
                differences.add("Both statements must be SELECT statements");
                return new ComparisonResult(false, differences);
            }
            
            Select select1 = (Select) stmt1;
            Select select2 = (Select) stmt2;
            
            // Compare select items
            compareSelectItems(select1.getSelectBody(), select2.getSelectBody(), differences);
            
            // Compare tables
            compareTables(select1.getSelectBody(), select2.getSelectBody(), differences);
            
            // Compare where conditions
            compareWhereConditions(select1.getSelectBody(), select2.getSelectBody(), differences);
            
            // Compare group by
            compareGroupBy(select1.getSelectBody(), select2.getSelectBody(), differences);
            
            return new ComparisonResult(differences.isEmpty(), differences);
            
        } catch (JSQLParserException e) {
            differences.add("Error parsing SQL: " + e.getMessage());
            return new ComparisonResult(false, differences);
        }
    }
    
    private void compareSelectItems(SelectBody body1, SelectBody body2, List<String> differences) {
        if (!(body1 instanceof PlainSelect) || !(body2 instanceof PlainSelect)) {
            differences.add("Complex SELECT statements are not supported");
            return;
        }
        
        PlainSelect ps1 = (PlainSelect) body1;
        PlainSelect ps2 = (PlainSelect) body2;
        
        List<SelectItem> items1 = ps1.getSelectItems();
        List<SelectItem> items2 = ps2.getSelectItems();
        
        if (items1.size() != items2.size()) {
            differences.add("Different number of selected columns");
            return;
        }
        
        for (int i = 0; i < items1.size(); i++) {
            if (!items1.get(i).toString().equals(items2.get(i).toString())) {
                differences.add("Different select items at position " + (i + 1));
            }
        }
    }
    
    private void compareTables(SelectBody body1, SelectBody body2, List<String> differences) {
        if (!(body1 instanceof PlainSelect) || !(body2 instanceof PlainSelect)) {
            return;
        }
        
        PlainSelect ps1 = (PlainSelect) body1;
        PlainSelect ps2 = (PlainSelect) body2;
        
        Set<String> tables1 = new HashSet<>();
        Set<String> tables2 = new HashSet<>();
        
        // Get tables from FROM clause
        if (ps1.getFromItem() != null) {
            tables1.add(ps1.getFromItem().toString());
        }
        if (ps2.getFromItem() != null) {
            tables2.add(ps2.getFromItem().toString());
        }
        
        // Get tables from JOIN clauses
        if (ps1.getJoins() != null) {
            ps1.getJoins().forEach(join -> tables1.add(join.getRightItem().toString()));
        }
        if (ps2.getJoins() != null) {
            ps2.getJoins().forEach(join -> tables2.add(join.getRightItem().toString()));
        }
        
        if (!tables1.equals(tables2)) {
            differences.add("Different tables used in the queries");
        }
    }
    
    private void compareWhereConditions(SelectBody body1, SelectBody body2, List<String> differences) {
        if (!(body1 instanceof PlainSelect) || !(body2 instanceof PlainSelect)) {
            return;
        }
        
        PlainSelect ps1 = (PlainSelect) body1;
        PlainSelect ps2 = (PlainSelect) body2;
        
        Expression where1 = ps1.getWhere();
        Expression where2 = ps2.getWhere();
        
        if (where1 == null && where2 == null) {
            return;
        }
        
        if (where1 == null || where2 == null) {
            differences.add("One query has WHERE clause while the other doesn't");
            return;
        }
        
        // Normalize the WHERE conditions by sorting AND/OR conditions
        String normalizedWhere1 = normalizeWhereCondition(where1.toString());
        String normalizedWhere2 = normalizeWhereCondition(where2.toString());
        
        if (!normalizedWhere1.equals(normalizedWhere2)) {
            differences.add("Different WHERE conditions");
        }
    }
    
    private String normalizeWhereCondition(String where) {
        // Split by AND/OR and sort the conditions
        String[] conditions = where.split("(?i)\\s+(AND|OR)\\s+");
        Arrays.sort(conditions);
        return String.join(" AND ", conditions);
    }
    
    private void compareGroupBy(SelectBody body1, SelectBody body2, List<String> differences) {
        if (!(body1 instanceof PlainSelect) || !(body2 instanceof PlainSelect)) {
            return;
        }
        
        PlainSelect ps1 = (PlainSelect) body1;
        PlainSelect ps2 = (PlainSelect) body2;
        
        List<Expression> groupBy1 = ps1.getGroupBy() != null ? ps1.getGroupBy().getGroupByExpressions() : null;
        List<Expression> groupBy2 = ps2.getGroupBy() != null ? ps2.getGroupBy().getGroupByExpressions() : null;
        
        if (groupBy1 == null && groupBy2 == null) {
            return;
        }
        
        if (groupBy1 == null || groupBy2 == null) {
            differences.add("One query has GROUP BY while the other doesn't");
            return;
        }
        
        if (groupBy1.size() != groupBy2.size()) {
            differences.add("Different number of GROUP BY expressions");
            return;
        }
        
        for (int i = 0; i < groupBy1.size(); i++) {
            if (!groupBy1.get(i).toString().equals(groupBy2.get(i).toString())) {
                differences.add("Different GROUP BY expressions at position " + (i + 1));
            }
        }
    }
} 