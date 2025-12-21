package com.stn.mobai.entity.ai.sense;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;

/**
 * Defines volume levels for different sound-producing actions.
 * Volume is on a 0.0 - 1.0 scale where:
 * - 1.0 = Maximum volume (can be heard from full detection range)
 * - 0.0 = Silent
 */
public class SoundVolumes {

    // Block Breaking Volumes (varies by material)
    public static final float BLOCK_BREAK_STONE = 0.8f;
    public static final float BLOCK_BREAK_METAL = 0.9f;
    public static final float BLOCK_BREAK_WOOD = 0.5f;
    public static final float BLOCK_BREAK_DIRT = 0.3f;
    public static final float BLOCK_BREAK_GLASS = 1.0f;
    public static final float BLOCK_BREAK_GRAVEL = 0.4f;
    public static final float BLOCK_BREAK_SAND = 0.2f;
    public static final float BLOCK_BREAK_LEAVES = 0.1f;
    public static final float BLOCK_BREAK_WOOL = 0.1f;
    public static final float BLOCK_BREAK_DEFAULT = 0.5f;

    // Block Placement Volumes
    public static final float BLOCK_PLACE_STONE = 0.4f;
    public static final float BLOCK_PLACE_WOOD = 0.3f;
    public static final float BLOCK_PLACE_DEFAULT = 0.25f;

    // Interaction Volumes
    public static final float CHEST_OPEN = 0.4f;
    public static final float CHEST_CLOSE = 0.3f;
    public static final float DOOR_OPEN = 0.6f;
    public static final float DOOR_CLOSE = 0.5f;
    public static final float TRAPDOOR_USE = 0.4f;
    public static final float FENCE_GATE_USE = 0.5f;
    public static final float LEVER_USE = 0.3f;
    public static final float BUTTON_USE = 0.2f;

    // Combat Volumes
    public static final float COMBAT_HIT = 0.7f;
    public static final float COMBAT_CRITICAL = 0.8f;
    public static final float COMBAT_SWEEP = 0.75f;
    public static final float BOW_SHOOT = 0.6f;
    public static final float CROSSBOW_SHOOT = 0.7f;

    // Weapon Attack Volumes
    public static final float WEAPON_MACE = 0.9f;      // Heavy, loud impact
    public static final float WEAPON_TRIDENT = 0.85f;  // Loud piercing
    public static final float WEAPON_SWORD = 0.5f;     // Medium slash
    public static final float WEAPON_AXE = 0.6f;       // Medium chop
    public static final float WEAPON_FIST = 0.3f;      // Quiet punch
    public static final float WEAPON_DEFAULT = 0.4f;   // Other tools

    // Movement Volumes
    public static final float PLAYER_WALK = 0.1f;
    public static final float PLAYER_SPRINT = 0.2f;
    public static final float PLAYER_JUMP = 0.3f;
    public static final float PLAYER_LAND = 0.35f;
    public static final float PLAYER_SNEAK = 0.0f;

    // Armor Noise Multipliers (applied to movement sounds)
    public static final float ARMOR_NONE = 1.0f;
    public static final float ARMOR_LEATHER = 1.1f;      // Quiet, flexible
    public static final float ARMOR_CHAINMAIL = 1.4f;    // Jingles a bit
    public static final float ARMOR_IRON = 1.8f;         // Heavy clanking
    public static final float ARMOR_GOLD = 1.6f;         // Slightly lighter than iron
    public static final float ARMOR_DIAMOND = 2.0f;      // Heavy and loud
    public static final float ARMOR_NETHERITE = 2.5f;    // Loudest - heavy ancient debris

    // Special Events
    public static final float EXPLOSION = 1.0f;
    public static final float ANVIL_USE = 0.9f;
    public static final float BELL_RING = 1.0f;
    public static final float FIREWORK_LAUNCH = 0.8f;
    public static final float TNT_PRIME = 0.5f;
    public static final float GOAT_HORN = 1.5f; // Extra loud - attracts mobs from far away

    // Villager Activity
    public static final float VILLAGER_WORK = 0.3f;
    public static final float VILLAGER_TRADE = 0.4f;
    public static final float VILLAGER_GOSSIP = 0.2f;

