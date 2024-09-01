package fr.lightnew.npc.entities;

import lombok.Getter;
import net.minecraft.world.entity.EntityPose;

@Getter
public enum PoseNPC {
    STANDING(0, EntityPose.a),
    FALL_FLYING(1, EntityPose.c),
    SWIMMING(2, EntityPose.d),
    SPIN_ATTACK(3, EntityPose.e),
    CROUCHING(4, EntityPose.f),
    LONG_JUMPING(5, EntityPose.g),
    DYING(6, EntityPose.h),
    CROAKING(7, EntityPose.i),
    USING_TONGUE(8, EntityPose.j),
    SITTING(9, EntityPose.k),
    ROARING(10, EntityPose.l),
    SNIFFING(11, EntityPose.m),
    EMERGING(12, EntityPose.n),
    DIGGING(13, EntityPose.o);

    private int id;
    private EntityPose pose;

    PoseNPC(int id, EntityPose pose) {
        this.id = id;
        this.pose = pose;
    }
}
