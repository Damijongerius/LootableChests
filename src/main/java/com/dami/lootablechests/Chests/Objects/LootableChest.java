package com.dami.lootablechests.Chests.Objects;

import com.dami.lootablechests.Chests.ChestYmlUtils;
import com.dami.lootablechests.LootableChests;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootableChest {

    private final List<ItemStack> items = new ArrayList<>();

    private final HashMap<Location,Long> chestMap = new HashMap<>();

    private ChestProperties properties = new ChestProperties();

    private long refreshTime = 72000;

    private int rnds = 3;

    private String name;


    //constructors
    public LootableChest(String name){
        this.name = name;

        applyConfig();
    }

    public LootableChest(String name,long refreshTime){
        this.name = name;
        this.refreshTime  = refreshTime;

        applyConfig();
    }

    public LootableChest(String name,long refreshTime, int rnds){
        this.name = name;
        this.refreshTime = refreshTime;
        this.rnds = rnds;

        applyConfig();
    }

    private void applyConfig(){
        FileConfiguration config = LootableChests.config();
        this.refreshTime = config.getLong("refreshTime");
        this.rnds = config.getInt("rnds");
    }

    public String addChestLocation(Location location, int direction, String name){
        location.setYaw(0);
        location.setPitch(0);
        location.setY(Math.floor(location.getY()));
        location.setX(Math.floor(location.getX()));
        location.setZ(Math.floor(location.getZ()));
        if(chestMap.containsKey(location)){
            return "Chest already exists";
        }

        chestMap.put(location,location.getWorld().getFullTime() - 72000);

        Block block = location.getWorld().getBlockAt(location);
        block.setType(Material.CHEST);

        Chest chest = (Chest) block.getState();
        chest.setCustomName(name);

        Directional directional = (Directional) chest.getBlockData();
        directional.setFacing(BlockFace.values()[direction]);

        chest.setBlockData(directional);

        chest.update();

        ChestYmlUtils.saveChest(name,this);

        return "Chest added";
    }

    public void removeLocation(Location location){
        chestMap.remove(location);
        ChestYmlUtils.saveChest(name,this);
    }

    public void deleteProcess(){
        for(Location location : chestMap.keySet()){
            Block block = location.getWorld().getBlockAt(location);
            block.setType(Material.AIR);
        }
    }


    public void refreshChest(Inventory inv, Location location) {

        if (!(chestMap.containsKey(location))) {
            return;
        }

        long lastRefresh = chestMap.get(location);
        int refreshes = (int) ((location.getWorld().getFullTime() - lastRefresh) / refreshTime);

        if(properties.roundRange){
            Random random = new Random();

            refreshes = random.nextInt(refreshes) + 1;
        }

        int i = 0;
        int c = 0;

        chestMap.replace(location, location.getWorld().getFullTime());

        if (inv.firstEmpty() != -1) {
            Random random = new Random();
            int randomSlot = random.nextInt(inv.getSize());
            ItemStack randomItem;


            if(items.isEmpty()){
                return;
            }

            if(refreshes > 0){
                inv.clear();
            }

            while (i < rnds * refreshes && c < 100) {

                c++;

                int randomIndex = random.nextInt(items.size());
                int randomInvIndex = random.nextInt(inv.getSize());
                randomItem = ((ItemStack) items.toArray()[randomIndex]).clone();

                if(inv.getItem(randomInvIndex) != null){
                    continue;
                }

                if(properties.itemRange){
                    randomItem.setAmount(random.nextInt(randomItem.getAmount()));
                }

                inv.setItem(randomInvIndex, randomItem);
                i++;
            }
        }
    }

    public void updateItems(Collection<ItemStack> newItems, int page) {

        if (page <= 0) {
            throw new IllegalArgumentException("Page number must be greater than 0.");
        }

        // Calculate the starting index for the sublist
        int startIndex = 45 * (page - 1);

        if (startIndex < 0) {
            startIndex = 0; // Ensure a non-negative index
        }

        int itemsSize = this.items.size();

        // Calculate the ending index for the sublist
        int endIndex = Math.min(startIndex + newItems.size(), itemsSize);

        // Remove the old items in the specified range
        this.items.subList(startIndex, endIndex).clear();

        // Insert the new items into the same position
        this.items.addAll(startIndex, newItems);

        ChestYmlUtils.saveChest(name, this);
    }


    //getters and setters
    public void addItem(ItemStack item){
        items.add(item);
    }

    public void removeItem(ItemStack item){
        items.remove(item);
    }

    public List<ItemStack> getItems(){
        return items;
    }

    public HashMap<Location,Long> getChestLocation(){
        return chestMap;
    }

    public String addChestLocation(Location location, String name){
        return addChestLocation(location,0,name);
    }

    public void setRefreshTime(long refreshTime){
        this.refreshTime = refreshTime;
    }

    public void setRnds(int rnds){
        this.rnds = rnds;
    }

    public ChestProperties getProperties(){
        return properties;
    }



    // Configuration methods for saving and loading chests
    public void saveToConfig(FileConfiguration config, String path) {
        config.set(path + ".refreshTime", refreshTime);
        config.set(path + ".rnds", rnds);
        config.set(path + ".replaceAll", properties.replaceAll);
        config.set(path + ".roundRange", properties.roundRange);
        config.set(path + ".itemRange", properties.itemRange);

        int entryIndex = 1;
        //save ChestLocation
        for (Map.Entry<Location, Long> entry : chestMap.entrySet()) {
            Location location = entry.getKey();
            Long timestamp = entry.getValue();

            String entryKey = "chest" + entryIndex;

            // Save timestamp and location under the entry key
            config.set(path + ".locations." + entryKey + ".timestamp", timestamp);
            config.set(path + ".locations." + entryKey + ".location", location.serialize());

            entryIndex++;
        }

        // Save the item list
        List<Map<String,Object>> itemStrings = new ArrayList<>();
        if(items.isEmpty()){
            return;
        }

        for (ItemStack item : items) {
            if(item == null){
                continue;
            }

            itemStrings.add(item.serialize());
        }
        config.set(path + ".items", itemStrings);
    }

    public static LootableChest loadFromConfig(String lootableName, FileConfiguration config, String path) {
        long refreshTime = config.getLong(path + ".refreshTime", 1000000);

        LootableChest chest = new LootableChest(lootableName,refreshTime);

        chest.setRnds(config.getInt(path + ".rnds", 3));
        chest.properties.replaceAll = config.getBoolean(path + ".replaceAll", false);
        chest.properties.roundRange = config.getBoolean(path + ".roundRange", false);
        chest.properties.itemRange = config.getBoolean(path + ".itemRange", false);


        // Load items from the configuration
        List<Map<String,Object>> itemStrings = convertToSpecificType(config.getMapList(path + ".items"));
        for (Map<String,Object> itemString : itemStrings) {
            ItemStack item = ItemStack.deserialize(itemString);
            chest.addItem(item);
        }

        if (config.contains("chest.locations")) {
            HashMap<Location, Long> chestMap = new HashMap<>();
            ConfigurationSection locationsSection = config.getConfigurationSection(path + ".locations");

            for (String entryKey : locationsSection.getKeys(false)) {
                ConfigurationSection maps = locationsSection.getConfigurationSection(entryKey + ".location");
                Location location = Location.deserialize(maps.getValues(true));


                Long timestamp = config.getLong(entryKey + ".timestamp");
                chestMap.put(location, timestamp);
            }

            chest.chestMap.putAll(chestMap);
        }

        return chest;
    }

    private static List<Map<String, Object>> convertToSpecificType(List<Map<?, ?>> itemStrings) {
        List<Map<String, Object>> convertedList = new ArrayList<>();

        for (Map<?, ?> itemMap : itemStrings) {
            Map<String, Object> convertedMap = new HashMap<>();

            // Iterate through the original map and cast keys and values
            for (Map.Entry<?, ?> entry : itemMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() != null) {
                    convertedMap.put((String) entry.getKey(), entry.getValue());
                }
            }

            convertedList.add(convertedMap);
        }

        return convertedList;
    }

}
