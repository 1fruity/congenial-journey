package com.fruityjourney.vampiriclineage;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(VampiricLineage.MODID)
public class VampiricLineage {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "vampiriclineage";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final Map<String, UUID> ENTITY_UUIDS = Map.ofEntries(
            Map.entry("Adrian DraxWind", UUID.fromString("781e356d-58c5-42c6-9cc9-705070bd7f90")),
            Map.entry("Crimson Cat", UUID.fromString("999c1dc3-a767-4fcd-b0ef-9407fe4ff81a")),
            Map.entry("Cultist Courn", UUID.fromString("101642c1-0c95-4717-b4ab-d2e4f7ea730c")),
            Map.entry("Marchioness Spade", UUID.fromString("5c5cd8e9-f6e8-4873-ba56-432a8fc01eef")),
            Map.entry("Mazdallier", UUID.fromString("5b9a7787-34b1-4a33-8aa6-0358b2e55f25")),
            Map.entry("Merciless Mike", UUID.fromString("15862779-6955-4d94-8b20-f0dd9c7e118a")),
            Map.entry("Sinister Solace", UUID.fromString("014ddac0-1704-47a3-b713-7af2724a9707")),
            Map.entry("The nightmarish Krab", UUID.fromString("4ea3807c-b1b5-42d0-b4b2-f3a08cc19311")),
            Map.entry("The powerful Bill", UUID.fromString("35b3bacb-7e8a-4f0b-af5c-e5e0e8a9c8cc")),
            Map.entry("Valorous Vol", UUID.fromString("8fa2aee9-d6d1-4e86-935e-12fdf529cdd1")),
            Map.entry("Zin", UUID.fromString("35b4a62f-47f4-4124-b784-895f071e8655"))
    );

    public VampiricLineage() {

    }

    @EventBusSubscriber(modid= VampiricLineage.MODID)
    public static class ModEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            SetLineage.register(event.getDispatcher());
        }
    }
}
