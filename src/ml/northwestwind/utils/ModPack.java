package ml.northwestwind.utils;

import ml.northwestwind.Main;
import org.apache.commons.io.FileUtils;
import org.fusesource.jansi.Ansi;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class ModPack {
    private static final String BASE_URL = "https://addons-ecs.forgesvc.net/api/v2/addon/";

    public static void setup() throws IOException, ParseException {
        deleteUnnecessary();
        copyFromOverride();
    }

    private static void deleteUnnecessary() {
        File modlist = new File("./modlist.html");
        if (modlist.exists()) modlist.delete();
    }

    private static void copyFromOverride() throws IOException, ParseException {
        File overrides = new File(Main.overrides);
        boolean exists = false;
        for (File override : overrides.listFiles()) {
            File existing = new File(override.getName());
            if (existing.exists()) {
                exists = true;
                break;
            }
        }
        if (exists) {
            String input;
            if (Main.autoDownload) input = "y";
            else {
                Logger.log(Ansi.Color.MAGENTA, "Some files/folders changed in the update. Would you like to override them? [Y/N]");
                input = Main.scanner.nextLine();
                if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                    Logger.log(Ansi.Color.MAGENTA, "Unknown response. I will take that as a Yes.");
                    input = "y";
                }
            }
            if (!input.equalsIgnoreCase("y") && input.equalsIgnoreCase("n")) {
                Logger.log(Ansi.Color.RED, "Cancelled copying from overrides.");
            }
        }
        FileUtils.copyDirectory(overrides, new File("."));
        FileUtils.deleteDirectory(overrides);
        Object[] blacklist = getBlacklist();
        for (Object folder : blacklist) {
            File f = new File(folder.toString());
            if (f.exists()) {
                if (f.isFile()) f.delete();
                else FileUtils.deleteDirectory(f);
                Logger.log(Ansi.Color.CYAN, "Deleted blacklisted file/folder " + folder);
            }
        }
    }

    private static Object[] getBlacklist() throws IOException, ParseException {
        File blacklist = new File("./blacklist.json");
        if (!blacklist.exists() || !blacklist.isFile()) return new String[0];
        JSONObject json = (JSONObject) Main.parser.parse(new FileReader(blacklist));
        JSONArray array = (JSONArray) json.get("folders");
        return array.toArray();
    }

    private static Object[] getBlacklistMods() throws IOException, ParseException {
        File blacklist = new File("./blacklist.json");
        if (!blacklist.exists() || !blacklist.isFile()) return new String[0];
        JSONObject json = (JSONObject) Main.parser.parse(new FileReader(blacklist));
        JSONArray array = (JSONArray) json.get("mods");
        return array.toArray();
    }

    public static void downloadModPack() {
        try {
            File config = new File("./installer.json");
            if (!config.exists() || !config.isFile()) throw new Exception("Invalid installer.json file! Exiting...");
            JSONObject json = (JSONObject) Main.parser.parse(new FileReader(config));
            String url = (String) json.get("url");
            if (url == null) throw new Exception("Modpack URL is malformed");
            Logger.log(Ansi.Color.GREEN, "Found modpack URL");
            String pack = HTTPDownload.downloadFile(url, ".");
            if (pack == null) throw new Exception("Failed to download modpack");
            Logger.log(Ansi.Color.GREEN, "Downloaded modpack from URL");
            Zip.unzip(pack, ".");
            Logger.log(Ansi.Color.GREEN, "Extracted modpack");
            new File(pack).delete();
            ModPack.setup();
            Logger.log(Ansi.Color.GREEN, "Finished modpack setup");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Ansi.Color.RED, "Failed to install modpack! Exiting...");
            System.exit(1);
        }
    }

    public static void downloadMods() {
        Logger.log(Ansi.Color.CYAN, "Starting mod download");
        try {
            File modsFolder = new File("./mods");
            if (!modsFolder.exists() || !modsFolder.isDirectory()) modsFolder.mkdir();
            File manifest = new File("./manifest.json");
            JSONObject json = (JSONObject) Main.parser.parse(new FileReader(manifest));
            JSONArray array = (JSONArray) json.get("files");
            Object[] blacklisted = getBlacklistMods();
            int i = 0, suc = 0, fai = 0, skipped = 0;
            for (Object o : array) {
                String name = "";
                try {
                    JSONObject obj = (JSONObject) o;
                    long project = (long) obj.get("projectID");
                    if (Arrays.stream(blacklisted).anyMatch(o12 -> ((long) o12) == project)) {
                        Logger.log(Ansi.Color.YELLOW, "Skipped project " + project);
                        skipped++;
                        continue;
                    }
                    long file = (long) obj.get("fileID");
                    JSONArray files = (JSONArray) JSON.readJsonFromUrl(BASE_URL + project + "/files");
                    Optional f = files.stream().filter(o1 -> ((JSONObject) o1).get("id").equals(file)).findFirst();
                    if (!f.isPresent()) throw new Exception("Cannot find required file");
                    JSONObject j = (JSONObject) f.get();
                    name = (String) j.get("displayName");
                    String downloaded = HTTPDownload.downloadFile((String) j.get("downloadUrl"), "./mods");
                    if (downloaded == null) throw new Exception("Failed to download mod " + name);
                    suc++;
                } catch (Exception e) {
                    fai++;
                    Logger.log(Ansi.Color.RED, e.getMessage());
                } finally {
                    Logger.log(Ansi.Color.GREEN, String.format("[%d/%d] [S: %d | F: %d] Downloaded %s", ++i, array.size() - skipped, suc, fai, name));
                }
            }
            if (suc == array.size() - skipped) {
                File config = new File("./installer.json");
                JSONObject j = (JSONObject) Main.parser.parse(new FileReader(config));
                j.put("skipMods", true);
                Writer writer = new FileWriter("./installer.json", false);
                JSONObject.writeJSONString(j, writer);
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Ansi.Color.RED, "Failed to download mods! Exiting...");
            System.exit(1);
        }
    }

    public static void downloadForge() {
        Logger.log(Ansi.Color.CYAN, "Starting Forge download");
        try {
            File manifest = new File("./manifest.json");
            JSONObject json = (JSONObject) Main.parser.parse(new FileReader(manifest));
            String forge = ((String) ((JSONObject) ((JSONArray) ((JSONObject) json.get("minecraft")).get("modLoaders")).get(0)).get("id")).split("-")[1];
            String mc = (String) ((JSONObject) json.get("minecraft")).get("version");
            String name = String.format("forge-%s-%s-installer.jar", mc, forge);
            String file = HTTPDownload.downloadFile(String.format("https://maven.minecraftforge.net/net/minecraftforge/forge/%s-%s/%s", mc, forge, name), ".");
            if (file == null) throw new Exception("Failed to download Forge installer");
            Logger.log(Ansi.Color.GREEN, "Downloaded Forge installer!");
            Logger.log(Ansi.Color.CYAN, "Proceeding to install...");
            File config = new File("./installer.json");
            json = (JSONObject) Main.parser.parse(new FileReader(config));
            new ProcessBuilder().inheritIO().command("java", "-jar", new File(file).getName(), "--installServer").start().waitFor();
            Logger.log(Ansi.Color.GREEN, "Installed Forge server!");
            json.put("skipForge", true);
            json.put("forgeFile", file.replace("-installer", ""));
            Writer writer = new FileWriter("./installer.json", false);
            JSONObject.writeJSONString(json, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Ansi.Color.RED, "Failed to download/install Forge server! Exiting...");
            System.exit(1);
        }
    }
}
