package fr.lightnew.npc.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLog {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[36m";

    private static final String prefixError = ANSI_RED + "[LiteFP ERROR] ";
    private static final String prefixInfo = ANSI_BLUE + "[LiteFP INFO] ";
    private static final String prefixSuccess = ANSI_GREEN + "[LiteFP SUCCESS] ";
    private static final String prefixDebug = "[LiteFP DEBUG] ";

    public static void debug(Object str) {
        System.out.println(prefixDebug + str + ANSI_RESET);
    }
    public static void debug(Object... str) {
        StringBuilder builder = new StringBuilder("[\n");
        for (Object o : str)
            builder.append(o + ",\n");
        builder.append("]");
        System.out.println(prefixDebug + builder.toString() + ANSI_RESET);
    }

    public static void error(Object str) {
        System.out.println(prefixError + str + ANSI_RESET);
    }

    public static void error(Object... str) {
        StringBuilder builder = new StringBuilder("\n[\n");
        for (Object o : str)
            builder.append(o + ",\n");
        builder.append("]");
        System.out.println(prefixError + builder.toString() + ANSI_RESET);
    }

    public static void info(Object str) {
        System.out.println(prefixInfo + str + ANSI_RESET);
    }

    public static void info(Object... str) {
        StringBuilder builder = new StringBuilder("\n[\n");
        for (Object o : str)
            builder.append(o + ",\n");
        builder.append("]");
        System.out.println(prefixInfo + builder.toString() + ANSI_RESET);
    }

    public static void basicInfo(Object str) {
        System.out.println(ANSI_RESET + str + ANSI_RESET);
    }

    public static void success(Object str) {
        System.out.println(prefixSuccess + str + ANSI_RESET);
    }

    private static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return "[" + sdf.format(new Date()) + "] ";
    }
}
