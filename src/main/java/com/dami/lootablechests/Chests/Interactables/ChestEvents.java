package com.dami.lootablechests.Chests.Interactables;

import com.dami.lootablechests.Chests.ChestManager;
import com.dami.lootablechests.Chests.ChestYmlUtils;
import com.dami.lootablechests.Chests.Objects.LootableChest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ChestEvents implements Listener {

    private final ChestManager chestManager = ChestManager.getInstance();

    private LootableViewer gui = new LootableViewer();

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction().toString().contains("RIGHT_CLICK") && event.getClickedBlock() != null) {

            Block clickedBlock = event.getClickedBlock();
            BlockState blockState = clickedBlock.getState();

            if (blockState instanceof Chest chestBlock) {
                if(chestBlock.getCustomName() == null || !chestManager.chestKeys().contains(chestBlock.getCustomName())){
                    return;
                }

                LootableChest chest = chestManager.getLootableChest(chestBlock.getCustomName());

                chest.refreshChest(chestBlock.getInventory(),chestBlock.getLocation());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){

        if(isLootableChest(event.getView().getTitle())){
            return;
        }

        Inventory inv = event.getInventory();

        LootableChest chest = chestManager.getLootableChest(getLootableChest(event.getView().getTitle()));
        ItemStack[] invItems = inv.getContents();
        Collection<ItemStack> lootableItems = new ArrayList<>();

        for(int i = 0; i < 45; i++){
            if (invItems[i] == null || invItems[i].getType() == Material.AIR) {
                continue;
            }

            lootableItems.add(invItems[i]);
        }

        chest.updateItems(lootableItems,Integer.parseInt(getPage(event.getView().getTitle()) + ""));

        ChestYmlUtils.saveChest(getLootableChest(event.getView().getTitle()),chest);
    }

    @EventHandler
    public void onBlockRemove(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.CHEST) {
            Chest chestBlock = (Chest) event.getBlock().getState();

            if (chestBlock.getCustomName() == null || !chestManager.chestKeys().contains(chestBlock.getCustomName())) {
                return;
            }

            LootableChest chest = chestManager.getLootableChest(chestBlock.getCustomName());

            chest.removeLocation(chestBlock.getLocation());
        }
    }

    @EventHandler
    public void onInteractEvent(InventoryClickEvent e){
        gui.onInteract(e);
    }

    //watch out method is inverted
    public boolean isLootableChest(String view){
        if(!view.contains("-")){
            return true; //inverted
        }
        int hyphenIndex = view.indexOf("-");
        String title = view.substring(0, hyphenIndex);
        return !chestManager.chestKeys().contains(title); //inverted
    }

    public char getPage(String view){
        return view.charAt(view.length() - 1);
    }

    public String getLootableChest(String view){
        int hyphenIndex = view.indexOf("-");
        String title = view.substring(0, hyphenIndex);
        return title;
    }
}

