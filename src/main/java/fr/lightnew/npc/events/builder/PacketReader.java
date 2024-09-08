package fr.lightnew.npc.events.builder;

import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.entities.npc.NPCCreator;
import fr.lightnew.npc.tools.ConsoleLog;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PacketReader {

    Channel channel;
    private static Map<UUID, Channel> channels = new HashMap<>();
    private Map<UUID, Integer> processedPackets = new WeakHashMap<>();

    public void uninject(Player player) {
        channel = channels.get(player.getUniqueId());
        if (channel.pipeline().get("PacketInjector") != null)
            channel.pipeline().remove("PacketInjector");
    }

    public void readPacket(Player player, Packet<?> packet) {
        if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {
            int id = (int) getValue(packet, "a");
            if (!processedPackets.containsValue(id)) {
                CompletableFuture<Void> completableFuture = new CompletableFuture<>();
                processedPackets.put(player.getUniqueId(), id);

                List<NPCCreator> list = LiteFP.list_npc.values().stream().filter(npcCreator -> npcCreator.getServerPlayer().getBukkitEntity().getEntityId() == id).toList();
                if (list.isEmpty())
                    return;
                NPCCreator npc = list.get(0);

                int size = getValue(packet, "b").getClass().getDeclaredFields().length;
                ClickType type;
                if (size == 0)
                    type = player.isSneaking() ? ClickType.SHIFT_LEFT : ClickType.LEFT;
                else if (size == 2)
                    type = player.isSneaking() ? ClickType.SHIFT_RIGHT : ClickType.RIGHT;
                else
                    type = ClickType.UNKNOWN;

                if (getValue(packet, "b").getClass().getDeclaredFields().length == 0) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(LiteFP.instance, () ->
                            Bukkit.getPluginManager().callEvent(new InteractNPCEvent(player, type, npc)), 0);
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(LiteFP.instance, () -> {
                        Bukkit.getPluginManager().callEvent(new InteractNPCEvent(player, type, npc));
                    }, 0);
                }
                completableFuture.completeOnTimeout(null, 50, TimeUnit.MILLISECONDS).thenRun(() -> processedPackets.remove(player.getUniqueId(), id));
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

    public Object getValuePlayer(Object packet, String fieldName) {
        try {
            Field field = packet.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(packet);
        } catch (Exception exception) {
            ConsoleLog.info("Field '" + fieldName + "' not found in class: " + packet.getClass().getName());
            exception.printStackTrace();
        }
        return null;
    }

    public void inject(Player player) {
        UUID uuid = player.getUniqueId();
        String readerName = "Reader-" + uuid;
        CraftPlayer craftPlayer = (CraftPlayer) player;
        Object connection = getValuePlayer(craftPlayer.getHandle().connection, "h");

        if (connection == null) {
            ConsoleLog.info("Connection field is null!");
            return;
        }
        if (!(connection instanceof Connection)) {
            ConsoleLog.info("Incorrect type: " + connection.getClass().getName());
            return;
        }
        channel = ((Connection) connection).channel;
        if (channel.pipeline() == null || channel.pipeline().get(readerName) != null) {
            ConsoleLog.info("Incorrect channel: " + channel);
            return;
        }
        channels.put(uuid, channel);
        channel.pipeline().addAfter("decoder", readerName, new MessageToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext channelHandlerContext, Object packet, List list) throws Exception {
                list.add(packet);
                readPacket(player, (Packet<?>) packet);
            }
        });
    }

}
