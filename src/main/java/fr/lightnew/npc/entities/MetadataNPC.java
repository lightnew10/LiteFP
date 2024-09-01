package fr.lightnew.npc.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class MetadataNPC {

    private boolean isFire;
    private boolean isCrouching;
    private boolean isUnused; //previously riding
    private boolean isSprinting;
    private boolean isSwimming;
    private boolean isInvisible;
    private boolean hasGlowingEffect;
    private boolean isFlying; //with an elytra

    private String customName;
    private boolean isCustomNameVisible;
    private boolean hasNoGravity;
    private PoseNPC pose;

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
        bytes |= 0b01000000;
        return bytes;
    }
}
