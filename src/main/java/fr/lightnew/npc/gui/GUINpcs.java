package fr.lightnew.npc.gui;

import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.commands.CommandNPC;
import fr.lightnew.npc.entities.NPCCreator;
import fr.lightnew.npc.tools.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GUINpcs {


    public static void giveGuiAllNPC(Player player, int page) {
        List<NPCCreator> npcs = LiteFP.list_npc.values().stream().toList();
        List<ItemStack> items = new ArrayList<>();
        int limit = npcs.size() > page*44 ? page*44 : npcs.size();
        for (int i = (page > 1 ? ((page-1)*44) : 0); i < limit; i++) {
            NPCCreator npc = npcs.get(i);
            String location = "X: " + npc.getLocation().getBlockX() + " Y: " + npc.getLocation().getBlockY() + " Z: " + npc.getLocation().getBlockZ();
            items.add(ItemBuilder.skull(1, npc.getName(), npc.getSkinName(), ChatColor.GOLD + "ID", ChatColor.YELLOW + String.valueOf(npc.getId()), ChatColor.GOLD + "Location", ChatColor.YELLOW + location, ChatColor.GOLD + "Have stuff ?", ChatColor.YELLOW + String.valueOf(!npc.getEquipment().isEmpty())));
        }
        Inventory inventory = Bukkit.createInventory(player, 9*6, ChatColor.RED + "All NPC HERE");
        items.forEach(inventory::addItem);
        if (page > 1)
            inventory.setItem(45, ItemBuilder.create(Material.ARROW, 1, "Previous page", ChatColor.GRAY + "Previous page is", String.valueOf((page-1))));
        if (npcs.size() > 44)
            inventory.setItem(53, ItemBuilder.create(Material.ARROW, 1, "Next page", ChatColor.GRAY + "Next page is", String.valueOf((page+1))));
        player.openInventory(inventory);
    }

    public static void giveInformationPageNPC(Player player, int idNPC) {
        if (!CommandNPC.npcExist(String.valueOf(idNPC))) {
            player.sendMessage(ChatColor.RED + "ID doesn't exist !");
            return;
        }
        NPCCreator npcManager = LiteFP.list_npc.get(idNPC);

        ItemStack ITEM_DELETE = ItemBuilder.create(Material.REDSTONE_BLOCK, 1, ChatColor.RED + "Delete NPC", ChatColor.GRAY + "remove NPC forever");
        ItemStack ITEM_TP = ItemBuilder.create(Material.FEATHER, 1, ChatColor.AQUA + "Teleport to NPC", ChatColor.GRAY + "tp to " + "X: " + npcManager.getLocation().getBlockX() + " Y: " + npcManager.getLocation().getBlockY() + " Z: " + npcManager.getLocation().getBlockZ());
        Inventory inventory = Bukkit.createInventory(player, 9*3, ChatColor.RED + "Modify NPC " + idNPC);

        inventory.setItem(12, ITEM_DELETE);
        inventory.setItem(14, ITEM_TP);

        player.openInventory(inventory);
    }
}
