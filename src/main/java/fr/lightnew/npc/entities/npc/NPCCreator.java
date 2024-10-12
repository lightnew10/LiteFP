package fr.lightnew.npc.entities.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.commands.CommandNPC;
import fr.lightnew.npc.entities.ExternHologram;
import fr.lightnew.npc.entities.LFPLocation;
import fr.lightnew.npc.entities.metas.AnimationNPC;
import fr.lightnew.npc.sql.RequestNPC;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
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

    private Set<UUID> showingNPCToPlayer = new HashSet<>();

    private int id;
    private ServerPlayer serverPlayer;
    private String name;
    private final String nameNPCMRegistry;
    private String skinName;
    private String texture;
    private String signature;
    private Location location;
    private int disappearNPCRange;
    private ExternHologram linkedHologram;
    private double linkedHologramHeight;
    private double distance_look;

    private List<List<Pair<EquipmentSlot, ItemStack>>> equipment;
    private EffectNPC effects;
    private MetadataNPC metadataNPC;
    private NavigableMap<Integer,List<LFPLocation>> multiLocation = new TreeMap<>();

    private MinecraftServer minecraftServer;
    private ServerLevel serverLevel;
    private GameProfile gameProfile;
    private PlayerTeam playerTeam;
    private Hologram hologram;
    private Location location_hologram;

    public NPCCreator(String nameNPC, Location locationNPC, String nameSkin) {
        this.id = new Random().nextInt(999999999);
        if (!nameNPC.startsWith("§r"))
            name = ChatColor.RESET + "" + ChatColor.translateAlternateColorCodes('&', nameNPC);
        else
            name = ChatColor.translateAlternateColorCodes('&', nameNPC);
        nameNPCMRegistry = "NPC-" + UUID.randomUUID().toString().substring(0, 12);
        this.disappearNPCRange = 48;
        this.linkedHologramHeight = 3.5;
        this.distance_look = 5;
        this.location = locationNPC;
        this.skinName = nameSkin;
        String[] skins = getSkin(skinName);
        if (skins != null) {
            this.texture = skins[0];
            this.signature = skins[1];
        }
        this.serverPlayer = defaultEntityPlayer();
        this.metadataNPC = new MetadataNPC();
    }

    public NPCCreator(String nameNPC, Location locationNPC, String nameSkin, String texture, String signature) {
        id = new Random().nextInt(0, 999999999);
        if (!nameNPC.startsWith("§r"))
            name = ChatColor.RESET + "" + ChatColor.translateAlternateColorCodes('&', nameNPC);
        else
            name = ChatColor.translateAlternateColorCodes('&', nameNPC);
        nameNPCMRegistry = "NPC-" + UUID.randomUUID().toString().substring(0, 12);
        this.disappearNPCRange = 48;
        this.linkedHologramHeight = 3.5;
        this.distance_look = 5;
        location = locationNPC;
        skinName = nameSkin;
        this.texture = texture;
        this.signature = signature;
        serverPlayer = defaultEntityPlayer();
        metadataNPC = new MetadataNPC();
    }

    private ServerPlayer defaultEntityPlayer() {
        this.minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        this.serverLevel = ((CraftWorld) Bukkit.getWorld(location.getWorld().getName())).getHandle();
        effects = new EffectNPC();
        equipment = new ArrayList<>();
        GameProfile gameProfile;
        if (Bukkit.getServer().getOnlineMode())
            gameProfile = new GameProfile(UUID.randomUUID(), nameNPCMRegistry);
        else
            gameProfile = new GameProfile(UUID.nameUUIDFromBytes((nameNPCMRegistry).getBytes()), nameNPCMRegistry);

        gameProfile.getProperties().put("textures", new Property("textures", texture, signature));
        ServerPlayer npc = new ServerPlayer(minecraftServer, serverLevel, gameProfile);
        this.gameProfile = gameProfile;
        npc.setPos(location.getX(), location.getY(), location.getZ());

        playerTeam = new PlayerTeam(new Scoreboard(), "npc" + id);
        playerTeam.setNameTagVisibility(Team.Visibility.NEVER);
        playerTeam.getPlayers().add(nameNPCMRegistry);
        Location loc = location.clone();
        loc.setY(loc.getY() + 2.5);
        location_hologram = loc;
        hologram = LiteFP.holographicDisplaysAPI.createHologram(location_hologram);
        hologram.getLines().appendText(name);
        return npc;
    }

    public void spawnNPC(Player player) {
        if (skinName != null) {
            String[] skin = getSkin(skinName);
            if (skin != null) {
                serverPlayer.getBukkitEntity().getProfile().getProperties().clear();
                serverPlayer.getBukkitEntity().getProfile().getProperties().put("textures", new Property("textures", skin[0], skin[1]));
            }
        }
        updateAllPacketEntityPlayer(player);
    }

    public void playAnimation(Player player, AnimationNPC animationNPC) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundAnimatePacket(serverPlayer, animationNPC.getId()));
    }

    public void changeMetadata(Player player, MetadataNPC metadataNPC, boolean delay) {
        SynchedEntityData dataWatcher = serverPlayer.getEntityData();

        dataWatcher.set(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), metadataNPC.getBytesDataLivingNPC());
        dataWatcher.set(new EntityDataAccessor<>(5, EntityDataSerializers.BOOLEAN), metadataNPC.isHasNoGravity());
        dataWatcher.set(new EntityDataAccessor<>(6, EntityDataSerializers.POSE), metadataNPC.getPose());

        changeTeam(player, metadataNPC.getColorGlow(), metadataNPC.isCollide());

        CompletableFuture<Void> future = new CompletableFuture<>();
        if (delay) {
            future.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
            future.thenRun(() -> ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetEntityDataPacket(serverPlayer.getId(), dataWatcher.getNonDefaultValues())));
        } else
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetEntityDataPacket(serverPlayer.getId(), dataWatcher.getNonDefaultValues()));
    }

    private void changeTeam(Player player, ChatFormatting color, boolean collide) {
        playerTeam.setNameTagVisibility(Team.Visibility.NEVER);
        if (!playerTeam.getPlayers().contains(nameNPCMRegistry))
            playerTeam.getPlayers().add(nameNPCMRegistry);

        playerTeam.setCollisionRule(collide ? Team.CollisionRule.ALWAYS : Team.CollisionRule.NEVER);
        if (color != null)
            playerTeam.setColor(color);

        ((CraftPlayer) player).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeam));
        ((CraftPlayer) player).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true));
    }

    public void moveNaturally(final Location startLocation, final Location endLocation, final double speed, boolean infinite) {
        Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize();

        new BukkitRunnable() {
            final Location currentLocation = startLocation.clone();

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
                    teleport(p, currentLocation, false);
                });
            }
        }.runTaskTimer(LiteFP.instance, 0L, 1L);
    }

    public void moveMultiLocalisationCommand(Player player, List<LFPLocation> locations) {
        NPCCreator creator = new NPCCreator("NPC-TESTING", locations.stream().findFirst().get().getLocation(), player.getName());
        creator.spawnNPC(player);
        MetadataNPC meta = creator.metadataNPC;
        if (locations == null)
            return;
        locations = new ArrayList<>(locations);
        List<LFPLocation> finalLocations = locations;
        new BukkitRunnable() {
            @Override
            public void run() {
                finalLocations.stream().findFirst().ifPresent(element -> {
                    if (element.getLocation() == null)
                        return;
                    //Teleport NPC
                    creator.teleportPresetMultiTeleport(player, element.getLocation());
                    creator.setLocation_hologram(element.getLocation());
                    //Play animation
                    if (element.getAnimationNPC() != null)
                        playAnimation(player, element.getAnimationNPC());
                    //Set Pose
                    meta.setPose(element.getPoseNPC());
                    creator.changeMetadata(player, meta, true);

                    finalLocations.remove(element);
                    if (finalLocations.isEmpty()) {
                        creator.hologram.delete();
                        CompletableFuture<Void> f = new CompletableFuture<>();
                        f.completeOnTimeout(null, 3, TimeUnit.SECONDS).thenRunAsync(() -> {
                            creator.destroy(player);
                            creator.hologram.delete();
                        });
                        cancel();
                    }
                });
            }
        }.runTaskTimer(LiteFP.instance, 0L, 0L);
    }

    public void updateAllPacketEntityPlayer(Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        SynchedEntityData dataWatcher = serverPlayer.getEntityData();
        dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) (0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40));

        CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture<Void> future2 = new CompletableFuture<>();
        CompletableFuture<Void> future3 = new CompletableFuture<>();
        CompletableFuture<Void> future4 = new CompletableFuture<>();
        serverPlayer.setPos(location.getX(), location.getY(), location.getZ());

        connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, serverPlayer));
        connection.send(new ClientboundAddPlayerPacket(serverPlayer));
        future.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
        future.thenRun(() -> connection.send(new ClientboundSetEntityDataPacket(serverPlayer.getId(), dataWatcher.getNonDefaultValues())));

        if (equipment != null)
            updateEquipment(player, equipment);

        future4.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
        future4.thenRun(() -> {
            ((CraftPlayer) player).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true));
            ((CraftPlayer) player).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createPlayerPacket(playerTeam, name, ClientboundSetPlayerTeamPacket.Action.ADD));
        });

        future2.completeOnTimeout(null, 400, TimeUnit.MILLISECONDS);
        future2.thenRun(() -> {
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundMoveEntityPacket.Pos(serverPlayer.getId(), (short)(int)(location.getX() * 4096.0D), (short)(int)(location.getY() * 4096.0D), (short)(int)(location.getZ() * 4096.0D), true));
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundTeleportEntityPacket(serverPlayer));
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundMoveEntityPacket.Rot(serverPlayer.getId(), (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D), (byte)(int)(location.getPitch() % 360.0D * 256.0D / 360.0D), true));
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundRotateHeadPacket(serverPlayer, (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D)));
        });

        future3.completeOnTimeout(null, 300, TimeUnit.MILLISECONDS);
        future3.thenRun(() -> connection.send(new ClientboundPlayerInfoRemovePacket(List.of(serverPlayer.getUUID()))));

        addWaiting();
    }

    public void updateName(String newname) {
        this.name = ChatColor.translateAlternateColorCodes('&', newname);
        hologram.getLines().remove(0);
        hologram.getLines().insertText(0, name);
        addWaiting();
    }

    public void updateSkin(Player player, String skin) {
        remove(player);
        skinName = skin;
        String[] skins = getSkin(skinName);
        if (skins != null) {
            serverPlayer.getBukkitEntity().getProfile().getProperties().removeAll("textures");
            serverPlayer.getBukkitEntity().getProfile().getProperties().put("textures", new Property("textures", skins[0], skins[1]));
        }
        updateAllPacketEntityPlayer(player);
        addWaiting();
    }

    public void teleport(Player player, Location location, boolean save) {
        this.serverPlayer.setPos(location.getX(), location.getY(), location.getZ());
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        this.serverPlayer.setYRot(((yaw % 360) * 256 / 360));
        this.serverPlayer.setXRot(((pitch % 360) * 256 / 360));

        CompletableFuture<Void> timeOutRotation = new CompletableFuture<>();

        ((CraftPlayer) player).getHandle().connection.send(new ClientboundTeleportEntityPacket(serverPlayer));

        if (linkedHologram != null)
            linkedHologram.setLocation(location, linkedHologramHeight);
        timeOutRotation.completeOnTimeout(null, 300, TimeUnit.MILLISECONDS);
        timeOutRotation.thenRun(() -> {
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundMoveEntityPacket.Rot(serverPlayer.getId(), (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D), (byte)(int)(location.getPitch() % 360.0D * 256.0D / 360.0D), true));
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundRotateHeadPacket(serverPlayer, (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D)));
        });

        this.location = location;
        if (save)
            addWaiting();
    }

    public void teleportForAll(Location location, boolean save) {
        this.serverPlayer.setPos(location.getX(), location.getY(), location.getZ());
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        this.serverPlayer.setYRot(((yaw % 360) * 256 / 360));
        this.serverPlayer.setXRot(((pitch % 360) * 256 / 360));

        ClientboundTeleportEntityPacket clientboundTeleportEntityPacket = new ClientboundTeleportEntityPacket(serverPlayer);
        ClientboundMoveEntityPacket.Rot clientboundMoveEntityPacket = new ClientboundMoveEntityPacket.Rot(serverPlayer.getId(), (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D), (byte)(int)(location.getPitch() % 360.0D * 256.0D / 360.0D), true);
        ClientboundRotateHeadPacket clientboundRotateHeadPacket = new ClientboundRotateHeadPacket(serverPlayer, (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D));

        if (linkedHologram != null)
            linkedHologram.setLocation(location, linkedHologramHeight);
        Bukkit.getOnlinePlayers().forEach(player -> {
            CompletableFuture<Void> timeOutRotation = new CompletableFuture<>();

            ((CraftPlayer) player).getHandle().connection.send(clientboundTeleportEntityPacket);

            timeOutRotation.completeOnTimeout(null, 300, TimeUnit.MILLISECONDS);
            timeOutRotation.thenRun(() -> {
                ((CraftPlayer) player).getHandle().connection.send(clientboundMoveEntityPacket);
                ((CraftPlayer) player).getHandle().connection.send(clientboundRotateHeadPacket);
            });
        });

        this.location = location;
        if (save)
            addWaiting();
    }

    //Don't use that
    public void teleportPresetMultiTeleport(Player player, Location location) {
        this.serverPlayer.setPos(location.getX(), location.getY(), location.getZ());
        this.serverPlayer.setYRot(((location.getYaw() % 360) * 256 / 360));
        this.serverPlayer.setXRot(((location.getPitch() % 360) * 256 / 360));

        ((CraftPlayer) player).getHandle().connection.send(new ClientboundTeleportEntityPacket(serverPlayer));
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundRotateHeadPacket(serverPlayer, (byte) (int) (location.getYaw() % 360.0D * 256.0D / 360.0D)));

        this.location = location;
    }

    public void updateEquipment(Player player, List<List<Pair<EquipmentSlot, ItemStack>>> content) {
        this.equipment = content;
        for (List<Pair<EquipmentSlot, ItemStack>> pairs : this.equipment) {
            new CompletableFuture<>()
                    .completeOnTimeout(null, 50, TimeUnit.MILLISECONDS)
                    .thenRun(() -> ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetEquipmentPacket(serverPlayer.getId(), pairs)));
        }
        addWaiting();
    }

    public void updateLookLock(boolean b) {
        effects.setLookLock(b);
    }

    public void updateCollides(boolean b) {
        Bukkit.getOnlinePlayers().forEach(player -> changeTeam(player, ChatFormatting.RESET, b));
        effects.setCollides(b);
    }

    public void remove(Player player) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(serverPlayer.getId()));
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, serverPlayer));
    }

    public void destroy(Player player) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(serverPlayer.getId()));
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundPlayerInfoRemovePacket(List.of(serverPlayer.getUUID())));
        hologram.delete();
        LiteFP.list_npc.remove(id);
        RequestNPC.removeNPC(id);
    }

    private void addWaiting() {
        LiteFP.list_waiting_update_npc.put(this.id, this);
        LiteFP.list_npc.put(this.id, this);
    }

    public void addMultiLocation(List<LFPLocation> locations) {
        int id = 1;
        if (multiLocation.isEmpty()) {
            multiLocation.put(id, locations);
            return;
        }
        Map.Entry<Integer, List<LFPLocation>> lastEntry = multiLocation.lastEntry();
        id = lastEntry.getKey()+1;
        multiLocation.put(id, locations);
        //TODO SAVE THIS
    }

    public ServerPlayer getNPC() {
        return serverPlayer;
    }

    public String[] getSkin(String nameSkin) {
        nameSkin = nameSkin.toLowerCase();
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

    public void setLocation_hologram(Location location_hologram) {
        Location loc = location_hologram.clone();
        loc.setY(loc.getY()+2.5);
        hologram.setPosition(loc);
        this.location_hologram = loc;
    }

    public void setLinkedHologram(ExternHologram linkedHologram) {
        this.linkedHologram = linkedHologram;
        addWaiting();
    }

    public void setLinkedHologram(String name) {
        if (name == null)
            this.linkedHologram = null;
        else
            this.linkedHologram = CommandNPC.getExternHologram().getOrDefault(name, null);
        addWaiting();
    }

    public void setLinkedHologramHeight(double linkedHologramHeight) {
        this.linkedHologramHeight = linkedHologramHeight;
        addWaiting();
    }
}
