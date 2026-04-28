package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.NetworkHandler;
import com.example.examplemod.RequestBloodlinePacket;
import com.google.common.graph.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

@EventBusSubscriber(modid= ExampleMod.MODID, value = Dist.CLIENT)
public class KeyInputHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            while (BloodlineKeybind.OPEN_BLOODLINE.consumeClick()) {
                UUID target = mc.player.getUUID();
                ClientPacketListener connection = mc.getConnection();
                if (connection != null) {
                    connection.send(new RequestBloodlinePacket(target));
                }
            }
        }
    }
}
