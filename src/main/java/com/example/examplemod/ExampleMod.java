package com.example.examplemod;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "examplemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public ExampleMod() {

    }

    @EventBusSubscriber(modid=ExampleMod.MODID)
    public static class ModEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            BloodlineCommand.register(event.getDispatcher());
        }
    }
}
