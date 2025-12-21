package com.stn.traders.screen;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * Data sent to client when opening the quest screen.
 */
public record QuestScreenData(
    List<String> offeredQuestIds,
    List<ActiveQuestData> activeQuests
) {
    public record ActiveQuestData(String questId, int progress, int targetCount, int gamestage) {}

    public static final PacketCodec<RegistryByteBuf, QuestScreenData> CODEC = new QuestScreenDataCodec();

    private static class QuestScreenDataCodec implements PacketCodec<RegistryByteBuf, QuestScreenData> {
        @Override
        public QuestScreenData decode(RegistryByteBuf buf) {
            // Read offered quest IDs
            int offeredCount = buf.readVarInt();
            List<String> offeredIds = new ArrayList<>(offeredCount);
            for (int i = 0; i < offeredCount; i++) {
                offeredIds.add(buf.readString());
            }

            // Read active quests
            int activeCount = buf.readVarInt();
            List<ActiveQuestData> active = new ArrayList<>(activeCount);
            for (int i = 0; i < activeCount; i++) {
                String questId = buf.readString();
                int progress = buf.readVarInt();
                int targetCount = buf.readVarInt();
                int gamestage = buf.readVarInt();
                active.add(new ActiveQuestData(questId, progress, targetCount, gamestage));
            }

            return new QuestScreenData(offeredIds, active);
        }

        @Override
        public void encode(RegistryByteBuf buf, QuestScreenData data) {
            // Write offered quest IDs
            buf.writeVarInt(data.offeredQuestIds().size());
            for (String id : data.offeredQuestIds()) {
                buf.writeString(id);
            }

            // Write active quests
            buf.writeVarInt(data.activeQuests().size());
            for (ActiveQuestData active : data.activeQuests()) {
                buf.writeString(active.questId());
                buf.writeVarInt(active.progress());
                buf.writeVarInt(active.targetCount());
                buf.writeVarInt(active.gamestage());
            }
        }
    }
}
