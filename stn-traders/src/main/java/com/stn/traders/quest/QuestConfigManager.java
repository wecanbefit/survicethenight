package com.stn.traders.quest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stn.traders.STNTraders;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages loading and saving quest configuration from JSON files.
 */
public class QuestConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "stn-traders/quests.json";

    private static List<Quest> loadedQuests = new ArrayList<>();

    /**
     * Load quests from config file, creating defaults if needed.
     */
    public static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);

        // Create default config if it doesn't exist
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath.getParent());
                createDefaultConfig(configPath);
                STNTraders.LOGGER.info("Created default quest config at {}", configPath);
            } catch (IOException e) {
                STNTraders.LOGGER.error("Failed to create default quest config", e);
                loadedQuests = getDefaultQuests();
                return;
            }
        }

        // Load from file
        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray questsArray = root.getAsJsonArray("quests");

            loadedQuests = new ArrayList<>();
            for (JsonElement element : questsArray) {
                try {
                    Quest quest = parseQuest(element.getAsJsonObject());
                    loadedQuests.add(quest);
                } catch (Exception e) {
                    STNTraders.LOGGER.warn("Failed to parse quest: {}", e.getMessage());
                }
            }

            STNTraders.LOGGER.info("Loaded {} quests from config", loadedQuests.size());
        } catch (IOException e) {
            STNTraders.LOGGER.error("Failed to load quest config", e);
            loadedQuests = getDefaultQuests();
        }
    }

    /**
     * Get all loaded quests.
     */
    public static List<Quest> getQuests() {
        return loadedQuests;
    }

    /**
     * Get a quest by ID.
     */
    public static Quest getQuest(String id) {
        return loadedQuests.stream()
            .filter(q -> q.id().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Parse a single quest from JSON.
     */
    private static Quest parseQuest(JsonObject json) {
        String id = json.get("id").getAsString();
        QuestType type = QuestType.valueOf(json.get("type").getAsString().toUpperCase());
        Identifier target = Identifier.of(json.get("target").getAsString());
        int baseCount = json.get("baseCount").getAsInt();
        double weight = json.get("weight").getAsDouble();
        int minGamestage = json.has("minGamestage") ? json.get("minGamestage").getAsInt() : 0;
        int maxGamestage = json.has("maxGamestage") ? json.get("maxGamestage").getAsInt() : -1;
        String description = json.get("description").getAsString();

        // Parse rewards
        JsonObject rewardJson = json.getAsJsonObject("rewards");
        int emeralds = rewardJson.has("emeralds") ? rewardJson.get("emeralds").getAsInt() : 0;
        int experience = rewardJson.has("experience") ? rewardJson.get("experience").getAsInt() : 0;
        Identifier item = rewardJson.has("item") ? Identifier.of(rewardJson.get("item").getAsString()) : null;
        int itemCount = rewardJson.has("itemCount") ? rewardJson.get("itemCount").getAsInt() : 1;
        boolean itemEnchanted = rewardJson.has("itemEnchanted") && rewardJson.get("itemEnchanted").getAsBoolean();

        QuestReward reward = new QuestReward(emeralds, experience, item, itemCount, itemEnchanted);

        return new Quest(id, type, target, baseCount, weight, minGamestage, maxGamestage, description, reward);
    }

    /**
     * Create default config file.
     */
    private static void createDefaultConfig(Path path) throws IOException {
        JsonObject root = new JsonObject();
        JsonArray quests = new JsonArray();

        for (Quest quest : getDefaultQuests()) {
            quests.add(questToJson(quest));
        }

        root.add("quests", quests);

        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(root, writer);
        }
    }

    /**
     * Convert a quest to JSON.
     */
    private static JsonObject questToJson(Quest quest) {
        JsonObject json = new JsonObject();
        json.addProperty("id", quest.id());
        json.addProperty("type", quest.type().name());
        json.addProperty("target", quest.target().toString());
        json.addProperty("baseCount", quest.baseCount());
        json.addProperty("weight", quest.weight());
        json.addProperty("minGamestage", quest.minGamestage());
        json.addProperty("maxGamestage", quest.maxGamestage());
        json.addProperty("description", quest.description());

        JsonObject rewards = new JsonObject();
        rewards.addProperty("emeralds", quest.reward().emeralds());
        rewards.addProperty("experience", quest.reward().experience());
        if (quest.reward().item() != null) {
            rewards.addProperty("item", quest.reward().item().toString());
            rewards.addProperty("itemCount", quest.reward().itemCount());
            rewards.addProperty("itemEnchanted", quest.reward().itemEnchanted());
        }
        json.add("rewards", rewards);

        return json;
    }

    /**
     * Get default quests.
     */
    private static List<Quest> getDefaultQuests() {
        List<Quest> quests = new ArrayList<>();

        // Basic kill quests
        quests.add(new Quest("kill_zombies_basic", QuestType.KILL,
            Identifier.of("minecraft", "zombie"), 10, 5.0, 0, -1,
            "Kill %count% zombies",
            new QuestReward(5, 100, Identifier.of("minecraft", "iron_ingot"), 3, false)));

        quests.add(new Quest("kill_skeletons_basic", QuestType.KILL,
            Identifier.of("minecraft", "skeleton"), 10, 5.0, 0, -1,
            "Kill %count% skeletons",
            new QuestReward(5, 100, Identifier.of("minecraft", "arrow"), 16, false)));

        quests.add(new Quest("kill_spiders_basic", QuestType.KILL,
            Identifier.of("minecraft", "spider"), 8, 4.0, 0, -1,
            "Kill %count% spiders",
            new QuestReward(4, 80, Identifier.of("minecraft", "string"), 8, false)));

        // Special mob kill quests
        quests.add(new Quest("kill_bruiser", QuestType.KILL,
            Identifier.of("stn_zombies", "bruiser_zombie"), 3, 2.0, 20, -1,
            "Eliminate %count% Bruiser Zombies",
            new QuestReward(15, 500, Identifier.of("minecraft", "diamond"), 2, false)));

        quests.add(new Quest("kill_howler", QuestType.KILL,
            Identifier.of("stn_zombies", "howler_zombie"), 2, 1.5, 25, -1,
            "Silence %count% Howler Zombies",
            new QuestReward(18, 600, Identifier.of("minecraft", "diamond_sword"), 1, true)));

        quests.add(new Quest("kill_broodmother", QuestType.KILL,
            Identifier.of("stn_spiders", "broodmother_spider"), 1, 0.5, 40, -1,
            "Destroy %count% Broodmother Spider",
            new QuestReward(25, 1000, Identifier.of("minecraft", "diamond_chestplate"), 1, true)));

        // Gather quests
        quests.add(new Quest("gather_iron", QuestType.GATHER,
            Identifier.of("minecraft", "iron_ingot"), 16, 4.0, 0, -1,
            "Collect %count% iron ingots",
            new QuestReward(8, 150, Identifier.of("minecraft", "chainmail_chestplate"), 1, false)));

        quests.add(new Quest("gather_coal", QuestType.GATHER,
            Identifier.of("minecraft", "coal"), 32, 5.0, 0, -1,
            "Collect %count% coal",
            new QuestReward(5, 100, Identifier.of("minecraft", "torch"), 16, false)));

        quests.add(new Quest("gather_diamonds", QuestType.GATHER,
            Identifier.of("minecraft", "diamond"), 5, 1.5, 30, -1,
            "Collect %count% diamonds",
            new QuestReward(20, 800, Identifier.of("minecraft", "diamond_pickaxe"), 1, true)));

        // Fetch quests (rare items)
        quests.add(new Quest("fetch_totem", QuestType.FETCH,
            Identifier.of("minecraft", "totem_of_undying"), 1, 0.5, 50, -1,
            "Bring me a Totem of Undying",
            new QuestReward(32, 1000, Identifier.of("minecraft", "diamond_sword"), 1, true)));

        quests.add(new Quest("fetch_nether_star", QuestType.FETCH,
            Identifier.of("minecraft", "nether_star"), 1, 0.3, 70, -1,
            "Bring me a Nether Star",
            new QuestReward(64, 2000, Identifier.of("minecraft", "netherite_ingot"), 2, false)));

        // Trophy fetch quests
        quests.add(new Quest("fetch_bruiser_fangs", QuestType.FETCH,
            Identifier.of("stn_traders", "bruiser_fang"), 3, 1.5, 25, -1,
            "Collect %count% Bruiser Fangs",
            new QuestReward(25, 750, Identifier.of("minecraft", "diamond_chestplate"), 1, true)));

        quests.add(new Quest("fetch_howler_cords", QuestType.FETCH,
            Identifier.of("stn_traders", "howler_vocal_cord"), 2, 1.0, 30, -1,
            "Collect %count% Howler Vocal Cords",
            new QuestReward(30, 900, Identifier.of("minecraft", "diamond_helmet"), 1, true)));

        quests.add(new Quest("fetch_plague_samples", QuestType.FETCH,
            Identifier.of("stn_traders", "plague_sample"), 1, 0.8, 35, -1,
            "Obtain a Plague Sample",
            new QuestReward(35, 1000, Identifier.of("minecraft", "enchanted_golden_apple"), 1, false)));

        quests.add(new Quest("fetch_broodmother_egg", QuestType.FETCH,
            Identifier.of("stn_traders", "broodmother_egg"), 1, 0.4, 50, -1,
            "Collect a Broodmother Egg",
            new QuestReward(50, 1500, Identifier.of("minecraft", "netherite_sword"), 1, true)));

        return quests;
    }
}
