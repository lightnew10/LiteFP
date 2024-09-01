package fr.lightnew.npc.events;

import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.commands.CommandNPC;
import fr.lightnew.npc.entities.AnimationNPC;
import fr.lightnew.npc.entities.MetadataNPC;
import fr.lightnew.npc.entities.NPCCreator;
import fr.lightnew.npc.entities.PoseNPC;
import fr.lightnew.npc.events.builder.InteractNPCEvent;
import fr.lightnew.npc.events.builder.PacketReader;
import fr.lightnew.npc.gui.GUINpcs;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerManager implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        LiteFP.list_npc.values().forEach(npcCreator -> LiteFP.workerLoadNPC.put(event.getPlayer(), npcCreator));
        PacketReader reader = new PacketReader();
        reader.inject(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PacketReader reader = new PacketReader();
        reader.uninject(event.getPlayer());
    }

    @EventHandler
    public void portal(PlayerPortalEvent event) {
        List<NPCCreator> list = LiteFP.list_npc.values().stream().filter(npcCreator -> npcCreator.getLocation().getWorld().getName().equalsIgnoreCase(event.getTo().getWorld().getName())).collect(Collectors.toList());
        list.forEach(npcCreator -> LiteFP.workerLoadNPC.put(event.getPlayer(), npcCreator));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        LiteFP.list_npc.values()
                .forEach(npc -> {
                    if (npc.getEffects().getLookLock())
                        return;
                    Location loc = npc.getLocation();
                    loc.setDirection(event.getPlayer().getLocation().subtract(loc).toVector());

                    float yaw = loc.getYaw();
                    float pitch = loc.getPitch();

                    PlayerConnection connection = ((CraftPlayer) event.getPlayer()).getHandle().c;

                    connection.a(new PacketPlayOutEntity.PacketPlayOutEntityLook(npc.getEntityPlayer().getBukkitEntity().getEntityId(), (byte) ((yaw%360.)*256/360), (byte) ((pitch%360.)*256/360), false));
                    connection.a(new PacketPlayOutEntityHeadRotation(npc.getEntityPlayer(), (byte) ((yaw%360)*256/360)));
                });
    }

    @EventHandler
    public void inventory(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir())
            return;
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.RED + "All NPC HERE")) {
            ItemStack itemStack = event.getCurrentItem();
            ItemMeta meta = itemStack.getItemMeta();
            if (itemStack.getType().equals(Material.PLAYER_HEAD)) {
                GUINpcs.giveInformationPageNPC(player, Integer.parseInt(meta.getLore().get(1).replace("§e", "")));
            }

            if (itemStack.getType().equals(Material.ARROW)) {
                String name = meta.getLore().get(1);
                if (meta.getDisplayName().equalsIgnoreCase("Next page"))
                    GUINpcs.giveGuiAllNPC(player, (Integer.parseInt(name)));

                if (meta.getDisplayName().equalsIgnoreCase("Previous page"))
                    GUINpcs.giveGuiAllNPC(player, (Integer.parseInt(name)));
            }
            event.setCancelled(true);
        }

        if (event.getView().getTitle().contains("Modify NPC")) {
            String s = event.getView().getTitle().split(" ")[2];
            if (!CommandNPC.npcExist(s)) {
                player.sendMessage(ChatColor.RED + "ID doesn't exist !");
                return;
            }
            NPCCreator npcCreator = LiteFP.list_npc.get(Integer.parseInt(s));
            ItemStack itemStack = event.getCurrentItem();
            ItemMeta meta = itemStack.getItemMeta();

            if (meta.getDisplayName().equalsIgnoreCase(ChatColor.RED + "Delete NPC")) {
                Bukkit.getOnlinePlayers().forEach(npcCreator::destroy);
                player.sendMessage(ChatColor.GOLD + "Npc (" + s + ") is removed !");
                player.closeInventory();
                GUINpcs.giveGuiAllNPC(player, 1);
            }

            if (meta.getDisplayName().equalsIgnoreCase(ChatColor.AQUA + "Teleport to NPC")) {
                player.teleport(npcCreator.getEntityPlayer().getBukkitEntity());
                player.sendMessage(ChatColor.GOLD + "Teleported to " + npcCreator.getName() + " (" + npcCreator.getId() +")");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void interactNPC(InteractNPCEvent event) {
        event.getPlayer().sendMessage("ID NPC -> " + event.getNpcManager().getId() + ", Click -> " + event.getClickType());
        MetadataNPC metadataNPC = event.getNpcManager().getMetadataNPC();
        if (metadataNPC.getPose() == null || metadataNPC.getPose().equals(PoseNPC.CROAKING)) {
            metadataNPC.setHasGlowingEffect(false);
            metadataNPC.setPose(PoseNPC.DYING);
            metadataNPC.setHasNoGravity(false);
        } else {
            metadataNPC.setHasGlowingEffect(true);
            metadataNPC.setPose(PoseNPC.CROAKING);
            metadataNPC.setHasNoGravity(false);
        }
        event.getNpcManager().changeMetadata(event.getPlayer(), metadataNPC);
        event.getNpcManager().playAnimation(event.getPlayer(), AnimationNPC.SWING_MAIN_HAND);
        /*event.getNpcManager().moveNaturally(new Location(Bukkit.getWorld("world"), 25.52, 96, 31.71),
                new Location(Bukkit.getWorld("world"), 25.59, 96, 21.30), 0.1);*/
    }
}