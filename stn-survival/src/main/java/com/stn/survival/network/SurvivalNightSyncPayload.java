package com.stn.survival.network;

import com.stn.survival.STNSurvival;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Packet payload for synchronizing survival night state from server to client.
 */
public record SurvivalNightSyncPayload(boolean isActive) implements CustomPayload {

    public static final CustomPayload.Id<SurvivalNightSyncPayload> ID =
        new CustomPayload.Id<>(Identifier.of(STNSurvival.MOD_ID, "survival_night_sync"));

    public static final PacketCodec<RegistryByteBuf, SurvivalNightSyncPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.BOOLEAN, SurvivalNightSyncPayload::isActive,
            SurvivalNightSyncPayload::new
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
