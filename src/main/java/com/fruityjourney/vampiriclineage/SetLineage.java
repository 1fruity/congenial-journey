package com.fruityjourney.vampiriclineage;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SetLineage {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("setlineage")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("parent", EntityArgument.player())
                                        .executes(ctx -> {

                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            ServerPlayer parent = EntityArgument.getPlayer(ctx, "parent");
                                            ServerLevel level = ctx.getSource().getLevel();

                                            BloodlineData data = BloodlineData.get(level);

                                            UUID targetId = target.getUUID();
                                            BloodlineData.Node infectorNode = data.getActiveNode(parent.getUUID());
                                            if (infectorNode == null) {
                                                infectorNode = data.addNode(parent.getUUID(), null);
                                            }
                                            data.addNode(targetId, infectorNode.nodeId);
                                            String sMsg = "Set " + target.getName().getString() + "'s Lineage to "
                                                    + parent.getName().getString();
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal(sMsg), false
                                            );

                                            return 1;
                                        })
                                )
                        )
        );
    }
}