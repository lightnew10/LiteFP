package fr.lightnew.npc.entities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.sql.RequestNPC;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@ToString
public class NPCCreator {

    private int id;
    private String name;
    private String skinName;
    private String texture;
    private String signature;
    private Location location;
    private List<List<Pair<EnumItemSlot, ItemStack>>> equipment;
    private EffectNPC effects;
    private EntityPlayer entityPlayer;
    private MetadataNPC metadataNPC;

    private MinecraftServer nmsServer;
    private WorldServer nmsWorld;
    private GameProfile gameProfile;

    public NPCCreator(String nameNPC, Location locationNPC, String nameSkin) {
        id = new Random().nextInt(99);
        name = nameNPC;
        location = locationNPC;
        skinName = nameSkin;
        this.texture = null;
        this.signature = null;
        entityPlayer = defaultEntityPlayer();
        metadataNPC = new MetadataNPC();
    }

    public NPCCreator(String nameNPC, Location locationNPC, String nameSkin, String texture, String signature) {
        id = new Random().nextInt(0, 999999999);
        name = nameNPC;
        location = locationNPC;
        skinName = nameSkin;
        this.texture = texture;
        this.signature = signature;
        entityPlayer = defaultEntityPlayer();
        metadataNPC = new MetadataNPC();
    }

    private EntityPlayer defaultEntityPlayer() {
        this.nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        this.nmsWorld = ((CraftWorld) Bukkit.getWorld(location.getWorld().getName())).getHandle();
        this.gameProfile = new GameProfile(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&', name));
        effects = new EffectNPC();
        equipment = new ArrayList<>();
        EntityPlayer npc = new EntityPlayer(nmsServer, nmsWorld,  gameProfile);
        npc.e(location.getX(), location.getY(), location.getZ());
        npc.getBukkitEntity().setSleepingIgnored(true);
        PlayerInteractManager interactManager = new PlayerInteractManager(npc);
        interactManager.a(nmsWorld);
        return npc;
    }

    public void createNPC(Player player) {
        if (skinName != null) {
            String[] skin = getSkin(skinName);
            if (skin != null) {
                entityPlayer.getBukkitEntity().getProfile().getProperties().clear();
                entityPlayer.getBukkitEntity().getProfile().getProperties().put("textures", new Property("textures", skin[0], skin[1]));
            }
        }
        updateAllPacketEntityPlayer(player);
        return;
    }

    public void playAnimation(Player player, AnimationNPC animationNPC) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        connection.a(new PacketPlayOutAnimation(entityPlayer, animationNPC.getId()));
    }

    public void changeMetadata(Player player, MetadataNPC metadataNPC) {
        DataWatcher dataWatcher = new DataWatcher(entityPlayer);
        DataWatcherObject<EntityPose> POSE = new DataWatcherObject<>(6, DataWatcherRegistry.v);
        DataWatcherObject<Boolean> GRAVITY = new DataWatcherObject<>(5, DataWatcherRegistry.k);
        DataWatcherObject<Byte> DEFAULT_DATA_INDEX = new DataWatcherObject<>(0, DataWatcherRegistry.a);

        setDataWatcher(dataWatcher, POSE, metadataNPC.getPose().getPose());
        setDataWatcher(dataWatcher, GRAVITY, metadataNPC.isHasNoGravity());
        setDataWatcher(dataWatcher, DEFAULT_DATA_INDEX, metadataNPC.getBytesDefaultData());
        dataWatcher.refresh(entityPlayer);

        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        connection.a(new PacketPlayOutEntityMetadata(entityPlayer.getBukkitEntity().getEntityId(), dataWatcher.b()));
        createTeamWithGlowColor(player, ChatColor.RED);
    }

    private <T> void setDataWatcher(DataWatcher dataWatcher, DataWatcherObject<T> datawatcherobject, T t0) {
        dataWatcher.a(datawatcherobject, t0);
        dataWatcher.markDirty(datawatcherobject);
    }

    public void createTeamWithGlowColor(Player player, ChatColor color) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        String teamName = String.valueOf(new Random().nextInt(9999));

        Scoreboard scoreboard = new Scoreboard();
        ScoreboardTeam team = new ScoreboardTeam(scoreboard, teamName);
        team.a(CraftChatMessage.fromJSONOrString(teamName));
        team.a(EnumChatFormat.valueOf(color.name()));

