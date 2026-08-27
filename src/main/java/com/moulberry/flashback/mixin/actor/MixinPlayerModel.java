package com.moulberry.flashback.mixin.actor;

import com.moulberry.flashback.actor.ActorPose;
import com.moulberry.flashback.actor.ActorRenderStateDuck;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class MixinPlayerModel {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At("RETURN"))
    public void flashback$onSetupAnim(PlayerRenderState state, CallbackInfo ci) {
        if (state instanceof ActorRenderStateDuck duck) {
            ActorPose pose = duck.flashback$getActorPose();
            if (pose != null) {
                PlayerModel model = (PlayerModel) (Object) this;

                model.body.xRot += (float) Math.toRadians(pose.bodyPitch);
                model.body.yRot += (float) Math.toRadians(pose.bodyYaw);
                model.body.zRot += (float) Math.toRadians(pose.bodyRoll);

                model.head.xRot += (float) Math.toRadians(pose.headPitch);
                model.head.yRot += (float) Math.toRadians(pose.headYaw);
                model.head.zRot += (float) Math.toRadians(pose.headRoll);

                model.leftArm.xRot += (float) Math.toRadians(pose.leftArmPitch);
                model.leftArm.yRot += (float) Math.toRadians(pose.leftArmYaw);
                model.leftArm.zRot += (float) Math.toRadians(pose.leftArmRoll);

                model.rightArm.xRot += (float) Math.toRadians(pose.rightArmPitch);
                model.rightArm.yRot += (float) Math.toRadians(pose.rightArmYaw);
                model.rightArm.zRot += (float) Math.toRadians(pose.rightArmRoll);

                model.leftLeg.xRot += (float) Math.toRadians(pose.leftLegPitch);
                model.leftLeg.yRot += (float) Math.toRadians(pose.leftLegYaw);
                model.leftLeg.zRot += (float) Math.toRadians(pose.leftLegRoll);

                model.rightLeg.xRot += (float) Math.toRadians(pose.rightLegPitch);
                model.rightLeg.yRot += (float) Math.toRadians(pose.rightLegYaw);
                model.rightLeg.zRot += (float) Math.toRadians(pose.rightLegRoll);
            }
        }
    }
}
