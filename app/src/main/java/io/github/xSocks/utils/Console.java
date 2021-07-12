package io.github.xSocks.utils;

public class Console {

    public static void runCommand(String[] cmds) {
        for (String cmd : cmds) {
            execCommand(cmd);
        }
    }

    public static Process execCommand(String command)   {
        try {
           return  Runtime.getRuntime().exec(command);
        }catch (java.io.IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
