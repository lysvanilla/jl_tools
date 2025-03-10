/*========================================================================================
 # 作业名称:      ${target_table_schema}.${target_table_en_name}
 # 编写人:        ${mapping_analyst}
 # 首次编写日期:  ${create_time}
 # 功能描述：     ${target_table_cn_name}
 # 需求来源： 
 # 源表(依赖表)    
 # 01：
 # 02：
 # 03：
 # 加载策略:      每日全量
 # 加工频率:      ${time_granule}
 # 备注:          ${table_comment}
 # 修改历史:      
 # 版本                 更改日期                     更改人               更改说明${update_log}   
========================================================================================*/



-- 先清除当天分区 "1.delete etl_date data"
delete from ${target_table_schema}.${target_table_en_name} where PART_DT='${etl_date}';

set argodb.dynamic.create.partition.enabled=false;
set hive.exec.dynamic.partition=true;
set stargate.dynamic.partition.enabled=true;

-----------------------------------------------------------------------------------------

-- 数据加工 "2.insert etl_date data"
${mapping}



