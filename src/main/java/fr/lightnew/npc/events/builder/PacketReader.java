package fr.lightnew.npc.events.builder;

import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.entities.NPCCreator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PacketReader {

    Channel channel;
    private static Map<UUID, Channel> channels = new HashMap<>();
    private Set<Integer> processedPackets = new HashSet<>();

    public void inject(Player player) {
        channel = getPlayerChannel(player);
        channels.put(player.getUniqueId(), channel);

        if (channel.pipeline().get("PacketInjector") != null)
            return;

        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
                list.add(packet);
                readPacket(player, packet);
            }
        });
    }

    public void uninject(Player player) {
        channel = channels.get(player.getUniqueId());
        if (channel.pipeline().get("PacketInjector") != null)
            channel.pipeline().remove("PacketInjector");
    }

    public void readPacket(Player player, Packet<?> packet) {
        if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {
            int id = (int) getValue(packet, "a");
            if (!processedPackets.contains(id)) {
                CompletableFuture<Void> completableFuture = new CompletableFuture<>();
                processedPackets.add(id);

                if (getValue(packet, "b").getClass().getDeclaredFields().length == 0) {
                    List<NPCCreator> list = LiteFP.list_npc.values().stream().filter(npcCreator -> npcCreator.getEntityPlayer().getBukkitEntity().getEntityId() == id).collect(Collectors.toList());
                    if (!list.isEmpty())
                        Bukkit.getScheduler().scheduleSyncDelayedTask(LiteFP.instance, () -> Bukkit.getPluginManager().callEvent(new InteractNPCEvent(player, player.isSneaking() ? ClickType.SHIFT_RIGHT : ClickType.RIGHT, list.get(0))), 0);
                } else {
                    List<NPCCreator> list = LiteFP.list_npc.values().stream().filter(npcCreator -> npcCreator.getEntityPlayer().getBukkitEntity().getEntityId() == id).collect(Collectors.toList());
                    if (!list.isEmpty()) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(LiteFP.instance, () -> {
                            Bukkit.getPluginManager().callEvent(new InteractNPCEvent(player, player.isSneaking() ? ClickType.SHIFT_RIGHT : ClickType.RIGHT, list.get(0)));
                            return;
                        }, 0);
                    }
                }
                completableFuture.completeOnTimeout(null, 300, TimeUnit.MILLISECONDS).thenRun(() -> processedPackets.remove(id));
            }
        }
    }


    private Object getValue(Object instance, String name) {
        Object result = null;
        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            result = field.get(instance);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Object getValueField(Object instance, String name) {
        Object result = null;
        try {
            Field field = instance.getClass().getField(name);
            if (field == null)
                return null;
            field.setAccessible(true);
            result = field.get(instance);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
        return result;
    }

    public static Channel getPlayerChannel(Player player) {
        try {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().c;
            Field field = PlayerConnection.class.getDeclaredField("h");
            field.setAccessible(true);
            return  ((NetworkManager) field.get(conn)).m;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
