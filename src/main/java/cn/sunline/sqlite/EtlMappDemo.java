package cn.sunline.sqlite;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.Session;
import cn.sunline.vo.etl.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static cn.sunline.mapping.EtlMappingExcelRead.readEtlMappExcel;

public class EtlMappDemo {

    // 创建ETL相关表
    public static void createTables() {
        try {
            // 创建ETL_MAPP表
            String createEtlMappSql =
                    "CREATE TABLE IF NOT EXISTS etl_mapp (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "sheet_name TEXT," +
                            "table_english_name TEXT," +
                            "table_chinese_name TEXT," +
                            "primary_key_field TEXT," +
                            "analyst TEXT," +
                            "attribution_level TEXT," +
                            "main_application TEXT," +
                            "time_granularity TEXT," +
                            "creation_date TEXT," +
                            "attribution_theme TEXT," +
                            "retention_period TEXT," +
                            "description TEXT," +
                            "initial_settings TEXT," +
                            "initial_load TEXT," +
                            "daily_load TEXT" +
                            ")";

            // 创建ETL_GROUP表
            String createEtlGroupSql =
                    "CREATE TABLE IF NOT EXISTS etl_group (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "mapp_id INTEGER," +
                            "group_id TEXT," +
                            "target_table_english_name TEXT," +
                            "target_table_chinese_name TEXT," +
                            "group_remarks TEXT," +
                            "template_type TEXT," +
                            "distribution_key TEXT," +
                            "filter_condition TEXT," +
                            "grouping_condition TEXT," +
                            "sorting_condition TEXT," +
                            "FOREIGN KEY (mapp_id) REFERENCES etl_mapp(id)" +
                            ")";

            // 创建ETL_GROUP_COL_MAPP表
            String createEtlGroupColMappSql =
                    "CREATE TABLE IF NOT EXISTS etl_group_col_mapp (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "group_id INTEGER," +
                            "source_table_schema TEXT," +
                            "source_table_english_name TEXT," +
                            "source_table_chinese_name TEXT," +
                            "source_field_english_name TEXT," +
                            "source_field_chinese_name TEXT," +
                            "source_system_code TEXT," +
                            "source_field_type TEXT," +
                            "target_table_english_name TEXT," +
                            "target_table_chinese_name TEXT," +
                            "target_field_english_name TEXT," +
                            "target_field_chinese_name TEXT," +
                            "target_field_type TEXT," +
                            "mapping_rule TEXT," +
                            "remarks TEXT," +
                            "FOREIGN KEY (group_id) REFERENCES etl_group(id)" +
                            ")";

            // 创建ETL_GROUP_JOIN_INFO表
            String createEtlGroupJoinInfoSql =
                    "CREATE TABLE IF NOT EXISTS etl_group_join_info (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "group_id INTEGER," +
                            "source_table_schema TEXT," +
                            "source_table_english_name TEXT," +
                            "source_table_chinese_name TEXT," +
                            "source_table_alias TEXT," +
                            "join_type TEXT," +
                            "join_condition TEXT," +
                            "comment TEXT," +
                            "FOREIGN KEY (group_id) REFERENCES etl_group(id)" +
                            ")";

            // 创建ETL_UPDATE_RECORD表
            String createEtlUpdateRecordSql =
                    "CREATE TABLE IF NOT EXISTS etl_update_record (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "mapp_id INTEGER," +
                            "date TEXT," +
                            "updater TEXT," +
                            "description TEXT," +
                            "FOREIGN KEY (mapp_id) REFERENCES etl_mapp(id)" +
                            ")";

            Db.use().execute(createEtlMappSql);
            Db.use().execute(createEtlGroupSql);
            Db.use().execute(createEtlGroupColMappSql);
            Db.use().execute(createEtlGroupJoinInfoSql);
            Db.use().execute(createEtlUpdateRecordSql);
            System.out.println("ETL相关表创建成功！");
        } catch (SQLException e) {
            System.out.println("创建表失败: " + e.getMessage());
        }
    }

    // 根据表英文名删除记录
    private static void deleteByTableNames(Session session, List<EtlMapp> etlMappList) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        for (EtlMapp mapp : etlMappList) {
            if (mapp.getTableEnglishName() != null) {
                tableNames.add(mapp.getTableEnglishName());
            }
        }

