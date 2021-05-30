package ml.northwestwind;

import ml.northwestwind.utils.ANSIColors;
import ml.northwestwind.utils.Logger;
import ml.northwestwind.utils.ModPack;
import ml.northwestwind.utils.Server;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    public static final JSONParser parser = new JSONParser();
    private static boolean skipMods, skipForge;

    public static void main(String[] args) {
        File config = new File("./installer.json");
        if (!config.exists() || !config.isFile()) {
            Logger.log(ANSIColors.RED, "Invalid installer.json file! Exiting...");
            return;
        }
        readConfig();
        File manifest = new File("./manifest.json");
        if (!manifest.exists()) ModPack.downloadModPack();
        Scanner scanner = new Scanner(System.in);
        if (!skipMods) {
            Logger.log(ANSIColors.MAGENTA, "Would you like to download the mods? [Y/N]");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) Logger.log(ANSIColors.MAGENTA, "Unknown response. I will take that as a Yes.");
                File modsFolder = new File("./mods");
                if (modsFolder.exists() && modsFolder.list().length > 0) {
                    Logger.log(ANSIColors.MAGENTA, "There are already items in the mods folder. Would you like to delete them? [Y/N]");
                    input = scanner.nextLine();
                    if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                        if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) Logger.log(ANSIColors.MAGENTA, "Unknown response. I will take that as a Yes.");
                        try {
                            FileUtils.cleanDirectory(modsFolder);
                            Logger.log(ANSIColors.GREEN, "Cleared content of mods folder");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.log(ANSIColors.RED, "Failed to clean mods folder");
                        }
                    }
                }
                ModPack.downloadMods();
            }
        }
        if (!skipForge) {
            Logger.log(ANSIColors.MAGENTA, "Would you like to download and install Forge? [Y/N]");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) {
                if (!input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n")) Logger.log(ANSIColors.MAGENTA, "Unknown response. I will take that as a Yes.");
                ModPack.downloadForge();
            }
        }
        Logger.log(ANSIColors.CYAN, "Starting server...");
        File eula = new File("./eula.txt");
        if (!eula.exists()) {
            Logger.log(ANSIColors.MAGENTA, "Do you accept Minecraft's EULA (https://account.mojang.com/documents/minecraft_eula)? Type TRUE to accept.");
            String input = scanner.nextLine();
            if (!input.equalsIgnoreCase("true")) {
                Logger.log(ANSIColors.RED, "You must accept Minecraft's EULA to proceed. Exiting...");
                System.exit(0);
            }
            Server.generateEULA();
        }
        Server.launchServer();
    }

    private static void readConfig() {
        try {
            File config = new File("./installer.json");
            JSONObject json = (JSONObject) parser.parse(new FileReader(config));
            skipMods = (boolean) json.getOrDefault("skipMods", false);
            skipForge = (boolean) json.getOrDefault("skipForge", false);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(ANSIColors.RED, "Failed to read installer.json");
        }
    }
}
