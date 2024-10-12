package fr.lightnew.npc.entities.custom;

import fr.lightnew.npc.entities.npc.MetadataNPC;
import fr.lightnew.npc.tools.ServerUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.npc.VillagerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@ToString
public class EntityCreator {

    private Entity entity;
    private EntityType type;
    private String name;
    private Boolean visibleName;
    private Location location;
    private MetadataNPC metadataNPC;

    public EntityCreator(EntityType entityType, Location location, String name) {
        this.type = entityType;
        this.entity = entityType.create(ServerUtils.getServerLevel());
        this.entity.setPos(location.getX(), location.getY(), location.getZ());
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.visibleName = true;
        this.location = location;
        this.metadataNPC = new MetadataNPC();
    }

    public void create(Player player) {
        updateAllPacketEntityPlayer(player);
    }

    public void create(Player player, MetadataNPC metadataNPC) {
        this.metadataNPC = metadataNPC;
        updateAllPacketEntityPlayer(player);
    }

    public <T> SynchedEntityData changeCustomMetadata(EntityDataAccessor<T> datawatcherobject, T t0) {
        SynchedEntityData dataWatcher = this.entity.getEntityData();
        dataWatcher.set(datawatcherobject, t0);
        return dataWatcher;
    }

    public <T> SynchedEntityData changeCustomMetadata(Map<EntityDataAccessor<T>, T> list) {
        SynchedEntityData dataWatcher = this.entity.getEntityData();
        list.forEach(dataWatcher::set);
        return dataWatcher;
    }

