-- 特殊删除语句 ${target_table_cn_name} ${target_table_comment}
\echo "delete ${target_table_cn_name} ${target_table_comment}"

delete from ${target_table_schema}.${target_table_name}
${where_condition};
commit;

