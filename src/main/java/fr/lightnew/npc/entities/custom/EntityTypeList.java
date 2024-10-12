package fr.lightnew.npc.entities.custom;

import lombok.Getter;
import net.minecraft.world.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum EntityTypeList {
    Allay ("Allay", EntityType.ALLAY),
    ArmorStand ("ArmorStand", EntityType.ARMOR_STAND),
    Arrow ("Arrow", EntityType.ARROW),
    Axolotl ("Axolotl", EntityType.AXOLOTL),
    Bat ("Bat", EntityType.BAT),
    Bee ("Bee", EntityType.BEE),
    Blaze ("Blaze", EntityType.BLAZE),
    Boat ("Boat", EntityType.BOAT),
    Camel ("Camel", EntityType.CAMEL),
    Cat ("Cat", EntityType.CAT),
    CaveSpider ("CaveSpider", EntityType.CAVE_SPIDER),
    ChestBoat ("ChestBoat", EntityType.CHEST_BOAT),
    MinecartChest ("MinecartChest", EntityType.CHEST_MINECART),
    Chicken ("Chicken", EntityType.CHICKEN),
    Cod ("Cod", EntityType.COD),
    MinecartCommandBlock ("MinecartCommandBlock", EntityType.COMMAND_BLOCK_MINECART),
    Cow ("Cow", EntityType.COW),
    Creeper ("Creeper", EntityType.CREEPER),
    Dolphin ("Dolphin", EntityType.DOLPHIN),
    Donkey ("Donkey", EntityType.DONKEY),
    DragonFireball ("DragonFireball", EntityType.DRAGON_FIREBALL),
    Drowned ("Drowned", EntityType.DROWNED),
    ThrownEgg ("ThrownEgg", EntityType.EGG),
    ElderGuardian ("ElderGuardian", EntityType.ELDER_GUARDIAN),
    EndCrystal ("EndCrystal", EntityType.END_CRYSTAL),
    EnderDragon ("EnderDragon", EntityType.ENDER_DRAGON),
    ThrownEnderpearl ("ThrownEnderpearl", EntityType.ENDER_PEARL),
    EnderMan ("EnderMan", EntityType.ENDERMAN),
    Endermite ("Endermite", EntityType.ENDERMITE),
    Evoker ("Evoker", EntityType.EVOKER),
    EvokerFangs ("EvokerFangs", EntityType.EVOKER_FANGS),
    ThrownExperienceBottle ("ThrownExperienceBottle", EntityType.EXPERIENCE_BOTTLE),
    ExperienceOrb ("ExperienceOrb", EntityType.EXPERIENCE_ORB),
    EyeOfEnder ("EyeOfEnder", EntityType.EYE_OF_ENDER),
    FallingBlockEntity ("FallingBlockEntity", EntityType.FALLING_BLOCK),
    FireworkRocketEntity ("FireworkRocketEntity", EntityType.FIREWORK_ROCKET),
    Fox ("Fox", EntityType.FOX),
    Frog ("Frog", EntityType.FROG),
    MinecartFurnace ("MinecartFurnace", EntityType.FURNACE_MINECART),
    Ghast ("Ghast", EntityType.GHAST),
    Giant ("Giant", EntityType.GIANT),
    GlowItemFrame ("GlowItemFrame", EntityType.GLOW_ITEM_FRAME),
    GlowSquid ("GlowSquid", EntityType.GLOW_SQUID),
    Goat ("Goat", EntityType.GOAT),
    Guardian ("Guardian", EntityType.GUARDIAN),
    Hoglin ("Hoglin", EntityType.HOGLIN),
    MinecartHopper ("MinecartHopper", EntityType.HOPPER_MINECART),
    Horse ("Horse", EntityType.HORSE),
    Husk ("Husk", EntityType.HUSK),
    Illusioner ("Illusioner", EntityType.ILLUSIONER),
    IronGolem ("IronGolem", EntityType.IRON_GOLEM),
    ItemFrame ("ItemFrame", EntityType.ITEM),
    LargeFireball ("LargeFireball", EntityType.FIREBALL),
    Llama ("Llama", EntityType.LLAMA),
    LlamaSpit ("LlamaSpit", EntityType.LLAMA_SPIT),
    MagmaCube ("MagmaCube", EntityType.MAGMA_CUBE),
    Marker ("Marker", EntityType.MARKER),
    Minecart ("Minecart", EntityType.MINECART),
    MushroomCow ("MushroomCow", EntityType.MOOSHROOM),
    Mule ("Mule", EntityType.MULE),
    Ocelot ("Ocelot", EntityType.OCELOT),
    Painting ("Painting", EntityType.PAINTING),
    Panda ("Panda", EntityType.PANDA),
    Parrot ("Parrot", EntityType.PARROT),
    Phantom ("Phantom", EntityType.PHANTOM),
    Pig ("Pig", EntityType.PIG),
    Piglin ("Piglin", EntityType.PIGLIN),
    PiglinBrute ("PiglinBrute", EntityType.PIGLIN_BRUTE),
    Pillager ("Pillager", EntityType.PILLAGER),
    PolarBear ("PolarBear", EntityType.POLAR_BEAR),
    ThrownPotion ("ThrownPotion", EntityType.POTION),
    Pufferfish ("Pufferfish", EntityType.PUFFERFISH),
    Rabbit ("Rabbit", EntityType.RABBIT),
    Ravager ("Ravager", EntityType.RAVAGER),
    Salmon ("Salmon", EntityType.SALMON),
    Sheep ("Sheep", EntityType.SHEEP),
    Shulker ("Shulker", EntityType.SHULKER),
    ShulkerBullet ("ShulkerBullet", EntityType.SHULKER_BULLET),
    Silverfish ("Silverfish", EntityType.SILVERFISH),
    Skeleton ("Skeleton", EntityType.SKELETON),
    SkeletonHorse ("SkeletonHorse", EntityType.SKELETON_HORSE),
    Slime ("Slime", EntityType.SLIME),
    SmallFireball ("SmallFireball", EntityType.SMALL_FIREBALL),
    Sniffer ("Sniffer", EntityType.SNIFFER),
    SnowGolem ("SnowGolem", EntityType.SNOW_GOLEM),
    Snowball ("Snowball", EntityType.SNOWBALL),
    MinecartSpawner ("MinecartSpawner", EntityType.SPAWNER_MINECART),
    SpectralArrow ("SpectralArrow", EntityType.SPECTRAL_ARROW),
    Spider ("Spider", EntityType.SPIDER),
    Squid ("Squid", EntityType.SQUID),
    Stray ("Stray", EntityType.STRAY),
    Strider ("Strider", EntityType.STRAY),
    Tadpole ("Tadpole", EntityType.TADPOLE),
    PrimedTnt ("PrimedTnt", EntityType.TNT),
    MinecartTNT ("MinecartTNT", EntityType.TNT_MINECART),
    TraderLlama ("TraderLlama", EntityType.TRADER_LLAMA),
    ThrownTrident ("ThrownTrident", EntityType.TRIDENT),
    TropicalFish ("TropicalFish", EntityType.TROPICAL_FISH),
    Turtle ("Turtle", EntityType.TURTLE),
    Vex ("Vex", EntityType.VEX),
    Villager ("Villager", EntityType.VILLAGER),
    Vindicator ("Vindicator", EntityType.VINDICATOR),
    WanderingTrader ("WanderingTrader", EntityType.WANDERING_TRADER),
    Warden ("Warden", EntityType.WARDEN),
    Witch ("Witch", EntityType.WITCH),
    WitherBoss ("WitherBoss", EntityType.WITHER),
    WitherSkeleton ("WitherSkeleton", EntityType.WITHER_SKELETON),
    WitherSkull ("WitherSkull", EntityType.WITHER_SKULL),
    Wolf ("Wolf", EntityType.WOLF),
    Zoglin ("Zoglin", EntityType.ZOGLIN),
    Zombie ("Zombie", EntityType.ZOMBIE),
    ZombieHorse ("ZombieHorse", EntityType.ZOMBIE_HORSE),
    ZombieVillager ("ZombieVillager", EntityType.ZOMBIE_VILLAGER),
    ZombifiedPiglin ("ZombifiedPiglin", EntityType.ZOMBIFIED_PIGLIN),
    FishingHook ("FishingHook", EntityType.FISHING_BOBBER);

    private String name;
    @Getter
    private EntityType type;
    EntityTypeList(String name, EntityType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name.toUpperCase();
    }

    public static List<String> getNames() {
        return Arrays.stream(EntityTypeList.values())
                .map(EntityTypeList::getName)
                .collect(Collectors.toList());
    }

    public static Map<String, EntityType> getData() {
        return Arrays.stream(EntityTypeList.values())
                .collect(Collectors.toMap(EntityTypeList::getName, EntityTypeList::getType));
    }
}
