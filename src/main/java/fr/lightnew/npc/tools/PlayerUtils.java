package fr.lightnew.npc.tools;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayerUtils {

    public static void changeCameraPlayer(Player player, Location target, int timeLeft, TimeUnit timeLeftUnit) {
        //TODO create a new entity invisible and teleport to
        /*new CompletableFuture<>()
                .completeOnTimeout(null, timeLeft, timeLeftUnit)
                .thenRun(() -> ((CraftPlayer) player).getHandle().connection
                        .send(
                                new ClientboundSetCameraPacket(((CraftPlayer) event.getPlayer()).getHandle().getCamera())
                        )
                );*/
        //sendPacket(player, new ClientboundGameProfilePacket(new GameProfile(player.getUniqueId(), "test")));
        sendPacket(player, new ClientboundOpenSignEditorPacket(BlockPos.ZERO, true));
        //sendPacket(player, new ClientboundAddEntityPacket());
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer)player).getHandle().connection.send(packet);
    }
}
