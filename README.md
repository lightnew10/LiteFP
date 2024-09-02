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

## Features Soon (walk npc, more text above npc)
