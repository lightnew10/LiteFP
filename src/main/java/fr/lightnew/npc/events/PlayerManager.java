package fr.lightnew.npc.events;

import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.commands.CommandNPC;
import fr.lightnew.npc.commands.RecordCommand;
import fr.lightnew.npc.entities.LFPLocation;
import fr.lightnew.npc.entities.custom.EntityCreator;
import fr.lightnew.npc.entities.npc.NPCCreator;
import fr.lightnew.npc.events.builder.PacketReader;
import fr.lightnew.npc.events.builder.PlayerSpawnInServerEvent;
import fr.lightnew.npc.gui.GUINpcs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Pose;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerManager implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PacketReader reader = new PacketReader();
        reader.inject(event.getPlayer());
    }

    @EventHandler
    public void playerSpawnInTheServer(PlayerSpawnInServerEvent event) {
        LiteFP.list_npc.values().forEach(npcCreator ->
                new CompletableFuture<>().completeOnTimeout(null, 150, TimeUnit.MILLISECONDS).thenRunAsync(() ->
                        LiteFP.workerLoadNPC.put(event.getPlayer(), npcCreator)));
    }

    @EventHandler
    public void changedWorld(PlayerChangedWorldEvent event) {
        List<NPCCreator> list = LiteFP.list_npc.values().stream().filter(npcCreator -> npcCreator.getLocation().getWorld().getName().equalsIgnoreCase(event.getPlayer().getWorld().getName())).collect(Collectors.toList());
        list.forEach(npcCreator -> LiteFP.workerLoadNPC.put(event.getPlayer(), npcCreator));
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

    public static Map<UUID, EntityCreator> entityCreatorMap = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (entityCreatorMap.containsKey(player.getUniqueId())) {
            EntityCreator creator = entityCreatorMap.get(player.getUniqueId());
            if (player.isSneaking()) {
                FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
                friendlyByteBuf.writeVarInt(creator.getEntity().getId());
                friendlyByteBuf.writeVarIntArray(new int[]{0});

                ClientboundSetPassengersPacket passengersPacket = new ClientboundSetPassengersPacket(friendlyByteBuf);
                ((CraftPlayer) player).getHandle().connection.send(passengersPacket);
                creator.destroy(player);
                entityCreatorMap.remove(player.getUniqueId());
            }
        }
        if (!LiteFP.list_npc.isEmpty()) {
            List<NPCCreator> list = LiteFP.list_npc.values().stream()
                    .filter(npc -> Objects.equals(npc.getLocation().getWorld(), player.getWorld())).toList();
            if (!list.isEmpty()) {
                list.forEach(npc -> {
                    double maxDistanceSquared = npc.getDisappearNPCRange() * npc.getDisappearNPCRange();
                    double distanceSquared = player.getLocation().distanceSquared(npc.getLocation());
                    UUID playerUUID = player.getUniqueId();

                    if (distanceSquared > maxDistanceSquared) {
                        if (npc.getShowingNPCToPlayer().contains(playerUUID)) {
                            npc.remove(player);
                            npc.getShowingNPCToPlayer().remove(playerUUID);
                        }
                    } else if (!npc.getShowingNPCToPlayer().contains(playerUUID)) {
                        npc.spawnNPC(player);
                        npc.getShowingNPCToPlayer().add(playerUUID);
                    }

                    if (!npc.getEffects().getLookLock()) {
                        Location loc = npc.getLocation().clone();
                        double distance = loc.distance(event.getPlayer().getLocation());
                        if (distance < 5) {
                            loc.setDirection(event.getPlayer().getLocation().subtract(loc).toVector());
                            float yaw = loc.getYaw();
                            float pitch = loc.getPitch();

                            npc.getNPC().setPos(loc.getX(), loc.getY(), loc.getZ());
                            npc.getNPC().setYRot(yaw);
                            npc.getNPC().setXRot(pitch);
                            ((CraftPlayer) player).getHandle().connection.send(new ClientboundMoveEntityPacket.Rot(npc.getNPC().getId(), (byte) (yaw * 256.0F / 360.0F), (byte) (pitch * 256.0F / 360.0F), true));
                            ((CraftPlayer) player).getHandle().connection.send(new ClientboundRotateHeadPacket(npc.getNPC(), (byte) (yaw * 256.0F / 360.0F)));
                        }
                    }
                });
            }
        }

        if (RecordCommand.inRecord.contains(event.getPlayer())) {
            LFPLocation location = new LFPLocation(event.getPlayer().getLocation());
            location.setPoseNPC(event.getPlayer().isSneaking() ? Pose.CROUCHING : Pose.SITTING);
            RecordCommand.recordPlayers.get(event.getPlayer()).addLocation(location);
        }
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
                player.teleport(npcCreator.getServerPlayer().getBukkitEntity());
                player.sendMessage(ChatColor.GOLD + "Teleported to " + npcCreator.getName() + " (" + npcCreator.getId() +")");
            }
            event.setCancelled(true);
        }
    }
}