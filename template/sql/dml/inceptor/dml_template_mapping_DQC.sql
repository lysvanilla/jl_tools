-- 主键检查 ${target_table_cn_name} ${target_table_comment}
\echo "PK check ${target_table_cn_name} ${target_table_comment}"
delete from ${dqc_schema}.${lvl}_table_pk_check where etl_dt = to_date('${batch_date}','yyyymmdd') and table_en_name = '${lvl}.${target_table_name}';

insert into ${dqc_schema}.${lvl}_table_pk_check(
    table_en_name  -- 表英文名
    ,table_cn_name  -- 表中文名
    ,pk_field  -- 主键字段
    ,repeat_qty  -- 重复数量
    ,etl_dt -- ETL处理日期
)
select
    table_en_name as table_en_name
    ,'${target_table_ch_name}' as table_cn_name
    ,'${pk_field}' as pk_field
    ,sum(cnt) as cnt
    to_date('${batch_date}', 'yyyymmdd') as etl_dt
from (select '${lvl}.${target_table_name}' as table_en_name
             ,${pk_field_col}
             ,count(*) as cnt
      from ${lvl_schema}.${target_table_name}
      where etl_dt = to_date('${batch_date}', 'yyyymmdd')
      group by ${pk_field}
      having count(1) > 1) t
group by table_en_name;

commit;

