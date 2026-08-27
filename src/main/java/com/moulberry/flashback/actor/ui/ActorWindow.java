package com.moulberry.flashback.actor.ui;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.actor.ActorAnimationClip;
import com.moulberry.flashback.actor.ActorAnimationTrack;
import com.moulberry.flashback.actor.ActorManager;
import com.moulberry.flashback.actor.ActorPose;
import com.moulberry.flashback.actor.ActorPosePresets;
import com.moulberry.flashback.actor.ActorSkin;
import com.moulberry.flashback.actor.ActorTrackType;
import com.moulberry.flashback.actor.FlashbackActor;
import com.moulberry.flashback.exporting.AsyncFileDialogs;
import com.moulberry.flashback.editor.ui.ImGuiHelper;
import com.moulberry.flashback.editor.ui.ReplayUI;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import com.moulberry.flashback.playback.ReplayServer;
import com.moulberry.flashback.state.EditorScene;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import imgui.moulberry90.ImGui;
import imgui.moulberry90.ImGuiViewport;
import imgui.moulberry90.flag.ImGuiCond;
import imgui.moulberry90.flag.ImGuiTreeNodeFlags;
import imgui.moulberry90.flag.ImGuiWindowFlags;
import imgui.moulberry90.type.ImBoolean;
import imgui.moulberry90.type.ImString;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ActorWindow {

    private static final ImString actorNameInput = new ImString(64);
    private static final ImString skinUsernameInput = new ImString(64);
    private static final ImString newPoseNameInput = new ImString(64);
    private static int selectedPresetIndex = 0;
    private static InterpolationType selectedInterpolation = InterpolationType.LINEAR;

    private static final float[] floatBuffer = new float[1];
    private static final float[] floatBuffer3 = new float[3];
    private static final int[] bakeStartTick = new int[]{0};
    private static final int[] bakeEndTick = new int[]{100};
    private static final int[] bakeStepTicks = new int[]{2};

    public static void render(ImBoolean open, boolean newlyOpened) {
        if (newlyOpened) {
            ImGuiViewport viewport = ImGui.getMainViewport();
            ImGui.setNextWindowPos(viewport.getCenterX(), viewport.getCenterY(), ImGuiCond.Appearing, 0.5f, 0.5f);
        }

        ImGui.setNextWindowSizeConstraints(320, 200, 5000, 5000);
        int flags = ImGuiWindowFlags.NoFocusOnAppearing;

        if (ImGui.begin("Actors", open, flags)) {
            EditorState editorState = EditorStateManager.getCurrent();
            if (editorState != null) {
                long stamp = editorState.acquireRead();
                EditorScene scene;
                try {
                    scene = editorState.getCurrentScene(stamp);
                } finally {
                    editorState.release(stamp);
                }

                if (scene != null) {
                    ReplayServer replayServer = Flashback.getReplayServer();
                    int tick = replayServer != null ? (int) Math.floor(replayServer.getPartialReplayTick()) : 0;
                    render(scene, tick, editorState);
                } else {
                    ImGui.textDisabled("No active scene.");
                }
            }
        }
        ImGui.end();
    }

    public static void render(EditorScene editorScene, int tick, EditorState editorState) {
        if (editorScene == null) {
            ImGui.textDisabled("No active scene.");
            return;
        }

        ActorManager manager = editorScene.actorManager;
        if (manager == null) {
            editorScene.actorManager = new ActorManager();
            manager = editorScene.actorManager;
        }

        // Top Toolbar
        if (ImGui.button("+ Add Actor")) {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            org.joml.Vector3f look = camera.getLookVector();
            Vec3 pos = camera.getPosition().add((double) look.x() * 3.5, (double) look.y() * 3.5, (double) look.z() * 3.5);
            Vector3f spawnPos = new Vector3f((float) pos.x, (float) pos.y, (float) pos.z);
            FlashbackActor actor = manager.addActor(null, spawnPos, tick);
            actor.setRotation(0.0f, -camera.getYRot(), 0.0f);
            actor.insertAllKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }

        ImGui.sameLine();
        FlashbackActor selectedActor = manager.getSelectedActor();
        if (selectedActor != null) {
            if (ImGui.button("Duplicate")) {
                manager.duplicateActor(selectedActor.getId());
                editorState.markDirty();
            }
            ImGui.sameLine();
            if (ImGui.button("Delete")) {
                manager.removeActor(selectedActor.getId());
                editorState.markDirty();
            }
        }

        ImGui.separator();

        List<FlashbackActor> actors = manager.getActors();
        if (actors.isEmpty()) {
            ImGui.textWrapped("No actors in this scene. Click '+ Add Actor' above to spawn a customizable actor.");
            return;
        }

        // Actor Dropdown Selector
        String currentActorName = selectedActor != null ? selectedActor.getName() : "None";
        if (ImGui.beginCombo("Selected Actor##ActorCombo", currentActorName)) {
            for (FlashbackActor actor : actors) {
                boolean isSel = (selectedActor != null && actor.getId().equals(selectedActor.getId()));
                if (ImGui.selectable(actor.getName() + "##" + actor.getId(), isSel)) {
                    manager.setSelectedActorId(actor.getId());
                }
            }
            ImGui.endCombo();
        }

        selectedActor = manager.getSelectedActor();
        if (selectedActor == null) {
            return;
        }

        // Actor Header Settings (Name & Visibility)
        ImGui.setNextItemWidth(ReplayUI.scaleUi(160));
        if (actorNameInput.isEmpty() || !actorNameInput.get().equals(selectedActor.getName())) {
            actorNameInput.set(selectedActor.getName());
        }
        if (ImGui.inputText("Name##ActorName", actorNameInput)) {
            selectedActor.setName(ImGuiHelper.getString(actorNameInput));
            editorState.markDirty();
        }

        ImGui.sameLine();
        boolean vis = selectedActor.isVisible();
        if (ImGui.checkbox("Visible in Scene", vis)) {
            selectedActor.setVisible(!vis);
            editorState.markDirty();
        }

        ImGui.separator();

        // Tabs
        if (ImGui.beginTabBar("ActorTabs")) {
            if (ImGui.beginTabItem("Transform")) {
                renderTransformTab(selectedActor, tick, editorState);
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Pose & Limbs")) {
                renderPoseTab(selectedActor, tick, editorState);
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Animation Loop")) {
                renderClipTab(selectedActor, tick, editorState);
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Skin & Model")) {
                renderSkinTab(selectedActor, editorState);
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Keyframes")) {
                renderKeyframesTab(selectedActor, tick, editorState);
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
    }

    private static void renderTransformTab(FlashbackActor actor, int tick, EditorState editorState) {
        ImGuiHelper.separatorWithText("Position (X / Y / Z)");
        floatBuffer3[0] = actor.getEvalPosX();
        floatBuffer3[1] = actor.getEvalPosY();
        floatBuffer3[2] = actor.getEvalPosZ();
        if (ImGui.dragFloat3("##Position", floatBuffer3, 0.05f)) {
            actor.setPosition(new Vector3f(floatBuffer3[0], floatBuffer3[1], floatBuffer3[2]));
            actor.setKeyframe(ActorTrackType.WORLD_POS_X, tick, floatBuffer3[0], selectedInterpolation);
            actor.setKeyframe(ActorTrackType.WORLD_POS_Y, tick, floatBuffer3[1], selectedInterpolation);
            actor.setKeyframe(ActorTrackType.WORLD_POS_Z, tick, floatBuffer3[2], selectedInterpolation);
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("Rotation (Pitch / Yaw / Roll)");
        floatBuffer3[0] = actor.getEvalRotPitch();
        floatBuffer3[1] = actor.getEvalRotYaw();
        floatBuffer3[2] = actor.getEvalRotRoll();
        if (ImGui.dragFloat3("##Rotation", floatBuffer3, 0.5f, -360.0f, 360.0f)) {
            actor.setRotation(floatBuffer3[0], floatBuffer3[1], floatBuffer3[2]);
            actor.setKeyframe(ActorTrackType.WORLD_ROT_PITCH, tick, floatBuffer3[0], selectedInterpolation);
            actor.setKeyframe(ActorTrackType.WORLD_ROT_YAW, tick, floatBuffer3[1], selectedInterpolation);
            actor.setKeyframe(ActorTrackType.WORLD_ROT_ROLL, tick, floatBuffer3[2], selectedInterpolation);
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("Scale (X / Y / Z)");
        floatBuffer3[0] = actor.getEvalScaleX();
        floatBuffer3[1] = actor.getEvalScaleY();
        floatBuffer3[2] = actor.getEvalScaleZ();
        if (ImGui.dragFloat3("##Scale", floatBuffer3, 0.01f, 0.01f, 50.0f)) {
            actor.setScale(floatBuffer3[0], floatBuffer3[1], floatBuffer3[2]);
            actor.setKeyframe(ActorTrackType.WORLD_SCALE_X, tick, floatBuffer3[0], selectedInterpolation);
            actor.setKeyframe(ActorTrackType.WORLD_SCALE_Y, tick, floatBuffer3[1], selectedInterpolation);
            actor.setKeyframe(ActorTrackType.WORLD_SCALE_Z, tick, floatBuffer3[2], selectedInterpolation);
            editorState.markDirty();
        }

        ImGui.spacing();
        if (ImGui.button("Key Transform at Tick " + tick)) {
            actor.insertAllTransformKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }
    }

    private static void renderPoseTab(FlashbackActor actor, int tick, EditorState editorState) {
        ActorPose pose = actor.getBasePose();

        ImGuiHelper.separatorWithText("Pose Preset Library");
        List<ActorPose> presets = ActorPosePresets.getPresets();
        if (selectedPresetIndex < 0 || selectedPresetIndex >= presets.size()) {
            selectedPresetIndex = 0;
        }
        String currentPresetName = presets.get(selectedPresetIndex).name;
        if (ImGui.beginCombo("Preset##PresetCombo", currentPresetName)) {
            for (int i = 0; i < presets.size(); i++) {
                boolean isSel = (i == selectedPresetIndex);
                if (ImGui.selectable(presets.get(i).name, isSel)) {
                    selectedPresetIndex = i;
                }
            }
            ImGui.endCombo();
        }
        ImGui.sameLine();
        if (ImGui.button("Apply Preset")) {
            actor.applyPose(presets.get(selectedPresetIndex));
            actor.insertAllPoseKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("Limb Rotations (Pitch / Yaw / Roll)");

        // Head
        if (ImGui.collapsingHeader("Head", ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.headPitch;
            floatBuffer3[1] = pose.headYaw;
            floatBuffer3[2] = pose.headRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##Head", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.headPitch = floatBuffer3[0];
                pose.headYaw = floatBuffer3[1];
                pose.headRoll = floatBuffer3[2];
                actor.setKeyframe(ActorTrackType.HEAD_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.HEAD_YAW, tick, floatBuffer3[1], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.HEAD_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Torso
        if (ImGui.collapsingHeader("Torso", ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.bodyPitch;
            floatBuffer3[1] = pose.bodyYaw;
            floatBuffer3[2] = pose.bodyRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##Torso", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.bodyPitch = floatBuffer3[0];
                pose.bodyYaw = floatBuffer3[1];
                pose.bodyRoll = floatBuffer3[2];
                actor.setKeyframe(ActorTrackType.BODY_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.BODY_YAW, tick, floatBuffer3[1], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.BODY_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Left Arm
        if (ImGui.collapsingHeader("Left Arm", ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.leftArmPitch;
            floatBuffer3[1] = pose.leftArmYaw;
            floatBuffer3[2] = pose.leftArmRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##LArm", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.leftArmPitch = floatBuffer3[0];
                pose.leftArmYaw = floatBuffer3[1];
                pose.leftArmRoll = floatBuffer3[2];
                actor.setKeyframe(ActorTrackType.LEFT_ARM_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.LEFT_ARM_YAW, tick, floatBuffer3[1], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.LEFT_ARM_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Right Arm
        if (ImGui.collapsingHeader("Right Arm", ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.rightArmPitch;
            floatBuffer3[1] = pose.rightArmYaw;
            floatBuffer3[2] = pose.rightArmRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##RArm", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.rightArmPitch = floatBuffer3[0];
                pose.rightArmYaw = floatBuffer3[1];
                pose.rightArmRoll = floatBuffer3[2];
                actor.setKeyframe(ActorTrackType.RIGHT_ARM_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.RIGHT_ARM_YAW, tick, floatBuffer3[1], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.RIGHT_ARM_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Left Leg
        if (ImGui.collapsingHeader("Left Leg", ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.leftLegPitch;
            floatBuffer3[1] = pose.leftLegYaw;
            floatBuffer3[2] = pose.leftLegRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##LLeg", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.leftLegPitch = floatBuffer3[0];
                pose.leftLegYaw = floatBuffer3[1];
                pose.leftLegRoll = floatBuffer3[2];
                actor.setKeyframe(ActorTrackType.LEFT_LEG_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.LEFT_LEG_YAW, tick, floatBuffer3[1], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.LEFT_LEG_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Right Leg
        if (ImGui.collapsingHeader("Right Leg", ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.rightLegPitch;
            floatBuffer3[1] = pose.rightLegYaw;
            floatBuffer3[2] = pose.rightLegRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##RLeg", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.rightLegPitch = floatBuffer3[0];
                pose.rightLegYaw = floatBuffer3[1];
                pose.rightLegRoll = floatBuffer3[2];
                actor.setKeyframe(ActorTrackType.RIGHT_LEG_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.RIGHT_LEG_YAW, tick, floatBuffer3[1], selectedInterpolation);
                actor.setKeyframe(ActorTrackType.RIGHT_LEG_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        ImGui.spacing();
        if (ImGui.button("Key Pose at Tick " + tick)) {
            actor.insertAllPoseKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }
        ImGui.sameLine();
        if (ImGui.button("Reset All Limbs to 0°")) {
            actor.resetLimbs(tick, selectedInterpolation);
            editorState.markDirty();
        }

        // Custom Pose Saving
        ImGuiHelper.separatorWithText("Custom Poses");
        ImGui.setNextItemWidth(ReplayUI.scaleUi(180));
        ImGui.inputText("##NewPoseName", newPoseNameInput);
        ImGui.sameLine();
        if (ImGui.button("Save Current Pose")) {
            ActorPose newPose = pose.copy();
            newPose.name = ImGuiHelper.getString(newPoseNameInput);
            actor.getSavedPoses().add(newPose);
            editorState.markDirty();
        }

        for (int i = 0; i < actor.getSavedPoses().size(); i++) {
            ActorPose saved = actor.getSavedPoses().get(i);
            ImGui.textUnformatted("• " + saved.name);
            ImGui.sameLine();
            if (ImGui.button("Apply##Pose" + i)) {
                actor.applyPose(saved);
                actor.insertAllPoseKeyframes(tick, selectedInterpolation);
                editorState.markDirty();
            }
            ImGui.sameLine();
            if (ImGui.button("Del##Pose" + i)) {
                actor.getSavedPoses().remove(i);
                editorState.markDirty();
                break;
            }
        }
    }

    private static void renderClipTab(FlashbackActor actor, int tick, EditorState editorState) {
        ActorAnimationClip clip = actor.getActiveClip();

        ImGuiHelper.separatorWithText("Procedural Animation Loop");
        ActorAnimationClip.ClipType curClip = clip.getClipType();
        ActorAnimationClip.ClipType newClip = ImGuiHelper.enumCombo("Active Loop##ClipType", curClip);
        if (newClip != curClip) {
            clip.setClipType(newClip);
            editorState.markDirty();
        }

        if (newClip != ActorAnimationClip.ClipType.NONE) {
            boolean loop = clip.isLoop();
            if (ImGui.checkbox("Loop Animation", loop)) {
                clip.setLoop(!loop);
                editorState.markDirty();
            }

            ImGui.sameLine();
            boolean rt = clip.isRealtimePreview();
            if (ImGui.checkbox("Live Real-time Preview in Viewport", rt)) {
                clip.setRealtimePreview(!rt);
                editorState.markDirty();
            }

            floatBuffer[0] = clip.getSpeed();
            if (ImGui.sliderFloat("Speed Multiplier", floatBuffer, 0.1f, 5.0f)) {
                clip.setSpeed(floatBuffer[0]);
                editorState.markDirty();
            }

            floatBuffer[0] = clip.getWeight();
            if (ImGui.sliderFloat("Blending Weight", floatBuffer, 0.0f, 1.0f)) {
                clip.setWeight(floatBuffer[0]);
                editorState.markDirty();
            }

            int[] lenBuf = new int[]{clip.getLengthTicks()};
            if (ImGui.dragInt("Loop Length (Ticks)", lenBuf, 1, 1, 600)) {
                clip.setLengthTicks(lenBuf[0]);
                editorState.markDirty();
            }

            int[] offsetBuf = new int[]{clip.getStartTickOffset()};
            if (ImGui.dragInt("Start Tick Offset", offsetBuf, 1, -10000, 10000)) {
                clip.setStartTickOffset(offsetBuf[0]);
                editorState.markDirty();
            }

            ImGuiHelper.separatorWithText("Bake Clip to Timeline Keyframes");
            ImGui.textWrapped("Sample this procedural cycle into keyframe tracks across a tick range for detailed keyframe editing:");

            ImGui.dragInt("Start Tick##BakeStart", bakeStartTick, 1, 0, 100000);
            ImGui.dragInt("End Tick##BakeEnd", bakeEndTick, 1, 0, 100000);
            ImGui.dragInt("Keyframe Step (Ticks)##BakeStep", bakeStepTicks, 1, 1, 20);

            if (ImGui.button("Bake Clip to Keyframes Now")) {
                clip.bakeToKeyframes(actor, bakeStartTick[0], bakeEndTick[0], bakeStepTicks[0], selectedInterpolation);
                clip.setClipType(ActorAnimationClip.ClipType.NONE);
                editorState.markDirty();
            }
        }
    }

    private static void renderSkinTab(FlashbackActor actor, EditorState editorState) {
        ActorSkin skin = actor.getSkin();

        ImGuiHelper.separatorWithText("Skin Source");
        ActorSkin.SkinType currentType = skin.getSkinType();
        ActorSkin.SkinType newType = ImGuiHelper.enumCombo("Skin Type##SkinType", currentType);
        if (newType != currentType) {
            skin.setSkinType(newType);
            editorState.markDirty();
        }

        if (newType == ActorSkin.SkinType.FILE) {
            ImGui.textWrapped("Current PNG: " + (skin.getSkinValue().isBlank() ? "None selected" : skin.getSkinValue()));
            if (ImGui.button("Browse PNG File...")) {
                Path gameDir = FabricLoader.getInstance().getGameDir();
                CompletableFuture<String> future = AsyncFileDialogs.openFileDialog(gameDir.toString(), "Skin Texture", "png");
                future.thenAccept(pathStr -> {
                    if (pathStr != null) {
                        skin.setSkinValue(pathStr);
                        editorState.markDirty();
                    }
                });
            }
        } else if (newType == ActorSkin.SkinType.USERNAME) {
            ImGui.setNextItemWidth(ReplayUI.scaleUi(200));
            if (skinUsernameInput.isEmpty() || !skinUsernameInput.get().equals(skin.getSkinValue())) {
                skinUsernameInput.set(skin.getSkinValue());
            }
            if (ImGui.inputText("Username/UUID##SkinVal", skinUsernameInput)) {
                skin.setSkinValue(ImGuiHelper.getString(skinUsernameInput));
                editorState.markDirty();
            }
            ImGui.sameLine();
            if (ImGui.button("Fetch Skin")) {
                skin.invalidateCache();
                editorState.markDirty();
            }
        }

        ImGuiHelper.separatorWithText("Model Type");
        ActorSkin.ModelType curModel = skin.getModelType();
        ActorSkin.ModelType newModel = ImGuiHelper.enumCombo("Model Type##ModelType", curModel);
        if (newModel != curModel) {
            skin.setModelType(newModel);
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("Outer Skin Layer Visibility");
        boolean hat = actor.isHatVisible();
        if (ImGui.checkbox("Hat", hat)) { actor.setHatVisible(!hat); editorState.markDirty(); }
        ImGui.sameLine();
        boolean jacket = actor.isJacketVisible();
        if (ImGui.checkbox("Jacket", jacket)) { actor.setJacketVisible(!jacket); editorState.markDirty(); }

        boolean ls = actor.isLeftSleeveVisible();
        if (ImGui.checkbox("Left Sleeve", ls)) { actor.setLeftSleeveVisible(!ls); editorState.markDirty(); }
        ImGui.sameLine();
        boolean rs = actor.isRightSleeveVisible();
        if (ImGui.checkbox("Right Sleeve", rs)) { actor.setRightSleeveVisible(!rs); editorState.markDirty(); }

        boolean lp = actor.isLeftPantsVisible();
        if (ImGui.checkbox("Left Pants", lp)) { actor.setLeftPantsVisible(!lp); editorState.markDirty(); }
        ImGui.sameLine();
        boolean rp = actor.isRightPantsVisible();
        if (ImGui.checkbox("Right Pants", rp)) { actor.setRightPantsVisible(!rp); editorState.markDirty(); }
    }

    private static void renderKeyframesTab(FlashbackActor actor, int tick, EditorState editorState) {
        ImGuiHelper.separatorWithText("Keyframing Actions");

        selectedInterpolation = ImGuiHelper.enumCombo("Interpolation Type##KeyInterp", selectedInterpolation);

        ImGui.spacing();
        if (ImGui.button("Key All Channels at Tick " + tick)) {
            actor.insertAllKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }

        ImGui.sameLine();
        if (ImGui.button("Key Transform at Tick " + tick)) {
            actor.insertAllTransformKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }

        ImGui.sameLine();
        if (ImGui.button("Key Limbs at Tick " + tick)) {
            actor.insertAllPoseKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }

        ImGui.spacing();
        if (ImGui.button("Clear All Keyframes at Tick " + tick)) {
            for (ActorAnimationTrack track : actor.getTracks().values()) {
                track.removeKeyframe(tick);
            }
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("Active Keyframe Channels");
        for (ActorTrackType type : ActorTrackType.values()) {
            ActorAnimationTrack track = actor.getTrack(type);
            int count = track.getKeyframes().size();
            if (count > 0) {
                ImGui.textUnformatted(type.getDisplayName() + ": " + count + " keyframes");
            }
        }
    }
}
