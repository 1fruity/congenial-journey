package com.fruityjourney.vampiriclineage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RequestBloodlinePacket(UUID target) implements CustomPacketPayload {
    public static final Type<RequestBloodlinePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("vampiriclineage","request_bl"));

    public static final StreamCodec<FriendlyByteBuf, RequestBloodlinePacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeUUID(pkt.target),
                    buf -> new RequestBloodlinePacket(buf.readUUID())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
