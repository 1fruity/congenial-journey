package com.fruityjourney.vampiriclineage;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class BloodlineCommand {

    public static void printTree(UUID player, BloodlineData data, int depth, CommandSourceStack source) {
        String indent = " ".repeat(depth);
        Component line = Component.literal(indent + player);
        source.sendSystemMessage(line);
        for (UUID child : data.getChildren(player)) {
            printTree(child, data, depth + 1, source);
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("bloodline")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {

                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    ServerLevel level = ctx.getSource().getLevel();

                                    BloodlineData data = BloodlineData.get(level);

                                    UUID targetId = target.getUUID();

                                    UUID root = BloodlineData.getRoot(targetId, data);

                                    ctx.getSource().sendSystemMessage(
                                            Component.literal("Bloodline root: " + root)
                                    );

                                    printTree(root, data, 0, ctx.getSource());

                                    return 1;
                                })
                        )
        );
    }
}