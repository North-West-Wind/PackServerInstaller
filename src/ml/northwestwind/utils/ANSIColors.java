package ml.northwestwind.utils;

public class ANSIColors {
    public static final String RESET = "\u001B[0m";
    public static final String YELLOW = getForegroundColor(255, 255, 0);
    public static final String CYAN = getForegroundColor(0, 255, 255);
    public static final String MAGENTA = getForegroundColor(255, 0, 255);
    public static final String GREEN = getForegroundColor(0, 255, 0);
    public static final String RED = getForegroundColor(255, 0, 0);

    public static String getForegroundColor(int r, int g, int b) {
        if (!isInRange(r, 0, 255) || !isInRange(g, 0, 255) || !isInRange(b, 0, 255)) return "";
        return String.format("\u001B[38;2;%d;%d;%dm", r, g, b);
    }

    private static boolean isInRange(int num, int min, int max) {
        return num <= max && num >= min;
    }
}
