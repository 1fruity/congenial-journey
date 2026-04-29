package com.fruityjourney.vampiriclineage.client;

import com.fruityjourney.vampiriclineage.BloodlineData;
import com.fruityjourney.vampiriclineage.VampiricLineage;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BloodlineScreen extends Screen {
    public static final ResourceLocation BG = ResourceLocation.parse("vampiriclineage:textures/gui_background.png");
    public static final ResourceLocation WINDOW = ResourceLocation.parse("vampiriclineage:textures/gui_window.png");
    public static final ResourceLocation NODE = ResourceLocation.parse("vampiriclineage:textures/node.png");
    public static final ResourceLocation START_NODE = ResourceLocation.parse("vampiriclineage:textures/start_node.png");

    private static final ExecutorService SKIN_EXECUTOR = Executors.newCachedThreadPool();

    static class BloodlineNode {
        UUID id;
        String name;
        UUID parent = null;
        int depth = 0;
        int guiX = 0;
        int guiY = 0;
        double tempX = 0;
        boolean expanded = true;
        ResourceLocation skin;
    }

    private List<BloodlineNode> nodes;
    private Map<UUID, BloodlineNode> nodeMap = new HashMap<>();
    private Map<UUID, List<BloodlineNode>> childrenMap = new HashMap<>();
    private List<BloodlineNode> visibleNodes = new ArrayList<>();
    private BloodlineNode selected = null;
    private BloodlineNode rootNode = null;
    private Button backButton;

    private int windowWidth = 256;
    private int windowHeight = 256;
    private int contentWidth = windowWidth - 22;
    private int contentHeight = windowHeight - 82;
    private int nodeSize = 32;

    private int windowX = 0;
    private int windowY = 0;
    private int contentX = 0;
    private int contentY = 0;
    private double minPanX, maxPanX;
    private double minPanY, maxPanY;
    private int minX = -contentWidth / 2;
    private int minY = -contentHeight / 2;
    private int maxX = 0;
    private int maxY = 0;

    private double zoom = 1.0;
    private double panX = 0;
    private double panY = 0;

    private void layout(Map<UUID, List<BloodlineNode>> childrenMap, BloodlineNode node, int[] cursor) {
        List<BloodlineNode> children = childrenMap.get(node.id);

        if (!node.expanded || children == null || children.isEmpty()) {
            node.tempX = cursor[0] ++;
        } else {
            for (BloodlineNode child : children) {
                layout(childrenMap, child, cursor);
            }

            double sum = 0;
            for (BloodlineNode child : children) {
                sum += child.tempX;
            }
            node.tempX = sum / children.size();
        }
    }

    private void buildVisible(BloodlineNode node, int depth) {
        node.depth = depth;
        visibleNodes.add(node);

        if (!node.expanded) return;
        List<BloodlineNode> children = childrenMap.get(node.id);
        if (children != null) {
            for (BloodlineNode child : children) {
                buildVisible(child, depth + 1);
            }
        }
    }

    private  void rebuildLayout() {
        minX = -contentWidth / 2;
        minY = -contentHeight / 2;
        maxX = 0;
        maxY = 0;

        visibleNodes.clear();

        buildVisible(rootNode, 0);
        int[] cursor = {0};
        layout(childrenMap, rootNode, cursor);

        for (BloodlineNode node : visibleNodes) {
            node.guiX = (int)(node.tempX * nodeSize * 2);
            node.guiY = node.depth * nodeSize * 3;

            minX = Math.min(minX, node.guiX - nodeSize);
            minY = Math.min(minY, node.guiY - nodeSize);
            maxX = Math.max(maxX, (node.guiX + nodeSize + contentWidth / 2) - contentWidth);
            maxY = Math.max(maxY, (node.guiY + nodeSize + contentHeight / 2) - contentHeight);
        }

    }

    public BloodlineScreen(List<BloodlineData.BloodlineEntry> entries) {
        super(Component.literal("Bloodline"));
        this.nodes = new ArrayList<>();

        // test bloodline
        BloodlineData.BloodlineEntry first = entries.getFirst();
        BloodlineData.BloodlineEntry second = new BloodlineData.BloodlineEntry(UUID.randomUUID(), first.id(), first.depth() + 1, "Dev2");
        entries.add(second);
        BloodlineData.BloodlineEntry another =
                new BloodlineData.BloodlineEntry(UUID.fromString("bfd8bbe6-1e10-480b-b987-97b69d8a2a98"), first.id(), first.depth() + 1, "Sepsis");
        entries.add(another);
        entries.add(new BloodlineData.BloodlineEntry(UUID.randomUUID(), another.id(), another.depth() + 1, "Dev4"));
        entries.add(new BloodlineData.BloodlineEntry(UUID.randomUUID(), another.id(), another.depth() + 1, "Dev6"));
        another = new BloodlineData.BloodlineEntry(UUID.randomUUID(), another.id(), another.depth() + 1, "Dev5");
        entries.add(another);
        entries.add(new BloodlineData.BloodlineEntry(UUID.randomUUID(), another.id(), another.depth() + 1, "Dev7"));
        entries.add(new BloodlineData.BloodlineEntry(UUID.randomUUID(), another.id(), another.depth() + 1, "Dev8"));
        entries.add(new BloodlineData.BloodlineEntry(UUID.randomUUID(), second.id(), second.depth() + 1, "Dev9"));
        entries.add(new BloodlineData.BloodlineEntry(UUID.randomUUID(), second.id(), second.depth() + 1, "Dev10"));
        // end test bloodline :3

        SkinManager skinManager = Minecraft.getInstance().getSkinManager();

        for (BloodlineData.BloodlineEntry entry : entries) {
            BloodlineNode node = new BloodlineNode();
            node.id = entry.id();
            node.name = entry.name();
            node.parent = entry.parent();
            node.depth = entry.depth();

            nodes.add(node);
            nodeMap.put(node.id, node);
            if (node.parent == null) {
                rootNode = node;
            } else {
                childrenMap.computeIfAbsent(node.parent, k -> new ArrayList<>()).add(node);
            }
            SKIN_EXECUTOR.execute(() -> {
                try {
                    var session = Minecraft.getInstance().getMinecraftSessionService();
                    ProfileResult profileResult = Minecraft.getInstance().getMinecraftSessionService().fetchProfile(node.id, false);
                    if (profileResult != null) {
                        skinManager.getOrLoad(profileResult.profile()).thenAccept(s -> {
                            node.skin = s.texture();
                        });
                    }
                } catch (Exception e) {
                    VampiricLineage.LOGGER.warn("Failed to load skin for " + node.name, e);
                }
            });

        }

        visibleNodes.clear();
        int[] cursor = {0};
        layout(childrenMap, rootNode, cursor);

        for (BloodlineNode node : nodes) {
            visibleNodes.add(node);
            node.guiX = (int)(node.tempX * nodeSize * 2);
            node.guiY = node.depth * nodeSize * 3;

            minX = Math.min(minX, node.guiX - nodeSize);
            minY = Math.min(minY, node.guiY - nodeSize);
            maxX = Math.max(maxX, (node.guiX + nodeSize + contentWidth / 2) - contentWidth);
            maxY = Math.max(maxY, (node.guiY + nodeSize + contentHeight / 2) - contentHeight);
        }
    }

    @Override
    protected void init() {
        super.init();
        windowX = (this.width - windowWidth) / 2;
        windowY = (this.height - windowHeight) / 2 + 20;
        contentX = windowX + 9;
        contentY = windowY + 18;

        backButton = Button.builder(Component.literal("Done"), button -> this.onClose()).bounds(
                windowX + (windowWidth / 3), contentY + contentHeight + 2, windowWidth / 3, 20
        ).build();
        UUID pId = Minecraft.getInstance().player.getUUID();
        for (BloodlineNode node : nodes) {
            if (node.id.equals(pId)) {
                panX = node.guiX - (windowWidth / 2.0);
                panY = (windowHeight / 6.0) - (node.guiY + nodeSize / 2.0);
                minPanX = -maxX * zoom;
                maxPanX = -minX * zoom;
                minPanY = -maxY * zoom;
                maxPanY = -minY * zoom;

                panX = Math.max(minPanX, Math.min(maxPanX, panX));
                panY = Math.max(minPanY, Math.min(maxPanY, panY));
            }
        }

        this.addRenderableWidget(backButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
//        this.renderBackground(gui, mouseX, mouseY, partialTick);
        super.render(gui, mouseX, mouseY, partialTick);


        gui.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);

        gui.pose().pushPose();
        gui.pose().translate(contentX + contentWidth/2f, contentY + contentHeight/2f, 0);
        gui.pose().scale((float) zoom, (float) zoom, 1f);
        gui.pose().translate(-contentWidth/2f,-contentHeight/2f,0);
        gui.pose().translate((float) panX, (float) panY, 0);

        double guiMouseX =
                (mouseX - contentX - contentWidth / 2.0) / zoom - panX + contentWidth / 2.0;

        double guiMouseY =
                (mouseY - contentY - contentHeight / 2.0) / zoom - panY + contentHeight / 2.0;
        selected = null;

        RenderSystem.setShaderTexture(0, BG);

        int size = 24;

        double left = -contentWidth / 2.0 / zoom - panX;
        double right = contentWidth / zoom - panX;
        double top = -contentHeight / 2.0 / zoom - panY;
        double bottom = contentHeight / zoom - panY;

        int startX = (int)Math.floor(left / size) * size;
        int startY = (int)Math.floor(top / size) * size;
        int endX = (int)Math.ceil(right / size) * size;
        int endY = (int)Math.ceil(bottom / size) * size;

        boolean mouseInContent = (mouseX >= contentX && mouseX <= contentX + contentWidth
            && mouseY >= contentY && mouseY <= contentY + contentHeight);

        for (int x = startX; x < endX + size * 2; x += size) {
            for (int y = startY; y < endY + size * 2; y += size) {
                gui.blit(BG, x, y, 0, 0, size, size, size, size);
            }
        }

        for (BloodlineNode node : visibleNodes) {
            int x = node.guiX;
            int y = node.guiY;

            if (node.parent != null) {
                BloodlineNode parent = nodeMap.get(node.parent);
                if (parent != null) {
                    int x1 = parent.guiX + nodeSize / 2;
                    int y1 = parent.guiY + nodeSize - 4;
                    int x2 = x + nodeSize / 2;
                    int y2 = y + 4;
                    int midY = (y1 + y2) / 2;

                    gui.vLine(x1 - 1, y1, midY, 0xFF635F5F);
                    gui.hLine(Math.min(x1, x2), Math.max(x1, x2), midY - 1, 0xFF635F5F);
                    gui.vLine(x2 - 1, midY - 1, y2, 0xFF635F5F);

                    gui.vLine(x1 + 1, y1, midY, 0xFF635F5F);
                    gui.hLine(Math.min(x1, x2), Math.max(x1, x2), midY + 1, 0xFF635F5F);
                    gui.vLine(x2 + 1, midY - 1, y2, 0xFF635F5F);

                    gui.vLine(x1, y1, midY, 0xFF641D5C);
                    gui.hLine(Math.min(x1, x2), Math.max(x1, x2), midY, 0xFF641D5C);
                    gui.vLine(x2, midY, y2, 0xFF641D5C);

                }
            }
        }

        for (BloodlineNode node : visibleNodes) {
            int x = node.guiX;
            int y = node.guiY;

            boolean isPlayerNode = node.id.equals(Minecraft.getInstance().player.getUUID());
            if (node.parent == null) {
                RenderSystem.setShaderColor((float)100/255, (float)29/255, (float)92/255, 1f);
            } else if (isPlayerNode) {
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            } else if (!node.expanded) {
                RenderSystem.setShaderColor(1f, 0f, 0f, 1f);
            } else {
                RenderSystem.setShaderColor(0.38f, 0.38f, 0.38f, 1f);
            }
            gui.blit((node.parent == null || isPlayerNode)
                    ? START_NODE : NODE, x, y, 0, 0, nodeSize, nodeSize, nodeSize, nodeSize);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            var connection = Minecraft.getInstance().getConnection();
            var info = connection != null ? connection.getPlayerInfo(node.id) : null;

            ResourceLocation skin = node.skin != null
                    ? node.skin
                    : DefaultPlayerSkin.getDefaultTexture();

            gui.blit(skin, x+6, y+6, nodeSize-12, nodeSize-12, 8, 8, 8, 8, 64, 64);
            gui.blit(skin, x+6, y+6, nodeSize-12, nodeSize-12, 40, 8, 8, 8, 64, 64);

            if (mouseInContent && guiMouseX >= x && guiMouseX <= x + nodeSize && guiMouseY >= y && guiMouseY <= y + nodeSize) {
                selected = node;
            }
        }


        gui.pose().popPose();
        gui.disableScissor();

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, WINDOW);
        gui.blit(WINDOW, windowX, windowY, 0, 0, windowWidth, windowHeight, 256, 256);
        RenderSystem.disableBlend();

        if (selected != null) {
            gui.renderTooltip(this.font, Component.literal(selected.name), mouseX, mouseY);
        }

        backButton.render(gui, mouseX, mouseY, partialTick);
        Component title = Component.literal(rootNode.name).withColor(0xFF641D5C)
                        .append(Component.literal("'s Bloodline").withColor(0xFF000000));
        gui.drawString(this.font, title, windowX + 8, windowY + 6, 0xFF000000, false);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(gui, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && selected != null && !selected.equals(rootNode)) {
            List<BloodlineNode> children = childrenMap.get(selected.id);
            if (children != null && !children.isEmpty()) {
                selected.expanded = !selected.expanded;
                rebuildLayout();
            }
            return true;
        }
        return  super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            panX += dragX / zoom;
            panY += dragY / zoom;

            minPanX = -maxX * zoom;
            maxPanX = -minX * zoom;
            minPanY = -maxY * zoom;
            maxPanY = -minY * zoom;

            panX = Math.max(minPanX, Math.min(maxPanX, panX));
            panY = Math.max(minPanY, Math.min(maxPanY, panY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        zoom += scrollY * 0.1;
        zoom = Math.max(0.3, Math.min(1.5, zoom));
        return true;
    }
}
