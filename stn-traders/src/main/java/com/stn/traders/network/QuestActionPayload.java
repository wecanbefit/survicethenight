package com.stn.traders.network;

import com.stn.traders.STNTraders;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Client-to-server payload for quest actions (accept, turn in).
 */
public record QuestActionPayload(
    Action action,
    String questId,
    int rewardType  // Only used for TURN_IN
) implements CustomPayload {

    public enum Action {
        ACCEPT,
        TURN_IN
    }

    public static final CustomPayload.Id<QuestActionPayload> ID =
        new CustomPayload.Id<>(Identifier.of(STNTraders.MOD_ID, "quest_action"));

    public static final PacketCodec<RegistryByteBuf, QuestActionPayload> CODEC =
        PacketCodec.of(QuestActionPayload::write, QuestActionPayload::read);

    private static QuestActionPayload read(RegistryByteBuf buf) {
        int actionOrdinal = buf.readVarInt();
        Action action = Action.values()[actionOrdinal];
        String questId = buf.readString();
        int rewardType = buf.readVarInt();
        return new QuestActionPayload(action, questId, rewardType);
    }

    private void write(RegistryByteBuf buf) {
        buf.writeVarInt(action.ordinal());
        buf.writeString(questId);
        buf.writeVarInt(rewardType);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    // Convenience constructors
    public static QuestActionPayload accept(String questId) {
        return new QuestActionPayload(Action.ACCEPT, questId, 0);
    }

    public static QuestActionPayload turnIn(String questId, int rewardType) {
        return new QuestActionPayload(Action.TURN_IN, questId, rewardType);
    }
}
