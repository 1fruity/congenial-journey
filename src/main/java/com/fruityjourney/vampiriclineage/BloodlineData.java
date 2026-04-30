package com.fruityjourney.vampiriclineage;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
    private final Map<UUID, Node> nodesById = new HashMap<>();
    private final Map<UUID, List<UUID>> childrenByNode = new HashMap<>();
    private final Map<UUID, List<UUID>> playerToNodes = new HashMap<>();

    public static class Node {
        public final UUID nodeId;
        public final UUID playerId;
        public UUID parentNodeId = null;
        public boolean active = true;

        public Node(UUID nodeId, UUID playerId, UUID parentNodeId) {
            this.nodeId = nodeId;
            this.playerId = playerId;
            this.parentNodeId = parentNodeId;
        }

        public Node(UUID playerId, UUID parentNodeId) {
            this(UUID.randomUUID(), playerId, parentNodeId);
        }
    }
    public record BloodlineEntry(UUID nodeId, UUID playerId, UUID parentNodeId, int depth, String name, boolean active) {}

    public Node addNode(UUID playerId, UUID parentNodeId) {
        Node node = new Node(playerId, parentNodeId);
        nodesById.put(node.nodeId, node);
        childrenByNode.computeIfAbsent(parentNodeId, k -> new ArrayList<>()).add(node.nodeId);
        playerToNodes.computeIfAbsent(playerId, k -> new ArrayList<>()).add(node.nodeId);

        setDirty();
        return node;
    }

    public Node loadNode(UUID nodeId, UUID playerId, UUID parentNodeId) {
        Node node = new Node(nodeId, playerId, parentNodeId);
        nodesById.put(nodeId, node);
        childrenByNode.computeIfAbsent(parentNodeId, k -> new ArrayList<>()).add(node.nodeId);
        playerToNodes.computeIfAbsent(playerId, k -> new ArrayList<>()).add(node.nodeId);

        return node;
    }

    public Node getActiveNode(UUID playerId) {
        List<UUID> nodes = playerToNodes.get(playerId);
        if (nodes == null) return  null;
        for (UUID nodeId : nodes) {
            Node n = nodesById.get(nodeId);
            if (n != null && n.active) return  n;
        }
        return  null;
    }

    public Node getRootNode(Node node) {
        if (node != null) {
            while (node.parentNodeId != null) {
                node = nodesById.get(node.parentNodeId);
            }
        }
        return node;
    }

    public Node getRootNode(UUID playerId) {
        Node node = getActiveNode(playerId);

        return getRootNode(node);
    }

    private void buildRecursive(MinecraftServer server, Node node, UUID parentNodeId, int depth, List<BloodlineEntry> list) {
        if (node == null) return;

        String name = null;
        GameProfile profile = server.getProfileCache().get(node.playerId).orElse(null);
        if (profile == null) {
            for (String eName : VampiricLineage.ENTITY_UUIDS.keySet()) {
                UUID id = VampiricLineage.ENTITY_UUIDS.get(eName);
                if (node.playerId.equals(id)) {
                    name = eName;
                    break;
                }
            }
        } else {
            name = profile.getName();
        }
        list.add(
                new BloodlineEntry(
                        node.nodeId,
                        node.playerId,
                        node.parentNodeId,
                        depth,
                        (name != null) ? name : node.playerId.toString(),
                        node.active)
        );
        List<UUID> children = childrenByNode.get(node.nodeId);
        if (children != null) {
            for (UUID childId : children) {
                buildRecursive(server, nodesById.get(childId), node.nodeId, depth + 1, list);
            }
        }
    }

    public List<BloodlineEntry> buildTree(ServerLevel level, Node root) {
        List<BloodlineEntry> result = new ArrayList<>();
        MinecraftServer server = level.getServer();
        buildRecursive(server, root, null, 0, result);
        return result;
    }

    public boolean isTreeActive(Node start) {
        if (start.active) return true;
        if (childrenByNode.get(start.nodeId) != null) {
            for (UUID childId : childrenByNode.get(start.nodeId)) {
                Node child = nodesById.get(childId);
                if (child != null && isTreeActive(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean areChildrenActive(Node node) {
        if (childrenByNode.get(node.nodeId) != null) {
            for (UUID childId : childrenByNode.get(node.nodeId)) {
                Node child = nodesById.get(childId);
                if (child != null && child.active) {
                    return true;
                }
            }
        }
        return  false;
    }

    private void removeNode(Node node) {
        nodesById.remove(node.nodeId);
        if (node.parentNodeId != null && childrenByNode.get(node.parentNodeId) != null) {
            childrenByNode.get(node.parentNodeId).remove(node.nodeId);
        }
        if (playerToNodes.get(node.playerId) != null) {
            playerToNodes.get(node.playerId).remove(node.nodeId);
        }
        childrenByNode.remove(node.nodeId);
    }

    private void removeTree(Node start) {
        List<UUID> children = childrenByNode.get(start.nodeId);
        if (children != null) {
            for (UUID childId : children) {
                Node child = nodesById.get(childId);
                if (child != null) {
                    removeTree(child);
                }
            }
            childrenByNode.remove(start.nodeId);
        }
        if (playerToNodes.get(start.playerId) != null) {
            playerToNodes.get(start.playerId).remove(start.nodeId);
        }
        start.parentNodeId = null;
        nodesById.remove(start.nodeId);
    }

    public void removeOrDeactivate(UUID playerId) {
        Node node = getActiveNode(playerId);
        if (node == null)
            return;

        node.active = false;
        if (!isTreeActive(node)) {
            Node cursor = node;
            Node parent = cursor.parentNodeId != null ? nodesById.get(cursor.parentNodeId) : null;
            while (parent != null && !parent.active && !areChildrenActive(parent)) {
                cursor = parent;
                parent = cursor.parentNodeId != null ? nodesById.get(cursor.parentNodeId) : null;
            }
            if (cursor.parentNodeId != null && childrenByNode.get(cursor.parentNodeId) != null) {
                childrenByNode.get(cursor.parentNodeId).remove(cursor.nodeId);
            }
            removeTree(cursor);
            VampiricLineage.LOGGER.info("Player's tree removed from bloodline");
        } else {
            VampiricLineage.LOGGER.info("Player marked inactive");
        }


        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Node node : nodesById.values()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("id", node.nodeId);
            e.putUUID("pid", node.playerId);
            if (node.parentNodeId != null) {
                e.putUUID("parid", node.parentNodeId);
            }
            if (!node.active) {
                e.putBoolean("active", false);
            }
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
            UUID nodeId = e.getUUID("id");
            UUID playerId = e.getUUID("pid");
            UUID parentNodeId = null;
            if (e.contains("parid")) {
                parentNodeId = e.getUUID("parid");
            }
            Node node = data.loadNode(nodeId, playerId, parentNodeId);
            if (e.contains("active")) {
                node.active = e.getBoolean("active");
            }
        }
        return data;
    }

    public static BloodlineData get(ServerLevel level) {
        return  level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }
}
