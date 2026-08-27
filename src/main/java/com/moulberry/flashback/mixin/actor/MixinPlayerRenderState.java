package com.moulberry.flashback.mixin.actor;

import com.moulberry.flashback.actor.ActorPose;
import com.moulberry.flashback.actor.ActorRenderStateDuck;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class MixinPlayerRenderState implements ActorRenderStateDuck {

    @Unique
    private ActorPose flashbackActorPose = null;

    @Override
    public ActorPose flashback$getActorPose() {
        return this.flashbackActorPose;
    }

    @Override
    public void flashback$setActorPose(ActorPose pose) {
        this.flashbackActorPose = pose;
    }
}
