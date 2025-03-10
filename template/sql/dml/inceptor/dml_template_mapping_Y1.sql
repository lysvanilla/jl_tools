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

insert into ${target_table_schema}.${target_table_name}(
${target_column_names}
)
select
${mapping_detail}
${table_relation}
${where_condition}
${groupby_condition}
${orderby_condition}
;

commit;

