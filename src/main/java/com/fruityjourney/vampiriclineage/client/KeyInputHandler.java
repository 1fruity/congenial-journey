package com.fruityjourney.vampiriclineage.client;

import com.fruityjourney.vampiriclineage.VampiricLineage;
import com.fruityjourney.vampiriclineage.RequestBloodlinePacket;
import com.google.common.graph.Network;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

@EventBusSubscriber(modid= VampiricLineage.MODID, value = Dist.CLIENT)
public class KeyInputHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            while (BloodlineKeybind.OPEN_BLOODLINE.consumeClick()) {
                UUID target = mc.player.getUUID();
                VampirePlayer vampire = VampirePlayer.get(mc.player);
                if (vampire.getLevel() > 0) {
                    ClientPacketListener connection = mc.getConnection();
                    if (connection != null) {
                        connection.send(new RequestBloodlinePacket(target));
                    }
                }
            }
        }
    }
}
