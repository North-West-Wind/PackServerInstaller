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
    public static final JSONParser parser = new JSONParser();
    private static boolean skipMods, skipForge, autoDownload;

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        File config = new File("./installer.json");
        if (!config.exists() || !config.isFile()) {
            Logger.log(Ansi.Color.RED.fgBright(), "Invalid installer.json file! Exiting...");
            return;
        }
        readConfig();
        File manifest = new File("./manifest.json");
        if (!manifest.exists()) ModPack.downloadModPack();
        Scanner scanner = new Scanner(System.in);
        if (!skipMods) {
            String input;
            if (autoDownload) input = "y";
            else {
                Logger.log(Ansi.Color.MAGENTA.fgBright(), "Would you like to download the mods? [Y/N]");
                input = scanner.nextLine();
            }
            if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) Logger.log(Ansi.Color.MAGENTA.fgBright(), "Unknown response. I will take that as a Yes.");
                File modsFolder = new File("./mods");
                if (modsFolder.exists() && modsFolder.list().length > 0) {
                    if (!autoDownload) {
                        Logger.log(Ansi.Color.MAGENTA.fgBright(), "There are already items in the mods folder. Would you like to delete them? [Y/N]");
                        input = scanner.nextLine();
                    }
                    if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                        if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) Logger.log(Ansi.Color.MAGENTA.fgBright(), "Unknown response. I will take that as a Yes.");
                        try {
                            FileUtils.cleanDirectory(modsFolder);
                            Logger.log(Ansi.Color.GREEN.fgBright(), "Cleared content of mods folder");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.log(Ansi.Color.RED.fgBright(), "Failed to clean mods folder");
                        }
                    }
                }
                ModPack.downloadMods();
            }
        }
        if (!skipForge) {
            String input;
            if (autoDownload) input = "y";
            else {
                Logger.log(Ansi.Color.MAGENTA.fgBright(), "Would you like to download and install Forge? [Y/N]");
                input = scanner.nextLine();
            }
            if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) Logger.log(Ansi.Color.MAGENTA.fgBright(), "Unknown response. I will take that as a Yes.");
                ModPack.downloadForge();
            }
        }
        Logger.log(Ansi.Color.CYAN.fgBright(), "Starting server...");
        File eula = new File("./eula.txt");
        if (!eula.exists()) {
            String input;
            if (autoDownload) {
                Logger.log(Ansi.Color.MAGENTA.fgBright(), "Auto Download is enabled. We assume you accept Minecraft's EULA");
                input = "true";
            } else {
                Logger.log(Ansi.Color.MAGENTA.fgBright(), "Do you accept Minecraft's EULA (https://account.mojang.com/documents/minecraft_eula)? Type TRUE to accept.");
                input = scanner.nextLine();
            }
            if (!input.equalsIgnoreCase("true")) {
                Logger.log(Ansi.Color.RED.fgBright(), "You must accept Minecraft's EULA to proceed. Exiting...");
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
            skipForge = (boolean) json.getOrDefault("skipForge", false);
            autoDownload = (boolean) json.getOrDefault("autoDownload", true);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Ansi.Color.RED.fgBright(), "Failed to read installer.json");
        }
    }
}
