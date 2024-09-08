package fr.lightnew.npc.events;

import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.commands.CommandNPC;
import fr.lightnew.npc.commands.RecordCommand;
import fr.lightnew.npc.entities.npc.MetadataNPC;
import fr.lightnew.npc.entities.npc.NPCCreator;
import fr.lightnew.npc.events.builder.InteractNPCEvent;
import fr.lightnew.npc.events.builder.PacketReader;
import fr.lightnew.npc.gui.GUINpcs;
import fr.lightnew.npc.tools.PlayerUtils;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerManager implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        LiteFP.list_npc.values().forEach(npcCreator -> {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.completeOnTimeout(null, 3, TimeUnit.SECONDS).thenRunAsync(() -> LiteFP.workerLoadNPC.put(event.getPlayer(), npcCreator));
        });
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
                    if (loc.distance(event.getPlayer().getLocation()) > 10)
                        return;
                    loc.setDirection(event.getPlayer().getLocation().subtract(loc).toVector());
                    float yaw = loc.getYaw();
                    float pitch = loc.getPitch();
                    ((CraftPlayer) event.getPlayer()).getHandle().connection.send(new ClientboundMoveEntityPacket.Rot(npc.getId(), (byte) ((yaw % 360) * 256 / 360), (byte) ((pitch % 360) * 256 / 360), true));
                    ((CraftPlayer) event.getPlayer()).getHandle().connection.send(new ClientboundRotateHeadPacket(npc.getNPC(), (byte) ((yaw % 360) * 256 / 360)));
                });
        if (RecordCommand.inRecord.contains(event.getPlayer()))
            RecordCommand.recordPlayers.get(event.getPlayer()).addLocation(event.getPlayer().getLocation());
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

    //TESTING EVENT
    /*@EventHandler
    public void interactNPC(InteractNPCEvent event) {
        event.getPlayer().sendMessage("ID NPC -> " + event.getNpcManager().getId() + ", Click -> " + event.getClickType());

        MetadataNPC metadataNPC = event.getNpcManager().getMetadataNPC();
        if (metadataNPC.getPose() == null || metadataNPC.getPose().equals(Pose.SLEEPING)) {
            metadataNPC.setHasGlowingEffect(true);
            metadataNPC.setPose(Pose.CROUCHING);
            metadataNPC.setHasNoGravity(false);
        } else {
            metadataNPC.setHasGlowingEffect(false);
            metadataNPC.setPose(Pose.SLEEPING);
            metadataNPC.setHasNoGravity(false);
        }
        event.getNpcManager().changeMetadata(event.getPlayer(), metadataNPC);

        PlayerUtils.changeCameraPlayer(event.getPlayer(), null, 0, null);
        /*event.getNpcManager().playAnimation(event.getPlayer(), AnimationNPC.SWING_MAIN_HAND);
        event.getNpcManager().moveNaturally(new Location(Bukkit.getWorld("world"), 25.52, 96, 31.71),
                new Location(Bukkit.getWorld("world"), 25.59, 96, 21.30), 0.1);*/
    }*/
}