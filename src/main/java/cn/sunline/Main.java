package cn.sunline;

import java.util.HashMap;

import static cn.sunline.util.ArgsUtil.parseArgs;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")){
            //System.out.println(help_file);  //输出帮助说明
        }else{
            HashMap<String,String> args_map = parseArgs(args);
        }
    }
}