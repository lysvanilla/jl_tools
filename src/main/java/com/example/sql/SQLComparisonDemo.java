package com.example.sql;

public class SQLComparisonDemo {
    public static void main(String[] args) {
        // Example SQL statements
        String sql1 = "SELECT CAST(#{DATA_DT} AS VARCHAR(10)) AS DATA_DT, " +
                     "TMP.CUSTTP3 AS CUSTTP3, " +
                     "TMP.CUST_NAME AS CUST_NAME, " +
                     "TMP.BRCHNO AS BRCHNO, " +
                     "((CASE WHEN sum(T0_RMB_BAL) IS NULL THEN 0 ELSE sum(T0_RMB_BAL) END)) AS INDEX_VAL " +
                     "FROM (SELECT (CASE WHEN T1.CUST_ID IS NULL THEN T0.CUST_ID ELSE T1.CUST_ID END) AS CUSTTP3, " +
                     "(CASE WHEN T1.CUST_NAME IS NULL THEN T0.CUST_NAME ELSE T1.CUST_NAME END) AS CUST_NAME, " +
                     "T1.CREATE_ORG AS BRCHNO, T0.CUST_ID AS T0_CUST_ID, T0.CUST_NAME AS T0_CUST_NAME, " +
                     "T0.CUST_TYPE_CD AS T0_CUST_TYPE_CD, T0.DATA_DATE AS T0_DATA_DATE, " +
                     "T0.IS_NO_VIR_ACCT AS T0_IS_NO_VIR_ACCT, T0.PROD_CD AS T0_PROD_CD, " +
                     "T0.RMB_BAL AS T0_RMB_BAL, T1.CREATE_ORG AS T1_CREATE_ORG " +
                     "FROM HUNAN_POC.L_AGM_DPT_ACCT_INFO_I T0 " +
                     "LEFT JOIN HUNAN_POC.L_W_CUST_PRIV_CUST_BASE T1 " +
                     "ON T0.CUST_ID = T1.CUST_ID AND T0.CUST_NAME = T1.CUST_NAME AND T0.DATA_DATE = T1.DATA_DATE " +
                     "WHERE (T0.DATA_DATE = TO_CHAR(TO_DATE(#{DATA_DT},'yyyyMMdd'),'yyyyMMdd')) " +
                     "AND(T0.PROD_CD NOT LIKE '018%') " +
                     "AND(T0.IS_NO_VIR_ACCT = 'N') " +
                     "AND(T0.CUST_TYPE_CD = '1')) TMP " +
                     "GROUP BY CUSTTP3, CUST_NAME, BRCHNO";

        String sql2 = "SELECT CAST(#{DATA_DT} AS VARCHAR(10)) AS DATA_DT, " +
                     "TMP.CUSTTP3 AS CUSTTP3, " +
                     "TMP.CUST_NAME AS CUST_NAME, " +
                     "TMP.BRCHNO AS BRCHNO, " +
                     "((CASE WHEN sum(T0_RMB_BAL) IS NULL THEN 0 ELSE sum(T0_RMB_BAL) END)) AS INDEX_VAL " +
                     "FROM (SELECT (CASE WHEN T1.CUST_ID IS NULL THEN T0.CUST_ID ELSE T1.CUST_ID END) AS CUSTTP3, " +
                     "(CASE WHEN T1.CUST_NAME IS NULL THEN T0.CUST_NAME ELSE T1.CUST_NAME END) AS CUST_NAME, " +
                     "T1.CREATE_ORG AS BRCHNO, T0.CUST_ID AS T0_CUST_ID, T0.CUST_NAME AS T0_CUST_NAME, " +
                     "T0.CUST_TYPE_CD AS T0_CUST_TYPE_CD, T0.DATA_DATE AS T0_DATA_DATE, " +
                     "T0.IS_NO_VIR_ACCT AS T0_IS_NO_VIR_ACCT, T0.PROD_CD AS T0_PROD_CD, " +
                     "T0.RMB_BAL AS T0_RMB_BAL, T1.CREATE_ORG AS T1_CREATE_ORG " +
                     "FROM HUNAN_POC.L_AGM_DPT_ACCT_INFO_I T0 " +
                     "LEFT JOIN HUNAN_POC.L_W_CUST_PRIV_CUST_BASE T1 " +
                     "ON T0.CUST_ID = T1.CUST_ID AND T0.CUST_NAME = T1.CUST_NAME AND T0.DATA_DATE = T1.DATA_DATE " +
                     "WHERE (T0.DATA_DATE = TO_CHAR(TO_DATE(#{DATA_DT},'yyyyMMdd'),'yyyyMMdd')) " +
                     "AND(T0.PROD_CD NOT LIKE '018%') " +
                     "AND(T0.CUST_TYPE_CD = '1') " +
                     "AND(T0.IS_NO_VIR_ACCT = 'N')) TMP " +
                     "GROUP BY CUSTTP3, CUST_NAME, BRCHNO";

        String sql3 = "SELECT CAST(#{DATA_DT} AS VARCHAR(10)) AS DATA_DT, " +
                     "TMP.CUSTTP3 AS CUSTTP3, " +
                     "TMP.CUST_NAME AS CUST_NAME, " +
                     "TMP.BRCHNO AS BRCHNO, " +
                     "((CASE WHEN sum(T0_RMB_BAL) IS NULL THEN 0 ELSE sum(T0_RMB_BAL) END)) AS INDEX_VAL " +
                     "FROM (SELECT (CASE WHEN T1.CUST_ID IS NULL THEN T0.CUST_ID ELSE T1.CUST_ID END) AS CUSTTP3, " +
                     "(CASE WHEN T1.CUST_NAME IS NULL THEN T0.CUST_NAME ELSE T1.CUST_NAME END) AS CUST_NAME, " +
                     "T1.CREATE_ORG AS BRCHNO, T0.CUST_ID AS T0_CUST_ID, T0.CUST_NAME AS T0_CUST_NAME, " +
                     "T0.CUST_TYPE_CD AS T0_CUST_TYPE_CD, T0.DATA_DATE AS T0_DATA_DATE, " +
                     "T0.IS_NO_VIR_ACCT AS T0_IS_NO_VIR_ACCT, T0.PROD_CD AS T0_PROD_CD, " +
                     "T0.RMB_BAL AS T0_RMB_BAL, T1.CREATE_ORG AS T1_CREATE_ORG " +
                     "FROM HUNAN_POC.L_AGM_DPT_ACCT_INFO_I T0 " +
                     "LEFT JOIN HUNAN_POC.L_W_CUST_PRIV_CUST_BASE T1 " +
                     "ON T0.CUST_ID = T1.CUST_ID AND T0.CUST_NAME = T1.CUST_NAME AND T0.DATA_DATE = T1.DATA_DATE " +
                     "WHERE (T0.DATA_DATE = TO_CHAR(TO_DATE(#{DATA_DT},'yyyyMMdd'),'yyyyMMdd')) " +
                     "AND(T0.PROD_CD NOT LIKE '018%') " +
                     "AND(T0.CUST_TYPE_CD = '1') " +
                     "OR(T0.IS_NO_VIR_ACCT = 'N')) TMP " +
                     "GROUP BY CUSTTP3, CUST_NAME, BRCHNO";

        SQLComparisonService service = new SQLComparisonService();

        // Compare SQL1 and SQL2
        System.out.println("Comparing SQL1 and SQL2:");
        SQLComparisonService.ComparisonResult result1 = service.compareSQLStatements(sql1, sql2);
        System.out.println("Are equivalent: " + result1.isEquivalent());
        if (!result1.isEquivalent()) {
            System.out.println("Differences:");
            result1.getDifferences().forEach(System.out::println);
        }
        System.out.println();

        // Compare SQL1 and SQL3
        System.out.println("Comparing SQL1 and SQL3:");
        SQLComparisonService.ComparisonResult result2 = service.compareSQLStatements(sql1, sql3);
        System.out.println("Are equivalent: " + result2.isEquivalent());
        if (!result2.isEquivalent()) {
            System.out.println("Differences:");
            result2.getDifferences().forEach(System.out::println);
        }
    }
} 