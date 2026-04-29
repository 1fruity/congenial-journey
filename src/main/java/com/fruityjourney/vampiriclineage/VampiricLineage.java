package com.fruityjourney.vampiriclineage;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(VampiricLineage.MODID)
public class VampiricLineage {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "vampiriclineage";

    public VampiricLineage() {

    }

    @EventBusSubscriber(modid= VampiricLineage.MODID)
    public static class ModEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            BloodlineCommand.register(event.getDispatcher());
        }
    }
}
