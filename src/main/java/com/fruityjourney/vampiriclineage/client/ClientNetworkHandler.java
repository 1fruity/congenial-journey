package com.fruityjourney.vampiriclineage.client;

import com.fruityjourney.vampiriclineage.BloodlinePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientNetworkHandler {
    public static void handle(BloodlinePacket packet) {
        net.minecraft.client.Minecraft.getInstance()
                .setScreen(new BloodlineScreen(packet.entries()));
    }
}
