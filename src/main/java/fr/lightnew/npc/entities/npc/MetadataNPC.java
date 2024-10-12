package fr.lightnew.npc.entities.npc;

import fr.lightnew.npc.entities.metas.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Rotations;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.DyeColor;
import org.bukkit.Location;

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

    private ChatFormatting colorGlow = ChatFormatting.WHITE;
    private boolean collide = true;

    //Villager
    private VillagerProfession profession = VillagerProfession.NONE;
    private VillagerType villagerType = VillagerType.PLAINS;
    //Parrot
    private ParrotColor parrotColor = ParrotColor.YELLOW_BLUE;
    //Wolf
    private DyeColor wolfColorCollar = DyeColor.RED;
    private boolean wolfSitting = false;
    private boolean wolfTamed = false;
    //Cat
    private ResourceKey<CatVariant> catVariant = CatVariant.CALICO;
    private DyeColor catColorCollar = DyeColor.RED;
    private boolean catRelaxed = false;
    private boolean catSitting = false;
    private boolean catTamed = false;
    //Goat
    private boolean goatScreaming = false;
    private boolean goatLeftHorn = false;
    private boolean goatRightHorn = false;
    //Strider
    private boolean striderShaking = false;
    private boolean striderSaddle = false;
    //Sheep
    private SheepColor sheepColor = SheepColor.WHITE;
    private boolean sheepSheared = false;
    //Mooshroom
    private MooshroomVariant mooshroomColor = MooshroomVariant.RED;
    //Polar bear
    private boolean polarBearStandingUP = false;
    //Rabbit
    private RabbitVariant rabbitColor = RabbitVariant.BROWN;
    //Pig
    private boolean pigSaddle = false;
    //Panda
    private boolean pandaSneezing = false;
    private boolean pandaRolling = false;
    private boolean pandaSitting = false;
    //Frog variant
    private FrogVariant frogVariant = FrogVariant.TEMPERATE;
    //Fox
    private FoxVariant foxVariant = FoxVariant.RED;
    private boolean foxSitting = false;
    private boolean foxCrouching = false;
    private boolean foxInterested = false;
    private boolean foxPouncing = false;
    private boolean foxSleeping = false;
    private boolean foxFacePlanted = false;
    private boolean foxDefending = false;
    //Bee
    private boolean beeAngry = false;
    private boolean beeStung = false;
    private boolean beeNectar = false;
    //Axolotl
    private AxolotlVariant axolotlVariant = AxolotlVariant.LUCY;
    //Llama
    private LlamaVariant llamaVariant = LlamaVariant.CREAMY;
    private LlamaCarpetColor llamaCarpetColor = LlamaCarpetColor.NO_CARPET;
    //Camel
    private boolean camelDashing = false;
    //Horse
    private HorseVariant horseVariant = HorseVariant.WHITE;
    private HorseStyle horseStyle = HorseStyle.NONE;
    private boolean horseTame = false;
    private boolean horseSaddle = false;
    private boolean horseBred = false;
    private boolean horseEating = false;
    private boolean horseRearing = false;
    private boolean horseMouthOpen = false;
    //Sniffer
    private SnifferVariant snifferVariant = SnifferVariant.IDLING;
    //Bat
    private boolean batHanging = false;
    //Armor Stand
    private boolean armorStandSmall = false;
    private boolean armorStandArms = false;
    private boolean armorStandBasePlate = false;
    private boolean armorStandMarker = false;
    private Rotations armorStandHead = new Rotations(0, 0, 0);
    private Rotations armorStandBody = new Rotations(0, 0, 0);
    private Rotations armorStandLeftArm = new Rotations(0, 0, 0);
    private Rotations armorStandRightArm = new Rotations(0, 0, 0);
    private Rotations armorStandRightLeg = new Rotations(0, 0, 0);
    private Rotations armorStandLeftLeg = new Rotations(0, 0, 0);
    //Slime
    private SlimeVariant slimeVariant = SlimeVariant.MEDIUM;
    //Enderman
    private boolean endermanScreaming = false;
    private boolean endermanStaring = false;
    //Zombie
    private boolean zombieBaby = false;
    private boolean zombieBecomingDrowned = false;
    //Zoglin
    private boolean zoglinBaby = false;
    //Wither
    private int witherTargetEntityCenterHead = 0;
    private int witherTargetEntityLeftHead = 0;
    private int witherTargetEntityRightHead = 0;
    //Spider
    private boolean spiderClimbing = false;
    //Vex
    private boolean vexAttacking = false;
    //Witch
    private boolean witchDrinkingPotion = false;
    //Pillager
    private boolean pillagerCharging = false;
    //Raider
    private boolean raiderCelebrating = false;
    //Creeper
    private CreeperVariant creeperVariant = CreeperVariant.IDLE;
    private boolean creeperCharged = false;
    private boolean creeperIgnited = false;
    //Blaze
    private boolean blazeOnFire = false;
    //Piglin
    private boolean piglinBaby = false;
    private boolean piglinChargingBow = false;
    private boolean piglinDancing = false;
    //Snow golem
    private boolean snowgolemPumpkinHat = true;
    //Turtle
    private boolean turtleEgg;
    private boolean turtleLayingEgg;

    public byte getBytesDataLivingNPC() {
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

    public byte getBytesTameableWolf() {
        byte bytes = 0;
        if (wolfSitting)
            bytes |= (byte) 0x01;
        if (wolfTamed)
            bytes |= (byte) 0x04;
        return bytes;
    }

    public byte getBytesTameableCat() {
        byte bytes = 0;
        if (catSitting)
            bytes |= (byte) 0x01;
        if (catTamed)
            bytes |= (byte) 0x04;
        return bytes;
    }

    public byte getBytesPanda() {
        byte bytes = 0;
        if (pandaSneezing)
            bytes |= (byte) 0x02;
        if (pandaRolling)
            bytes |= (byte) 0x04;
        if (pandaSitting)
            bytes |= (byte) 0x08;
        return bytes;
    }

    public byte getBytesFox() {
        byte bytes = 0;
        if (foxSitting)
            bytes |= (byte) 0x01;
        if (foxCrouching)
            bytes |= (byte) 0x04;
        if (foxInterested)
            bytes |= (byte) 0x08;
        if (foxPouncing)
            bytes |= (byte) 0x10;
        if (foxSleeping)
            bytes |= (byte) 0x20;
        if (foxFacePlanted)
            bytes |= (byte) 0x40;
        if (foxDefending)
            bytes |= (byte) 0x80;
        return bytes;
    }

    public byte getBytesBee() {
        byte bytes = 0;
        if (beeAngry)
            bytes |= (byte) 0x02;
        if (beeStung)
            bytes |= (byte) 0x04;
        if (beeNectar)
            bytes |= (byte) 0x08;
        return bytes;
    }

    public byte getBytesHorse() {
        byte bytes = 0;
        if (horseTame)
            bytes |= (byte) 0x02;
        if (horseSaddle)
            bytes |= (byte) 0x04;
        if (horseBred)
            bytes |= (byte) 0x08;
        if (horseEating)
            bytes |= (byte) 0x10;
        if (horseRearing)
            bytes |= (byte) 0x20;
        if (horseMouthOpen)
            bytes |= (byte) 0x40;
        return bytes;
    }

    public byte getByteBat() {
        return batHanging ? (byte) 0x01 : 0;
    }

    public byte getByteSpider() {
        return spiderClimbing ? (byte) 0x01 : 0;
    }

    public byte getByteVex() {
        return vexAttacking ? (byte) 0x01 : 0;
    }

    public byte getByteBlaze() {
        return blazeOnFire ? (byte) 0x01 : 0;
    }

    public byte getByteSnowGolem() {
        return snowgolemPumpkinHat ? (byte) 0x10 : 0x00;
    }

    public int getIntegerHorse() {
        return (horseVariant.getId() & 0x7F) | ((horseStyle.getId() & 0x1F) << 7);
    }

    public byte getBytesArmorStand() {
        byte bytes = 0;
        if (armorStandSmall)
            bytes |= (byte) 0x01;
        if (armorStandArms)
            bytes |= (byte) 0x04;
        if (armorStandBasePlate)
            bytes |= (byte) 0x08;
        if (armorStandMarker)
            bytes |= (byte) 0x10;
        return bytes;
    }
}
