package cn.sunline.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ArgsUtil {
    public static HashMap<String,String> parseArgs(List<String> arg_list){
        HashMap<String,String> args_map = new HashMap<>();
        arg_list.forEach(x -> {
            x = x.replace("\"","").replace("'","");
            int idx = x.indexOf("=");
            if(idx>0){
                String key = x.substring(0,idx);
                String value = x.substring(idx+1);
                args_map.put(key,value);
            }
        });
        return args_map;
    }

    public static HashMap<String,String> parseArgs(String[] arg_arr){
        List<String> arg_list = Arrays.asList(arg_arr);
        return parseArgs(arg_list);
    }


}
