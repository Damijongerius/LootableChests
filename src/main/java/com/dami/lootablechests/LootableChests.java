package com.dami.lootablechests;

import com.dami.lootablechests.Chests.Interactables.ChestEvents;
import com.dami.lootablechests.Chests.ChestManager;
import com.dami.lootablechests.Chests.ChestYmlUtils;
import com.dami.lootablechests.Chests.Commands.LootableChestCommand;
import com.dami.lootablechests.Chests.Objects.LootableChest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class LootableChests extends JavaPlugin {

    private static LootableChests instance;
    private static String basePath;
    @Override
    public void onEnable() {
        instance = this;
        basePath = getDataFolder().getPath();
        // Plugin startup logic
        saveDefaultConfig();

        List<String> chestfiles = ChestYmlUtils.getAllFileNamesInFolder();
        for(String chestfile : chestfiles){
            LootableChest chest = ChestYmlUtils.loadChest(chestfile.replace(".yml",""));
            ChestManager.getInstance().setLootableChest(chestfile.replace(".yml",""),chest);
        }

        getServer().getPluginManager().registerEvents(new ChestEvents(),this);
        new LootableChestCommand(this);
    }

    @Override
    public void onDisable() {
        ChestManager.getInstance().saveChests();
    }

    public static String configPath(){
        return basePath;
    }

    public static FileConfiguration config(){
        return instance.getConfig();
    }
}
