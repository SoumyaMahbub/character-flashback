package com.moulberry.flashback.mixin.actor;

import com.moulberry.flashback.actor.ActorPlayer;
import com.moulberry.flashback.actor.ActorRenderStateDuck;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class MixinPlayerRenderer {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("RETURN"))
    public void flashback$onExtractRenderState(AbstractClientPlayer entity, PlayerRenderState state, float partialTick, CallbackInfo ci) {
        if (entity instanceof ActorPlayer actorPlayer) {
            ((ActorRenderStateDuck) state).flashback$setActorPose(actorPlayer.getActor().getEvaluatedPose());
        }
    }
}
