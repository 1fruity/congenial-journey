package com.fruityjourney.vampiriclineage.mixin;

import com.fruityjourney.vampiriclineage.BloodlineData;
import com.fruityjourney.vampiriclineage.InfectionData;
import com.fruityjourney.vampiriclineage.VampiricLineage;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(FactionPlayerHandler.class)
public class FactionPlayerHandlerMixin {
    @Final
    @Shadow private Player player;

    @Inject(method="setFactionAndLevel",at=@At("HEAD"))
    private void onSetFactionAndLevel(@Nullable IPlayableFaction<?> faction, int l, CallbackInfoReturnable<Boolean> cir) {
        if (faction == null) {
            if (player.level() instanceof ServerLevel level) {
                UUID playerId = player.getUUID();
                BloodlineData bloodlines = BloodlineData.get(level);
                bloodlines.removeOrDeactivate(playerId);
            }
        }
    }
}
