package com.dami.lootablechests.Chests;

import com.dami.lootablechests.Chests.Objects.LootableChest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChestManager {

    private static ChestManager instance;

    private final Map<String, LootableChest> lootableChests = new HashMap<>();

    private ChestManager() {

    }

    public static ChestManager getInstance(){
        if(instance == null){
            instance = new ChestManager();
        }

        return instance;
    }

    public void saveChests(){
        for(String key : lootableChests.keySet()){
            System.out.println("saving chest: " + key);
            ChestYmlUtils.saveChest(key,lootableChests.get(key));
        }
    }

    public LootableChest getLootableChest(String lootableName){
        return lootableChests.get(lootableName);
    }

    public void setLootableChest(String name, LootableChest chest){
        lootableChests.put(name,chest);
    }

    public void removeLootableChest(String name){
        lootableChests.get(name).deleteProcess();
        lootableChests.remove(name);
    }

    public Collection<String> chestKeys(){
        return lootableChests.keySet();
    }


}
