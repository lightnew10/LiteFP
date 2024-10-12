package fr.lightnew.npc.entities;

import fr.lightnew.npc.entities.npc.NPCCreator;
import lombok.Data;
import lombok.ToString;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class ConstructLFPLocation {

    private List<LFPLocation> locationList;
    private NPCCreator npc;

    public ConstructLFPLocation(NPCCreator npc) {
        this.locationList = new ArrayList<>();
        this.npc = npc;
    }

    public void addLocation(Location location) {
        locationList.add(new LFPLocation(location));
    }

    public void addLocation(LFPLocation location) {
        locationList.add(location);
    }
}