    public void changeMetadata(Player player, MetadataNPC metadataNPC, boolean delay) {
        SynchedEntityData dataWatcher = this.entity.getEntityData();

        dataWatcher.set(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT), Optional.ofNullable(CraftChatMessage.fromStringOrNull(name)));
        dataWatcher.set(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), visibleName);

        switch (this.entity.getBukkitEntity().getType().toString()) {
            case "VILLAGER": {
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.VILLAGER_DATA), new VillagerData(metadataNPC.getVillagerType(), metadataNPC.getProfession(), 0));
                break;
            }
            case "PARROT": {
                dataWatcher.set(new EntityDataAccessor<>(19, EntityDataSerializers.INT), metadataNPC.getParrotColor().getId());
                break;
            }
            case "WOLF": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), metadataNPC.getBytesTameableWolf());
                dataWatcher.set(new EntityDataAccessor<>(20, EntityDataSerializers.INT), metadataNPC.getWolfColorCollar().getId());
                break;
            }
            case "CAT": {
                Registry<CatVariant> catVariantRegistry = BuiltInRegistries.CAT_VARIANT;
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), metadataNPC.getBytesTameableCat());
                dataWatcher.set(new EntityDataAccessor<>(19, EntityDataSerializers.CAT_VARIANT), catVariantRegistry.get(metadataNPC.getCatVariant()));
                dataWatcher.set(new EntityDataAccessor<>(21, EntityDataSerializers.BOOLEAN), metadataNPC.isCatRelaxed());
                dataWatcher.set(new EntityDataAccessor<>(22, EntityDataSerializers.INT), metadataNPC.getCatColorCollar().getId());
                break;
            }
            case "GOAT": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BOOLEAN), metadataNPC.isGoatScreaming());
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.BOOLEAN), metadataNPC.isGoatLeftHorn());
                dataWatcher.set(new EntityDataAccessor<>(19, EntityDataSerializers.BOOLEAN), metadataNPC.isGoatRightHorn());
                break;
            }
            case "STRIDER": {
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.BOOLEAN), metadataNPC.isStriderShaking());
                dataWatcher.set(new EntityDataAccessor<>(19, EntityDataSerializers.BOOLEAN), metadataNPC.isStriderShaking());
                break;
            }
            case "SHEEP": {
                byte bitmask = metadataNPC.getSheepColor().getId();
                if (metadataNPC.isSheepSheared())
                    bitmask |= 0x10;
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), bitmask);
                break;
            }
            case "MOOSHROOM": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.STRING), metadataNPC.getMooshroomColor().getVariant());
                break;
            }
            case "POLAR_BEAR": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BOOLEAN), metadataNPC.isPolarBearStandingUP());
                break;
            }
            case "TURTLE": {
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.BOOLEAN), metadataNPC.isTurtleEgg());
                dataWatcher.set(new EntityDataAccessor<>(19, EntityDataSerializers.BOOLEAN), metadataNPC.isTurtleLayingEgg());
                break;
            }
            case "RABBIT": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.INT), metadataNPC.getRabbitColor().getId());
                break;
            }
            case "PIG": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BOOLEAN), metadataNPC.isPigSaddle());
                break;
            }
            case "PANDA": {
                dataWatcher.set(new EntityDataAccessor<>(22, EntityDataSerializers.BYTE), metadataNPC.getBytesPanda());
                break;
            }
            case "FROG": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.FROG_VARIANT), metadataNPC.getFrogVariant());
                break;
            }
            case "FOX": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.INT), metadataNPC.getFoxVariant().getId());
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.BYTE), metadataNPC.getBytesFox());
                break;
            }
            case "BEE": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), metadataNPC.getBytesBee());
                break;
            }
            case "AXOLOTL": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.INT), metadataNPC.getAxolotlVariant().getId());
                break;
            }
            case "LLAMA": {
                dataWatcher.set(new EntityDataAccessor<>(20, EntityDataSerializers.INT), metadataNPC.getLlamaCarpetColor().getId());
                dataWatcher.set(new EntityDataAccessor<>(20, EntityDataSerializers.INT), metadataNPC.getLlamaVariant().getId());
                break;
            }
            case "CAMEL": {
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.BOOLEAN), metadataNPC.isCamelDashing());
                break;
            }
            case "HORSE": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), metadataNPC.getBytesHorse());
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.INT), metadataNPC.getIntegerHorse());
                break;
            }
            case "SNIFFER": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.INT), metadataNPC.getSnifferVariant().getId());
                break;
            }
            case "BAT": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.BYTE), metadataNPC.getByteBat());
                break;
            }
            case "ARMOR_STAND": {
                dataWatcher.set(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), metadataNPC.getBytesDataLivingNPC());
                dataWatcher.set(new EntityDataAccessor<>(15, EntityDataSerializers.BYTE), metadataNPC.getBytesArmorStand());
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.ROTATIONS), metadataNPC.getArmorStandHead());
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.ROTATIONS), metadataNPC.getArmorStandBody());
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.ROTATIONS), metadataNPC.getArmorStandLeftArm());
                dataWatcher.set(new EntityDataAccessor<>(19, EntityDataSerializers.ROTATIONS), metadataNPC.getArmorStandRightArm());
                dataWatcher.set(new EntityDataAccessor<>(20, EntityDataSerializers.ROTATIONS), metadataNPC.getArmorStandLeftLeg());
                dataWatcher.set(new EntityDataAccessor<>(21, EntityDataSerializers.ROTATIONS), metadataNPC.getArmorStandRightLeg());
                break;
            }
            case "SLIME": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.INT), metadataNPC.getSlimeVariant().getSize());
                break;
            }
            case "ENDERMAN": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BOOLEAN), metadataNPC.isEndermanScreaming());
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.BOOLEAN), metadataNPC.isEndermanStaring());
                break;
            }
            case "ZOMBIE": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.BOOLEAN), metadataNPC.isZombieBaby());
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.BOOLEAN), metadataNPC.isZombieBecomingDrowned());
                break;
            }
            case "ZOGLIN": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.BOOLEAN), metadataNPC.isZoglinBaby());
                break;
            }
            case "WITHER": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.INT), metadataNPC.getWitherTargetEntityCenterHead());
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.INT), metadataNPC.getWitherTargetEntityLeftHead());
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.INT), metadataNPC.getWitherTargetEntityRightHead());
                break;
            }
            case "SPIDER": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.BYTE), metadataNPC.getByteSpider());
                break;
            }
            case "VEX": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.BYTE), metadataNPC.getByteVex());
                break;
            }
            case "WITCH": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BOOLEAN), metadataNPC.isWitchDrinkingPotion());
                break;
            }
            case "PILLAGER": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BOOLEAN), metadataNPC.isPillagerCharging());
                break;
            }
            case "RAIDER": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.BOOLEAN), metadataNPC.isRaiderCelebrating());
                break;
            }
            case "CREEPER": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.INT), metadataNPC.getCreeperVariant().getId());
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BOOLEAN), metadataNPC.isCreeperCharged());
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.BOOLEAN), metadataNPC.isCreeperIgnited());
                break;
            }
            case "BLAZE": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.BYTE), metadataNPC.getByteBlaze());
                break;
            }
            case "PIGLIN": {
                dataWatcher.set(new EntityDataAccessor<>(17, EntityDataSerializers.BOOLEAN), metadataNPC.isPiglinBaby());
                dataWatcher.set(new EntityDataAccessor<>(18, EntityDataSerializers.BOOLEAN), metadataNPC.isPiglinChargingBow());
                dataWatcher.set(new EntityDataAccessor<>(19, EntityDataSerializers.BOOLEAN), metadataNPC.isPiglinDancing());
                break;
            }
            case "SNOW_GOLEM": {
                dataWatcher.set(new EntityDataAccessor<>(16, EntityDataSerializers.BYTE), metadataNPC.getByteSnowGolem());
                break;
            }
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        if (delay) {
            future.completeOnTimeout(null, 150, TimeUnit.MILLISECONDS);
            future.thenRun(() -> ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetEntityDataPacket(this.entity.getId(), dataWatcher.getNonDefaultValues())));
        } else
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetEntityDataPacket(this.entity.getId(), dataWatcher.getNonDefaultValues()));
        this.metadataNPC = metadataNPC;
    }

    public void teleport(Player player, Location location) {
        this.entity.setPos(location.getX(), location.getY(), location.getZ());
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        this.entity.setYRot(((yaw % 360) * 256 / 360));
        this.entity.setXRot(((pitch % 360) * 256 / 360));

        CompletableFuture<Void> timeOutRotation = new CompletableFuture<>();

        ((CraftPlayer) player).getHandle().connection.send(new ClientboundTeleportEntityPacket(this.entity));

        timeOutRotation.completeOnTimeout(null, 400, TimeUnit.MILLISECONDS);
        timeOutRotation.thenRun(() -> {
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)(int)(location.getX() * 4096.0D), (short)(int)(location.getY() * 4096.0D), (short)(int)(location.getZ() * 4096.0D), true));
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundTeleportEntityPacket(this.entity));
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D), (byte)(int)(location.getPitch() % 360.0D * 256.0D / 360.0D), true));
            ((CraftPlayer) player).getHandle().connection.send(new ClientboundRotateHeadPacket(this.entity, (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D)));
        });
    }

    public void teleportForAll(Location location) {
        this.entity.setPos(location.getX(), location.getY(), location.getZ());
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        this.entity.setYRot(((yaw % 360) * 256 / 360));
        this.entity.setXRot(((pitch % 360) * 256 / 360));

        Bukkit.getOnlinePlayers().forEach(player -> {
            CompletableFuture<Void> timeOutRotation = new CompletableFuture<>();

            ((CraftPlayer) player).getHandle().connection.send(new ClientboundTeleportEntityPacket(this.entity));

            timeOutRotation.completeOnTimeout(null, 400, TimeUnit.MILLISECONDS);
            timeOutRotation.thenRun(() -> {
                ((CraftPlayer) player).getHandle().connection.send(new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)(int)(location.getX() * 4096.0D), (short)(int)(location.getY() * 4096.0D), (short)(int)(location.getZ() * 4096.0D), true));
                ((CraftPlayer) player).getHandle().connection.send(new ClientboundTeleportEntityPacket(this.entity));
                ((CraftPlayer) player).getHandle().connection.send(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D), (byte)(int)(location.getPitch() % 360.0D * 256.0D / 360.0D), true));
                ((CraftPlayer) player).getHandle().connection.send(new ClientboundRotateHeadPacket(this.entity, (byte)(int)(location.getYaw() % 360.0D * 256.0D / 360.0D)));
            });
        });
    }

    private void updateAllPacketEntityPlayer(Player player) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        this.entity.setPos(location.getX(), location.getY(), location.getZ());

        connection.send(new ClientboundAddEntityPacket(this.entity));
        changeMetadata(player, metadataNPC, false);

        connection.send(new ClientboundMoveEntityPacket.Pos(this.entity.getId(),
                (short) (int) (location.getX() * 4096.0D),
                (short) (int) (location.getY() * 4096.0D),
                (short) (int) (location.getZ() * 4096.0D),
                true));
        connection.send(new ClientboundTeleportEntityPacket(this.entity));
        connection.send(new ClientboundMoveEntityPacket.Rot(this.entity.getId(),
                (byte) (int) (location.getYaw() % 360.0D * 256.0D / 360.0D),
                (byte) (int) (location.getPitch() % 360.0D * 256.0D / 360.0D),
                true));
        connection.send(new ClientboundRotateHeadPacket(this.entity,
                (byte) (int) (location.getYaw() % 360.0D * 256.0D / 360.0D)));

        changeMetadata(player, metadataNPC, true);

        CompletableFuture<Void> future3 = CompletableFuture.completedFuture(null);
        future3.completeOnTimeout(null, 300, TimeUnit.MILLISECONDS);
        future3.thenRun(() -> connection.send(new ClientboundPlayerInfoRemovePacket(List.of(this.entity.getUUID()))));
    }

    public void updateName(Player player, String newname) {
        SynchedEntityData dataWatcher = this.entity.getEntityData();
        this.name = ChatColor.translateAlternateColorCodes('&', newname);

        ((CraftPlayer) player).getHandle().connection.send(new ClientboundAddEntityPacket(this.entity));
        dataWatcher.set(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT), Optional.ofNullable(CraftChatMessage.fromStringOrNull(name)));
        dataWatcher.set(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), visibleName);

        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(this.entity.getId(), dataWatcher.getNonDefaultValues());

        ((CraftPlayer) player).getHandle().connection.send(packet);
    }


    public void remove(Player player) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
    }

    public void destroy(Player player) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
        //LiteFP.list_npc.remove(id);
        //RequestNPC.removeNPC(id);
    }

}
