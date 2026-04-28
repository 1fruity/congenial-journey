package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class BloodlineKeybind {
    public static final KeyMapping OPEN_BLOODLINE = new KeyMapping(
            "key." + ExampleMod.MODID + ".open_bloodline",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories." + ExampleMod.MODID
    );

    @SubscribeEvent
    public static void registerKey(RegisterKeyMappingsEvent event) {
        event.register(OPEN_BLOODLINE);
    }
}
