package fr.lightnew.npc.commands;

import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.entities.ConstructLFPLocation;
import fr.lightnew.npc.tools.Verifications;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class RecordCommand implements CommandExecutor, TabCompleter {

    public static Map<Player, ConstructLFPLocation> recordPlayers = new WeakHashMap<>();
    public static List<Player> inRecord = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player player) {
            if (!player.isOp()) {
                player.sendMessage(ChatColor.RED + "Vous ne pouvez pas faire ça.");
                return true;
            }
            String error = ChatColor.RED + "[ERREUR] Soit vous avez mis un ID non valide soit vous avez mal fait votre commande !\n";
            String help = ChatColor.RED + "Voici la commande : " +
                    ChatColor.RED + "\n/record <id npc> " + ChatColor.GOLD + " - " + ChatColor.YELLOW + "Faire suivre vos traces à un npc" +
                    ChatColor.RED + "\n/record visualization" + ChatColor.GOLD + " - " + ChatColor.YELLOW + "Voir votre chemin fait par vous même" +
                    ChatColor.RED + "\n/record end" + ChatColor.GOLD + " - " + ChatColor.YELLOW + "Enregistre votre chemin pour le NPC";
            if (args.length == 0) {
                player.sendMessage(help);
                return true;
            }

            if (args.length == 1) {
                if (Verifications.isInt(args[0])) {
                    if (LiteFP.list_npc.values().stream().anyMatch(npcCreator -> npcCreator.getId() == Integer.valueOf(args[0]))) {
                        if (inRecord.contains(player)) {
                            player.sendMessage(ChatColor.RED + "Vous êtes en mode record vous ne pouvez faire ceci !");
                            return true;
                        }
                        if (recordPlayers.containsKey(player)) {
                            recordPlayers.remove(player);
                            player.sendMessage(ChatColor.RED + "Réinitialisation pour un nouvel NPC");
                        }
                        recordPlayers.put(player, new ConstructLFPLocation(LiteFP.list_npc.values().stream().filter(npcCreator -> npcCreator.getId() == Integer.valueOf(args[0])).findFirst().get()));
                        player.sendMessage(ChatColor.YELLOW + "Vous éditez le NPC n°" + args[0] + " faite /record start pour lancer le record");
                        return true;
                    } else
                        player.sendMessage(ChatColor.RED + "L'id du NPC choisis n'existe plus !");
                }
                if (!recordPlayers.containsKey(player)) {
                    player.sendMessage(ChatColor.RED + "Ajoutez un id de npc dans le help de base\n" + help);
                    return true;
                }
                if (args[0].equalsIgnoreCase("start")) {
                    inRecord.add(player);
                    player.sendMessage(ChatColor.GREEN + "Record lancer !");
                    return true;
                }
                if (args[0].equalsIgnoreCase("visualization")) {
                    ConstructLFPLocation cl = recordPlayers.get(player);
                    if (cl.getLocationList().isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Pas de position connue, faite /record start");
                        return true;
                    }
                    Bukkit.getServer().getOnlinePlayers().forEach(player1 -> cl.getNpc().moveMultiLocalisationCommand(player1, cl.getLocationList()));
                    if (inRecord.contains(player)) {
                        inRecord.remove(player);
                        player.sendMessage(ChatColor.RED + "Vous n'êtes plus dans le système de record. la visualisation est lancée");
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("end")) {
                    ConstructLFPLocation cl = recordPlayers.get(player);
                    cl.getNpc().addMultiLocation(cl.getLocationList());
                    inRecord.remove(player);
                    recordPlayers.remove(player);
                    player.sendMessage(ChatColor.RED + "Vous n'êtes plus dans le système de record. Votre enregistrement à été sauvegarder");
                    return true;
                }
                player.sendMessage(error + help);
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("visualization");
            list.add("end");
            list.add("start");
            LiteFP.list_npc.keySet().forEach(integer -> list.add(String.valueOf(integer)));
            return list;
        }
        return List.of();
    }
}