        // Name tag visibility
        //team.a(ScoreboardTeamBase.EnumNameTagVisibility.b);
        // Collision rule
        team.a(ScoreboardTeamBase.EnumTeamPush.b);

        //add NPC in the team
        team.g().add(entityPlayer.getBukkitEntity().getName());

        PacketPlayOutScoreboardTeam createTeamPacket = PacketPlayOutScoreboardTeam.a(team, true);
        connection.a(createTeamPacket);
    }

    public void moveNaturally(final Location startLocation, final Location endLocation, final double speed, boolean infinite) {
        org.bukkit.util.Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize();

        new BukkitRunnable() {
            Location currentLocation = startLocation.clone();

            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (currentLocation.distance(endLocation) <= 1) {
                        this.cancel();
                        if (infinite)
                            moveNaturally(endLocation, startLocation, speed, true);
                        return;
                    }
                    Vector moveVector = direction.clone().multiply(speed);
                    currentLocation.add(moveVector);
                    if (!p.isOnline())
                        return;
                    updatePosition(p, currentLocation);
                });
            }
        }.runTaskTimer(LiteFP.instance, 0L, 1L);
    }

    public void updateAllPacketEntityPlayer(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture<Void> future2 = new CompletableFuture<>();
        CompletableFuture<Void> future3 = new CompletableFuture<>();
        entityPlayer.e(location.getX(), location.getY(), location.getZ());
        entityPlayer.a(EnumGamemode.a);

        connection.a(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.a, entityPlayer));
        connection.a(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.d, entityPlayer));
        connection.a(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.f, entityPlayer));
        connection.a(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        future.completeOnTimeout(null, 300, TimeUnit.MILLISECONDS);
        future.thenRun(() -> connection.a(new PacketPlayOutEntityMetadata(entityPlayer.getBukkitEntity().getEntityId(), Arrays.asList(new DataWatcher.b<>(17, DataWatcherRegistry.a, (byte) 127)))));

        for (List<Pair<EnumItemSlot, ItemStack>> pairs : equipment)
            connection.a(new PacketPlayOutEntityEquipment(entityPlayer.getBukkitEntity().getEntityId(), pairs));

        future2.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
        future2.thenRun(() -> {
            connection.a(new PacketPlayOutEntityTeleport(entityPlayer));
            connection.a(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityPlayer.getBukkitEntity().getEntityId(), (byte) ((location.getYaw()%360.)*256/360), (byte) ((location.getPitch()%360.)*256/360), false));
            connection.a(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw()%360.)*256/360)));
        });

        future3.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
        future3.thenRun(() -> connection.a(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entityPlayer.ct()))));
        addWaiting();
    }

    public void updateName(Player player, String newname) {
        remove(player);
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture<Void> future2 = new CompletableFuture<>();
        CompletableFuture<Void> future3 = new CompletableFuture<>();

        this.name = ChatColor.translateAlternateColorCodes('&', newname);
        EntityPlayer npc = new EntityPlayer(nmsServer, nmsWorld,  new GameProfile(UUID.randomUUID(), name));
        String[] skin = getSkin(skinName);
        if (skin != null) {
            npc.getBukkitEntity().getProfile().getProperties().removeAll("textures");
            npc.getBukkitEntity().getProfile().getProperties().put("textures", new Property("textures", skin[0], skin[1]));
        }
        npc.e(location.getX(), location.getY(), location.getZ());
        entityPlayer = npc;

        connection.a(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.a, entityPlayer));
        connection.a(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.d, entityPlayer));
        connection.a(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.f, entityPlayer));
        connection.a(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        future.completeOnTimeout(null, 300, TimeUnit.MILLISECONDS);
        future.thenRun(() -> connection.a(new PacketPlayOutEntityMetadata(entityPlayer.getBukkitEntity().getEntityId(), Arrays.asList(new DataWatcher.b<>(17, DataWatcherRegistry.a, (byte) 127)))));

        for (List<Pair<EnumItemSlot, ItemStack>> pairs : equipment)
            connection.a(new PacketPlayOutEntityEquipment(entityPlayer.getBukkitEntity().getEntityId(), pairs));

        future2.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
        future2.thenRun(() -> {
            connection.a(new PacketPlayOutEntityTeleport(entityPlayer));
            connection.a(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityPlayer.getBukkitEntity().getEntityId(), (byte) ((location.getYaw()%360.)*256/360), (byte) ((location.getPitch()%360.)*256/360), false));
            connection.a(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw()%360.)*256/360)));
        });

        future3.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
        future3.thenRun(() -> connection.a(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entityPlayer.ct()))));
        addWaiting();
    }

    public void updateSkin(Player player, String skin) {
        remove(player);
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture<Void> future2 = new CompletableFuture<>();
        CompletableFuture<Void> future3 = new CompletableFuture<>();

        skinName = skin;
        String[] skins = getSkin(skinName);
        if (skins != null) {
            entityPlayer.getBukkitEntity().getProfile().getProperties().removeAll("textures");
            entityPlayer.getBukkitEntity().getProfile().getProperties().put("textures", new Property("textures", skins[0], skins[1]));
        }

        connection.a(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.a, entityPlayer));
        connection.a(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.d, entityPlayer));
        connection.a(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.a.f, entityPlayer));
        connection.a(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        future.completeOnTimeout(null, 300, TimeUnit.MILLISECONDS);
        future.thenRun(() -> connection.a(new PacketPlayOutEntityMetadata(entityPlayer.getBukkitEntity().getEntityId(), Arrays.asList(new DataWatcher.b<>(17, DataWatcherRegistry.a, (byte) 127)))));

        for (List<Pair<EnumItemSlot, ItemStack>> pairs : equipment)
            connection.a(new PacketPlayOutEntityEquipment(entityPlayer.getBukkitEntity().getEntityId(), pairs));

        future2.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
        future2.thenRun(() -> {
            connection.a(new PacketPlayOutEntityTeleport(entityPlayer));
            connection.a(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityPlayer.getBukkitEntity().getEntityId(), (byte) ((location.getYaw()%360.)*256/360), (byte) ((location.getPitch()%360.)*256/360), false));
            connection.a(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw()%360.)*256/360)));
        });

        future3.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
        future3.thenRun(() -> connection.a(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entityPlayer.ct()))));
        addWaiting();
    }

    public void updatePosition(Player player, Location newlocation) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        CompletableFuture<Void> future = new CompletableFuture<>();
        location = newlocation;
        entityPlayer.e(location.getX(), location.getY(), location.getZ());

        updateAllPacketEntityPlayer(player);
        /*future.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
        future.thenRun(() -> {
            connection.a(new PacketPlayOutEntityTeleport(entityPlayer));
            connection.a(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityPlayer.getBukkitEntity().getEntityId(), (byte) ((location.getYaw()%360.)*256/360), (byte) ((location.getPitch()%360.)*256/360), false));
            connection.a(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw()%360.)*256/360)));
        });*/
        addWaiting();
    }

    public void updateStuff(Player player, List<List<Pair<EnumItemSlot, ItemStack>>> content) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        CompletableFuture<Void> future = new CompletableFuture<>();
        equipment = content;

        for (List<Pair<EnumItemSlot, ItemStack>> pairs : equipment) {
            future.completeOnTimeout(null, 50, TimeUnit.MILLISECONDS);
            future.thenRun(() -> connection.a(new PacketPlayOutEntityEquipment(entityPlayer.getBukkitEntity().getEntityId(), pairs)));
        }
        addWaiting();
    }

    public void updateLookLock(boolean b) {
        effects.setLookLock(b);
    }

    public void updateCollides(boolean b) {
        entityPlayer.i(true);
        effects.setCollides(b);
    }

    public void remove(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        connection.a(new PacketPlayOutEntityDestroy(entityPlayer.af(), 0));
        connection.a(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entityPlayer.ct())));
    }

    public void destroy(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        connection.a(new PacketPlayOutEntityDestroy(entityPlayer.af(), 0));
        connection.a(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entityPlayer.ct())));
        LiteFP.list_npc.remove(id);
        RequestNPC.removeNPC(id);
    }

    private void addWaiting() {
        LiteFP.list_waiting_update_npc.put(this.id, this);
        LiteFP.list_npc.put(this.id, this);
    }

    public String[] getSkin(String nameSkin) {
        nameSkin = nameSkin.toLowerCase();
        if (texture != null || signature != null)
            return new String[] {texture, signature};
        if (LiteFP.skinCache.containsKey(nameSkin))
            return LiteFP.skinCache.get(nameSkin);
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + nameSkin);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            String uuid = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();

            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = new JsonParser().parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = property.get("value").getAsString();
            String signature = property.get("signature").getAsString();
            return new String[] {texture, signature};
        } catch (Exception e) {
            return null;
        }
    }
}
