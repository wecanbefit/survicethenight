package com.stn.repair.client.hud;

import com.stn.fortifications.block.BarbedWireBlock;
import com.stn.fortifications.block.ElectricFenceBlock;
import com.stn.fortifications.block.SpikeBlock;
import com.stn.fortifications.client.network.ClientBlockDurabilityCache;
import com.stn.fortifications.durability.BlockDurabilityUtil;
import com.stn.fortifications.registry.STNBlocks;
import com.stn.repair.config.STNRepairConfig;
import com.stn.repair.item.HammerItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * HUD overlay that shows block durability when holding any hammer
 * and looking at a repairable block.
 */
@Environment(EnvType.CLIENT)
public class HammerHudOverlay {

    public static void register() {
        HudRenderCallback.EVENT.register(HammerHudOverlay::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) {
            return;
        }

        PlayerEntity player = client.player;

        // Check if player is holding any hammer
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        boolean holdingHammer = mainHand.getItem() instanceof HammerItem ||
                               offHand.getItem() instanceof HammerItem;

        if (!holdingHammer) {
            return;
        }

        // Check what block the player is looking at
        HitResult hitResult = client.crosshairTarget;

        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = client.world.getBlockState(pos);
        Block block = state.getBlock();

        // Check if it's a repairable block
        BlockInfo info = getBlockInfo(block, state, pos);

        if (info == null) {
            return;
        }

        // Render the durability overlay
        renderDurabilityOverlay(context, client, info);
    }

    private static void renderDurabilityOverlay(DrawContext context, MinecraftClient client, BlockInfo info) {
        TextRenderer textRenderer = client.textRenderer;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Calculate position based on config
        int[] pos = getHudPosition(screenWidth, screenHeight, 100, 30);
        int centerX = pos[0] + 50;
        int yPos = pos[1];

        // Block name
        String blockName = info.blockName;
        int nameWidth = textRenderer.getWidth(blockName);
        context.drawText(textRenderer, blockName, centerX - nameWidth / 2, yPos, 0xFFFFFF, true);
        yPos += 10;

        // Durability text showing current/max with color
        if (info.currentDurability >= 0) {
            float durabilityPercent = info.getDurabilityPercent();

            // Color based on durability (green -> yellow -> red)
            int textColor;
            if (durabilityPercent > 0.6f) {
                textColor = 0xFF44FF44; // Green
            } else if (durabilityPercent > 0.3f) {
                textColor = 0xFFFFFF44; // Yellow
            } else {
                textColor = 0xFFFF4444; // Red
            }

            String durabilityText = info.currentDurability + "/" + info.maxDurability;
            int textWidth = textRenderer.getWidth(durabilityText);
            context.drawText(textRenderer, durabilityText, centerX - textWidth / 2, yPos, textColor, true);
            yPos += 10;
        } else {
            // Unknown durability (tracked but client can't see exact value)
            String unknownText = "Checking...";
            int unknownWidth = textRenderer.getWidth(unknownText);
            context.drawText(textRenderer, unknownText, centerX - unknownWidth / 2, yPos, 0x888888, true);
            yPos += 10;
        }

        // Show required material if damaged
        boolean showMaterial = info.requiredMaterial != null && info.currentDurability < info.maxDurability;
        if (showMaterial) {
            String materialText = "Needs: " + info.requiredMaterial;
            int materialWidth = textRenderer.getWidth(materialText);
            context.drawText(textRenderer, materialText, centerX - materialWidth / 2, yPos, 0xFFAA44, true);
        }
    }

    private static int[] getHudPosition(int screenWidth, int screenHeight, int boxWidth, int boxHeight) {
        String position = STNRepairConfig.HAMMER_HUD_POSITION;
        int margin = 10;
        int boxX, boxY;

        switch (position) {
            case "tl" -> { // Top Left
                boxX = margin;
                boxY = margin;
            }
            case "tm" -> { // Top Middle
                boxX = (screenWidth - boxWidth) / 2;
                boxY = margin;
            }
            case "tr" -> { // Top Right
                boxX = screenWidth - boxWidth - margin;
                boxY = margin;
            }
            case "ml" -> { // Middle Left
                boxX = margin;
                boxY = (screenHeight - boxHeight) / 2;
            }
            case "mr" -> { // Middle Right
                boxX = screenWidth - boxWidth - margin;
                boxY = (screenHeight - boxHeight) / 2;
            }
            case "bl" -> { // Bottom Left
                boxX = margin;
                boxY = screenHeight - boxHeight - margin - 40; // Above hotbar
            }
            case "br" -> { // Bottom Right
                boxX = screenWidth - boxWidth - margin;
                boxY = screenHeight - boxHeight - margin - 40; // Above hotbar
            }
            default -> { // "bm" Bottom Middle (default - below crosshair)
                boxX = (screenWidth - boxWidth) / 2;
                boxY = screenHeight / 2 + 16;
            }
        }

        return new int[]{boxX, boxY};
    }

