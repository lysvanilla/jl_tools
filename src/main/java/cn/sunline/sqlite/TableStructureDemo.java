package cn.sunline.sqlite;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.Session;
import cn.sunline.table.ExcelTableStructureReader;
import cn.sunline.vo.TableStructure;
import cn.sunline.vo.TableFieldInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TableStructureDemo {

    // 创建表结构表和字段表
    public static void createTables() {
        try {
            // 创建表结构表
            String createTableStructureSql =
                    "CREATE TABLE IF NOT EXISTS table_structure (" +
                            "    id TEXT PRIMARY KEY," +
                            "    system_module TEXT," +
                            "    subject TEXT," +
                            "    table_name_en TEXT," +
                            "    table_name_cn TEXT," +
                            "    description TEXT," +
                            "    table_creation_type TEXT," +
                            "    algorithm_type TEXT," +
                            "    has_primary_key TEXT," +
                            "    partition_method TEXT," +
                            "    bucket_count TEXT," +
                            "    importance_level TEXT," +
                            "    online_time TEXT," +
                            "    downstream_applications TEXT," +
                            "    public_status TEXT," +
                            "    source_system TEXT," +
                            "    source_table_name_en TEXT," +
                            "    designer TEXT," +
                            "    status TEXT," +
                            "    update_date TEXT," +
                            "    remark TEXT," +
                            "    update_person TEXT" +
                            ")";

            // 修改字段表结构以匹配 TableFieldInfo
            String createTableFieldSql =
                    "CREATE TABLE IF NOT EXISTS table_field (" +
                            "    id TEXT," +
                            "    system_module TEXT," +
                            "    subject TEXT," +
                            "    table_name_en TEXT," +
                            "    table_name_cn TEXT," +
                            "    field_name_en TEXT," +
                            "    field_name_cn TEXT," +
                            "    primary_key TEXT," +
                            "    bucket_key TEXT," +
                            "    not_null TEXT," +
                            "    field_order INTEGER," +
                            "    field_type TEXT," +
                            "    part_key TEXT," +
                            "    table_creation_type TEXT," +
                            "    foreign_key TEXT," +
                            "    if_code_field TEXT," +
                            "    reference_code TEXT," +
                            "    code_description TEXT," +
                            "    check_rule TEXT," +
                            "    sensitive_type TEXT," +
                            "    online_time TEXT," +
                            "    source_system TEXT," +
                            "    downstream_applications TEXT," +
                            "    remark TEXT," +
                            "    update_date TEXT," +
                            "    update_person TEXT," +
                            "    source_field_name_en TEXT," +
                            "    FOREIGN KEY (table_name_en) REFERENCES table_structure(table_name_en)" +
                            ")";

            Db.use().execute(createTableStructureSql);
            Db.use().execute(createTableFieldSql);
            System.out.println("表创建成功！");
        } catch (SQLException e) {
            System.out.println("创建表失败: " + e.getMessage());
        }
    }

    // 根据表名删除记录
    private static void deleteByTableNames(Session session, LinkedHashMap<String, TableStructure> tableStructures) throws SQLException {
        // 收集所有表名
        List<String> tableNames = new ArrayList<>();
        for (TableStructure table : tableStructures.values()) {
            if (table.getTableNameEn() != null) {
                tableNames.add(table.getTableNameEn());
            }
        }

        if (tableNames.isEmpty()) {
            return;
        }

        // 构建IN条件的SQL
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                inClause.append(",");
            }
            inClause.append("?");
        }

        // 先删除字段表中的记录
        String deleteFieldsSql = "DELETE FROM table_field WHERE table_name_en IN (" +
                inClause.toString() + ")";

        // 再删除表结构记录
        String deleteTablesSql = "DELETE FROM table_structure WHERE table_name_en IN (" +
                inClause.toString() + ")";

        // 执行删除操作
        int deletedFields = session.execute(deleteFieldsSql, tableNames.toArray());
        int deletedTables = session.execute(deleteTablesSql, tableNames.toArray());

        System.out.println("删除字段记录数: " + deletedFields);
        System.out.println("删除表结构记录数: " + deletedTables);
    }

    // 修改保存方法，添加删除逻辑
    public static void saveTableStructures(LinkedHashMap<String, TableStructure> tableStructures) {
        Session session = null;
        try {
            session = Session.create();
            session.beginTransaction();

            // 先删除已存在的记录
            deleteByTableNames(session, tableStructures);

            // 批量保存表结构
            List<Entity> tableEntities = new ArrayList<>();
            List<Entity> fieldEntities = new ArrayList<>();

            for (TableStructure table : tableStructures.values()) {
                Entity tableEntity = Entity.create("table_structure")
                        .set("id", table.getId())
                        .set("system_module", table.getSystemModule())
                        .set("subject", table.getSubject())
                        .set("table_name_en", table.getTableNameEn())
                        .set("table_name_cn", table.getTableNameCn())
                        .set("description", table.getDescription())
                        .set("table_creation_type", table.getTableCreationType())
                        .set("algorithm_type", table.getAlgorithmType())
                        .set("has_primary_key", table.getHasPrimaryKey())
                        .set("partition_method", table.getPartitionMethod())
                        .set("bucket_count", table.getBucketCount())
                        .set("importance_level", table.getImportanceLevel())
                        .set("online_time", table.getOnlineTime())
                        .set("downstream_applications", table.getDownstreamApplications())
                        .set("public_status", table.getPublicStatus())
                        .set("source_system", table.getSourceSystem())
                        .set("source_table_name_en", table.getSourceTableNameEn())
                        .set("designer", table.getDesigner())
                        .set("status", table.getStatus())
                        .set("update_date", table.getUpdateDate())
                        .set("remark", table.getRemark())
                        .set("update_person", table.getUpdatePerson());

                tableEntities.add(tableEntity);

                // 收集字段信息
                if (table.getFields() != null) {
                    for (TableFieldInfo field : table.getFields()) {
                        Entity fieldEntity = Entity.create("table_field")
                                .set("id", field.getId())
                                .set("system_module", field.getSystemModule())
                                .set("subject", field.getSubject())
                                .set("table_name_en", field.getTableNameEn())
                                .set("table_name_cn", field.getTableNameCn())
                                .set("field_name_en", field.getFieldNameEn())
                                .set("field_name_cn", field.getFieldNameCn())
                                .set("primary_key", field.getPrimaryKey())
                                .set("bucket_key", field.getBucketKey())
                                .set("not_null", field.getNotNull())
                                .set("field_order", field.getFieldOrder())
                                .set("field_type", field.getFieldType())
                                .set("part_key", field.getPartKey())
                                .set("table_creation_type", field.getTableCreationType())
                                .set("foreign_key", field.getForeignKey())
                                .set("if_code_field", field.getIfCodeField())
                                .set("reference_code", field.getReferenceCode())
                                .set("code_description", field.getCodeDescription())
                                .set("check_rule", field.getCheckRule())
                                .set("sensitive_type", field.getSensitiveType())
                                .set("online_time", field.getOnlineTime())
                                .set("source_system", field.getSourceSystem())
                                .set("downstream_applications", field.getDownstreamApplications())
                                .set("remark", field.getRemark())
                                .set("update_date", field.getUpdateDate())
                                .set("update_person", field.getUpdatePerson())
                                .set("source_field_name_en", field.getSourceFieldNameEn());

                        fieldEntities.add(fieldEntity);
                    }
                }
            }

            // 批量插入表结构
            if (!tableEntities.isEmpty()) {
                int[] tableResults = session.insert(tableEntities);
                System.out.println("批量插入表结构记录数: " + tableResults.length);
            }

            // 批量插入字段信息
            if (!fieldEntities.isEmpty()) {
                int[] fieldResults = session.insert(fieldEntities);
                System.out.println("批量插入字段记录数: " + fieldResults.length);
            }

            session.commit();
            System.out.println("数据保存成功！");
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

    // 查询所有表结构信息
    public static LinkedHashMap<String, TableStructure> queryAllTableStructures() {
        LinkedHashMap<String, TableStructure> result = new LinkedHashMap<>();
        try {
            List<Entity> tableEntities = Db.use().findAll("table_structure");

            for (Entity tableEntity : tableEntities) {
                TableStructure table = new TableStructure();
                // 设置表基本信息
                table.setId(tableEntity.getStr("id"));
                table.setSystemModule(tableEntity.getStr("system_module"));
                table.setSubject(tableEntity.getStr("subject"));
                table.setTableNameEn(tableEntity.getStr("table_name_en"));
                table.setTableNameCn(tableEntity.getStr("table_name_cn"));
                table.setDescription(tableEntity.getStr("description"));
                table.setTableCreationType(tableEntity.getStr("table_creation_type"));
                table.setAlgorithmType(tableEntity.getStr("algorithm_type"));
                table.setHasPrimaryKey(tableEntity.getStr("has_primary_key"));
                table.setPartitionMethod(tableEntity.getStr("partition_method"));
                table.setBucketCount(tableEntity.getStr("bucket_count"));
                table.setImportanceLevel(tableEntity.getStr("importance_level"));
                table.setOnlineTime(tableEntity.getStr("online_time"));
                table.setDownstreamApplications(tableEntity.getStr("downstream_applications"));
                table.setPublicStatus(tableEntity.getStr("public_status"));
                table.setSourceSystem(tableEntity.getStr("source_system"));
                table.setSourceTableNameEn(tableEntity.getStr("source_table_name_en"));
                table.setDesigner(tableEntity.getStr("designer"));
                table.setStatus(tableEntity.getStr("status"));
                table.setUpdateDate(tableEntity.getStr("update_date"));
                table.setRemark(tableEntity.getStr("remark"));
                table.setUpdatePerson(tableEntity.getStr("update_person"));

                // 查询对应的字段信息
                List<Entity> fieldEntities = Db.use().find(
                        Entity.create("table_field").set("table_name_en", table.getTableNameEn())
                );

                for (Entity fieldEntity : fieldEntities) {
                    TableFieldInfo field = new TableFieldInfo();
                    field.setId(fieldEntity.getStr("id"));
                    field.setSystemModule(fieldEntity.getStr("system_module"));
                    field.setSubject(fieldEntity.getStr("subject"));
                    field.setTableNameEn(fieldEntity.getStr("table_name_en"));
                    field.setTableNameCn(fieldEntity.getStr("table_name_cn"));
                    field.setFieldNameEn(fieldEntity.getStr("field_name_en"));
                    field.setFieldNameCn(fieldEntity.getStr("field_name_cn"));
                    field.setPrimaryKey(fieldEntity.getStr("primary_key"));
                    field.setBucketKey(fieldEntity.getStr("bucket_key"));
                    field.setNotNull(fieldEntity.getStr("not_null"));
                    field.setFieldOrder(fieldEntity.getInt("field_order"));
                    field.setFieldType(fieldEntity.getStr("field_type"));
                    field.setPartKey(fieldEntity.getStr("part_key"));
                    field.setTableCreationType(fieldEntity.getStr("table_creation_type"));
                    field.setForeignKey(fieldEntity.getStr("foreign_key"));
                    field.setIfCodeField(fieldEntity.getStr("if_code_field"));
                    field.setReferenceCode(fieldEntity.getStr("reference_code"));
                    field.setCodeDescription(fieldEntity.getStr("code_description"));
                    field.setCheckRule(fieldEntity.getStr("check_rule"));
                    field.setSensitiveType(fieldEntity.getStr("sensitive_type"));
                    field.setOnlineTime(fieldEntity.getStr("online_time"));
                    field.setSourceSystem(fieldEntity.getStr("source_system"));
                    field.setDownstreamApplications(fieldEntity.getStr("downstream_applications"));
                    field.setRemark(fieldEntity.getStr("remark"));
                    field.setUpdateDate(fieldEntity.getStr("update_date"));
                    field.setUpdatePerson(fieldEntity.getStr("update_person"));
                    field.setSourceFieldNameEn(fieldEntity.getStr("source_field_name_en"));

                    table.addField(field);
                }

                result.put(table.getTableNameEn(), table);
            }
        } catch (SQLException e) {
            System.out.println("查询数据失败: " + e.getMessage());
        }
        return result;
    }

    // 测试示例
    public static void main(String[] args) {
        DatabaseConfigManager.getInstance();
        //GlobalDbConfig.setDbSettingPath(BasicInfo.dbConfigFile);
        // 创建表
        createTables();
        String filePath = "D:\\svn\\jilin\\03.模型设计\\风险数据集市物理模型-模板.xlsx";
        // 调用 readExcel 方法读取表结构信息
        LinkedHashMap<String, TableStructure> tableStructures  = ExcelTableStructureReader.readExcel(filePath);

        // 保存数据
        saveTableStructures(tableStructures );

        // 查询并打印数据
        LinkedHashMap<String, TableStructure> queriedData = queryAllTableStructures();
        printTableStructures(queriedData.values());
    }



    // 打印表结构信息
    private static void printTableStructures(Iterable<TableStructure> tables) {
        for (TableStructure table : tables) {
            System.out.println("\n表信息：");
            System.out.println("ID: " + table.getId());
            System.out.println("表名(英文): " + table.getTableNameEn());
            System.out.println("表名(中文): " + table.getTableNameCn());
            System.out.println("系统模块: " + table.getSystemModule());

            System.out.println("\n字段信息：");
            if (table.getFields() != null) {
                for (TableFieldInfo field : table.getFields()) {
                    System.out.printf("字段: %s(%s), 类型: %s, 主键: %s, 序号: %d%n",
                            field.getFieldNameEn(),
                            field.getFieldNameCn(),
                            field.getFieldType(),
                            field.getPrimaryKey(),
                            field.getFieldOrder() != null ? field.getFieldOrder() : 0);
                }
            }
        }
    }
}