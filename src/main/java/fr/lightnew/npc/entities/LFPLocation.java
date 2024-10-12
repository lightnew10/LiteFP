package fr.lightnew.npc.entities;

import fr.lightnew.npc.entities.metas.AnimationNPC;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.world.entity.Pose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.JSONObject;

@Getter
@Setter
@ToString
public class LFPLocation {

    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private AnimationNPC animationNPC;
    private Pose poseNPC;

    public LFPLocation(String world, double x, double y, double z, float pitch, float yaw, AnimationNPC animationNPC) {
        setDefaultValue(world, x, y, z, yaw, pitch);
        this.animationNPC = animationNPC;
    }

    public LFPLocation(Location location) {
        setDefaultValue(location);
    }

    public LFPLocation(Location location, AnimationNPC animationNPC) {
        setDefaultValue(location);
        this.animationNPC = animationNPC;
    }

    public LFPLocation(Location location, Pose pose) {
        setDefaultValue(location);
        this.poseNPC = pose;
    }

    private void setDefaultValue(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.poseNPC = Pose.SITTING;
        this.animationNPC = null;
    }

    private void setDefaultValue(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.poseNPC = Pose.SITTING;
        this.animationNPC = null;
    }

    public Location getLocation() {
        if (Bukkit.getWorld(world) != null)
            return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
        else
            return null;
    }

    public JSONObject serialize() {
        JSONObject json = new JSONObject();
        if (world != null)
            json.put("world", world);
        json.put("x", x);
        json.put("y", y);
        json.put("z", z);
        json.put("yaw", yaw);
        json.put("pitch", pitch);
        if (animationNPC != null)
            json.put("animation", animationNPC.name());
        if (poseNPC != null)
            json.put("pose", poseNPC.name());
        return json;
    }

    public LFPLocation deserialize(JSONObject json) {
        if (json.has("world"))
            this.world = json.getString("world");
        if (json.has("x"))
            this.x = json.getDouble("x");
        if (json.has("y"))
            this.y = json.getDouble("y");
        if (json.has("z"))
            this.z = json.getDouble("z");
        if (json.has("yaw"))
            this.yaw = json.getFloat("yaw");
        if (json.has("pitch"))
            this.pitch = json.getFloat("pitch");
        if (json.has("animation"))
            this.animationNPC = AnimationNPC.valueOf(json.getString("animation"));
        if (json.has("pose"))
            this.poseNPC = Pose.valueOf(json.getString("pose"));
        return this;
    }
}
