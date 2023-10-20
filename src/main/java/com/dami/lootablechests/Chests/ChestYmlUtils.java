package com.dami.lootablechests.Chests;

import com.dami.lootablechests.Chests.Objects.LootableChest;
import com.dami.lootablechests.LootableChests;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChestYmlUtils {

    private static String basePath = "plugins/LootableChests/";

    //function to save chest to file
    public static void saveChest(String lootableName, LootableChest chest){
        if(basePath == null){
            basePath = LootableChests.configPath();
        }

        deleteChest(lootableName);

        YamlConfiguration configurationFile = YamlConfiguration.loadConfiguration(new File(basePath + "chest/"+ lootableName +".yml"));
        chest.saveToConfig(configurationFile, "chest");

        try {
            configurationFile.save(new File(basePath + "chest/" + lootableName +  ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //function to load chest from file
    public static LootableChest loadChest(String lootableName){
        File configFile = new File(basePath  + "chest/", lootableName + ".yml");

// Load the configuration from the file
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return LootableChest.loadFromConfig(lootableName,config, "chest");
    }

    public static void deleteChest(String lootableName){
        File configFile = new File(basePath  + "chest/", lootableName + ".yml");
        configFile.delete();
    }

    //function to get all chests in directory
    public static List<String> getAllFileNamesInFolder() {
        List<String> fileNames = new ArrayList<>();

        File folder = new File(basePath + "chest");

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileNames.add(file.getName());
                    }
                }
            }
        }

        return fileNames;
    }
}
