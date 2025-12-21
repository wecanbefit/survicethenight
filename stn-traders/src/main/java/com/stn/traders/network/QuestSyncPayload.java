package com.stn.traders.network;

import com.stn.traders.STNTraders;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Payload for syncing quest data from server to client when opening the jobs board.
 */
public record QuestSyncPayload(
    List<String> offeredQuestIds,
    List<ActiveQuestEntry> activeQuests
) implements CustomPayload {

    public record ActiveQuestEntry(String questId, int progress, int targetCount, int gamestage, boolean canTurnIn) {}

    public static final CustomPayload.Id<QuestSyncPayload> ID =
        new CustomPayload.Id<>(Identifier.of(STNTraders.MOD_ID, "quest_sync"));

    public static final PacketCodec<RegistryByteBuf, QuestSyncPayload> CODEC =
        PacketCodec.of(QuestSyncPayload::write, QuestSyncPayload::read);

    private static QuestSyncPayload read(RegistryByteBuf buf) {
        // Read offered quest IDs
        int offeredCount = buf.readVarInt();
        List<String> offeredIds = new ArrayList<>(offeredCount);
        for (int i = 0; i < offeredCount; i++) {
            offeredIds.add(buf.readString());
        }

        // Read active quests
        int activeCount = buf.readVarInt();
        List<ActiveQuestEntry> active = new ArrayList<>(activeCount);
        for (int i = 0; i < activeCount; i++) {
            String questId = buf.readString();
            int progress = buf.readVarInt();
            int targetCount = buf.readVarInt();
            int gamestage = buf.readVarInt();
            boolean canTurnIn = buf.readBoolean();
            active.add(new ActiveQuestEntry(questId, progress, targetCount, gamestage, canTurnIn));
        }

        return new QuestSyncPayload(offeredIds, active);
    }

    private void write(RegistryByteBuf buf) {
        // Write offered quest IDs
        buf.writeVarInt(offeredQuestIds.size());
        for (String id : offeredQuestIds) {
            buf.writeString(id);
        }

        // Write active quests
        buf.writeVarInt(activeQuests.size());
        for (ActiveQuestEntry entry : activeQuests) {
            buf.writeString(entry.questId());
            buf.writeVarInt(entry.progress());
            buf.writeVarInt(entry.targetCount());
            buf.writeVarInt(entry.gamestage());
            buf.writeBoolean(entry.canTurnIn());
        }
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
