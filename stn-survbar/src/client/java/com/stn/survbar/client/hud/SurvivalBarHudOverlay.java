package com.stn.survbar.client.hud;

import com.stn.survbar.config.STNSurvbarConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

/**
 * HUD overlay that displays a bar showing days until the next survival night.
 * Shows 10 icons representing the 10-day cycle:
 * - day.png: Days that have passed
 * - day_empty.png: Days that haven't passed yet
 * - survnight.png: The 10th day (survival night indicator)
 */
@Environment(EnvType.CLIENT)
public class SurvivalBarHudOverlay {

    private static final Identifier TEX_DAY = Identifier.of("stn_survbar", "textures/gui/sprites/day.png");
    private static final Identifier TEX_SURVNIGHT = Identifier.of("stn_survbar", "textures/gui/sprites/survnight.png");

    private static final int SURVIVAL_NIGHT_INTERVAL = 10;
    private static final int ICON_SIZE = 9;
    private static final int ICON_SPACING = -1;

    public static void register() {
        HudRenderCallback.EVENT.register(SurvivalBarHudOverlay::render);
    }

    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }

        // Don't show in creative or spectator mode
        if (client.player.isCreative() || client.player.isSpectator()) {
            return;
        }

        // Hide when underwater (air bubbles show in this area)
        if (client.player.isSubmergedInWater() || client.player.getAir() < client.player.getMaxAir()) {
            return;
        }

        // Calculate current day in the survival night cycle
        long timeOfDay = client.world.getTimeOfDay();
        long currentDay = timeOfDay / 24000;

        // Day 0 is the first day - no progress yet, don't show any icons
        if (currentDay == 0) {
            return;
        }

        int dayInCycle = (int) (currentDay % SURVIVAL_NIGHT_INTERVAL);

        // dayInCycle 0 means we're on a survival night day (day 10, 20, 30, etc.)
        // Otherwise it's days 1-9 in the cycle
        boolean isSurvivalNight = dayInCycle == 0;
        int effectiveDay = isSurvivalNight ? SURVIVAL_NIGHT_INTERVAL : dayInCycle;

        drawSurvivalBar(ctx, client, effectiveDay);
    }

    private static void drawSurvivalBar(DrawContext ctx, MinecraftClient client, int currentDayInCycle) {
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();

        int step = ICON_SIZE + ICON_SPACING;

        // Right side positioning, aligned with food bar (configurable via /stnsb command)
        int xOffset = STNSurvbarConfig.xOffset;
        int yOffset = STNSurvbarConfig.yOffset;

        int rightEdge = sw / 2 + xOffset;
        int y = sh - yOffset - (ICON_SIZE * 2);

        // Determine how many icons to draw
        boolean isSurvivalNight = currentDayInCycle == SURVIVAL_NIGHT_INTERVAL;
        int dayIcons = isSurvivalNight ? SURVIVAL_NIGHT_INTERVAL - 1 : currentDayInCycle;
        int totalIcons = isSurvivalNight ? SURVIVAL_NIGHT_INTERVAL : currentDayInCycle;

        // Calculate total width and starting position (grow from right to left)
        int totalWidth = ICON_SIZE + (totalIcons - 1) * step;
        int x0 = rightEdge - totalWidth;

        int iconIndex = 0;

        // On survival night, draw survnight icon first (far left)
        if (isSurvivalNight) {
            ctx.drawTexturedQuad(
                TEX_SURVNIGHT,
                x0 + iconIndex * step,
                y,
                x0 + iconIndex * step + ICON_SIZE,
                y + ICON_SIZE,
                0f, 1f, 0f, 1f
            );
            iconIndex++;
        }

        // Draw day icons (left to right, after survnight if present)
        for (int i = 0; i < dayIcons; i++) {
            ctx.drawTexturedQuad(
                TEX_DAY,
                x0 + iconIndex * step,
                y,
                x0 + iconIndex * step + ICON_SIZE,
                y + ICON_SIZE,
                0f, 1f, 0f, 1f
            );
            iconIndex++;
        }
    }
}
