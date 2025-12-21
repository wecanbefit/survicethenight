package com.stn.traders.registry;

import com.stn.traders.STNTraders;
import com.stn.traders.item.TrophyItem;
import com.stn.traders.item.TrophyItem.Rarity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registry for STN trader items (trophies, quest items, etc).
 */
public class STNTraderItems {

    // === ZOMBIE TROPHIES ===

    public static final TrophyItem BRUISER_FANG = registerTrophy("bruiser_fang",
        "Bruiser Zombie", Rarity.UNCOMMON);

    public static final TrophyItem HOWLER_VOCAL_CORD = registerTrophy("howler_vocal_cord",
        "Howler Zombie", Rarity.RARE);

    public static final TrophyItem SPRINTER_HEART = registerTrophy("sprinter_heart",
        "Sprinter Zombie", Rarity.UNCOMMON);

    public static final TrophyItem SPITTER_GLAND = registerTrophy("spitter_gland",
        "Spitter Zombie", Rarity.UNCOMMON);

    public static final TrophyItem PLAGUE_SAMPLE = registerTrophy("plague_sample",
        "Plague Zombie", Rarity.RARE);

    public static final TrophyItem CHARGED_CORE = registerTrophy("charged_core",
        "Electric Zombie", Rarity.RARE);

    public static final TrophyItem LUMBERJACK_AXE_HEAD = registerTrophy("lumberjack_axe_head",
        "Lumberjack Zombie", Rarity.UNCOMMON);

    public static final TrophyItem LEECH_FANG = registerTrophy("leech_fang",
        "Leech Zombie", Rarity.UNCOMMON);

    public static final TrophyItem SHATTERED_SHIELD = registerTrophy("shattered_shield",
        "Shielded Zombie", Rarity.UNCOMMON);

    // === SKELETON TROPHIES ===

    public static final TrophyItem MARKSMAN_EYE = registerTrophy("marksman_eye",
        "Marksman Skeleton", Rarity.RARE);

    public static final TrophyItem FLAME_ARROW = registerTrophy("flame_arrow",
        "Flame Archer Skeleton", Rarity.UNCOMMON);

    public static final TrophyItem REAPER_BONE = registerTrophy("reaper_bone",
        "Reaper Skeleton", Rarity.EPIC);

    public static final TrophyItem VANGUARD_PLATE = registerTrophy("vanguard_plate",
        "Vanguard Skeleton", Rarity.UNCOMMON);

    // === SPIDER TROPHIES ===

    public static final TrophyItem BROODMOTHER_EGG = registerTrophy("broodmother_egg",
        "Broodmother Spider", Rarity.EPIC);

    public static final TrophyItem VENOM_SAC = registerTrophy("venom_sac",
        "Venom Spider", Rarity.UNCOMMON);

    public static final TrophyItem LEAPER_LEG = registerTrophy("leaper_leg",
        "Leaper Spider", Rarity.UNCOMMON);

    public static final TrophyItem WEB_GLAND = registerTrophy("web_gland",
        "Webspinner Spider", Rarity.UNCOMMON);

    /**
     * Register a trophy item.
     */
    private static TrophyItem registerTrophy(String name, String mobSource, Rarity rarity) {
        Identifier id = Identifier.of(STNTraders.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        TrophyItem item = new TrophyItem(new Item.Settings().registryKey(key), mobSource, rarity);
        return Registry.register(Registries.ITEM, id, item);
    }

    /**
     * Initialize all items (triggers static initialization).
     */
    public static void register() {
        STNTraders.LOGGER.info("Registering STN trader items...");
        // Static fields are initialized when class loads
        STNTraders.LOGGER.info("Registered 17 trophy items");
    }
}
