package com.fruityjourney.vampiriclineage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InfectionData extends SavedData {
    private static final String NAME = "cosmeticlboodlines_infections";
    private static final SavedData.Factory<InfectionData> FACTORY =
            new SavedData.Factory<InfectionData>(
                    InfectionData::new,
                    InfectionData::load
            );
    private final Map<UUID, UUID> infections = new HashMap<>();

    public void setInfected(UUID player, UUID infector) {
        infections.put(player, infector);
        setDirty();
    }

    public UUID getInfector(UUID player) {
        return infections.get(player);
    }

    public UUID remove(UUID player) {
        UUID removed = infections.remove(player);
        if (removed != null) setDirty();
        return removed;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, UUID> entry : infections.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("plr", entry.getKey());
            e.putUUID("infr", entry.getValue());
            list.add(e);
        }
        tag.put("infections", list);
        return tag;
    }

    public static InfectionData load(CompoundTag tag, HolderLookup.Provider registries) {
        InfectionData data = new InfectionData();
        ListTag list = tag.getList("infections", Tag.TAG_COMPOUND);
        for (Tag t: list) {
            CompoundTag e = (CompoundTag) t;
            UUID player = e.getUUID("plr");
            UUID infector = e.getUUID("infr");
            data.infections.put(player, infector);
        }
        return  data;
    }

    public static InfectionData get(ServerLevel level) {
        return  level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }
}
