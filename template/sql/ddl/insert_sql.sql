/*========================================================================================
 # 作业名称:      ${table_schema}.${table_name_en_lower}
 # 编写人:        ${mapping_analyst}
 # 首次编写日期:  ${create_time}
 # 功能描述：     ${table_name_cn}
 # 需求来源：
 # 源表(依赖表):  ${table_schema}.${src_table_name_en_lower}
 # 加载策略:      每日全量
 # 加工频率:      日
 # 备注:          ${table_name_cn}
 # 修改历史:
 # 版本                 更改日期                     更改人               更改说明
========================================================================================*/



-- 先清除当天分区 "1.delete etl_date data"
delete from ${table_schema}.${table_name_en_lower} where PART_DT='${etl_date}';

set argodb.dynamic.create.partition.enabled=false;
set hive.exec.dynamic.partition=true;
set stargate.dynamic.partition.enabled=true;

-----------------------------------------------------------------------------------------

-- 数据加工 "2.insert etl_date data"
insert into ${table_schema}.${table_name_en_lower}
(
    ,@{column_name_en}  --@{column_name_cn}
)
select
     ,t1.@{src_column_name_en} --@{column_name_cn}
from ${table_schema}.${src_table_name_en_lower} t1
where t1.PART_DT='${etl_date}';

commit;