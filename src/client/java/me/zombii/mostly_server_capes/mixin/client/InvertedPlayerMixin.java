package me.zombii.mostly_server_capes.mixin.client;

import me.zombii.mostly_server_capes.Constants;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public class InvertedPlayerMixin {

    public InvertedPlayerMixin() {
    }

    @Inject(
            method = {"shouldFlipUpsideDown"},
            at = {@At("RETURN")},
            cancellable = true
    )
    private static void modifyShouldFlipUpsideDown(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Constants.UPSIDE_DOWN_PLAYERS.contains(entity.getUuid().toString()));
    }

}