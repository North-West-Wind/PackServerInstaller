package ml.northwestwind.utils;

import ml.northwestwind.Main;
import org.apache.commons.io.FileUtils;
import org.fusesource.jansi.Ansi;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.rmi.NoSuchObjectException;
import java.util.Arrays;
import java.util.Optional;

public class ModPack {
    private static final String BASE_URL = "https://northwestwind.ml/api/curseforge/mods/";

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
        Object[] blacklist = getBlocklist();
        for (Object folder : blacklist) {
            File f = new File(folder.toString());
            if (f.exists()) {
                if (f.isFile()) f.delete();
                else FileUtils.deleteDirectory(f);
                Logger.log(Ansi.Color.CYAN, "Deleted blocked file/folder " + folder);
            }
        }
    }

    private static Object[] getBlocklist() throws IOException, ParseException {
        File blacklist = new File("./blocklist.json");
        if (!blacklist.exists() || !blacklist.isFile()) return new String[0];
        JSONObject json = (JSONObject) Main.parser.parse(new FileReader(blacklist));
        JSONArray array = (JSONArray) json.get("folders");
        return array.toArray();
    }

    private static Object[] getBlocklistMods() throws IOException, ParseException {
        File blacklist = new File("./blocklist.json");
        if (!blacklist.exists() || !blacklist.isFile()) return new String[0];
        JSONObject json = (JSONObject) Main.parser.parse(new FileReader(blacklist));
        JSONArray array = (JSONArray) json.get("mods");
        return array.toArray();
    }

    public static void downloadModPack() {
        try {
            File config = new File("./installer.json");
            if (!config.exists() || !config.isFile()) throw new FileNotFoundException("Invalid installer.json file! Exiting...");
            JSONObject json = (JSONObject) Main.parser.parse(new FileReader(config));
            String url = (String) json.get("url");
            if (url == null) throw new NoSuchObjectException("Modpack URL is malformed");
            Logger.log(Ansi.Color.GREEN, "Found modpack URL");
            String pack;
            if (Main.local) {
                File packFile = new File(url);
                if (!packFile.exists() || !packFile.isFile()) throw new FileNotFoundException("The path does not lead to a valid modpack export");
                pack = url;
            } else {
                pack = HTTPDownload.downloadFile(url, ".");
                if (pack == null) throw new SyncFailedException("Failed to download modpack");
            }
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
            Object[] blacklisted = getBlocklistMods();
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
                    if (!f.isPresent()) throw new FileNotFoundException("Cannot find required file");
                    JSONObject j = (JSONObject) f.get();
                    name = (String) j.get("displayName");
                    String downloaded = HTTPDownload.downloadFile((String) j.get("downloadUrl"), "./mods");
                    if (downloaded == null) throw new SyncFailedException("Failed to download mod " + name);
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

    public static void downloadServer() {
        Logger.log(Ansi.Color.CYAN, "Starting mod server download");
        try {
            File manifest = new File("./manifest.json");
            JSONObject json = (JSONObject) Main.parser.parse(new FileReader(manifest));
            JSONObject minecraftJson = (JSONObject) json.get("minecraft");
            String[] id = ((String) ((JSONObject) ((JSONArray) minecraftJson.get("modLoaders")).get(0)).get("id")).split("-");
            String launcher = id[0];
            String version = id[1];
            File config = new File("./installer.json");
            json = (JSONObject) Main.parser.parse(new FileReader(config));
            String file;
            if (launcher.equalsIgnoreCase("forge")) {
                String mc = (String) minecraftJson.get("version");
                String name = String.format("forge-%s-%s-installer.jar", mc, version);
                file = HTTPDownload.downloadFile(String.format("https://maven.minecraftforge.net/net/minecraftforge/forge/%s-%s/%s", mc, version, name), ".");
                if (file == null) throw new SyncFailedException("Failed to download Forge installer");
                Logger.log(Ansi.Color.GREEN, "Downloaded Forge installer!");
                Logger.log(Ansi.Color.CYAN, "Proceeding to install...");
                new ProcessBuilder().inheritIO().command("java", "-jar", new File(file).getName(), "--installServer").start().waitFor();
                Logger.log(Ansi.Color.GREEN, "Installed Forge server!");
                file = String.format("forge-%s-%s.jar", mc, version);
            } else if (launcher.equalsIgnoreCase("fabric")) {
                file = HTTPDownload.downloadFile("https://maven.fabricmc.net/net/fabricmc/fabric-installer/0.7.4/fabric-installer-0.7.4.jar", ".");
                if (file == null) throw new SyncFailedException("Failed to download Fabric installer");
                Logger.log(Ansi.Color.GREEN, "Downloaded Fabric installer!");
                Logger.log(Ansi.Color.CYAN, "Proceeding to install...");
                new ProcessBuilder().inheritIO().command("java", "-jar", new File(file).getName(), "server", "-loader", version, "-mcversion", (String) minecraftJson.get("version")).start().waitFor();
                Logger.log(Ansi.Color.GREEN, "Installed Fabric server!");
                file = "fabric-server-launch.jar";
            } else {
                throw new IllegalArgumentException("Unknown launcher!");
            }
            json.put("skipServer", true);
            json.put("serverJar", file);
            Writer writer = new FileWriter("./installer.json", false);
            JSONObject.writeJSONString(json, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Ansi.Color.RED, "Failed to download/install mod server! Exiting...");
            System.exit(1);
        }
    }
}
