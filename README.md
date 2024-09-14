# LiteFP

Minecraft version : `1.20.1`

By `[Lightnew]`

## How to Interact with NPC ?

```java
@EventHandler
public void onInteractNPC(InteractNPCEvent event) {
    // Get player do action
    event.getPlayer();
    // Type of click (Shift click, click)
    event.getClickType();
    //With this you have All NPC (id, name, skinName...)
    event.getNpcManager().getNPC();
}
```

## How to create new NPC ?

```java
public void createMyNPC() {
    NPCCreator npc = new NPCCreator("name npc", location, "name skin");
    //Send NPC for one player
    npc.createNPC(player);
    //send NPC for all player
    Bukkit.getOnlinePlayers().forEach(npc::createNPC);
    //remove visual NPC
    npc.remove(player);
    //remove forever NPC
    npc.destroy(player);
}

public void createMyNPCCustomTexture(String texture, String signature) {
    //if you want custom texture you have 2 choices :
    //1
    NPCCreator npc = new NPCCreator("name npc", location, "name skin");
    npc.setTexture(texture);
    npc.setSignature(signature);
    //2
    NPCCreator npc = new NPCCreator("name npc", location, "name skin", texture, signature);
    //And same logic for spawn npc to players
}
```

## How to create custom entity NPC

```java
public void createMyEntityCustom(String name, Location location) {
    //Create simple armor stand with a name
    //Server level you can get with 
    EntityCreator entityCreator = new EntityCreator(EntityType.ARMOR_STAND, player.getLocation(), ServerUtils.getServerLevel(), "test");
    //if you want remove the name
    entityCreator.setVisibleName(false);

    //Metadata !
    //Metadata is all data of entity if you want set baby, small, on fire...
    //Example with ArmorStand
    MetadataNPC metadataNPC = new MetadataNPC();
    metadataNPC.setArmorStandSmall(true);
    metadataNPC.setInvisible(true);
    //Example with parrot
    metadataNPC.setParrotColor(ParrotColor.RED_BLUE);

    //With your Metadata
    entityCreator.create(player, metadataNPC);
    //With nothing
    entityCreator.create(player);
}
```

## New event PlayerSpawnInServerEvent
PlayerSpawnInServerEvent is not the same as PlayerJoinEvent! 

- PlayerJoinEvent corresponds to the arrival of a player on the server 

- PlayerSpawnInServerEvent corresponds to the arrival of a player on the server physically when his corpse spawns on the server.
```java
@EventHandler
public void onSpawn(PlayerSpawnInServerEvent event) {
    //You have only event.getPlayer();
    event.getPlayer();
}
```