    /**
     * Get the volume for breaking a specific block.
     */
    public static float getBlockBreakVolume(BlockState state) {
        Block block = state.getBlock();

        // Glass is always loudest
        if (block == Blocks.GLASS || block == Blocks.GLASS_PANE ||
            state.isIn(BlockTags.IMPERMEABLE)) {
            return BLOCK_BREAK_GLASS;
        }

        // Metal blocks
        if (block == Blocks.IRON_BLOCK || block == Blocks.GOLD_BLOCK ||
            block == Blocks.COPPER_BLOCK || block == Blocks.ANVIL ||
            block == Blocks.CHAIN || block == Blocks.IRON_BARS ||
            block == Blocks.IRON_DOOR || block == Blocks.IRON_TRAPDOOR) {
            return BLOCK_BREAK_METAL;
        }

        // Stone-like materials
        if (state.isIn(BlockTags.BASE_STONE_OVERWORLD) ||
            state.isIn(BlockTags.BASE_STONE_NETHER) ||
            state.isIn(BlockTags.STONE_BRICKS) ||
            block == Blocks.COBBLESTONE || block == Blocks.STONE ||
            block == Blocks.BRICKS || block == Blocks.DEEPSLATE) {
            return BLOCK_BREAK_STONE;
        }

        // Wooden materials
        if (state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.PLANKS) ||
            state.isIn(BlockTags.WOODEN_DOORS) || state.isIn(BlockTags.WOODEN_FENCES) ||
            state.isIn(BlockTags.WOODEN_STAIRS) || state.isIn(BlockTags.WOODEN_SLABS)) {
            return BLOCK_BREAK_WOOD;
        }

        // Soft materials
        if (state.isIn(BlockTags.WOOL) || state.isIn(BlockTags.WOOL_CARPETS)) {
            return BLOCK_BREAK_WOOL;
        }

        // Leaves
        if (state.isIn(BlockTags.LEAVES)) {
            return BLOCK_BREAK_LEAVES;
        }

        // Gravel/sand
        if (block == Blocks.GRAVEL) {
            return BLOCK_BREAK_GRAVEL;
        }
        if (state.isIn(BlockTags.SAND)) {
            return BLOCK_BREAK_SAND;
        }

        // Dirt-like
        if (state.isIn(BlockTags.DIRT) || block == Blocks.FARMLAND ||
            block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL ||
            block == Blocks.MYCELIUM) {
            return BLOCK_BREAK_DIRT;
        }

        // Default - use hardness as a guide
        float hardness = state.getHardness(null, null);
        if (hardness < 0) return 0.0f;
        if (hardness < 0.5f) return 0.2f;
        if (hardness < 1.5f) return 0.4f;
        if (hardness < 3.0f) return 0.6f;
        return BLOCK_BREAK_DEFAULT;
    }

    /**
     * Get the volume for placing a specific block.
     */
    public static float getBlockPlaceVolume(BlockState state) {
        return getBlockBreakVolume(state) * 0.5f;
    }

    /**
     * Get the armor noise multiplier for a player based on equipped armor.
     * Returns the highest multiplier from all armor pieces.
     * Swift Sneak enchantment on leggings reduces the final multiplier.
     */
    public static float getArmorNoiseMultiplier(net.minecraft.entity.player.PlayerEntity player) {
        float maxMultiplier = ARMOR_NONE;
        int swiftSneakLevel = 0;

        for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
            if (slot.getType() != net.minecraft.entity.EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            net.minecraft.item.ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            // Get item ID and check material by name
            String itemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString().toLowerCase();

            // Check for Swift Sneak on leggings
            if (itemId.contains("leggings")) {
                // Check enchantments on the item
                var enchantments = stack.getEnchantments();
                for (var entry : enchantments.getEnchantmentEntries()) {
                    String enchantId = entry.getKey().getIdAsString().toLowerCase();
                    if (enchantId.contains("swift_sneak")) {
                        swiftSneakLevel = entry.getIntValue();
                        break;
                    }
                }
            }

            float mult = ARMOR_NONE;
            if (itemId.contains("netherite")) {
                mult = ARMOR_NETHERITE;
            } else if (itemId.contains("diamond")) {
                mult = ARMOR_DIAMOND;
            } else if (itemId.contains("iron")) {
                mult = ARMOR_IRON;
            } else if (itemId.contains("gold")) {
                mult = ARMOR_GOLD;
            } else if (itemId.contains("chain")) {
                mult = ARMOR_CHAINMAIL;
            } else if (itemId.contains("leather")) {
                mult = ARMOR_LEATHER;
            }

            if (mult > maxMultiplier) {
                maxMultiplier = mult;
            }
        }

        // Swift Sneak reduces armor noise: level 1 = 25%, level 2 = 50%, level 3 = 75% reduction
        if (swiftSneakLevel > 0) {
            float reduction = swiftSneakLevel * 0.25f;
            maxMultiplier = maxMultiplier * (1.0f - reduction);
        }

        return Math.max(ARMOR_NONE, maxMultiplier);
    }
}
