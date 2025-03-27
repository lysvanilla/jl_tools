/*
Purpose:    快照/流水表建表脚本，此脚本由生成引擎自动生成。
Author:     Sunline
CreateDate: 20250305
FileType:   DDL
Logs:
    sunlinedata 2025-03-05 新建脚本
    Version: 1.2
*/
-- 1.0 drop table if exists table
drop table if exists ${table_name_en};

-- 1.1 create table
create table ${table_name_en}
(
    ,@{column_name_en} @{column_type} @{if_null} comment '@{column_name_cn}'
)ENGINE=OLAP
DUPLICATE KEY(${primaryKey})
PARTITIONED BY RANGE (DATA_DATE)
(
    PARTITION p202412 VALUES LESS THAN (202412),
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    PARTITION p202503 VALUES LESS THAN (202504)
)
DISTRIBUTED BY HASH(${bucketKey}) BUCKETS 10
comment '${table_name_cn}';    
