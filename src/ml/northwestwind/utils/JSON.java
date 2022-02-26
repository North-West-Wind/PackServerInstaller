package ml.northwestwind.utils;

import ml.northwestwind.Main;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JSON {
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) sb.append((char) cp);
        return sb.toString();
    }

    public static Object readJsonFromUrl(String url) throws IOException, ParseException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return Main.parser.parse(jsonText);
        }
    }
}
