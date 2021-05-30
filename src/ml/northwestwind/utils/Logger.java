package ml.northwestwind.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static void log(String color, Object info) {
        System.out.println(color + addDate() + info.toString());
    }

    public static void log(Object info) {
        log("", info);
    }

    private static String addDate() {
        return new SimpleDateFormat("[HH:mm:ss]").format(new Date(System.currentTimeMillis())) + " ";
    }
}
