package fr.lightnew.npc.commands;

import com.mojang.datafixers.util.Pair;
import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.entities.ExternHologram;
import fr.lightnew.npc.entities.custom.EntityCreator;
import fr.lightnew.npc.entities.npc.MetadataNPC;
import fr.lightnew.npc.entities.npc.NPCCreator;
import fr.lightnew.npc.events.PlayerManager;
import fr.lightnew.npc.gui.GUINpcs;
import fr.lightnew.npc.sql.RequestNPC;
import fr.lightnew.npc.tools.ClickMSG;
import fr.lightnew.npc.tools.ConsoleLog;
import fr.lightnew.npc.tools.ItemBuilder;
import fr.lightnew.npc.tools.Verifications;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.EntityType;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Console;
import java.io.File;
import java.util.*;

public class CommandNPC implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player player) {
            if (!player.isOp()) {
                player.sendMessage(ChatColor.RED + "Vous ne pouvez pas faire ça.");
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(
                        ChatColor.RED + "Help : \n" +
                                ChatColor.YELLOW + "/npc select - " + ChatColor.GOLD + "Select npc front of you" + "\n" +
                                ChatColor.YELLOW + "/npc config <id> - " + ChatColor.GOLD + "See page of config" + "\n" +
                                ChatColor.YELLOW + "/npc create <name> <skin> - " + ChatColor.GOLD + "Create new NPC" + "\n" +
                                ChatColor.YELLOW + "/npc remove <id> - " + ChatColor.GOLD + "Remove definitely" + "\n" +
                                ChatColor.YELLOW + "/npc list - " + ChatColor.GOLD + "List all NPC" + "\n" +
                                ChatColor.YELLOW + "/npc skin <id> <name> - " + ChatColor.GOLD + "Change skin of NPC" + "\n" +
                                ChatColor.YELLOW + "/npc name <id> <name> - " + ChatColor.GOLD + "Change name of NPC" + "\n" +
                                ChatColor.YELLOW + "/npc lookme <id> - " + ChatColor.GOLD + "Enable/Disable auto look you" + "\n" +
                                ChatColor.YELLOW + "/npc collide <id> - " + ChatColor.GOLD + "Enable/Disable collides" + "\n" +
                                ChatColor.YELLOW + "/npc move <id> - " + ChatColor.GOLD + "Teleport NPC on you" + "\n" +
                                ChatColor.YELLOW + "/npc tp <id> - " + ChatColor.GOLD + "Teleport to NPC" + "\n" +
                                ChatColor.YELLOW + "/npc equip <id> - " + ChatColor.GOLD + "Equip all stuff you have on your inventory" + "\n" +
                                ChatColor.YELLOW + "/npc linkholo <idHologram> <idNPC> - " + ChatColor.GOLD + "Link a holographic to npc" + "\n" +
                                ChatColor.YELLOW + "/npc heightholo <idNPC> <height> - " + ChatColor.GOLD + "Change height of hologram"
                );
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                GUINpcs.giveGuiAllNPC(player, 1);
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("select")) {
                NPCCreator npcCreator = ClickSimulator.getNPCFrontOfPlayer(player, 5);
                if (npcCreator == null)
                    player.sendMessage(ChatColor.YELLOW + "Aucun NPC trouvé devant vous !");
                else
                    player.spigot().sendMessage(ClickMSG.clickMSG(ChatColor.YELLOW + "Information du NPC :\n" +
                                    ChatColor.GOLD + "ID" + ChatColor.GRAY + " - " + ChatColor.AQUA + npcCreator.getId() +
                                    ChatColor.GOLD + "\nName" + ChatColor.GRAY + " - " + ChatColor.AQUA + npcCreator.getName() +
                                    ChatColor.RED + "\nVoir la page d'info " + ChatColor.GRAY + "(cliquez ici)",
                            HoverEvent.Action.SHOW_TEXT, "Voir plus d'infos", ClickEvent.Action.RUN_COMMAND, "/npc config " + npcCreator.getId()));
                return true;
            }

