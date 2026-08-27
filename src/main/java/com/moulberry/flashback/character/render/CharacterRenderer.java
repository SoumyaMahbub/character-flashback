package com.moulberry.flashback.character.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.character.AnimatedCharacter;
import com.moulberry.flashback.character.CharacterManager;
import com.moulberry.flashback.character.CharacterPose;
import com.moulberry.flashback.state.EditorScene;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class CharacterRenderer {

    private static PlayerModel wideModel = null;
    private static PlayerModel slimModel = null;

    private static void initModelsIfNeeded() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getEntityModels() == null) {
            return;
        }

        if (wideModel == null) {
            ModelPart wideRoot = minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER);
            wideModel = new PlayerModel(wideRoot, false);
        }
        if (slimModel == null) {
            ModelPart slimRoot = minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM);
            slimModel = new PlayerModel(slimRoot, true);
        }
    }

    public static void renderCharacters(PoseStack poseStack, Camera camera, float replayTick) {
        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState == null) {
            return;
        }

        long stamp = editorState.acquireRead();
        EditorScene currentScene;
        try {
            currentScene = editorState.getCurrentScene(stamp);
        } finally {
            editorState.release(stamp);
        }

        if (currentScene == null || currentScene.characterManager == null) {
            return;
        }

        CharacterManager characterManager = currentScene.characterManager;
        if (characterManager.getCharacters().isEmpty()) {
            return;
        }

        initModelsIfNeeded();
        if (wideModel == null || slimModel == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Vec3 cameraPos = camera.getPosition();

        com.moulberry.flashback.playback.ReplayServer replayServer = Flashback.getReplayServer();
        boolean isPlaying = Flashback.isExporting() || (replayServer != null && !replayServer.replayPaused);

        for (AnimatedCharacter character : characterManager.getCharacters()) {
            if (!character.isVisible()) {
                continue;
            }

            character.evaluate(replayTick, isPlaying);

            PlayerSkin skin = character.getSkin().getOrLoadSkin();
            boolean isSlim = character.getSkin().isSlimModel();
            PlayerModel model = isSlim ? slimModel : wideModel;

            // Reset all model part transforms to initial baked pose baseline
            model.resetPose();

            // Apply outer layer visibility
            model.hat.visible = character.isHatVisible();
            model.jacket.visible = character.isJacketVisible();
            model.leftSleeve.visible = character.isLeftSleeveVisible();
            model.rightSleeve.visible = character.isRightSleeveVisible();
            model.leftPants.visible = character.isLeftPantsVisible();
            model.rightPants.visible = character.isRightPantsVisible();

            // Apply limb rotations directly from evaluated pose (degrees converted to radians)
            CharacterPose pose = character.getEvaluatedPose();

            // Body / Torso
            model.body.xRot = (float) Math.toRadians(pose.bodyPitch);
            model.body.yRot = (float) Math.toRadians(pose.bodyYaw);
            model.body.zRot = (float) Math.toRadians(pose.bodyRoll);

            // Head (relative to neck)
            model.head.xRot = (float) Math.toRadians(pose.headPitch);
            model.head.yRot = (float) Math.toRadians(pose.headYaw);
            model.head.zRot = (float) Math.toRadians(pose.headRoll);

            // Left Arm
            model.leftArm.xRot = (float) Math.toRadians(pose.leftArmPitch);
            model.leftArm.yRot = (float) Math.toRadians(pose.leftArmYaw);
            model.leftArm.zRot = (float) Math.toRadians(pose.leftArmRoll);

            // Right Arm
            model.rightArm.xRot = (float) Math.toRadians(pose.rightArmPitch);
            model.rightArm.yRot = (float) Math.toRadians(pose.rightArmYaw);
            model.rightArm.zRot = (float) Math.toRadians(pose.rightArmRoll);

            // Left Leg
            model.leftLeg.xRot = (float) Math.toRadians(pose.leftLegPitch);
            model.leftLeg.yRot = (float) Math.toRadians(pose.leftLegYaw);
            model.leftLeg.zRot = (float) Math.toRadians(pose.leftLegRoll);

            // Right Leg
            model.rightLeg.xRot = (float) Math.toRadians(pose.rightLegPitch);
            model.rightLeg.yRot = (float) Math.toRadians(pose.rightLegYaw);
            model.rightLeg.zRot = (float) Math.toRadians(pose.rightLegRoll);

            // World transform positioning
            poseStack.pushPose();
            poseStack.translate(
                    character.getEvalPosX() - cameraPos.x,
                    character.getEvalPosY() - cameraPos.y,
                    character.getEvalPosZ() - cameraPos.z
            );

            poseStack.mulPose(Axis.YP.rotationDegrees(-character.getEvalRotYaw()));
            poseStack.mulPose(Axis.XP.rotationDegrees(character.getEvalRotPitch()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(character.getEvalRotRoll()));

            poseStack.scale(character.getEvalScaleX(), character.getEvalScaleY(), character.getEvalScaleZ());

            // Orient model with feet at origin (0, 0, 0)
            poseStack.scale(-1.0f, -1.0f, 1.0f);
            poseStack.translate(0.0, -1.501, 0.0);

            // Lighting computation at character position
            int light = LevelRenderer.getLightColor(minecraft.level, BlockPos.containing(
                    character.getEvalPosX(),
                    character.getEvalPosY() + 1.0,
                    character.getEvalPosZ()
            ));

            RenderType renderType = RenderType.entityCutoutNoCull(skin.texture());
            VertexConsumer buffer = bufferSource.getBuffer(renderType);

            model.renderToBuffer(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
            bufferSource.endBatch(renderType);

            poseStack.popPose();
        }
    }
}
