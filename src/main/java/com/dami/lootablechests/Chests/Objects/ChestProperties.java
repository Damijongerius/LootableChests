package com.dami.lootablechests.Chests.Objects;

import com.dami.lootablechests.LootableChests;
import org.bukkit.configuration.file.FileConfiguration;

public class ChestProperties {
    public boolean roundRange = false;
    public boolean itemRange = false;
    public boolean replaceAll = false;

    public ChestProperties(){
        FileConfiguration config = LootableChests.config();
        this.roundRange = config.getBoolean("roundRange");
        this.itemRange = config.getBoolean("itemRange");
        this.replaceAll = config.getBoolean("replaceAll");
    }
}
