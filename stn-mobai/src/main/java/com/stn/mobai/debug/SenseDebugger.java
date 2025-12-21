package com.stn.mobai.debug;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/**
 * Debug utility for tracking mob sensory detection.
 * Supports separate flags for different debug outputs:
 * - sounds: Shows sound events in chat
 * - awareness: Shows which mobs detect you via action bar
 */
public class SenseDebugger {

    private static boolean soundsEnabled = false;
    private static boolean awarenessEnabled = false;
    private static int tickCounter = 0;

    public static void setSoundsEnabled(boolean enable) {
        soundsEnabled = enable;
    }

    public static boolean isSoundsEnabled() {
        return soundsEnabled;
    }

    public static void toggleSounds() {
        soundsEnabled = !soundsEnabled;
    }

    public static void setAwarenessEnabled(boolean enable) {
        awarenessEnabled = enable;
    }

    public static boolean isAwarenessEnabled() {
        return awarenessEnabled;
    }

    public static void toggleAwareness() {
        awarenessEnabled = !awarenessEnabled;
    }

    public static void toggleAll() {
        boolean anyEnabled = soundsEnabled || awarenessEnabled;
        soundsEnabled = !anyEnabled;
        awarenessEnabled = !anyEnabled;
    }

    public static boolean isAnyEnabled() {
        return soundsEnabled || awarenessEnabled;
    }

    /**
     * Called when a sound is emitted. Notifies nearby players in debug mode.
     */
    public static void onSoundEmitted(ServerWorld world, BlockPos pos, float volume, String type) {
        if (!soundsEnabled) return;

        // Find players within 50 blocks
        List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(
            ServerPlayerEntity.class,
            new Box(pos).expand(50),
            p -> true
        );

        for (ServerPlayerEntity player : nearbyPlayers) {
            double distance = Math.sqrt(player.getBlockPos().getSquaredDistance(pos));
            String distText = distance < 1.0 ? "(you)" : "%.0fm away".formatted(distance);
            player.sendMessage(
                Text.literal("[SOUND] ")
                    .formatted(Formatting.YELLOW)
                    .append(Text.literal(type).formatted(Formatting.WHITE))
                    .append(Text.literal(" vol=%.2f %s".formatted(volume, distText)).formatted(Formatting.GRAY)),
                false
            );
        }
    }

    /**
     * Tick the debugger - shows mobs targeting the player.
     */
    public static void tick(ServerWorld world) {
        if (!awarenessEnabled) return;

        tickCounter++;
        if (tickCounter < 20) return; // Update once per second
        tickCounter = 0;

        for (ServerPlayerEntity player : world.getPlayers()) {
            List<String> detectingMobs = new ArrayList<>();

            // Find mobs within 64 blocks that are targeting this player
            List<MobEntity> nearbyMobs = world.getEntitiesByClass(
                MobEntity.class,
                new Box(player.getBlockPos()).expand(64),
                mob -> mob.getTarget() == player
            );

            for (MobEntity mob : nearbyMobs) {
                String mobName = mob.getType().getName().getString();
                double distance = Math.sqrt(mob.getBlockPos().getSquaredDistance(player.getBlockPos()));
                detectingMobs.add("%s (%.0fm)".formatted(mobName, distance));
            }

            if (!detectingMobs.isEmpty()) {
                // Send as action bar for non-intrusive display
                String mobList = String.join(", ", detectingMobs.subList(0, Math.min(3, detectingMobs.size())));
                if (detectingMobs.size() > 3) {
                    mobList += " +" + (detectingMobs.size() - 3) + " more";
                }

                player.sendMessage(
                    Text.literal("Detected by: ")
                        .formatted(Formatting.RED)
                        .append(Text.literal(mobList).formatted(Formatting.WHITE)),
                    true // action bar
                );
            }
        }
    }

    /**
     * Send debug info about player's current environment.
     */
    public static void sendEnvironmentInfo(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos pos = player.getBlockPos();

        // Count nearby light sources
        int lightSources = 0;
        int heatSources = 0;

        for (int x = -16; x <= 16; x++) {
            for (int y = -8; y <= 8; y++) {
                for (int z = -16; z <= 16; z++) {
                    BlockPos checkPos = pos.add(x, y, z);
                    if (world.getBlockState(checkPos).getLuminance() > 10) {
                        lightSources++;
                    }
                    // Check for fire/lava
                    var block = world.getBlockState(checkPos).getBlock();
                    if (block == net.minecraft.block.Blocks.FIRE ||
                        block == net.minecraft.block.Blocks.LAVA ||
                        block == net.minecraft.block.Blocks.CAMPFIRE) {
                        heatSources++;
                    }
                }
            }
        }

        player.sendMessage(
            Text.literal("[ENV] ")
                .formatted(Formatting.AQUA)
                .append(Text.literal("Light sources: " + lightSources).formatted(Formatting.YELLOW))
                .append(Text.literal(" | Heat sources: " + heatSources).formatted(Formatting.RED)),
            false
        );
    }
}
