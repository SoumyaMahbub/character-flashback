package com.moulberry.flashback.character;

import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.editor.ui.ImGuiHelper;
import com.moulberry.flashback.editor.ui.ReplayUI;
import com.moulberry.flashback.exporting.AsyncFileDialogs;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import com.moulberry.flashback.playback.ReplayServer;
import com.moulberry.flashback.state.EditorScene;
import com.moulberry.flashback.state.EditorState;
import com.moulberry.flashback.state.EditorStateManager;
import imgui.moulberry90.ImGui;
import imgui.moulberry90.flag.ImGuiCond;
import imgui.moulberry90.flag.ImGuiInputTextFlags;
import imgui.moulberry90.flag.ImGuiTabBarFlags;
import imgui.moulberry90.flag.ImGuiTabItemFlags;
import imgui.moulberry90.flag.ImGuiWindowFlags;
import imgui.moulberry90.type.ImBoolean;
import imgui.moulberry90.type.ImString;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CharacterWindow {

    private static final ImString characterNameInput = ImGuiHelper.createResizableImString("");
    private static final ImString skinUsernameInput = ImGuiHelper.createResizableImString("");
    private static final float[] floatBuffer = new float[1];
    private static final float[] floatBuffer3 = new float[3];
    private static InterpolationType selectedInterpolation = InterpolationType.LINEAR;
    private static int selectedPresetIndex = 0;
    private static final ImString newPoseNameInput = ImGuiHelper.createResizableImString("My Custom Pose");

    private static final int[] bakeStartTick = new int[]{0};
    private static final int[] bakeEndTick = new int[]{80};
    private static final int[] bakeStepTicks = new int[]{2};

    public static void render(ImBoolean open, boolean justOpened) {
        ReplayServer replayServer = Flashback.getReplayServer();
        if (replayServer == null) {
            return;
        }

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

        CharacterManager manager = currentScene.characterManager;
        float currentTick = (float) replayServer.getPartialReplayTick();
        int intTick = replayServer.getReplayTick();

        ImGui.setNextWindowSize(ReplayUI.scaleUi(440), ReplayUI.scaleUi(640), ImGuiCond.FirstUseEver);

        if (ImGui.begin("Characters###Characters", open, ImGuiWindowFlags.NoCollapse)) {
            // --- TOP: Character Selection & Actions ---
            ImGuiHelper.separatorWithText("Scene Characters");

            List<AnimatedCharacter> characters = manager.getCharacters();
            AnimatedCharacter selected = manager.getSelectedCharacter();

            if (ImGui.button("+ Add Character")) {
                Minecraft mc = Minecraft.getInstance();
                Vector3f spawnPos = new Vector3f(0, 64, 0);
                if (mc.player != null) {
                    Vec3 look = mc.player.getLookAngle();
                    Vec3 pos = mc.player.position().add(look.scale(3.0));
                    spawnPos = new Vector3f((float) pos.x, (float) pos.y, (float) pos.z);
                }
                manager.addCharacter("Character " + (characters.size() + 1), spawnPos, intTick);
                editorState.markDirty();
            }

            ImGui.sameLine();
            if (selected != null) {
                if (ImGui.button("Duplicate")) {
                    manager.duplicateCharacter(selected.getId());
                    editorState.markDirty();
                }
                ImGui.sameLine();
                if (ImGui.button("Delete")) {
                    manager.removeCharacter(selected.getId());
                    editorState.markDirty();
                    selected = manager.getSelectedCharacter();
                }
            }

            if (characters.isEmpty()) {
                ImGui.textDisabled("No characters in this scene. Click '+ Add Character' to spawn one.");
                ImGui.end();
                return;
            }

            // Character selector combo
            String currentName = selected != null ? selected.getName() : "None";
            if (ImGui.beginCombo("##CharacterListCombo", currentName)) {
                for (AnimatedCharacter character : characters) {
                    boolean isSelected = selected != null && character.getId().equals(selected.getId());
                    if (ImGui.selectable(character.getName() + "##" + character.getId(), isSelected)) {
                        manager.setSelectedCharacterId(character.getId());
                        selected = character;
                        characterNameInput.set(character.getName());
                    }
                }
                ImGui.endCombo();
            }

            if (selected == null) {
                ImGui.end();
                return;
            }

            // Name & Visibility
            ImGui.setNextItemWidth(ReplayUI.scaleUi(200));
            if (characterNameInput.isEmpty() || !characterNameInput.get().equals(selected.getName())) {
                characterNameInput.set(selected.getName());
            }
            if (ImGui.inputText("Name##CharName", characterNameInput)) {
                selected.setName(ImGuiHelper.getString(characterNameInput));
                editorState.markDirty();
            }

            ImGui.sameLine();
            boolean vis = selected.isVisible();
            if (ImGui.checkbox("Visible##CharVis", vis)) {
                selected.setVisible(!vis);
                editorState.markDirty();
            }

            // Camera helpers
            if (ImGui.button("Look At Character")) {
                Minecraft.getInstance().cameraEntity.lookAt(EntityAnchorArgument.Anchor.EYES,
                        new Vec3(selected.getEvalPosX(), selected.getEvalPosY() + 1.6, selected.getEvalPosZ()));
            }
            ImGui.sameLine();
            if (ImGui.button("Move To Camera")) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    Vec3 look = mc.player.getLookAngle();
                    Vec3 pos = mc.player.position().add(look.scale(3.0));
                    selected.setPosition(new Vector3f((float) pos.x, (float) pos.y, (float) pos.z));
                    selected.insertAllTransformKeyframes(intTick, selectedInterpolation);
                    editorState.markDirty();
                }
            }

            ImGui.spacing();

            // --- TAB BAR FOR EDITING SECTIONS ---
            if (ImGui.beginTabBar("##CharacterEditTabs", ImGuiTabBarFlags.None)) {

                // TAB 1: World Transform
                if (ImGui.beginTabItem("Transform##Tab")) {
                    renderTransformTab(selected, intTick, editorState);
                    ImGui.endTabItem();
                }

                // TAB 2: Pose & Limbs
                if (ImGui.beginTabItem("Pose & Limbs##Tab")) {
                    renderPoseTab(selected, intTick, editorState);
                    ImGui.endTabItem();
                }

                // TAB 3: Skin & Model
                if (ImGui.beginTabItem("Skin & Model##Tab")) {
                    renderSkinTab(selected, editorState);
                    ImGui.endTabItem();
                }

                // TAB 4: Animation Clip & Loops
                if (ImGui.beginTabItem("Animation Clip##Tab")) {
                    renderClipTab(selected, intTick, editorState);
                    ImGui.endTabItem();
                }

                // TAB 5: Keyframes & Timeline
                if (ImGui.beginTabItem("Keyframes##Tab")) {
                    renderKeyframesTab(selected, intTick, editorState);
                    ImGui.endTabItem();
                }

                ImGui.endTabBar();
            }
        }
        ImGui.end();
    }

    private static void renderTransformTab(AnimatedCharacter character, int tick, EditorState editorState) {
        ImGuiHelper.separatorWithText("World Position");
        floatBuffer3[0] = character.getEvalPosX();
        floatBuffer3[1] = character.getEvalPosY();
        floatBuffer3[2] = character.getEvalPosZ();
        if (ImGui.dragFloat3("Position (X, Y, Z)", floatBuffer3, 0.1f)) {
            character.setPosition(new Vector3f(floatBuffer3[0], floatBuffer3[1], floatBuffer3[2]));
            character.setKeyframe(CharacterTrackType.WORLD_POS_X, tick, floatBuffer3[0], selectedInterpolation);
            character.setKeyframe(CharacterTrackType.WORLD_POS_Y, tick, floatBuffer3[1], selectedInterpolation);
            character.setKeyframe(CharacterTrackType.WORLD_POS_Z, tick, floatBuffer3[2], selectedInterpolation);
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("World Rotation");
        floatBuffer3[0] = character.getEvalRotPitch();
        floatBuffer3[1] = character.getEvalRotYaw();
        floatBuffer3[2] = character.getEvalRotRoll();
        if (ImGui.dragFloat3("Rotation (Pitch, Yaw, Roll)", floatBuffer3, 1.0f)) {
            character.setKeyframe(CharacterTrackType.WORLD_ROT_PITCH, tick, floatBuffer3[0], selectedInterpolation);
            character.setKeyframe(CharacterTrackType.WORLD_ROT_YAW, tick, floatBuffer3[1], selectedInterpolation);
            character.setKeyframe(CharacterTrackType.WORLD_ROT_ROLL, tick, floatBuffer3[2], selectedInterpolation);
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("Scale");
        floatBuffer3[0] = character.getEvalScaleX();
        floatBuffer3[1] = character.getEvalScaleY();
        floatBuffer3[2] = character.getEvalScaleZ();
        if (ImGui.dragFloat3("Scale (X, Y, Z)", floatBuffer3, 0.05f, 0.01f, 20.0f)) {
            character.setKeyframe(CharacterTrackType.WORLD_SCALE_X, tick, floatBuffer3[0], selectedInterpolation);
            character.setKeyframe(CharacterTrackType.WORLD_SCALE_Y, tick, floatBuffer3[1], selectedInterpolation);
            character.setKeyframe(CharacterTrackType.WORLD_SCALE_Z, tick, floatBuffer3[2], selectedInterpolation);
            editorState.markDirty();
        }

        ImGui.spacing();
        if (ImGui.button("Key Transform at Tick " + tick)) {
            character.insertAllTransformKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }
    }

    private static void renderPoseTab(AnimatedCharacter character, int tick, EditorState editorState) {
        CharacterPose pose = character.getBasePose();

        ImGuiHelper.separatorWithText("Pose Presets");
        List<CharacterPose> presets = CharacterPosePresets.getPresets();
        if (selectedPresetIndex < 0 || selectedPresetIndex >= presets.size()) {
            selectedPresetIndex = 0;
        }
        String currentPresetName = presets.get(selectedPresetIndex).name;
        if (ImGui.beginCombo("Preset Library##PresetCombo", currentPresetName)) {
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
            character.applyPose(presets.get(selectedPresetIndex));
            character.insertAllPoseKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("Limb Rotations");

        // Head
        if (ImGui.collapsingHeader("Head", imgui.moulberry90.flag.ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.headPitch;
            floatBuffer3[1] = pose.headYaw;
            floatBuffer3[2] = pose.headRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##Head", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.headPitch = floatBuffer3[0];
                pose.headYaw = floatBuffer3[1];
                pose.headRoll = floatBuffer3[2];
                character.setKeyframe(CharacterTrackType.HEAD_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.HEAD_YAW, tick, floatBuffer3[1], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.HEAD_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Torso / Body
        if (ImGui.collapsingHeader("Torso", imgui.moulberry90.flag.ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.bodyPitch;
            floatBuffer3[1] = pose.bodyYaw;
            floatBuffer3[2] = pose.bodyRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##Torso", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.bodyPitch = floatBuffer3[0];
                pose.bodyYaw = floatBuffer3[1];
                pose.bodyRoll = floatBuffer3[2];
                character.setKeyframe(CharacterTrackType.BODY_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.BODY_YAW, tick, floatBuffer3[1], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.BODY_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Left Arm
        if (ImGui.collapsingHeader("Left Arm", imgui.moulberry90.flag.ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.leftArmPitch;
            floatBuffer3[1] = pose.leftArmYaw;
            floatBuffer3[2] = pose.leftArmRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##LArm", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.leftArmPitch = floatBuffer3[0];
                pose.leftArmYaw = floatBuffer3[1];
                pose.leftArmRoll = floatBuffer3[2];
                character.setKeyframe(CharacterTrackType.LEFT_ARM_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.LEFT_ARM_YAW, tick, floatBuffer3[1], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.LEFT_ARM_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Right Arm
        if (ImGui.collapsingHeader("Right Arm", imgui.moulberry90.flag.ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.rightArmPitch;
            floatBuffer3[1] = pose.rightArmYaw;
            floatBuffer3[2] = pose.rightArmRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##RArm", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.rightArmPitch = floatBuffer3[0];
                pose.rightArmYaw = floatBuffer3[1];
                pose.rightArmRoll = floatBuffer3[2];
                character.setKeyframe(CharacterTrackType.RIGHT_ARM_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.RIGHT_ARM_YAW, tick, floatBuffer3[1], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.RIGHT_ARM_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Left Leg
        if (ImGui.collapsingHeader("Left Leg", imgui.moulberry90.flag.ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.leftLegPitch;
            floatBuffer3[1] = pose.leftLegYaw;
            floatBuffer3[2] = pose.leftLegRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##LLeg", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.leftLegPitch = floatBuffer3[0];
                pose.leftLegYaw = floatBuffer3[1];
                pose.leftLegRoll = floatBuffer3[2];
                character.setKeyframe(CharacterTrackType.LEFT_LEG_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.LEFT_LEG_YAW, tick, floatBuffer3[1], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.LEFT_LEG_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        // Right Leg
        if (ImGui.collapsingHeader("Right Leg", imgui.moulberry90.flag.ImGuiTreeNodeFlags.DefaultOpen)) {
            floatBuffer3[0] = pose.rightLegPitch;
            floatBuffer3[1] = pose.rightLegYaw;
            floatBuffer3[2] = pose.rightLegRoll;
            if (ImGui.dragFloat3("Pitch / Yaw / Roll##RLeg", floatBuffer3, 1.0f, -180.0f, 180.0f)) {
                pose.rightLegPitch = floatBuffer3[0];
                pose.rightLegYaw = floatBuffer3[1];
                pose.rightLegRoll = floatBuffer3[2];
                character.setKeyframe(CharacterTrackType.RIGHT_LEG_PITCH, tick, floatBuffer3[0], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.RIGHT_LEG_YAW, tick, floatBuffer3[1], selectedInterpolation);
                character.setKeyframe(CharacterTrackType.RIGHT_LEG_ROLL, tick, floatBuffer3[2], selectedInterpolation);
                editorState.markDirty();
            }
        }

        ImGui.spacing();
        if (ImGui.button("Key Pose at Tick " + tick)) {
            character.insertAllPoseKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }
        ImGui.sameLine();
        if (ImGui.button("Reset All Limbs to 0°")) {
            character.resetLimbs(tick, selectedInterpolation);
            editorState.markDirty();
        }

        // Custom Pose Saving
        ImGuiHelper.separatorWithText("Custom Poses");
        ImGui.setNextItemWidth(ReplayUI.scaleUi(180));
        ImGui.inputText("##NewPoseName", newPoseNameInput);
        ImGui.sameLine();
        if (ImGui.button("Save Current Pose")) {
            CharacterPose newPose = pose.copy();
            newPose.name = ImGuiHelper.getString(newPoseNameInput);
            character.getSavedPoses().add(newPose);
            editorState.markDirty();
        }

        for (int i = 0; i < character.getSavedPoses().size(); i++) {
            CharacterPose saved = character.getSavedPoses().get(i);
            ImGui.textUnformatted("• " + saved.name);
            ImGui.sameLine();
            if (ImGui.button("Apply##Pose" + i)) {
                character.applyPose(saved);
                character.insertAllPoseKeyframes(tick, selectedInterpolation);
                editorState.markDirty();
            }
            ImGui.sameLine();
            if (ImGui.button("Del##Pose" + i)) {
                character.getSavedPoses().remove(i);
                editorState.markDirty();
                break;
            }
        }
    }

    private static void renderSkinTab(AnimatedCharacter character, EditorState editorState) {
        CharacterSkin skin = character.getSkin();

        ImGuiHelper.separatorWithText("Skin Source");
        CharacterSkin.SkinType currentType = skin.getSkinType();
        CharacterSkin.SkinType newType = ImGuiHelper.enumCombo("Skin Type##SkinType", currentType);
        if (newType != currentType) {
            skin.setSkinType(newType);
            editorState.markDirty();
        }

        if (newType == CharacterSkin.SkinType.FILE) {
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
        } else if (newType == CharacterSkin.SkinType.USERNAME) {
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
                skin.getOrLoadSkin();
            }
        }

        ImGuiHelper.separatorWithText("Player Model Type");
        CharacterSkin.ModelType curModel = skin.getModelType();
        CharacterSkin.ModelType newModel = ImGuiHelper.enumCombo("Model Type##ModelType", curModel);
        if (newModel != curModel) {
            skin.setModelType(newModel);
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("Outer Skin Layers");
        boolean hat = character.isHatVisible();
        if (ImGui.checkbox("Hat Layer", hat)) { character.setHatVisible(!hat); editorState.markDirty(); }
        ImGui.sameLine();
        boolean jacket = character.isJacketVisible();
        if (ImGui.checkbox("Jacket", jacket)) { character.setJacketVisible(!jacket); editorState.markDirty(); }

        boolean ls = character.isLeftSleeveVisible();
        if (ImGui.checkbox("Left Sleeve", ls)) { character.setLeftSleeveVisible(!ls); editorState.markDirty(); }
        ImGui.sameLine();
        boolean rs = character.isRightSleeveVisible();
        if (ImGui.checkbox("Right Sleeve", rs)) { character.setRightSleeveVisible(!rs); editorState.markDirty(); }

        boolean lp = character.isLeftPantsVisible();
        if (ImGui.checkbox("Left Pants", lp)) { character.setLeftPantsVisible(!lp); editorState.markDirty(); }
        ImGui.sameLine();
        boolean rp = character.isRightPantsVisible();
        if (ImGui.checkbox("Right Pants", rp)) { character.setRightPantsVisible(!rp); editorState.markDirty(); }
    }

    private static void renderClipTab(AnimatedCharacter character, int tick, EditorState editorState) {
        CharacterAnimationClip clip = character.getActiveClip();

        ImGuiHelper.separatorWithText("Procedural Animation Loop");
        CharacterAnimationClip.ClipType curClip = clip.getClipType();
        CharacterAnimationClip.ClipType newClip = ImGuiHelper.enumCombo("Active Loop##ClipType", curClip);
        if (newClip != curClip) {
            clip.setClipType(newClip);
            editorState.markDirty();
        }

        if (newClip != CharacterAnimationClip.ClipType.NONE) {
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

            // Bake Clip to Keyframes
            ImGuiHelper.separatorWithText("Bake Clip to Timeline Keyframes");
            ImGui.textWrapped("Sample this procedural cycle into keyframe tracks across a tick range for detailed keyframe editing:");

            ImGui.dragInt("Start Tick##BakeStart", bakeStartTick, 1, 0, 100000);
            ImGui.dragInt("End Tick##BakeEnd", bakeEndTick, 1, 0, 100000);
            ImGui.dragInt("Keyframe Step (Ticks)##BakeStep", bakeStepTicks, 1, 1, 20);

            if (ImGui.button("Bake Clip to Keyframes Now")) {
                clip.bakeToKeyframes(character, bakeStartTick[0], bakeEndTick[0], bakeStepTicks[0], selectedInterpolation);
                clip.setClipType(CharacterAnimationClip.ClipType.NONE);
                editorState.markDirty();
            }
        }
    }

    private static void renderKeyframesTab(AnimatedCharacter character, int tick, EditorState editorState) {
        ImGuiHelper.separatorWithText("Keyframing Actions");

        selectedInterpolation = ImGuiHelper.enumCombo("Interpolation##InterpCombo", selectedInterpolation);

        if (ImGui.button("INSERT KEYFRAME (ALL CHANNELS) [I]")) {
            character.insertAllKeyframes(tick, selectedInterpolation);
            editorState.markDirty();
        }

        ImGui.sameLine();
        if (ImGui.button("Clear at Tick " + tick)) {
            for (CharacterTrackType type : CharacterTrackType.values()) {
                character.removeKeyframe(type, tick);
            }
            editorState.markDirty();
        }

        ImGuiHelper.separatorWithText("Active Keyframe Tracks");

        for (Map.Entry<CharacterTrackType, CharacterAnimationTrack> entry : character.getTracks().entrySet()) {
            CharacterAnimationTrack track = entry.getValue();
            if (track.isEmpty()) continue;

            boolean hasKeyAtCur = track.hasKeyframe(tick);
            String label = entry.getKey().getDisplayName() + " (" + track.getKeyframesByTick().size() + " keys)" + (hasKeyAtCur ? " [Key at " + tick + "]" : "");

            if (ImGui.treeNode(label)) {
                for (Map.Entry<Integer, CharacterKeyframe> kfEntry : track.getKeyframesByTick().entrySet()) {
                    int kfTick = kfEntry.getKey();
                    CharacterKeyframe kf = kfEntry.getValue();

                    ImGui.textUnformatted("Tick " + kfTick + ": " + String.format("%.2f", kf.getValue()) + " (" + kf.getInterpolationType().name() + ")");
                    ImGui.sameLine();
                    if (ImGui.button("Go To##" + entry.getKey() + kfTick)) {
                        Flashback.getReplayServer().goToReplayTick(kfTick);
                    }
                    ImGui.sameLine();
                    if (ImGui.button("Del##" + entry.getKey() + kfTick)) {
                        track.removeKeyframe(kfTick);
                        editorState.markDirty();
                        break;
                    }
                }
                ImGui.treePop();
            }
        }
    }
}
