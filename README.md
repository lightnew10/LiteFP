# LiteFP

## Information's

![Static Badge](https://img.shields.io/badge/By-lightnew-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen)
![Storage](https://img.shields.io/badge/Storage-MySQL-purple)


**Dependencies** :
 - [HolographicDisplays](https://ci.codemc.io/job/filoghost/job/HolographicDisplays/)

**Mysql is the only storage system for this plugin**
<br>
**In the future, adding new storage systems**

## Maven
https://github.com/lightnew10/LiteFP/packages/2277942
<br>
or
```xml
<dependency>
    <groupId>fr.lightnew.npc</groupId>
    <artifactId>litefp</artifactId>
    <version>0.3.1</version>
</dependency>
```

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
    //if you want to remove the name
    entityCreator.setVisibleName(false);

    //Metadata !
    //Metadata is all data of entity if you want set baby, small, on fire...
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

## New features coming soon
- Modify type of npc (Zombie, Villager, Allay...)
- Add action (execute command...)
- recording system (to move the NPC infinitely to a specific location)
- sort in gui all npc (sort by world, id, nearby...)
- System lang custom (multi languages)
- Design information and config
- Add action (execute command...)
