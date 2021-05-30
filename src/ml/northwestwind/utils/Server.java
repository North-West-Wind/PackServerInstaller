package ml.northwestwind.utils;

import ml.northwestwind.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
    public static void launchServer() {
        try {
            File config = new File("./installer.json");
            JSONObject json = (JSONObject) Main.parser.parse(new FileReader(config));
            int minMem = ((int) json.getOrDefault("minMem", 1024));
            int maxMem = ((int) json.getOrDefault("maxMem", 4096));
            String serverFile = ((String) json.getOrDefault("forgeFile", getServerFile()));
            Logger.log(ANSIColors.RESET, "Here we go!");
            Process server = new ProcessBuilder().inheritIO().command("java", "-jar", "-Xmx" + maxMem + "M", "-Xms" + minMem + "M", serverFile, "nogui").start();
            server.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(ANSIColors.RED, "Failed to launch Forge server! Exiting...");
            System.exit(1);
        }
    }

    public static void generateEULA() {
        try {
            Writer writer = new FileWriter("./eula.txt", false);
            writer.write("#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\n");
            writer.write("#" + getDate() + "\n");
            writer.write("eula=true");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(ANSIColors.RED, "Failed to create EULA! Exiting...");
            System.exit(1);
        }
    }

    private static String getDate() {
        return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").format(new Date(System.currentTimeMillis()));
    }

    private static String getServerFile() throws IOException, ParseException {
        File manifest = new File("./manifest.json");
        JSONObject json = (JSONObject) Main.parser.parse(new FileReader(manifest));
        String forge = ((String) ((JSONObject) ((JSONArray) ((JSONObject) json.get("minecraft")).get("modLoaders")).get(0)).get("id")).split("-")[1];
        String mc = (String) ((JSONObject) json.get("minecraft")).get("version");
        String name = String.format("forge-%s-%s.jar", mc, forge);
        return name;
    }
}
