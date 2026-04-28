package com.example.examplemod;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.fixes.SavedDataUUIDFix;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BloodlineData extends SavedData {
    private static final String NAME = "cosmeticlboodlines_bloodlines";
    private static final SavedData.Factory<BloodlineData> FACTORY =
            new SavedData.Factory<BloodlineData>(
                    BloodlineData::new,
                    BloodlineData::load
            );
    private final Map<UUID, UUID> parentMap = new HashMap<>();
    private final Map<UUID, Set<UUID>> childrenMap = new HashMap<>();

    public record BloodlineEntry(UUID id, UUID parent, int depth, String name) {}

    public static UUID getRoot(UUID player, BloodlineData data) {
        UUID c = player;
        while (data.getParent(c) != null) {
            c = data.getParent(c);
        }
        return c;
    }

    private void loadLink(UUID child, UUID parent) {
        parentMap.put(child, parent);
        childrenMap
                .computeIfAbsent(parent, k -> new HashSet<>())
                .add(child);
    }

    public void addLink(UUID child, UUID parent) {
        if (!parentMap.containsKey(child)) {
            parentMap.put(child, parent);
            childrenMap
                    .computeIfAbsent(parent, k -> new HashSet<>())
                    .add(child);
            setDirty();
        }
    }

    public UUID getParent(UUID child) {
        return parentMap.get(child);
    }

    public Set<UUID> getChildren(UUID parent) {
        return  childrenMap.getOrDefault(parent, Collections.emptySet());
    }

    private void buildRecursive(MinecraftServer server, UUID node, UUID parent, int depth, List<BloodlineEntry> list) {
        GameProfile profile = server.getProfileCache().get(node).orElse(null);
        list.add(new BloodlineEntry(node, parent, depth, (profile != null) ? profile.getName() : node.toString()));
        for (UUID child : getChildren(node)) {
            buildRecursive(server, child, node, depth + 1, list);
        }
    }

    public List<BloodlineEntry> buildTree(ServerLevel level, UUID root) {
        List<BloodlineEntry> result = new ArrayList<>();
        MinecraftServer server = level.getServer();
        buildRecursive(server, root, null, 0, result);
        return result;
    }


    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, UUID> entry : parentMap.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("ch", entry.getKey());
            e.putUUID("pa", entry.getValue());
            list.add(e);
        }
        tag.put("links", list);
        return tag;
    }

    public static BloodlineData load(CompoundTag tag, HolderLookup.Provider registries) {
        BloodlineData data = new BloodlineData();
        ListTag list = tag.getList("links", Tag.TAG_COMPOUND);
        for (Tag t: list) {
            CompoundTag e = (CompoundTag) t;
            UUID player = e.getUUID("ch");
            UUID infector = e.getUUID("pa");
            data.loadLink(player, infector);
        }
        return data;
    }

    public static BloodlineData get(ServerLevel level) {
        return  level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }
}
