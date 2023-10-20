package com.dami.lootablechests.Chests.Interactables;

import com.dami.lootablechests.Chests.ChestManager;
import com.dami.lootablechests.Chests.Objects.LootableChest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;

public class LootableViewer {

    public void openGui(Player p, String lootableChest){
        openGui(p,lootableChest,1);
    }
    public void openGui(Player p, String lootableChest, int page) {
        LootableChest chest = ChestManager.getInstance().getLootableChest(lootableChest);


        List<ItemStack> items = chest.getItems();

        Inventory inv = Bukkit.createInventory(null, 54, lootableChest + "-" + page);


        if(items.size() >  45 * (page - 1)) {
            List<ItemStack> sub = items.subList(45 * (page - 1), Math.min(45 * (page - 1) + 45, items.size()));

            int i = 0;

            for (ItemStack item : sub) {
                inv.setItem(i, item);
                i++;
            }
        }

        ItemStack nextPage = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);

        ItemMeta metaNext = nextPage.getItemMeta();
        metaNext.setDisplayName("next Page");

        nextPage.setItemMeta(metaNext);

        inv.setItem(50, nextPage);


        ItemStack prevPage = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);

        ItemMeta metaPrev = prevPage.getItemMeta();
        metaPrev.setDisplayName("previous Page");

        prevPage.setItemMeta(metaPrev);

        inv.setItem(48, prevPage);


        p.openInventory(inv);

    }

    public void onInteract(InventoryClickEvent e){

        if(!e.getView().getTitle().contains("-")){
            return;
        }

        String title = e.getView().getTitle();
        int hyphenIndex = title.indexOf("-");
        String lootableChest = title.substring(0, hyphenIndex);

        if(!ChestManager.getInstance().chestKeys().contains(lootableChest)){
            return;
        }


        if(e.getClick().isShiftClick()){
            if(e.getInventory().getItem(45) != null){
                e.setCancelled(true);
                e.getInventory().setItem(45, new ItemStack(Material.AIR));
            }
        }

        if(e.getSlot() >= 45 && e.getSlot() <= 53){
            e.setCancelled(true);
        }

        if(e.getCurrentItem() == null){
            return;
        }

        int i = Integer.parseInt(title.substring(title.length() - 1));
        if(e.getCurrentItem().getItemMeta().getDisplayName().equals("next Page") && e.getSlot() == 50){
            int page = i;
            page++;
            LootableChest chest = ChestManager.getInstance().getLootableChest(lootableChest);
            if(chest.getItems().size() > (45 * (page - 1)) - 5 && page > 1){
                e.getWhoClicked().closeInventory();
                openGui((Player) e.getWhoClicked(),lootableChest,page);
            }
        }

        if(e.getCurrentItem().getItemMeta().getDisplayName().equals("previous Page") && e.getSlot() == 48){
            int page = i;

            if(page == 1){
                return;
            }

            page--;
            e.getWhoClicked().closeInventory();
            openGui((Player) e.getWhoClicked(),lootableChest,page);
        }
    }
}
