-- ${target_table_cn_name} ${target_table_comment}
drop table if exists ${target_table_schema}.${target_table_name};

create table ${target_table_schema}.${target_table_name} (
${target_column_names_with_data_type}
)
with(orientation = column)
 distribute by hash(
     @{db_key}
 )
;

commit;

