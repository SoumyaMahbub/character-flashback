package com.moulberry.flashback.character;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AnimatedCharacter {

    private final UUID id;
    private String name;
    private boolean visible = true;
    private CharacterSkin skin = new CharacterSkin();

    // Outer layer visibility
    private boolean hatVisible = true;
    private boolean jacketVisible = true;
    private boolean leftSleeveVisible = true;
    private boolean rightSleeveVisible = true;
    private boolean leftPantsVisible = true;
    private boolean rightPantsVisible = true;

    // Animation tracks
    private final Map<CharacterTrackType, CharacterAnimationTrack> tracks = new EnumMap<>(CharacterTrackType.class);

    // Saved poses and active clip
    private final List<CharacterPose> savedPoses = new ArrayList<>();
    private CharacterAnimationClip activeClip = new CharacterAnimationClip("Active Clip", CharacterAnimationClip.ClipType.NONE, 20);

    // Base user/keyframed pose (unclipped)
    private final CharacterPose basePose = new CharacterPose("Base");

    // Runtime evaluated state (with procedural clips applied)
    private transient CharacterPose evaluatedPose = new CharacterPose("Evaluated");
    private transient float evalPosX = 0.0f;
    private transient float evalPosY = 0.0f;
    private transient float evalPosZ = 0.0f;
    private transient float evalRotPitch = 0.0f;
    private transient float evalRotYaw = 0.0f;
    private transient float evalRotRoll = 0.0f;
    private transient float evalScaleX = 1.0f;
    private transient float evalScaleY = 1.0f;
    private transient float evalScaleZ = 1.0f;
    private transient float lastEvaluatedTick = Float.NaN;

    public AnimatedCharacter(UUID id, String name) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNullElse(name, "Character");
        this.initTracks();
    }

    private void initTracks() {
        for (CharacterTrackType type : CharacterTrackType.values()) {
            if (!this.tracks.containsKey(type)) {
                this.tracks.put(type, new CharacterAnimationTrack(type));
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public CharacterSkin getSkin() {
        return skin;
    }

    public void setSkin(CharacterSkin skin) {
        this.skin = Objects.requireNonNullElseGet(skin, CharacterSkin::new);
    }

    public boolean isHatVisible() {
        return hatVisible;
    }

    public void setHatVisible(boolean hatVisible) {
        this.hatVisible = hatVisible;
    }

    public boolean isJacketVisible() {
        return jacketVisible;
    }

    public void setJacketVisible(boolean jacketVisible) {
        this.jacketVisible = jacketVisible;
    }

    public boolean isLeftSleeveVisible() {
        return leftSleeveVisible;
    }

    public void setLeftSleeveVisible(boolean leftSleeveVisible) {
        this.leftSleeveVisible = leftSleeveVisible;
    }

    public boolean isRightSleeveVisible() {
        return rightSleeveVisible;
    }

    public void setRightSleeveVisible(boolean rightSleeveVisible) {
        this.rightSleeveVisible = rightSleeveVisible;
    }

    public boolean isLeftPantsVisible() {
        return leftPantsVisible;
    }

    public void setLeftPantsVisible(boolean leftPantsVisible) {
        this.leftPantsVisible = leftPantsVisible;
    }

    public boolean isRightPantsVisible() {
        return rightPantsVisible;
    }

    public void setRightPantsVisible(boolean rightPantsVisible) {
        this.rightPantsVisible = rightPantsVisible;
    }

    public Map<CharacterTrackType, CharacterAnimationTrack> getTracks() {
        return tracks;
    }

    public CharacterAnimationTrack getTrack(CharacterTrackType trackType) {
        return tracks.computeIfAbsent(trackType, CharacterAnimationTrack::new);
    }

    public List<CharacterPose> getSavedPoses() {
        return savedPoses;
    }

    public CharacterAnimationClip getActiveClip() {
        return activeClip;
    }

    public void setActiveClip(CharacterAnimationClip activeClip) {
        this.activeClip = Objects.requireNonNullElseGet(activeClip, () -> new CharacterAnimationClip("Active Clip", CharacterAnimationClip.ClipType.NONE, 20));
    }

    public CharacterPose getBasePose() {
        return basePose;
    }

    public CharacterPose getEvaluatedPose() {
        return evaluatedPose;
    }

    public float getEvalPosX() {
        return evalPosX;
    }

    public float getEvalPosY() {
        return evalPosY;
    }

    public float getEvalPosZ() {
        return evalPosZ;
    }

    public float getEvalRotPitch() {
        return evalRotPitch;
    }

    public float getEvalRotYaw() {
        return evalRotYaw;
    }

    public float getEvalRotRoll() {
        return evalRotRoll;
    }

    public float getEvalScaleX() {
        return evalScaleX;
    }

    public float getEvalScaleY() {
        return evalScaleY;
    }

    public float getEvalScaleZ() {
        return evalScaleZ;
    }

    public void evaluate(float tick, boolean isReplayPlaying) {
        this.lastEvaluatedTick = tick;

        // Evaluate World Transform
        if (!getTrack(CharacterTrackType.WORLD_POS_X).isEmpty()) this.evalPosX = getTrack(CharacterTrackType.WORLD_POS_X).evaluate(tick);
        if (!getTrack(CharacterTrackType.WORLD_POS_Y).isEmpty()) this.evalPosY = getTrack(CharacterTrackType.WORLD_POS_Y).evaluate(tick);
        if (!getTrack(CharacterTrackType.WORLD_POS_Z).isEmpty()) this.evalPosZ = getTrack(CharacterTrackType.WORLD_POS_Z).evaluate(tick);
        if (!getTrack(CharacterTrackType.WORLD_ROT_PITCH).isEmpty()) this.evalRotPitch = getTrack(CharacterTrackType.WORLD_ROT_PITCH).evaluate(tick);
        if (!getTrack(CharacterTrackType.WORLD_ROT_YAW).isEmpty()) this.evalRotYaw = getTrack(CharacterTrackType.WORLD_ROT_YAW).evaluate(tick);
        if (!getTrack(CharacterTrackType.WORLD_ROT_ROLL).isEmpty()) this.evalRotRoll = getTrack(CharacterTrackType.WORLD_ROT_ROLL).evaluate(tick);
        if (!getTrack(CharacterTrackType.WORLD_SCALE_X).isEmpty()) this.evalScaleX = getTrack(CharacterTrackType.WORLD_SCALE_X).evaluate(tick);
        if (!getTrack(CharacterTrackType.WORLD_SCALE_Y).isEmpty()) this.evalScaleY = getTrack(CharacterTrackType.WORLD_SCALE_Y).evaluate(tick);
        if (!getTrack(CharacterTrackType.WORLD_SCALE_Z).isEmpty()) this.evalScaleZ = getTrack(CharacterTrackType.WORLD_SCALE_Z).evaluate(tick);

        // Evaluate Limbs from tracks into basePose
        for (CharacterTrackType type : CharacterTrackType.values()) {
            if (type.isBodyPose()) {
                CharacterAnimationTrack track = getTrack(type);
                if (!track.isEmpty()) {
                    float val = track.evaluate(tick);
                    this.basePose.setTrackValue(type, val);
                }
            }
        }

        // Copy clean base pose into evaluated pose
        this.evaluatedPose.setFrom(this.basePose);

        // Apply active procedural animation clip if any
        if (this.activeClip != null) {
            this.activeClip.applyToPose(this.evaluatedPose, tick, isReplayPlaying);
        }
    }

    public void setKeyframe(CharacterTrackType trackType, int tick, float value, InterpolationType interpolationType) {
        getTrack(trackType).setKeyframe(tick, value, interpolationType);
        if (trackType.isBodyPose()) {
            this.basePose.setTrackValue(trackType, value);
            this.evaluatedPose.setTrackValue(trackType, value);
        }
    }

    public void removeKeyframe(CharacterTrackType trackType, int tick) {
        getTrack(trackType).removeKeyframe(tick);
    }

    public void insertAllTransformKeyframes(int tick, InterpolationType interpolationType) {
        setKeyframe(CharacterTrackType.WORLD_POS_X, tick, this.evalPosX, interpolationType);
        setKeyframe(CharacterTrackType.WORLD_POS_Y, tick, this.evalPosY, interpolationType);
        setKeyframe(CharacterTrackType.WORLD_POS_Z, tick, this.evalPosZ, interpolationType);
        setKeyframe(CharacterTrackType.WORLD_ROT_PITCH, tick, this.evalRotPitch, interpolationType);
        setKeyframe(CharacterTrackType.WORLD_ROT_YAW, tick, this.evalRotYaw, interpolationType);
        setKeyframe(CharacterTrackType.WORLD_ROT_ROLL, tick, this.evalRotRoll, interpolationType);
        setKeyframe(CharacterTrackType.WORLD_SCALE_X, tick, this.evalScaleX, interpolationType);
        setKeyframe(CharacterTrackType.WORLD_SCALE_Y, tick, this.evalScaleY, interpolationType);
        setKeyframe(CharacterTrackType.WORLD_SCALE_Z, tick, this.evalScaleZ, interpolationType);
    }

    public void insertAllPoseKeyframes(int tick, InterpolationType interpolationType) {
        for (CharacterTrackType type : CharacterTrackType.values()) {
            if (type.isBodyPose()) {
                setKeyframe(type, tick, this.basePose.getTrackValue(type), interpolationType);
            }
        }
    }

    public void insertAllKeyframes(int tick, InterpolationType interpolationType) {
        insertAllTransformKeyframes(tick, interpolationType);
        insertAllPoseKeyframes(tick, interpolationType);
    }

    public void applyPose(CharacterPose pose) {
        if (pose == null) return;
        this.basePose.setFrom(pose);
        this.evaluatedPose.setFrom(pose);
    }

    public void resetLimbs(int tick, InterpolationType interpolationType) {
        this.basePose.reset();
        this.evaluatedPose.reset();
        insertAllPoseKeyframes(tick, interpolationType);
    }

    public void bakeActiveClipToKeyframes(int startTick, int endTick, int stepTicks, InterpolationType interpolationType) {
        if (this.activeClip != null) {
            this.activeClip.bakeToKeyframes(this, startTick, endTick, stepTicks, interpolationType);
        }
    }

    public void setPosition(Vector3f pos) {
        this.evalPosX = pos.x;
        this.evalPosY = pos.y;
        this.evalPosZ = pos.z;
    }

    public AnimatedCharacter duplicate(UUID newId, String newName) {
        AnimatedCharacter copy = new AnimatedCharacter(newId, newName);
        copy.visible = this.visible;
        copy.skin = this.skin.copy();
        copy.hatVisible = this.hatVisible;
        copy.jacketVisible = this.jacketVisible;
        copy.leftSleeveVisible = this.leftSleeveVisible;
        copy.rightSleeveVisible = this.rightSleeveVisible;
        copy.leftPantsVisible = this.leftPantsVisible;
        copy.rightPantsVisible = this.rightPantsVisible;

        for (Map.Entry<CharacterTrackType, CharacterAnimationTrack> entry : this.tracks.entrySet()) {
            copy.tracks.put(entry.getKey(), entry.getValue().copy());
        }

        for (CharacterPose pose : this.savedPoses) {
            copy.savedPoses.add(pose.copy());
        }

        copy.activeClip = this.activeClip.copy();
        copy.basePose.setFrom(this.basePose);
        copy.evaluatedPose.setFrom(this.evaluatedPose);
        copy.evalPosX = this.evalPosX;
        copy.evalPosY = this.evalPosY;
        copy.evalPosZ = this.evalPosZ;
        copy.evalRotPitch = this.evalRotPitch;
        copy.evalRotYaw = this.evalRotYaw;
        copy.evalRotRoll = this.evalRotRoll;
        copy.evalScaleX = this.evalScaleX;
        copy.evalScaleY = this.evalScaleY;
        copy.evalScaleZ = this.evalScaleZ;

        return copy;
    }

    public static class TypeAdapter implements JsonSerializer<AnimatedCharacter>, JsonDeserializer<AnimatedCharacter> {
        @Override
        public AnimatedCharacter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            UUID id = UUID.fromString(obj.get("id").getAsString());
            String name = obj.get("name").getAsString();
            AnimatedCharacter character = new AnimatedCharacter(id, name);

            if (obj.has("visible")) character.visible = obj.get("visible").getAsBoolean();
            if (obj.has("skin")) character.skin = context.deserialize(obj.get("skin"), CharacterSkin.class);
            if (obj.has("hatVisible")) character.hatVisible = obj.get("hatVisible").getAsBoolean();
            if (obj.has("jacketVisible")) character.jacketVisible = obj.get("jacketVisible").getAsBoolean();
            if (obj.has("leftSleeveVisible")) character.leftSleeveVisible = obj.get("leftSleeveVisible").getAsBoolean();
            if (obj.has("rightSleeveVisible")) character.rightSleeveVisible = obj.get("rightSleeveVisible").getAsBoolean();
            if (obj.has("leftPantsVisible")) character.leftPantsVisible = obj.get("leftPantsVisible").getAsBoolean();
            if (obj.has("rightPantsVisible")) character.rightPantsVisible = obj.get("rightPantsVisible").getAsBoolean();

            if (obj.has("tracks")) {
                JsonArray tracksArray = obj.getAsJsonArray("tracks");
                for (JsonElement trackElem : tracksArray) {
                    CharacterAnimationTrack track = context.deserialize(trackElem, CharacterAnimationTrack.class);
                    if (track != null) {
                        character.tracks.put(track.getTrackType(), track);
                    }
                }
            }

            if (obj.has("savedPoses")) {
                JsonArray posesArray = obj.getAsJsonArray("savedPoses");
                for (JsonElement poseElem : posesArray) {
                    CharacterPose pose = context.deserialize(poseElem, CharacterPose.class);
                    if (pose != null) {
                        character.savedPoses.add(pose);
                    }
                }
            }

            if (obj.has("activeClip")) {
                character.activeClip = context.deserialize(obj.get("activeClip"), CharacterAnimationClip.class);
            }

            if (obj.has("basePose")) {
                CharacterPose bp = context.deserialize(obj.get("basePose"), CharacterPose.class);
                if (bp != null) {
                    character.basePose.setFrom(bp);
                }
            }

            return character;
        }

        @Override
        public JsonElement serialize(AnimatedCharacter src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", src.id.toString());
            obj.addProperty("name", src.name);
            obj.addProperty("visible", src.visible);
            obj.add("skin", context.serialize(src.skin));
            obj.addProperty("hatVisible", src.hatVisible);
            obj.addProperty("jacketVisible", src.jacketVisible);
            obj.addProperty("leftSleeveVisible", src.leftSleeveVisible);
            obj.addProperty("rightSleeveVisible", src.rightSleeveVisible);
            obj.addProperty("leftPantsVisible", src.leftPantsVisible);
            obj.addProperty("rightPantsVisible", src.rightPantsVisible);

            JsonArray tracksArray = new JsonArray();
            for (CharacterAnimationTrack track : src.tracks.values()) {
                tracksArray.add(context.serialize(track));
            }
            obj.add("tracks", tracksArray);

            JsonArray posesArray = new JsonArray();
            for (CharacterPose pose : src.savedPoses) {
                posesArray.add(context.serialize(pose));
            }
            obj.add("savedPoses", posesArray);

            obj.add("activeClip", context.serialize(src.activeClip));
            obj.add("basePose", context.serialize(src.basePose));
            return obj;
        }
    }
}