        if (tableNames.isEmpty()) {
            return;
        }

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                inClause.append(",");
            }
            inClause.append("?");
        }

        // 按照外键关系顺序删除
        String deleteUpdateRecordsSql = "DELETE FROM etl_update_record WHERE mapp_id IN " +
                "(SELECT id FROM etl_mapp WHERE table_english_name IN (" + inClause.toString() + "))";

        String deleteColMappsSql = "DELETE FROM etl_group_col_mapp WHERE group_id IN " +
                "(SELECT g.id FROM etl_group g JOIN etl_mapp m ON g.mapp_id = m.id " +
                "WHERE m.table_english_name IN (" + inClause.toString() + "))";

        String deleteJoinInfosSql = "DELETE FROM etl_group_join_info WHERE group_id IN " +
                "(SELECT g.id FROM etl_group g JOIN etl_mapp m ON g.mapp_id = m.id " +
                "WHERE m.table_english_name IN (" + inClause.toString() + "))";

        String deleteGroupsSql = "DELETE FROM etl_group WHERE mapp_id IN " +
                "(SELECT id FROM etl_mapp WHERE table_english_name IN (" + inClause.toString() + "))";

        String deleteMappsSql = "DELETE FROM etl_mapp WHERE table_english_name IN (" +
                inClause.toString() + ")";

        session.execute(deleteUpdateRecordsSql, tableNames.toArray());
        session.execute(deleteColMappsSql, tableNames.toArray());
        session.execute(deleteJoinInfosSql, tableNames.toArray());
        session.execute(deleteGroupsSql, tableNames.toArray());
        session.execute(deleteMappsSql, tableNames.toArray());
    }

    // 保存ETL映射数据
    public static void saveEtlMappList(List<EtlMapp> etlMappList) {
        Session session = null;
        try {
            session = Session.create();
            session.beginTransaction();

            // 先删除已存在的记录
            deleteByTableNames(session, etlMappList);

            for (EtlMapp mapp : etlMappList) {
                // 保存ETL_MAPP
                Entity mappEntity = Entity.create("etl_mapp")
                        .set("sheet_name", mapp.getSheetName())
                        .set("table_english_name", mapp.getTableEnglishName())
                        .set("table_chinese_name", mapp.getTableChineseName())
                        .set("primary_key_field", mapp.getPrimaryKeyField())
                        .set("analyst", mapp.getAnalyst())
                        .set("attribution_level", mapp.getAttributionLevel())
                        .set("main_application", mapp.getMainApplication())
                        .set("time_granularity", mapp.getTimeGranularity())
                        .set("creation_date", mapp.getCreationDate())
                        .set("attribution_theme", mapp.getAttributionTheme())
                        .set("retention_period", mapp.getRetentionPeriod())
                        .set("description", mapp.getDescription())
                        .set("initial_settings", mapp.getInitialSettings())
                        .set("initial_load", mapp.getInitialLoad())
                        .set("daily_load", mapp.getDailyLoad());

                Long mappId = session.insertForGeneratedKey(mappEntity).longValue();

                // 保存ETL_GROUP及相关数据
                if (mapp.getEtlGroupList() != null) {
                    for (EtlGroup group : mapp.getEtlGroupList()) {
                        Entity groupEntity = Entity.create("etl_group")
                                .set("mapp_id", mappId)
                                .set("group_id", group.getGroupId())
                                .set("target_table_english_name", group.getTargetTableEnglishName())
                                .set("target_table_chinese_name", group.getTargetTableChineseName())
                                .set("group_remarks", group.getGroupRemarks())
                                .set("template_type", group.getTemplateType())
                                .set("distribution_key", group.getDistributionKey())
                                .set("filter_condition", group.getFilterCondition())
                                .set("grouping_condition", group.getGroupingCondition())
                                .set("sorting_condition", group.getSortingCondition());

                        Long groupId = session.insertForGeneratedKey(groupEntity).longValue();

                        // 保存列映射信息
                        if (group.getEtlGroupColMappList() != null) {
                            for (EtlGroupColMapp colMapp : group.getEtlGroupColMappList()) {
                                Entity colMappEntity = Entity.create("etl_group_col_mapp")
                                        .set("group_id", groupId)
                                        .set("source_table_schema", colMapp.getSourceTableSchema())
                                        .set("source_table_english_name", colMapp.getSourceTableEnglishName())
                                        .set("source_table_chinese_name", colMapp.getSourceTableChineseName())
                                        .set("source_field_english_name", colMapp.getSourceFieldEnglishName())
                                        .set("source_field_chinese_name", colMapp.getSourceFieldChineseName())
                                        .set("source_system_code", colMapp.getSourceSystemCode())
                                        .set("source_field_type", colMapp.getSourceFieldType())
                                        .set("target_table_english_name", colMapp.getTargetTableEnglishName())
                                        .set("target_table_chinese_name", colMapp.getTargetTableChineseName())
                                        .set("target_field_english_name", colMapp.getTargetFieldEnglishName())
                                        .set("target_field_chinese_name", colMapp.getTargetFieldChineseName())
                                        .set("target_field_type", colMapp.getTargetFieldType())
                                        .set("mapping_rule", colMapp.getMappingRule())
                                        .set("remarks", colMapp.getRemarks());

                                session.insert(colMappEntity);
                            }
                        }

                        // 保存连接信息
                        if (group.getEtlGroupJoinInfoList() != null) {
                            for (EtlGroupJoinInfo joinInfo : group.getEtlGroupJoinInfoList()) {
                                Entity joinInfoEntity = Entity.create("etl_group_join_info")
                                        .set("group_id", groupId)
                                        .set("source_table_schema", joinInfo.getSourceTableSchema())
                                        .set("source_table_english_name", joinInfo.getSourceTableEnglishName())
                                        .set("source_table_chinese_name", joinInfo.getSourceTableChineseName())
                                        .set("source_table_alias", joinInfo.getSourceTableAlias())
                                        .set("join_type", joinInfo.getJoinType())
                                        .set("join_condition", joinInfo.getJoinCondition())
                                        .set("comment", joinInfo.getComment());

                                session.insert(joinInfoEntity);
                            }
                        }
                    }
                }

                // 保存更新记录
                if (mapp.getEtlUpdateRecordList() != null) {
                    for (EtlUpdateRecord updateRecord : mapp.getEtlUpdateRecordList()) {
                        Entity updateRecordEntity = Entity.create("etl_update_record")
                                .set("mapp_id", mappId)
                                .set("date", updateRecord.getDate())
                                .set("updater", updateRecord.getUpdater())
                                .set("description", updateRecord.getDescription());

                        session.insert(updateRecordEntity);
                    }
                }
            }

            session.commit();
            System.out.println("ETL映射数据保存成功！");
        } catch (SQLException e) {
            if (session != null) {
                try {
                    session.rollback();
                } catch (SQLException ex) {
                    System.out.println("事务回滚失败: " + ex.getMessage());
                }
            }
            System.out.println("保存数据失败: " + e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // 查询所有ETL映射数据
    public static List<EtlMapp> queryAllEtlMappList() {
        List<EtlMapp> result = new ArrayList<>();
        try {
            List<Entity> mappEntities = Db.use().findAll("etl_mapp");

            for (Entity mappEntity : mappEntities) {
                EtlMapp mapp = new EtlMapp();
                // 设置ETL_MAPP基本信息
                mapp.setSheetName(mappEntity.getStr("sheet_name"));
                mapp.setTableEnglishName(mappEntity.getStr("table_english_name"));
                mapp.setTableChineseName(mappEntity.getStr("table_chinese_name"));
                mapp.setPrimaryKeyField(mappEntity.getStr("primary_key_field"));
                mapp.setAnalyst(mappEntity.getStr("analyst"));
                mapp.setAttributionLevel(mappEntity.getStr("attribution_level"));
                mapp.setMainApplication(mappEntity.getStr("main_application"));
                mapp.setTimeGranularity(mappEntity.getStr("time_granularity"));
                mapp.setCreationDate(mappEntity.getStr("creation_date"));
                mapp.setAttributionTheme(mappEntity.getStr("attribution_theme"));
                mapp.setRetentionPeriod(mappEntity.getStr("retention_period"));
                mapp.setDescription(mappEntity.getStr("description"));
                mapp.setInitialSettings(mappEntity.getStr("initial_settings"));
                mapp.setInitialLoad(mappEntity.getStr("initial_load"));
                mapp.setDailyLoad(mappEntity.getStr("daily_load"));

                Long mappId = mappEntity.getLong("id");

                // 查询ETL_GROUP
                List<Entity> groupEntities = Db.use().find(
                        Entity.create("etl_group").set("mapp_id", mappId)
                );

                for (Entity groupEntity : groupEntities) {
                    EtlGroup group = new EtlGroup();
                    group.setGroupId(groupEntity.getStr("group_id"));
                    group.setTargetTableEnglishName(groupEntity.getStr("target_table_english_name"));
                    group.setTargetTableChineseName(groupEntity.getStr("target_table_chinese_name"));
                    group.setGroupRemarks(groupEntity.getStr("group_remarks"));
                    group.setTemplateType(groupEntity.getStr("template_type"));
                    group.setDistributionKey(groupEntity.getStr("distribution_key"));
                    group.setFilterCondition(groupEntity.getStr("filter_condition"));
                    group.setGroupingCondition(groupEntity.getStr("grouping_condition"));
                    group.setSortingCondition(groupEntity.getStr("sorting_condition"));

                    Long groupId = groupEntity.getLong("id");

                    // 查询列映射信息
                    List<Entity> colMappEntities = Db.use().find(
                            Entity.create("etl_group_col_mapp").set("group_id", groupId)
                    );

                    for (Entity colMappEntity : colMappEntities) {
                        EtlGroupColMapp colMapp = new EtlGroupColMapp();
                        colMapp.setSourceTableSchema(colMappEntity.getStr("source_table_schema"));
                        colMapp.setSourceTableEnglishName(colMappEntity.getStr("source_table_english_name"));
                        colMapp.setSourceTableChineseName(colMappEntity.getStr("source_table_chinese_name"));
                        colMapp.setSourceFieldEnglishName(colMappEntity.getStr("source_field_english_name"));
                        colMapp.setSourceFieldChineseName(colMappEntity.getStr("source_field_chinese_name"));
                        colMapp.setSourceSystemCode(colMappEntity.getStr("source_system_code"));
                        colMapp.setSourceFieldType(colMappEntity.getStr("source_field_type"));
                        colMapp.setTargetTableEnglishName(colMappEntity.getStr("target_table_english_name"));
                        colMapp.setTargetTableChineseName(colMappEntity.getStr("target_table_chinese_name"));
                        colMapp.setTargetFieldEnglishName(colMappEntity.getStr("target_field_english_name"));
                        colMapp.setTargetFieldChineseName(colMappEntity.getStr("target_field_chinese_name"));
                        colMapp.setTargetFieldType(colMappEntity.getStr("target_field_type"));
                        colMapp.setMappingRule(colMappEntity.getStr("mapping_rule"));
                        colMapp.setRemarks(colMappEntity.getStr("remarks"));

                        group.getEtlGroupColMappList().add(colMapp);
                    }

                    // 查询连接信息
                    List<Entity> joinInfoEntities = Db.use().find(
                            Entity.create("etl_group_join_info").set("group_id", groupId)
                    );

                    for (Entity joinInfoEntity : joinInfoEntities) {
                        EtlGroupJoinInfo joinInfo = new EtlGroupJoinInfo();
                        joinInfo.setSourceTableSchema(joinInfoEntity.getStr("source_table_schema"));
                        joinInfo.setSourceTableEnglishName(joinInfoEntity.getStr("source_table_english_name"));
                        joinInfo.setSourceTableChineseName(joinInfoEntity.getStr("source_table_chinese_name"));
                        joinInfo.setSourceTableAlias(joinInfoEntity.getStr("source_table_alias"));
                        joinInfo.setJoinType(joinInfoEntity.getStr("join_type"));
                        joinInfo.setJoinCondition(joinInfoEntity.getStr("join_condition"));
                        joinInfo.setComment(joinInfoEntity.getStr("comment"));

                        group.getEtlGroupJoinInfoList().add(joinInfo);
                    }

                    mapp.addEtlGroup(group);
                }

                // 查询更新记录
                List<Entity> updateRecordEntities = Db.use().find(
                        Entity.create("etl_update_record").set("mapp_id", mappId)
                );

                for (Entity updateRecordEntity : updateRecordEntities) {
                    EtlUpdateRecord updateRecord = new EtlUpdateRecord();
                    updateRecord.setDate(updateRecordEntity.getStr("date"));
                    updateRecord.setUpdater(updateRecordEntity.getStr("updater"));
                    updateRecord.setDescription(updateRecordEntity.getStr("description"));

                    mapp.addEtlUpdateRecord(updateRecord);
                }

                result.add(mapp);
            }
        } catch (SQLException e) {
            System.out.println("查询数据失败: " + e.getMessage());
        }
        return result;
    }

    // 测试示例
    public static void main(String[] args) {
        DatabaseConfigManager.getInstance();
        // 创建表
        createTables();

        // 创建测试数据
        List<EtlMapp> etlMappList = readEtlMappExcel("D:\\svn\\jilin\\04.映射设计\\0402.计量模型层\\宝奇订单指标表.xlsx");

        // 保存数据
        saveEtlMappList(etlMappList);

        // 查询并打印数据
        List<EtlMapp> queriedData = queryAllEtlMappList();
        printEtlMappList(queriedData);
    }


    // 打印ETL映射信息
    private static void printEtlMappList(List<EtlMapp> etlMappList) {
        for (EtlMapp mapp : etlMappList) {
            System.out.println("\nETL映射信息：");
            System.out.println("表名(英文): " + mapp.getTableEnglishName());
            System.out.println("表名(中文): " + mapp.getTableChineseName());
            System.out.println("分析人员: " + mapp.getAnalyst());

            if (mapp.getEtlGroupList() != null) {
                for (EtlGroup group : mapp.getEtlGroupList()) {
                    System.out.println("\n分组信息：");
                    System.out.println("分组ID: " + group.getGroupId());
                    System.out.println("目标表名: " + group.getTargetTableEnglishName());

                    System.out.println("\n字段映射信息：");
                    for (EtlGroupColMapp colMapp : group.getEtlGroupColMappList()) {
                        System.out.printf("源字段: %s(%s) -> 目标字段: %s(%s)%n",
                                colMapp.getSourceFieldEnglishName(),
                                colMapp.getSourceFieldChineseName(),
                                colMapp.getTargetFieldEnglishName(),
                                colMapp.getTargetFieldChineseName());
                    }
                }
            }
        }
    }
} 