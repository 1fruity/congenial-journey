package com.fruityjourney.vampiriclineage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record BloodlinePacket(List<BloodlineData.BloodlineEntry> entries) implements CustomPacketPayload {
    public static final Type<BloodlinePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(VampiricLineage.MODID, "bloodline"));

    public static final StreamCodec<FriendlyByteBuf, BloodlinePacket> CODEC =
            StreamCodec.of((buf, pkt) -> {
                buf.writeInt(pkt.entries.size());

                for (var entry : pkt.entries) {
                    buf.writeUUID(entry.id());
                    if (entry.parent() != null) {
                        buf.writeBoolean(true);
                        buf.writeUUID(entry.parent());
                    } else {
                        buf.writeBoolean(false);
                    }
                    buf.writeInt(entry.depth());
                    buf.writeUtf(entry.name());
                }
            }, buf -> {
                int size = buf.readInt();
                List<BloodlineData.BloodlineEntry> list = new ArrayList<>();
                for (int i=0; i<size; i++) {
                    UUID id = buf.readUUID();
                    UUID parent = buf.readBoolean() ? buf.readUUID() : null;
                    int depth = buf.readInt();
                    String name = buf.readUtf();

                    list.add(new BloodlineData.BloodlineEntry(id, parent, depth, name));
                }
                return  new BloodlinePacket(list);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
