package ml.northwestwind.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.fusesource.jansi.Ansi.Color;
import static org.fusesource.jansi.Ansi.ansi;

public class Logger {
    public static void log(Color color, Object info) {
        System.out.println(ansi().fg(color).a(addDate()).a(info.toString()).reset());
    }

    public static void log(Object info) {
        System.out.println(info.toString());
    }

    private static String addDate() {
        return new SimpleDateFormat("[HH:mm:ss]").format(new Date(System.currentTimeMillis())) + " ";
    }
}
