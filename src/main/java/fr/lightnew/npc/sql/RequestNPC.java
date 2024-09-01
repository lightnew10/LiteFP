package fr.lightnew.npc.sql;

import com.mojang.datafixers.util.Pair;
import fr.lightnew.npc.LiteFP;
import fr.lightnew.npc.entities.EffectNPC;
import fr.lightnew.npc.entities.NPCCreator;
import fr.lightnew.npc.tools.ConsoleLog;
import fr.lightnew.npc.tools.ItemBuilder;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RequestNPC {

    private static String tableDB = "npc_storage";

    public static void createTable() {
        String table = "CREATE TABLE IF NOT EXISTS " + tableDB + "(id int NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "npc_name TEXT," +
                "npc_skin_name TEXT," +
                "npc_skin_texture TEXT," +
                "npc_skin_signature TEXT," +
                "location TEXT," +
                "stuff TEXT," +
                "effects TEXT," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP);";
        try {
            PreparedStatement statement = LiteFP.getConnection().prepareStatement(table);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static NPCCreator getNPC(String id) {
        NPCCreator npcCreator = null;
        try {
            PreparedStatement statement = LiteFP.getConnection().prepareStatement("SELECT * FROM " + tableDB + " WHERE npc_id=?");
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if (jsonToLocation(new JSONObject(resultSet.getString("location"))) != null) {
                    npcCreator = new NPCCreator(
                            resultSet.getString("npc_name"),
                            jsonToLocation(new JSONObject(resultSet.getString("location"))),
                            resultSet.getString("npc_skin_name"),
                            resultSet.getString("npc_skin_texture"),
                            resultSet.getString("npc_skin_signature"));
                    npcCreator.setId(resultSet.getInt("id"));
                    npcCreator.setEquipment(jsonToPairs(new JSONObject(resultSet.getString("stuff"))));
                    EffectNPC effectNPC = jsonToEffectNPC(new JSONObject(resultSet.getString("effects")));
                    npcCreator.getEffects().setLookLock(effectNPC.getLookLock());
                    npcCreator.getEffects().setCollides(effectNPC.getCollides());
                } else
                    ConsoleLog.error("NPC (" + resultSet.getInt("id") + ") cannot spawn, the location is null or world is null");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return npcCreator;
    }

    public static Map<Integer, NPCCreator> getAllNPC() {
        Map<Integer, NPCCreator> NPCsCreator = new HashMap<>();
        try {
            PreparedStatement statement = LiteFP.getConnection().prepareStatement("SELECT * FROM " + tableDB);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (jsonToLocation(new JSONObject(resultSet.getString("location"))) != null) {
                    NPCCreator npcCreator = new NPCCreator(
                            resultSet.getString("npc_name"),
                            jsonToLocation(new JSONObject(resultSet.getString("location"))),
                            resultSet.getString("npc_skin_name"),
                            resultSet.getString("npc_skin_texture"),
                            resultSet.getString("npc_skin_signature"));

                    npcCreator.setId(resultSet.getInt("id"));
                    npcCreator.setEquipment(jsonToPairs(new JSONObject(resultSet.getString("stuff"))));
                    EffectNPC effectNPC = jsonToEffectNPC(new JSONObject(resultSet.getString("effects")));
                    npcCreator.getEffects().setLookLock(effectNPC.getLookLock());
                    npcCreator.getEffects().setCollides(effectNPC.getCollides());
                    NPCsCreator.put(Integer.parseInt(resultSet.getString("id")), npcCreator);
                } else
                    ConsoleLog.error("NPC (" + resultSet.getInt("id") + ") cannot spawn, the location is null or world is null");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return NPCsCreator;
    }

    public static void createNewNPC(NPCCreator npcCreator, Consumer<? super Integer> success) {
        try {
            PreparedStatement insert = LiteFP.getConnection().prepareStatement("INSERT INTO " + tableDB + " (" +
                    "npc_name," +
                    "npc_skin_name," +
                    "npc_skin_texture," +
                    "npc_skin_signature," +
                    "location," +
                    "stuff," +
                    "effects)" +
                    "values (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

            insert.setString(1, npcCreator.getName());
            insert.setString(2, npcCreator.getSkinName());
            insert.setString(3, npcCreator.getTexture());
            insert.setString(4, npcCreator.getSignature());
            insert.setString(5, locationToJson(npcCreator.getLocation()).toString());
            insert.setString(6, pairsToJson(npcCreator.getEquipment()).toString());
            insert.setString(7, effectNPCToJson(npcCreator.getEffects()).toString());

            int i = insert.executeUpdate();
            if (i == 1) {
                ResultSet key = insert.getGeneratedKeys();
                if (key.next()) {
                    int id = key.getInt(1);
                    success.accept(id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeNPC(int id) {
        try {
            PreparedStatement remove = LiteFP.getConnection().prepareStatement("DELETE FROM " + tableDB + " WHERE id=?;");
            remove.setString(1, String.valueOf(id));
            remove.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateNPC(NPCCreator npcCreator) {
        try (PreparedStatement update = LiteFP.getConnection().prepareStatement("UPDATE " + tableDB + " SET " +
                    "npc_name=?," +
                    "npc_skin_name=?," +
                    "npc_skin_texture=?," +
                    "npc_skin_signature=?," +
                    "location=?," +
                    "stuff=?," +
                    "effects=?" +
                    " WHERE id=?")) {
            update.setString(1, npcCreator.getName());
            update.setString(2, npcCreator.getSkinName());
            update.setString(3, npcCreator.getTexture());
            update.setString(4, npcCreator.getSignature());
            update.setString(5, locationToJson(npcCreator.getLocation()).toString());
            update.setString(6, pairsToJson(npcCreator.getEquipment()).toString());
            update.setString(7, effectNPCToJson(npcCreator.getEffects()).toString());
            update.setString(8, String.valueOf(npcCreator.getId()));

            update.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean existNPC(int id) {
        try {
            PreparedStatement statement = LiteFP.getConnection().prepareStatement("SELECT id FROM " + tableDB + " WHERE id=?");
            statement.setString(1, String.valueOf(id));
            return statement.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static EffectNPC jsonToEffectNPC(JSONObject jsonObject) {
        EffectNPC effectNPC = new EffectNPC(
                jsonObject.getBoolean("lookLock"),
                jsonObject.getBoolean("collides"));
        return effectNPC;
    }

    private static JSONObject effectNPCToJson(EffectNPC effectNPC) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lookLock", effectNPC.getLookLock());
        jsonObject.put("collides", effectNPC.getCollides());
        return jsonObject;
    }

    private static JSONObject pairsToJson(List<List<Pair<EnumItemSlot, ItemStack>>> stuff) {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < stuff.size(); i++) {
            JSONObject json = new JSONObject();
            for (int i1 = 0; i1 < stuff.get(i).size(); i1++) {
                json.put("first", stuff.get(i).get(i1).getFirst().name());
                JSONObject jsonItem = new JSONObject();
                JSONObject jsonEnchant = new JSONObject();
                org.bukkit.inventory.ItemStack itemStack = CraftItemStack.asBukkitCopy(stuff.get(i).get(i1).getSecond());
                jsonItem.put("type", itemStack.getType().name());
                jsonItem.put("amount", itemStack.getAmount());
                if (!itemStack.getEnchantments().isEmpty())
                    for (Enchantment enchantment : itemStack.getEnchantments().keySet())
                        jsonEnchant.put(enchantment.getName(), itemStack.getEnchantments().get(enchantment));

                jsonItem.put("enchantment", jsonEnchant);
                json.put("second", jsonItem);
            }
            jsonObject.put(String.valueOf(i), json);
        }
        return jsonObject;
    }

    private static List<List<Pair<EnumItemSlot, ItemStack>>> jsonToPairs(JSONObject jsonObject) {
        List<List<Pair<EnumItemSlot, ItemStack>>> result = new ArrayList<>();
        for (int i = 0; i < jsonObject.length(); i++) {
            JSONObject json = jsonObject.getJSONObject(String.valueOf(i));
            EnumItemSlot slot = EnumItemSlot.valueOf(json.getString("first"));
            JSONObject itemJson = json.getJSONObject("second");
            org.bukkit.inventory.ItemStack itemStack = ItemBuilder.create(Material.valueOf(itemJson.getString("type")), itemJson.getInt("amount"), null);
            JSONObject enchantmentsJson = itemJson.optJSONObject("enchantment");
            if (enchantmentsJson != null && !enchantmentsJson.isEmpty()) {
                for (String enchantmentName : enchantmentsJson.keySet()) {
                    int level = enchantmentsJson.getInt(enchantmentName);
                    itemStack.addUnsafeEnchantment(Enchantment.getByName(enchantmentName), level);
                }
            }
            result.add(List.of(Pair.of(slot, CraftItemStack.asNMSCopy(itemStack))));
        }
        return result;
    }

    private static JSONObject locationToJson(Location location) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("world", location.getWorld().getName());
        jsonObject.put("x", location.getX());
        jsonObject.put("y", location.getY());
        jsonObject.put("z", location.getZ());
        jsonObject.put("yaw", location.getYaw());
        jsonObject.put("pitch", location.getPitch());
        return jsonObject;
    }

    private static Location jsonToLocation(JSONObject jsonObject) {
        if (Bukkit.getWorld(jsonObject.getString("world")) == null)
            return null;
        return new Location(Bukkit.getWorld(jsonObject.getString("world")), jsonObject.getDouble("x"), jsonObject.getDouble("y"), jsonObject.getDouble("z"), jsonObject.getFloat("yaw"), jsonObject.getFloat("pitch"));
    }

}
