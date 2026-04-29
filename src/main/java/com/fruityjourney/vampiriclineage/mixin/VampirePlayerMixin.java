package com.fruityjourney.vampiriclineage.mixin;

import com.fruityjourney.vampiriclineage.BloodlineData;
import com.fruityjourney.vampiriclineage.InfectionData;
import com.fruityjourney.vampiriclineage.VampiricLineage;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(VampirePlayer.class)
public class VampirePlayerMixin {

    @Inject(method="tryInfect",at=@At("HEAD"))
    private void onTryInfect(IVampire infector, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = infector.asEntity();
        if (entity.level() instanceof ServerLevel level) {
            Player player = ((FactionBasePlayerAccessor)(Object)this).getPlayer();
            UUID playerId = player.getUUID();
            UUID infectorId = entity.getUUID();

            if (!(entity instanceof Player)) {
                UUID entityId = VampiricLineage.ENTITY_UUIDS.get(entity.getName().getString());
                if (entityId != null) {
                    infectorId = entityId;
                } else {return;}
            }
            // add infector id to infected map
            InfectionData infections = InfectionData.get(level);
            infections.setInfected(playerId, infectorId);
        }
    }

    @Inject(method="onSanguinareFinished",at=@At("TAIL"))
    private void onSanguinareFinished(CallbackInfo ci) {
        Player player = ((FactionBasePlayerAccessor)(Object)this).getPlayer();
        if (player.level() instanceof ServerLevel level) {
            UUID playerId = player.getUUID();
            InfectionData infections = InfectionData.get(level);

            UUID infectorId = infections.getInfector(playerId);
            if (infectorId != null) {
                BloodlineData bloodlines = BloodlineData.get(level);
                bloodlines.addLink(playerId, infectorId);
                infections.remove(playerId);
            }
        }
    }

}
