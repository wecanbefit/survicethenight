package com.stn.traders.client.screen;

import com.stn.traders.network.QuestActionPayload;
import com.stn.traders.quest.Quest;
import com.stn.traders.quest.QuestConfigManager;
import com.stn.traders.quest.QuestInstance;
import com.stn.traders.screen.QuestScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Client-side quest screen UI.
 */
public class QuestScreen extends HandledScreen<QuestScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of("stn_traders", "textures/gui/quest_screen.png");
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 220;

    // Selected quest for turn-in reward selection
    private String selectedQuestId = null;

    // Track data state to detect when to rebuild buttons
    private int lastOfferedCount = -1;
    private int lastActiveCount = -1;

    public QuestScreen(QuestScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = GUI_WIDTH;
        this.backgroundHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // Hide default inventory labels
        this.titleX = 8;
        this.titleY = 6;
        this.playerInventoryTitleY = 10000; // Hide

        // Refresh and add buttons
        rebuildButtons();
    }

    private void rebuildButtons() {
        // Clear existing buttons
        this.clearChildren();

        int buttonWidth = GUI_WIDTH - 20;
        int buttonHeight = 20;
        int startX = this.x + 10;
        int startY = this.y + 35;

        // === OFFERED QUESTS SECTION ===
        List<Quest> offered = handler.getOfferedQuests();
        for (int i = 0; i < offered.size(); i++) {
            Quest quest = offered.get(i);
            final String questId = quest.id();
            String desc = quest.getFormattedDescription(0);

            // Full-width button with quest text
            ButtonWidget questBtn = ButtonWidget.builder(
                Text.literal("[Accept] " + desc),
                button -> {
                    // Send accept packet to server
                    ClientPlayNetworking.send(QuestActionPayload.accept(questId));
                }
            ).dimensions(startX, startY + (i * 22), buttonWidth, buttonHeight).build();

            this.addDrawableChild(questBtn);
        }

        // === ACTIVE QUESTS SECTION ===
        startY = this.y + 115;
        List<QuestInstance> active = handler.getActiveQuests();
        for (int i = 0; i < active.size(); i++) {
            QuestInstance instance = active.get(i);
            Quest quest = QuestConfigManager.getQuest(instance.getQuestId());
            if (quest == null) continue;

            final String questId = instance.getQuestId();
            boolean canTurnIn = instance.canTurnIn();
            String progress = instance.getProgress() + "/" + instance.getTargetCount();
            String desc = quest.getFormattedDescription(instance.getGamestageAtAccept());

            if (canTurnIn) {
                // Clickable turn-in button
                ButtonWidget turnInBtn = ButtonWidget.builder(
                    Text.literal("[Turn In] " + desc + " [" + progress + "]").formatted(Formatting.GREEN),
                    button -> {
                        selectedQuestId = questId;
                        showRewardSelection();
                    }
                ).dimensions(startX, startY + (i * 22), buttonWidth, buttonHeight).build();

                this.addDrawableChild(turnInBtn);
            } else {
                // Non-clickable progress display (still a button for consistent look)
                ButtonWidget progressBtn = ButtonWidget.builder(
                    Text.literal(desc + " [" + progress + "]").formatted(Formatting.GRAY),
                    button -> {}
                ).dimensions(startX, startY + (i * 22), buttonWidth, buttonHeight).build();
                progressBtn.active = false;

                this.addDrawableChild(progressBtn);
            }
        }

        // === CLOSE BUTTON ===
        ButtonWidget closeBtn = ButtonWidget.builder(
            Text.literal("Close"),
            button -> this.close()
        ).dimensions(this.x + GUI_WIDTH / 2 - 30, this.y + GUI_HEIGHT - 25, 60, 20).build();

        this.addDrawableChild(closeBtn);
    }

    private void showRewardSelection() {
        if (selectedQuestId == null) return;

        // Clear and show reward selection buttons
        this.clearChildren();

        int centerX = this.x + GUI_WIDTH / 2;
        int startY = this.y + 80;

        // Emeralds button
        ButtonWidget emeraldsBtn = ButtonWidget.builder(
            Text.literal("Emeralds").formatted(Formatting.GREEN),
            button -> claimReward(0)
        ).dimensions(centerX - 100, startY, 80, 20).build();

        // Experience button
        ButtonWidget xpBtn = ButtonWidget.builder(
            Text.literal("Experience").formatted(Formatting.YELLOW),
            button -> claimReward(1)
        ).dimensions(centerX - 40, startY + 25, 80, 20).build();

        // Item button
        ButtonWidget itemBtn = ButtonWidget.builder(
            Text.literal("Item").formatted(Formatting.AQUA),
            button -> claimReward(2)
        ).dimensions(centerX + 20, startY, 80, 20).build();

        // Cancel button
        ButtonWidget cancelBtn = ButtonWidget.builder(
            Text.literal("Cancel"),
            button -> {
                selectedQuestId = null;
                rebuildButtons();
            }
        ).dimensions(centerX - 40, startY + 50, 80, 20).build();

        this.addDrawableChild(emeraldsBtn);
        this.addDrawableChild(xpBtn);
        this.addDrawableChild(itemBtn);
        this.addDrawableChild(cancelBtn);
    }

    private void claimReward(int rewardType) {
        if (selectedQuestId != null) {
            // Send turn-in packet to server
            ClientPlayNetworking.send(QuestActionPayload.turnIn(selectedQuestId, rewardType));
            selectedQuestId = null;
            rebuildButtons();
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // Draw a simple background (will use texture when available)
        context.fill(this.x, this.y, this.x + GUI_WIDTH, this.y + GUI_HEIGHT, 0xCC000000);
        context.drawBorder(this.x, this.y, GUI_WIDTH, GUI_HEIGHT, 0xFF888888);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // Text is drawn in render() instead
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Check if data has changed and rebuild buttons if needed
        int currentOfferedCount = handler.getOfferedQuests().size();
        int currentActiveCount = handler.getActiveQuests().size();
        if (selectedQuestId == null && (currentOfferedCount != lastOfferedCount || currentActiveCount != lastActiveCount)) {
            lastOfferedCount = currentOfferedCount;
            lastActiveCount = currentActiveCount;
            rebuildButtons();
        }

        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Draw section headers
        int textX = this.x + 8;

        // Title (gold)
        context.drawText(this.textRenderer, "Jobs Board", textX, this.y + 6, 0xFFFFD700, true);

        if (selectedQuestId != null) {
            // Show reward selection prompt
            context.drawText(this.textRenderer, "Choose Your Reward:",
                this.x + GUI_WIDTH / 2 - 50, this.y + 60, 0xFFFFFF00, true);
        } else {
            // Section headers
            context.drawText(this.textRenderer, "Available Quests:", textX, this.y + 22, 0xFF55FF55, true);

            List<Quest> offered = handler.getOfferedQuests();
            if (offered.isEmpty()) {
                context.drawText(this.textRenderer, "Loading...", textX + 4, this.y + 40, 0xFFAAAAAA, true);
            }

            context.drawText(this.textRenderer, "Active Quests:", textX, this.y + 102, 0xFF55FFFF, true);

            List<QuestInstance> active = handler.getActiveQuests();
            if (active.isEmpty()) {
                context.drawText(this.textRenderer, "No active quests", textX + 4, this.y + 120, 0xFF888888, true);
            }
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
