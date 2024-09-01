package fr.lightnew.npc.commands;

import com.mojang.datafixers.util.Pair;
import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.entities.NPCCreator;
import fr.lightnew.npc.gui.GUINpcs;
import fr.lightnew.npc.sql.RequestNPC;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.EnumItemSlot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                                ChatColor.YELLOW + "/npc holo <id> text... - " + ChatColor.GOLD + "Add a line above a name"
                );
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                GUINpcs.giveGuiAllNPC(player, 1);
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
                    case "remove": {
                        Bukkit.getOnlinePlayers().forEach(npc::destroy);
                        player.sendMessage(ChatColor.GOLD + "Npc (" + id + ") is removed !");
                        break;
                    }
                    case "lookme": {
                        boolean newB = npc.getEffects().getLookLock() ? false : true;
                        npc.updateLookLock(newB);
                        player.sendMessage(ChatColor.GOLD + "NPC lookme is " + (newB ? ChatColor.RED + "Disable" :ChatColor.GREEN + "Enable"));
                        break;
                    }
                    case "collide": {
                        boolean newB = npc.getEffects().getCollides() ? false : true;
                        npc.updateCollides(newB);
                        player.sendMessage(ChatColor.GOLD + "NPC collide is " + (newB ? ChatColor.GREEN + "Enable" : ChatColor.RED + "Disable" + ChatColor.GRAY + " (does not work)"));
                        break;
                    }
                    case "move": {
                        Bukkit.getOnlinePlayers().forEach(players -> npc.updatePosition(players, player.getLocation()));
                        player.sendMessage(ChatColor.GOLD + "NPC has been teleported");
                        break;
                    }
                    case "tp": {
                        player.teleport(npc.getEntityPlayer().getBukkitEntity());
                        player.sendMessage(ChatColor.GOLD + "Teleported to " + npc.getName() + " (" + npc.getId() +")");
                        break;
                    }
                    case "equip": {
                        Bukkit.getOnlinePlayers().forEach(players -> npc.updateStuff(players, Arrays.asList(
                                List.of(Pair.of(EnumItemSlot.a, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.HAND)))),
                                List.of(Pair.of(EnumItemSlot.b, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.OFF_HAND)))),
                                List.of(Pair.of(EnumItemSlot.c, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.FEET)))),
                                List.of(Pair.of(EnumItemSlot.d, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.LEGS)))),
                                List.of(Pair.of(EnumItemSlot.e, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.CHEST)))),
                                List.of(Pair.of(EnumItemSlot.f, CraftItemStack.asNMSCopy(player.getInventory().getItem(EquipmentSlot.HEAD))))
                        )));
                        player.sendMessage(ChatColor.GOLD + "Your stuff is given");
                        break;
                    }
                    case "effect": {
                        MobEffect mobEffect = new MobEffect(MobEffectList.a(24), 1000, 1, true, true);
                        ((CraftPlayer) player).getHandle().c.a(new PacketPlayOutEntityEffect(player.getEntityId(), mobEffect));
                        //((CraftPlayer) player).getHandle().c.a(new PacketPlayOutRemoveEntityEffect(player.getEntityId(), new MobEffect(MobEffectList.a(2), 1, 1).c()));
                        player.sendMessage(ChatColor.GOLD + "Effect given");
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
                        Bukkit.getOnlinePlayers().forEach(npcCreator::createNPC);
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
                }
            }

            if (args.length >= 3) {
                if (args[0].equalsIgnoreCase("name")) {
                    if (!npcExist(args[1])) {
                        player.sendMessage(ChatColor.RED + "Id doesn't exist in the list");
                        return false;
                    }
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i < args.length; i++)
                        builder.append(args[i] + " ");

                    String name = builder.toString();
                    if (name.length() <= 2 || name.length() >= 16) {
                        player.sendMessage(ChatColor.RED + "Min character 3 & Max 16 !");
                        return true;
                    }
                    NPCCreator npc = LiteFP.list_npc.get(Integer.parseInt(args[1]));
                    Bukkit.getOnlinePlayers().forEach(players -> npc.updateName(players, name));
                    player.sendMessage(ChatColor.GOLD + "NPC changed name !");
                }

                if (args[0].equalsIgnoreCase("holo")) {
                    StringBuilder text = new StringBuilder();
                    if (!npcExist(args[1])) {
                        player.sendMessage(ChatColor.RED + "Id doesn't exist in the list");
                        return false;
                    }
                    for (int i = 2; i < args.length; i++)
                        text.append(args[i] + " ");

                    player.sendMessage(ChatColor.GOLD + "NPC Adding holographic above a head");
                    player.sendMessage(ChatColor.GOLD + "Text -> " + text);
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

    private NPCCreator getTargetEntity(Player player, double range) {
        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection();
        for (double i = 0; i < range; i += 0.1) {
            Location newLoc = loc.clone().add(dir.clone().multiply(i));
            for (Entity entity : player.getWorld().getNearbyEntities(newLoc, 1, 1, 1)) {
                Bukkit.broadcastMessage("Entity : " + entity + " | ID >>> " + entity.getEntityId() + " | type >>> " + entity.getType());
            }
        }
        return null;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "remove", "list", "skin", "name", "lookme", "collide", "move", "tp", "equip", "holo");
        if (args.length == 2) {
            switch (args[0]) {
                case "create": {
                    return Arrays.asList("name");
                }
                case "remove", "skin", "name", "lookme", "collide", "move", "tp", "equip", "holo": {
                    List<String> list = new ArrayList<>();
                    LiteFP.list_npc.keySet().forEach(integer -> list.add(String.valueOf(integer)));
                    return list;
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
                case "holo": {
                    return Arrays.asList();
                }
                default: {
                    return List.of();
                }
            }
        }
        return List.of();
    }
}
