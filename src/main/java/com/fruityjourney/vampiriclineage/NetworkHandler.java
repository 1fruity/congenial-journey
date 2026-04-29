package com.fruityjourney.vampiriclineage;

import com.fruityjourney.vampiriclineage.client.ClientNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.UUID;

@EventBusSubscriber(modid= VampiricLineage.MODID, bus=EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        registrar.playToServer(
                RequestBloodlinePacket.TYPE,
                RequestBloodlinePacket.CODEC,
                (packet, context) -> {
                    context.enqueueWork(() -> {
                        ServerPlayer player = (ServerPlayer) context.player();
                        var level = player.serverLevel();
                        var data = BloodlineData.get(level);
                        UUID root = BloodlineData.getRoot(packet.target(), data);
                        var tree = data.buildTree(level, root);

                        context.reply(new BloodlinePacket(tree));
                    });
                }
        );

        registrar.playToClient(
                BloodlinePacket.TYPE,
                BloodlinePacket.CODEC,
                (packet, context) -> {
                    context.enqueueWork(() -> {
                        ClientNetworkHandler.handle(packet);
                    });
                }
        );
    }
}
