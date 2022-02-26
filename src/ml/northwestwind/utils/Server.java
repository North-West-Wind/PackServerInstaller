package ml.northwestwind.utils;

import ml.northwestwind.Main;
import org.fusesource.jansi.Ansi;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;

public class Server {
    public static void launchServer() {
        try {
            File config = new File("./installer.json");
            JSONObject json = (JSONObject) Main.parser.parse(new FileReader(config));
            String serverFile = ((String) json.getOrDefault("serverJar", getServerFile()));
            Logger.log(Ansi.Color.CYAN, "Here we go!");
            Process server = new ProcessBuilder().inheritIO().command("java", "-jar", serverFile, "nogui").start();
            server.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Ansi.Color.RED, "Failed to launch server! Exiting...");
            System.exit(1);
        }
    }

    public static void generateEULA() {
        try {
            Writer writer = new FileWriter("./eula.txt", false);
            writer.write("eula=true");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Ansi.Color.RED, "Failed to create EULA! Exiting...");
            System.exit(1);
        }
    }

    private static String getServerFile() throws IOException, ParseException {
        File manifest = new File("./manifest.json");
        JSONObject json = (JSONObject) Main.parser.parse(new FileReader(manifest));
        JSONObject minecraftJson = (JSONObject) json.get("minecraft");
        String[] id = ((String) ((JSONObject) ((JSONArray) minecraftJson.get("modLoaders")).get(0)).get("id")).split("-");
        String launcher = id[0];
        if (launcher.equalsIgnoreCase("fabric")) return "./fabric-server-launch.jar";
        String version = id[1];
        String mc = (String) minecraftJson.get("version");
        return String.format("forge-%s-%s.jar", mc, version);
    }
}
