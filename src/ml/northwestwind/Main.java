package ml.northwestwind;

import ml.northwestwind.utils.Logger;
import ml.northwestwind.utils.ModPack;
import ml.northwestwind.utils.Server;
import org.apache.commons.io.FileUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    public static final String VERSION = "1.0.3";
    public static final JSONParser parser = new JSONParser();
    public static String overrides = "overrides";
    public static boolean skipMods, skipServer, autoDownload, local;
    public static Scanner scanner;

    public static void main(String[] args) {
        Logger.log("Pack-Server-Installer v" + VERSION + " - Made by NorthWestWind");
        AnsiConsole.systemInstall();
        File config = new File("./installer.json");
        if (!config.exists() || !config.isFile()) {
            Logger.log(Ansi.Color.RED, "Invalid installer.json file! Exiting...");
            return;
        }
        readConfig();
        scanner = new Scanner(System.in);
        File manifest = new File("./manifest.json");
        if (manifest.exists()) {
            String input;
            if (autoDownload) input = "y";
            else {
                Logger.log(Ansi.Color.MAGENTA, "Manifest file already exists. Would you like to download it again/its updated version? [Y/N]");
                input = scanner.nextLine();
                if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                    Logger.log(Ansi.Color.MAGENTA, "Unknown response. I will take that as a Yes.");
                    input = "y";
                }
            }
            if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                manifest.delete();
                ModPack.downloadModPack();
            }
        } else ModPack.downloadModPack();
        readManifest();
        if (!skipMods) {
            String input;
            if (autoDownload) input = "y";
            else {
                Logger.log(Ansi.Color.MAGENTA, "Would you like to download the mods? [Y/N]");
                input = scanner.nextLine();
                if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                    Logger.log(Ansi.Color.MAGENTA, "Unknown response. I will take that as a Yes.");
                    input = "y";
                }
            }
            if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                File modsFolder = new File("./mods");
                if (modsFolder.exists() && modsFolder.list().length > 0) {
                    if (!autoDownload) {
                        Logger.log(Ansi.Color.MAGENTA, "There are already items in the mods folder. Would you like to delete them? [Y/N]");
                        input = scanner.nextLine();
                        if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                            Logger.log(Ansi.Color.MAGENTA, "Unknown response. I will take that as a Yes.");
                            input = "y";
                        }
                    }
                    if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                        try {
                            FileUtils.cleanDirectory(modsFolder);
                            Logger.log(Ansi.Color.GREEN, "Cleared content of mods folder");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.log(Ansi.Color.RED, "Failed to clean mods folder");
                        }
                    }
                }
                ModPack.downloadMods();
            }
        }
        if (!skipServer) {
            String input;
            if (autoDownload) input = "y";
            else {
                Logger.log(Ansi.Color.MAGENTA, "Would you like to download and install mod server? [Y/N]");
                input = scanner.nextLine();
            }
            if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n"))
                    Logger.log(Ansi.Color.MAGENTA, "Unknown response. I will take that as a Yes.");
                ModPack.downloadServer();
            }
        }
        Logger.log(Ansi.Color.CYAN, "Starting server...");
        File eula = new File("./eula.txt");
        if (!eula.exists()) {
            Logger.log(Ansi.Color.MAGENTA, "Do you accept Minecraft's EULA (https://account.mojang.com/documents/minecraft_eula)? Type TRUE to accept.");
            String input = scanner.nextLine();
            if (!input.equalsIgnoreCase("true")) {
                Logger.log(Ansi.Color.RED, "You must accept Minecraft's EULA to proceed. Exiting...");
                System.exit(0);
            }
            Server.generateEULA();
        }
        Server.launchServer();
        AnsiConsole.systemUninstall();
    }

    private static void readConfig() {
        try {
            File config = new File("./installer.json");
            JSONObject json = (JSONObject) parser.parse(new FileReader(config));
            skipMods = (boolean) json.getOrDefault("skipMods", false);
            skipServer = (boolean) json.getOrDefault("skipServer", false);
            autoDownload = (boolean) json.getOrDefault("autoDownload", true);
            local = (boolean) json.getOrDefault("local", false);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Ansi.Color.RED, "Failed to read installer.json");
        }
    }

    private static void readManifest() {
        try {
            File manifest = new File("./manifest.json");
            JSONObject json = (JSONObject) parser.parse(new FileReader(manifest));
            overrides = (String) json.getOrDefault("overrides", "overrides");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Ansi.Color.RED, "Failed to read manifest.json");
        }
    }
}