            if (args.length == 2) {
                String argument = args[0].toLowerCase();
                int id;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Id incorrect ! (is not a number...)");
                    return false;
                }
                if (!npcExist(args[1])) {
                    player.sendMessage(ChatColor.RED + "NPC doesn't exist !");
                    return false;
                }
                NPCCreator npc = LiteFP.list_npc.get(id);

                switch (argument) {
                    case "config": {
                        //openMenuConfig(player);
                        player.sendMessage(informationNPC(npc));
                        break;
                    }
                    case "remove": {
                        Bukkit.getOnlinePlayers().forEach(npc::destroy);
                        player.sendMessage(ChatColor.GOLD + "Npc (" + id + ") is removed !");
                        break;
                    }
                    case "lookme": {
                        boolean newB = npc.getEffects().getLookLock() ? false : true;
                        npc.updateLookLock(newB);
                        player.sendMessage(ChatColor.GOLD + "NPC lookme is " + (newB ? ChatColor.RED + "Disable" : ChatColor.GREEN + "Enable"));
                        break;
                    }
                    case "collide": {
                        boolean newB = !npc.getEffects().getCollides();
                        npc.updateCollides(newB);
                        player.sendMessage(ChatColor.GOLD + "NPC collide is " + (newB ? ChatColor.GREEN + "Enable" : ChatColor.RED + "Disable"));
                        break;
                    }
                    case "move": {
                        Location location = player.getLocation().clone();
                        npc.teleportForAll(location, true);
                        npc.setLocation_hologram(location);
                        player.sendMessage(ChatColor.GOLD + "NPC has been teleported");
                        break;
                    }
                    case "tp": {
                        player.teleport(npc.getServerPlayer().getBukkitEntity());
                        player.sendMessage(ChatColor.GOLD + "Teleported to " + npc.getName() + " (" + npc.getId() +")");
                        break;
                    }
                    case "equip": {
                        Bukkit.getOnlinePlayers().forEach(players -> npc.updateEquipment(players, Arrays.asList(
                                List.of(Pair.of(net.minecraft.world.entity.EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.HAND)))),
                                List.of(Pair.of(net.minecraft.world.entity.EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.OFF_HAND)))),
                                List.of(Pair.of(net.minecraft.world.entity.EquipmentSlot.FEET, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.FEET)))),
                                List.of(Pair.of(net.minecraft.world.entity.EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.LEGS)))),
                                List.of(Pair.of(net.minecraft.world.entity.EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.CHEST)))),
                                List.of(Pair.of(net.minecraft.world.entity.EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.HEAD))))
                        )));
                        player.sendMessage(ChatColor.GOLD + "Your stuff is given");
                        break;
                    }
                }
            }

            if (args.length == 3) {
                String argument = args[0].toLowerCase();
                switch (argument) {
                    case "create": {
                        String name = args[1];
                        String skin = args[2];
                        if (args[2].length() <= 2 || args[0].length() >= 16) {
                            player.sendMessage(ChatColor.RED + "Min character 3 & Max 16 !");
                            return true;
                        }
                        NPCCreator npcCreator = new NPCCreator(name, player.getLocation(), skin);
                        Bukkit.getOnlinePlayers().forEach(npcCreator::spawnNPC);
                        LiteFP.list_npc.put(npcCreator.getId(), npcCreator);
                        RequestNPC.createNewNPC(npcCreator, npcCreator::setId);
                        player.sendMessage(ChatColor.GOLD + "NPC created (" + npcCreator.getId() + " | " + npcCreator.getName() + ")");
                        break;
                    }
                    case "skin": {
                        if (!npcExist(args[1])) {
                            player.sendMessage(ChatColor.RED + "Id doesn't exist in the list");
                            return false;
                        }
                        String skin = args[2];
                        if (args[2].length() <= 2 || args[0].length() >= 16) {
                            player.sendMessage(ChatColor.RED + "Min character 3 & Max 16 !");
                            return true;
                        }
                        NPCCreator npc = LiteFP.list_npc.get(Integer.parseInt(args[1]));
                        Bukkit.getOnlinePlayers().forEach(players -> npc.updateSkin(players, skin));
                        player.sendMessage(ChatColor.GOLD + "NPC changed skin !");
                        break;
                    }
                    case "linkholo": {
                        String idHologram = args[1];
                        String idNPC = args[2];
                        if (!Verifications.isInt(idNPC)) {
                            player.sendMessage(ChatColor.RED + "Id doesn't exist in the list");
                            return false;
                        }
                        NPCCreator npc = LiteFP.list_npc.get(Integer.parseInt(args[2]));
                        Map<String, ExternHologram> list = getExternHologram();
                        List<String> keys = list.keySet().stream().toList();
                        if (npc == null) {
                            player.sendMessage(ChatColor.RED + "NPC doesn't exist");
                            return false;
                        }
                        if (!keys.contains(idHologram)) {
                            player.sendMessage(ChatColor.RED + "Hologram doesn't exist");
                            return false;
                        }
                        ExternHologram externHologram = list.get(args[1]);
                        externHologram.setLocation(npc.getLocation(), npc.getLinkedHologramHeight());
                        npc.setLinkedHologram(externHologram);
                        player.sendMessage(ChatColor.YELLOW + "Hologram '" + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "' linked to npc '" + ChatColor.GOLD + args[2] + "'");
                        player.sendMessage(ChatColor.GRAY + "If you want change height of hologram use /npc heightholo");
                        break;
                    }
                    case "heightholo": {
                        String idNPC = args[1];
                        String height = args[2];
                        if (!Verifications.isDouble(idNPC)) {
                            player.sendMessage(ChatColor.RED + "Id doesn't exist in the list");
                            return false;
                        }
                        NPCCreator npc = LiteFP.list_npc.get(Integer.parseInt(args[1]));
                        if (npc == null) {
                            player.sendMessage(ChatColor.RED + "NPC doesn't exist");
                            return false;
                        }
                        npc.setLinkedHologramHeight(Double.parseDouble(height));
                        if (npc.getLinkedHologram() != null) {
                            ExternHologram externHologram = npc.getLinkedHologram();
                            externHologram.setLocation(npc.getLocation(), npc.getLinkedHologramHeight());
                        }
                        player.sendMessage(ChatColor.YELLOW + "Hologram height change to '" + ChatColor.GOLD + height + ChatColor.YELLOW + "'");
                        break;
                    }
                }
            }

            if (args.length >= 3) {
                if (args[0].equalsIgnoreCase("name")) {
                    if (!npcExist(args[1])) {
                        player.sendMessage(ChatColor.RED + "Id doesn't exist in the list");
                        return false;
                    }
                    StringBuilder builder = new StringBuilder();
                    for (int i = 2; i < args.length; i++)
                        builder.append(args[i] + (i == args.length-1 ? "" : " "));

                    String name = builder.toString();
                    NPCCreator npc = LiteFP.list_npc.get(Integer.parseInt(args[1]));
                    npc.updateName(name);
                    player.sendMessage(ChatColor.GOLD + "NPC changed name !");
                }
            }
        }
        return false;
    }

    public static boolean npcExist(String idd) {
        int id;
        try {
            id = Integer.parseInt(idd);
            return LiteFP.list_npc.containsKey(id);
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList("select", "create", "remove", "list", "skin", "name", "lookme", "collide", "move", "tp", "equip", "linkholo", "heightholo");
        if (args.length == 2) {
            switch (args[0]) {
                case "create": {
                    return Arrays.asList("name");
                }
                case "remove", "skin", "name", "lookme", "collide", "move", "tp", "equip", "heightholo": {
                    List<String> list = new ArrayList<>();
                    LiteFP.list_npc.keySet().forEach(integer -> list.add(String.valueOf(integer)));
                    return list;
                }
                case "linkholo": {
                    return getExternHologram().keySet().stream().toList();
                }
                default: {
                    return List.of();
                }
            }
        }
        if (args.length == 3) {
            switch (args[0]) {
                case "create": {
                    return Arrays.asList("name_skin");
                }
                case "skin", "name": {
                    return Arrays.asList("name");
                }
                case "linkholo": {
                    List<String> list = new ArrayList<>();
                    LiteFP.list_npc.keySet().forEach(integer -> list.add(String.valueOf(integer)));
                    return list;
                }
                default: {
                    return List.of();
                }
            }
        }
        return List.of();
    }

    /*private void openMenuConfig(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 6*9, ChatColor.YELLOW + "Config");
        inventory.setItem(0, ItemBuilder.create(Material.TOTEM_OF_UNDYING, ChatColor.RED + "Changer le type"));
        inventory.setItem(1, ItemBuilder.create(Material.TOTEM_OF_UNDYING, ChatColor.RED + "Changer le type"));
        inventory.setItem(2, ItemBuilder.create(Material.TOTEM_OF_UNDYING, ChatColor.RED + "Changer le type"));
        inventory.setItem(3, ItemBuilder.create(Material.TOTEM_OF_UNDYING, ChatColor.RED + "Changer le type"));
        inventory.setItem(4, ItemBuilder.create(Material.TOTEM_OF_UNDYING, ChatColor.RED + "Changer le type"));
        player.openInventory(inventory);
    }*/

    private String informationNPC(NPCCreator npc) {
        return
                ChatColor.GOLD + "Name : " + ChatColor.YELLOW + npc.getName() + "\n" +
                ChatColor.GOLD + "Hologram linked : " + ChatColor.YELLOW + (npc.getLinkedHologram() != null ? npc.getLinkedHologram().getName() : "none") + "\n" +
                ChatColor.GOLD + "Pose : " + ChatColor.YELLOW + npc.getMetadataNPC().getPose() + "\n" +
                ChatColor.GOLD + "Collides : " + ChatColor.YELLOW + npc.getEffects().getCollides() + "\n" +
                ChatColor.GOLD + "LookLock : " + ChatColor.YELLOW + npc.getEffects().getLookLock() + "\n" +
                ChatColor.GOLD + "Have equipment : " + ChatColor.YELLOW + !npc.getEquipment().isEmpty() + "\n" +
                ChatColor.GOLD + "Disappear range : " + ChatColor.YELLOW + npc.getDisappearNPCRange() + "\n" +
                ChatColor.GOLD + "World : " + ChatColor.YELLOW + npc.getLocation().getWorld().getName() + "\n" +
                ChatColor.GOLD + "Location : X: " + ChatColor.YELLOW + npc.getLocation().getBlockX() + ", Y: " + npc.getLocation().getBlockY() + ", Z: " + npc.getLocation().getBlockZ() + "\n";
    }

    public static HashMap<String, ExternHologram> getExternHologram() {
        HashMap<String, ExternHologram> list = new HashMap<>();
        File file = new File(LiteFP.instance.getDataFolder().getParentFile() + "\\HolographicDisplays", "database.yml");
        if (!file.exists())
            return list;
        YamlConfiguration ymlConfig = YamlConfiguration.loadConfiguration(file);
        ymlConfig.getKeys(false).forEach(string ->
                list.put(string, new ExternHologram(
                        string, ymlConfig.getStringList(string + ".lines"), new Location(Bukkit.getWorld(ymlConfig.getString(string + ".position.world")),
                ymlConfig.getDouble(string + ".position.x"), ymlConfig.getDouble(string + ".position.y"), ymlConfig.getDouble(string + ".position.z")))));
        return list;
    }
}
