package com.fruityjourney.vampiriclineage.mixin;

import de.teamlapen.vampirism.entity.player.FactionBasePlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FactionBasePlayer.class)
public interface FactionBasePlayerAccessor {
    @Accessor("player")
    Player getPlayer();
}
