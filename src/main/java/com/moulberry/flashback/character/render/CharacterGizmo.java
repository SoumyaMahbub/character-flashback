package com.moulberry.flashback.character.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.moulberry.flashback.character.AnimatedCharacter;
import com.moulberry.flashback.character.CharacterManager;
import com.moulberry.flashback.editor.ui.ReplayUI;
import com.moulberry.flashback.state.EditorScene;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import com.moulberry.flashback.visuals.Shapes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

public class CharacterGizmo {

    public static void renderGizmos(PoseStack poseStack, Camera camera) {
        if (!ReplayUI.isActive()) {
            return;
        }

        EditorState editorState = EditorStateManager.getCurrent();
        if (editorState == null) return;

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

        CharacterManager manager = currentScene.characterManager;
        AnimatedCharacter selected = manager.getSelectedCharacter();
        if (selected == null || !selected.isVisible()) {
            return;
        }

        Vec3 cameraPos = camera.getPosition();
        float posX = selected.getEvalPosX();
        float posY = selected.getEvalPosY();
        float posZ = selected.getEvalPosZ();

        poseStack.pushPose();
        poseStack.translate(posX - cameraPos.x, posY - cameraPos.y, posZ - cameraPos.z);

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        PoseStack.Pose lastPose = poseStack.last();

        // 1. Draw Bounding Box around selected character
        float minX = -0.35f * selected.getEvalScaleX();
        float maxX = 0.35f * selected.getEvalScaleX();
        float minY = 0.0f;
        float maxY = 1.9f * selected.getEvalScaleY();
        float minZ = -0.35f * selected.getEvalScaleZ();
        float maxZ = 0.35f * selected.getEvalScaleZ();

        float boxR = 0.2f, boxG = 0.8f, boxB = 1.0f, boxA = 0.85f;

        // Bottom quad
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, minX, minY, minZ, maxX, minY, minZ);
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, maxX, minY, minZ, maxX, minY, maxZ);
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, maxX, minY, maxZ, minX, minY, maxZ);
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, minX, minY, maxZ, minX, minY, minZ);

        // Top quad
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, minX, maxY, minZ, maxX, maxY, minZ);
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, maxX, maxY, minZ, maxX, maxY, maxZ);
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, maxX, maxY, maxZ, minX, maxY, maxZ);
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, minX, maxY, maxZ, minX, maxY, minZ);

        // Vertical pillars
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, minX, minY, minZ, minX, maxY, minZ);
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, maxX, minY, minZ, maxX, maxY, minZ);
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, maxX, minY, maxZ, maxX, maxY, maxZ);
        Shapes.line(bufferBuilder, lastPose, boxR, boxG, boxB, boxA, minX, minY, maxZ, minX, maxY, maxZ);

        // 2. Draw 3D Transform Gizmo Axes (X=Red, Y=Green, Z=Blue)
        float axisLen = 1.2f;

        // X-axis (Red)
        Shapes.line(bufferBuilder, lastPose, 1.0f, 0.1f, 0.1f, 1.0f, 0.0f, 0.05f, 0.0f, axisLen, 0.05f, 0.0f);
        Shapes.line(bufferBuilder, lastPose, 1.0f, 0.1f, 0.1f, 1.0f, axisLen, 0.05f, 0.0f, axisLen - 0.15f, 0.15f, 0.0f);
        Shapes.line(bufferBuilder, lastPose, 1.0f, 0.1f, 0.1f, 1.0f, axisLen, 0.05f, 0.0f, axisLen - 0.15f, -0.05f, 0.0f);

        // Y-axis (Green)
        Shapes.line(bufferBuilder, lastPose, 0.1f, 1.0f, 0.1f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, axisLen, 0.0f);
        Shapes.line(bufferBuilder, lastPose, 0.1f, 1.0f, 0.1f, 1.0f, 0.0f, axisLen, 0.0f, 0.1f, axisLen - 0.15f, 0.0f);
        Shapes.line(bufferBuilder, lastPose, 0.1f, 1.0f, 0.1f, 1.0f, 0.0f, axisLen, 0.0f, -0.1f, axisLen - 0.15f, 0.0f);

        // Z-axis (Blue)
        Shapes.line(bufferBuilder, lastPose, 0.2f, 0.4f, 1.0f, 1.0f, 0.0f, 0.05f, 0.0f, 0.0f, 0.05f, axisLen);
        Shapes.line(bufferBuilder, lastPose, 0.2f, 0.4f, 1.0f, 1.0f, 0.0f, 0.05f, axisLen, 0.0f, 0.15f, axisLen - 0.15f);
        Shapes.line(bufferBuilder, lastPose, 0.2f, 0.4f, 1.0f, 1.0f, 0.0f, 0.05f, axisLen, 0.0f, -0.05f, axisLen - 0.15f);

        MeshData meshData = bufferBuilder.build();
        if (meshData != null) {
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(CoreShaders.POSITION_COLOR);
            BufferUploader.drawWithShader(meshData);
            RenderSystem.enableDepthTest();
        }

        poseStack.popPose();
    }

    public static UUID raycastCharacter(Vec3 rayOrigin, Vec3 rayDirection, CharacterManager manager) {
        if (manager == null || manager.getCharacters().isEmpty()) return null;

        UUID closestId = null;
        double closestDist = Double.MAX_VALUE;

        for (AnimatedCharacter character : manager.getCharacters()) {
            if (!character.isVisible()) continue;

            AABB box = new AABB(
                    character.getEvalPosX() - 0.4, character.getEvalPosY(), character.getEvalPosZ() - 0.4,
                    character.getEvalPosX() + 0.4, character.getEvalPosY() + 1.9, character.getEvalPosZ() + 0.4
            );

            var hit = box.clip(rayOrigin, rayOrigin.add(rayDirection.scale(100.0)));
            if (hit.isPresent()) {
                double dist = rayOrigin.distanceToSqr(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closestId = character.getId();
                }
            }
        }

        return closestId;
    }
}