    private static BlockInfo getBlockInfo(Block block, BlockState state, BlockPos pos) {
        // Check for spike blocks with DAMAGE_COUNT
        if (block instanceof SpikeBlock && state.contains(SpikeBlock.DAMAGE_COUNT)) {
            int damage = state.get(SpikeBlock.DAMAGE_COUNT);
            int current = 15 - damage; // Convert damage to remaining durability
            String blockName = getSpecialBlockName(block);
            String material = getSpikeRepairMaterial(block);
            return new BlockInfo(blockName, current, 15, "Trap", damage > 0 ? material : null);
        }

        // Check for barbed wire with DAMAGE_COUNT
        if (block instanceof BarbedWireBlock && state.contains(BarbedWireBlock.DAMAGE_COUNT)) {
            int damage = state.get(BarbedWireBlock.DAMAGE_COUNT);
            int current = 15 - damage;
            return new BlockInfo("Barbed Wire", current, 15, "Trap", damage > 0 ? "Iron Nugget" : null);
        }

        // Check for electric fence with DAMAGE_COUNT
        if (block instanceof ElectricFenceBlock && state.contains(ElectricFenceBlock.DAMAGE_COUNT)) {
            int damage = state.get(ElectricFenceBlock.DAMAGE_COUNT);
            int current = 15 - damage;
            return new BlockInfo("Electric Fence", current, 15, "Trap", damage > 0 ? "Iron Nugget" : null);
        }

        // Check if it's a block that can be tracked (player-placed)
        int maxDurability = BlockDurabilityUtil.getMaxDurability(state);

        if (maxDurability > 0) {
            String blockName = block.getName().getString();
            String tierName = BlockDurabilityUtil.getDurabilityTierName(maxDurability);

            // Try to get durability from client cache
            ClientBlockDurabilityCache.DurabilityData cachedData = ClientBlockDurabilityCache.getOrRequestDurability(pos);

            if (cachedData != null && cachedData.current() >= 0) {
                // We have cached data from server
                int current = cachedData.current();
                int max = cachedData.max();

                // Steel blocks need steel ingots to repair (if applicable)
                String material = null;
                // Skipping steel ingot requirement for now

                return new BlockInfo(blockName, current, max, tierName, material);
            }

            // No cached data yet, show loading indicator
            return new BlockInfo(blockName, -1, maxDurability, tierName);
        }

        return null;
    }

    private static String getSpikeRepairMaterial(Block block) {
        if (block == STNBlocks.BAMBOO_SPIKES) return "Stick";
        if (block == STNBlocks.WOODEN_SPIKES) return "Stick";
        if (block == STNBlocks.IRON_SPIKES) return "Iron Nugget";
        if (block == STNBlocks.REINFORCED_SPIKES) return "Iron Nugget";
        return "Materials";
    }

    private static String getSpecialBlockName(Block block) {
        if (block == STNBlocks.BAMBOO_SPIKES) return "Bamboo Spikes";
        if (block == STNBlocks.WOODEN_SPIKES) return "Wooden Spikes";
        if (block == STNBlocks.IRON_SPIKES) return "Iron Spikes";
        if (block == STNBlocks.REINFORCED_SPIKES) return "Reinforced Spikes";
        if (block == STNBlocks.BARBED_WIRE) return "Barbed Wire";
        if (block == STNBlocks.ELECTRIC_FENCE) return "Electric Fence";
        return block.getName().getString();
    }

    private record BlockInfo(String blockName, int currentDurability, int maxDurability, String tierName, String requiredMaterial) {
        BlockInfo(String blockName, int currentDurability, int maxDurability, String tierName) {
            this(blockName, currentDurability, maxDurability, tierName, null);
        }

        float getDurabilityPercent() {
            if (maxDurability <= 0) return 1.0f;
            return (float) currentDurability / maxDurability;
        }
    }
}
