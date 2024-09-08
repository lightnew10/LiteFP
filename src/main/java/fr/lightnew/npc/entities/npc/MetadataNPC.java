package fr.lightnew.npc.entities.npc;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Pose;

@Data
@NoArgsConstructor
@ToString
public class MetadataNPC {

    private boolean isFire = false;
    private boolean isCrouching = false;
    private boolean isUnused = false; //previously riding
    private boolean isSprinting = false;
    private boolean isSwimming = false;
    private boolean isInvisible = false;
    private boolean hasGlowingEffect = false;
    private boolean isFlying = false; //with an elytra

    private boolean hasNoGravity = false;
    private Pose pose = Pose.STANDING;

    private ChatFormatting colorGlow;
    private boolean collide = true;

    public byte getBytesDefaultData() {
        byte bytes = 0;
        if (isFire)
            bytes |= (byte) 0x01;
        if (isCrouching)
            bytes |= (byte) 0x02;
        if (isUnused)
            bytes |= (byte) 0x04;
        if (isSprinting)
            bytes |= (byte) 0x08;
        if (isSwimming)
            bytes |= (byte) 0x10;
        if (isInvisible)
            bytes |= (byte) 0x20;
        if (hasGlowingEffect)
            bytes |= (byte) 0x40;
        if (isFlying)
            bytes |= (byte) 0x80;
        return bytes;
    }
}
