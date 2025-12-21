package com.stn.survival.network;

import com.stn.survival.STNSurvival;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Packet payload for synchronizing gamestage HUD data from server to client.
 * Contains: current day, world gamestage, player deaths, and global deaths.
 */
public record GamestageHudPayload(
    long currentDay,
    int worldGamestage,
    int playerDeaths,
    int globalDeaths
) implements CustomPayload {

    public static final CustomPayload.Id<GamestageHudPayload> ID =
        new CustomPayload.Id<>(Identifier.of(STNSurvival.MOD_ID, "gamestage_hud"));

    public static final PacketCodec<RegistryByteBuf, GamestageHudPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.VAR_LONG, GamestageHudPayload::currentDay,
            PacketCodecs.VAR_INT, GamestageHudPayload::worldGamestage,
            PacketCodecs.VAR_INT, GamestageHudPayload::playerDeaths,
            PacketCodecs.VAR_INT, GamestageHudPayload::globalDeaths,
            GamestageHudPayload::new
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
