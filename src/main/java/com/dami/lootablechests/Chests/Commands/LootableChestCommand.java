package com.dami.lootablechests.Chests.Commands;

import com.dami.lootablechests.Chests.ChestManager;
import com.dami.lootablechests.Chests.ChestYmlUtils;
import com.dami.lootablechests.Chests.Interactables.LootableViewer;
import com.dami.lootablechests.Chests.Objects.LootableChest;
import com.dami.lootablechests.LootableChests;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LootableChestCommand implements TabExecutor {
    /*
    create <name> <refreshTime> <rnds>
    set <name>
    set <name> <x> <y> <z> <direction>
    set <name> <direction>
    edit <name>
    delete <name>
     */

    private ChestManager chestManager = ChestManager.getInstance();

    private LootableViewer viewer = new LootableViewer();

    public LootableChestCommand(LootableChests lootableChests){
        lootableChests.getCommand("lootablechest").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!sender.hasPermission("lootablechest.admin")){
            sender.sendMessage(Color.RED + "You do not have permission to use this command");
            return false;
        }

        if(args.length < 1){
            sender.sendMessage(Color.RED + "Not enough arguments");
            return false;
        }

        switch (args[0]){
            case "create" -> create(args, sender);
            case "set" -> set(args, sender);
            case "edit" -> edit(args, sender);
            case "delete" -> delete(args, sender);
            case "properties" -> properties(args, sender);
            case "help" -> sender.sendMessage(Color.GREEN + "create <name> <refreshTime> <rnds>\n" +
                    "set <name>\n" +
                    "set <name> <x> <y> <z> <direction>\n" +
                    "set <name> <direction>\n" +
                    "edit <name>\n" +
                    "delete <name>\n" +
                    "properties\n" +
                    "help\n" +
                    "list");
            case "list" -> {
                for(String key : chestManager.chestKeys()){
                    sender.sendMessage(key);
                }
            }
        }
        return true;
    }

    private void properties(String[] args, CommandSender sender){
        if(args.length < 1){
            sender.sendMessage(Color.RED + "missing name");
            return;
        }

        if(args.length  < 2){
            sender.sendMessage(Color.RED + "missing property");
            return;
        }

        if(!chestManager.chestKeys().contains(args[1])){
            sender.sendMessage(Color.RED + "Chest does not exist");
            return;
        }

        LootableChest chest = chestManager.getLootableChest(args[1]);

        switch (args[2]){
            case "refreshTime" -> {

                chest.setRefreshTime(Long.parseLong(args[3]));
                ChestYmlUtils.saveChest(args[1],chest);
            }
            case "rnds" -> {
                chest.setRnds(Integer.parseInt(args[3]));
                ChestYmlUtils.saveChest(args[1],chest);
            }
            case "replaceAll" -> {
                chest.getProperties().replaceAll = Boolean.parseBoolean(args[3]);
                ChestYmlUtils.saveChest(args[1],chest);
            }
            case "roundRange" -> {
                chest.getProperties().roundRange = Boolean.parseBoolean(args[3]);
                ChestYmlUtils.saveChest(args[1],chest);
            }
            case "itemRange" -> {
                chest.getProperties().itemRange = Boolean.parseBoolean(args[3]);
                ChestYmlUtils.saveChest(args[1],chest);}
        }
    }

    private void create(String[] args, CommandSender sender){
        if(args.length < 2){
            sender.sendMessage(Color.RED + "missing name");
            return;
        }

        if(chestManager.chestKeys().contains(args[1])){
            sender.sendMessage(Color.YELLOW + "Chest already exists");
            return;
        }

        switch (args.length){
            case 2 -> chestManager.setLootableChest(args[1],new LootableChest(args[1]));//create <name>
            case 3 -> chestManager.setLootableChest(args[1],new LootableChest(args[1],Long.parseLong(args[2])));//create <name> <refreshtime>
            case 4 -> chestManager.setLootableChest(args[1],new LootableChest(args[1],Long.parseLong(args[2]),Integer.parseInt(args[3])));//create <name> <refreshtime> <rnds>
            default ->
            {
                sender.sendMessage(Color.RED + "Too many arguments");
                return;
            }

        }

        ChestYmlUtils.saveChest(args[1],chestManager.getLootableChest(args[1]));
        sender.sendMessage(Color.GREEN + "Created lootable chest\n use the set command to set the location of a chest with these lootables");
        viewer.openGui((Player) sender,args[1]);
    }

    private void set(String[] args, CommandSender sender){
        if(args.length < 2) {
            sender.sendMessage(Color.RED + "missing name");
            return;
        }

        if(sender instanceof Player player){
            if(!chestManager.chestKeys().contains(args[1])){
                sender.sendMessage(Color.RED + "LootableChest does not exist");
                return;
            }

            switch (args.length){
                case 2 -> sender.sendMessage(chestManager.getLootableChest(args[1]).addChestLocation(player.getLocation(), args[1]));//set on standing pos
                case 3 -> sender.sendMessage(chestManager.getLootableChest(args[1]).addChestLocation(player.getLocation(),Integer.parseInt(args[2]),args[1]));//set on standing pos with direction
                case 5 -> sender.sendMessage(chestManager.getLootableChest(args[1]).addChestLocation(new Location(player.getWorld(),Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4])), args[1]));//set on given pos
                case 6 -> sender.sendMessage(chestManager.getLootableChest(args[1]).addChestLocation(new Location(player.getWorld(),Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4])),Integer.parseInt(args[5]),args[1]));//set on given pos with direction
                default -> {
                    sender.sendMessage(Color.RED + "not the right arguments");
                    sender.sendMessage(Color.GRAY + "set <name>\n" +
                            "set <name> <x> <y> <z> <direction>\n" +
                            "set <name> <direction>");
                    return;
                }
            }
        }else{
            sender.sendMessage(Color.RED + "You need to be a player to use this command");
        }

    }

    private void edit(String[] args, CommandSender sender){
        if(args.length < 2){
            sender.sendMessage(Color.RED + "missing name");
            return;
        }

        if(!chestManager.chestKeys().contains(args[1])){
            sender.sendMessage(Color.RED + "Chest does not exist");
            return;
        }

        viewer.openGui((Player) sender,args[1]);
    }

    private void delete(String[] args, CommandSender sender){
        if(args.length < 2){
            sender.sendMessage(Color.RED + "missing name");
            return;
        }

        if(!chestManager.chestKeys().contains(args[1])){
            sender.sendMessage(Color.RED + "Chest does not exist");
            return;
        }

        chestManager.removeLootableChest(args[1]);
        ChestYmlUtils.deleteChest(args[1]);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("create");
            completions.add("set");
            completions.add("edit");
            completions.add("delete");
            completions.add("list");
            completions.add("properties");
            completions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            completions.add("chestName");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set") || args.length == 2 && args[0].equalsIgnoreCase("delete") || args.length == 2 && args[0].equalsIgnoreCase("edit")) {
            completions.addAll(chestManager.chestKeys());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            completions.add("direction");
            completions.add("x");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")){
            completions.add("y");
        }else if (args.length == 5 && args[0].equalsIgnoreCase("set")) {
            completions.add("z");
        }else if (args.length == 6 && args[0].equalsIgnoreCase("set")) {
            completions.add("direction");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")){
            completions.add("refreshTime");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("create")){
            completions.add("rnds");
        }else if(args.length == 2 && args[0].equalsIgnoreCase("properties")) {
            completions.addAll(chestManager.chestKeys());
        }else if (args.length == 3 && args[0].equalsIgnoreCase("properties")) {
            completions.add("refreshTime");
            completions.add("rnds");
            completions.add("replaceAll");
            completions.add("roundRange");
            completions.add("itemRange");
        }

        return completions;
    }
}
